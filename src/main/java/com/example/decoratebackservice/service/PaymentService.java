package com.example.decoratebackservice.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.decoratebackservice.config.AlipayProperties;
import com.example.decoratebackservice.entity.ConstructionPaymentNode;
import com.example.decoratebackservice.entity.PaymentRecord;
import com.example.decoratebackservice.mapper.PaymentRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付业务编排
 * <p>
 * 完整链路：
 * <pre>
 * 1. 业主点「支付本期款」→ {@link #create} 按付款节点金额创建交易，返回收银台表单/模拟支付页
 * 2. 业主付款 → 支付宝异步通知 /payment/alipay/notify → {@link #handleNotify} 验签 + 校验金额
 * 3. 落库：payment_record.status = 1，同时把 construction_payment_node 置为已支付（尾款付清则订单完工）
 * 4. 前端轮询 {@link #status} 拿到 paid=true 后刷新页面
 * </pre>
 * 金额一律取自数据库的付款节点，前端传什么都不采信。
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final DateTimeFormatter TRADE_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private PaymentRecordMapper recordMapper;

    @Autowired
    private ConstructionPaymentNodeService paymentNodeService;

    @Autowired
    private AlipayProperties properties;

    @Autowired
    private AlipayGatewayService gateway;

    /**
     * 创建支付：校验节点可付 → 生成（或复用）交易流水 → 返回前端要执行的动作
     *
     * @return mode=mock 时给 pay_url（本地模拟收银台）；mode=alipay 时给 form（自动提交到支付宝）
     */
    public Map<String, Object> create(int nodeId, int userId) {
        ConstructionPaymentNode node = paymentNodeService.validatePayable(nodeId, userId);
        BigDecimal amount = node.getAmount() == null
                ? BigDecimal.ZERO : node.getAmount().setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("该节点应付金额为 0，无需支付");
        }
        if (!properties.isMock() && !properties.configured()) {
            throw new IllegalArgumentException("支付宝参数未配置完整，请先填写 application.properties 中的 alipay.*");
        }

        // 同一节点重复点击支付时复用未支付的交易，避免刷出一堆脏单
        PaymentRecord record = recordMapper.findPendingByNode(nodeId);
        if (record == null) {
            record = new PaymentRecord();
            record.setOut_trade_no(buildTradeNo(node.getService_id()));
            record.setService_id(node.getService_id());
            record.setNode_id(node.getId());
            record.setUser_id(userId);
            record.setAmount(amount);
            record.setSubject(node.getNode_name());
            record.setPay_mode(properties.isMock() ? "mock" : "alipay");
            record.setStatus(0);
            recordMapper.insert(record);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("out_trade_no", record.getOut_trade_no());
        data.put("node_id", node.getId());
        data.put("amount", amount);
        data.put("subject", node.getNode_name());
        data.put("mock", properties.isMock());
        if (properties.isMock()) {
            data.put("mode", "mock");
            data.put("pay_url", "/payment/mock/" + record.getOut_trade_no());
        } else {
            try {
                data.put("mode", "alipay");
                data.put("form", gateway.pagePayForm(record.getOut_trade_no(), amount, node.getNode_name()));
            } catch (AlipayApiException e) {
                log.error("创建支付宝订单失败 outTradeNo={}", record.getOut_trade_no(), e);
                throw new IllegalArgumentException("创建支付宝订单失败：" + e.getMessage());
            }
        }
        return data;
    }

    /**
     * 查询支付结果，前端支付完成后轮询用
     * <p>
     * 真实模式下如果回调还没到，这里会主动向支付宝查单补偿，避免用户付了钱页面一直显示待支付。
     */
    public Map<String, Object> status(String outTradeNo, int userId) {
        PaymentRecord record = recordMapper.findByOutTradeNo(outTradeNo);
        if (record == null) {
            throw new IllegalArgumentException("支付交易不存在");
        }
        if (record.getUser_id() != userId) {
            throw new IllegalArgumentException("无权查看该笔支付");
        }
        if (record.getStatus() == 0 && !properties.isMock() && properties.configured()) {
            AlipayTradeQueryResponse response = gateway.queryTrade(outTradeNo);
            if (response != null && response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 写入支付宝交易号，便于后续对账 / 退款
                    markPaid(record, response.getTradeNo());
                }
            }
        }
        PaymentRecord fresh = recordMapper.findById(record.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("out_trade_no", fresh.getOut_trade_no());
        data.put("node_id", fresh.getNode_id());
        data.put("amount", fresh.getAmount());
        data.put("status", fresh.getStatus());
        data.put("paid", fresh.getStatus() == 1);
        return data;
    }

    /**
     * 处理支付宝异步通知
     *
     * @return 必须返回纯文本 success / failure，返回 JSON 会被支付宝判定失败并反复重试
     */
    public String handleNotify(Map<String, String> params) {
        try {
            if (!properties.isMock() && !gateway.verify(params)) {
                log.warn("支付宝回调验签失败 outTradeNo={}", params.get("out_trade_no"));
                return "failure";
            }
            String outTradeNo = params.get("out_trade_no");
            if (outTradeNo == null || outTradeNo.isBlank()) {
                return "failure";
            }
            PaymentRecord record = recordMapper.findByOutTradeNo(outTradeNo);
            if (record == null) {
                log.warn("支付宝回调对应的交易不存在 outTradeNo={}", outTradeNo);
                return "failure";
            }
            // 幂等：重复通知直接应答成功，不再重复改节点
            if (record.getStatus() == 1) {
                return "success";
            }
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                // 等待付款等中间状态不处理
                return "success";
            }
            String totalAmount = params.get("total_amount");
            if (totalAmount != null && !totalAmount.isBlank()) {
                BigDecimal paid = new BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_UP);
                if (paid.compareTo(record.getAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
                    log.error("回调金额与订单不一致 outTradeNo={} 应收={} 实收={}",
                            outTradeNo, record.getAmount(), paid);
                    return "failure";
                }
            }
            markPaid(record, params.get("trade_no"));
            return "success";
        } catch (AlipayApiException | NumberFormatException e) {
            log.error("处理支付宝回调异常", e);
            return "failure";
        } catch (RuntimeException e) {
            log.error("处理支付宝回调异常", e);
            return "failure";
        }
    }

    /**
     * 处理支付宝同步跳回（return_url）：能验签就先落账，再跳回前端页面
     */
    public String handleReturn(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo != null && !outTradeNo.isBlank() && properties.configured()) {
            try {
                if (gateway.verify(params)) {
                    PaymentRecord record = recordMapper.findByOutTradeNo(outTradeNo);
                    if (record != null && record.getStatus() == 0) {
                        markPaid(record, params.get("trade_no"));
                    }
                }
            } catch (AlipayApiException | RuntimeException e) {
                log.warn("支付宝同步跳回处理失败：{}", e.getMessage());
            }
        }
        return properties.getFrontUrl() + (outTradeNo == null ? "" : "?pay=" + outTradeNo);
    }

    /**
     * 本地模拟收银台确认付款：走与真实回调完全相同的落库逻辑
     */
    public void mockConfirm(String outTradeNo) {
        if (!properties.isMock()) {
            throw new IllegalArgumentException("当前为真实支付模式，不支持模拟支付");
        }
        PaymentRecord record = recordMapper.findByOutTradeNo(outTradeNo);
        if (record == null) {
            throw new IllegalArgumentException("支付交易不存在");
        }
        if (record.getStatus() == 1) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", outTradeNo);
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("total_amount", record.getAmount().toPlainString());
        params.put("trade_no", "MOCK" + System.currentTimeMillis());
        if (!"success".equals(handleNotify(params))) {
            throw new IllegalArgumentException("模拟支付失败，请稍后重试");
        }
    }

    /**
     * 模拟收银台页面要展示的交易信息
     */
    public PaymentRecord requireRecord(String outTradeNo) {
        PaymentRecord record = recordMapper.findByOutTradeNo(outTradeNo);
        if (record == null) {
            throw new IllegalArgumentException("支付交易不存在");
        }
        return record;
    }

    /**
     * 交易流水落账：先标记流水（条件更新保证幂等），再回写付款节点
     */
    private void markPaid(PaymentRecord record, String tradeNo) {
        String no = (tradeNo == null || tradeNo.isBlank()) ? record.getOut_trade_no() : tradeNo;
        if (recordMapper.markPaid(record.getId(), no) == 0) {
            // 已被重复回调 / 并发线程处理过
            return;
        }
        try {
            paymentNodeService.pay(record.getNode_id(), record.getUser_id(), record.getOut_trade_no());
        } catch (RuntimeException e) {
            // 钱已经收到，节点回写失败不能算支付失败，记录日志由人工/补偿处理
            log.warn("交易 {} 已收款，但付款节点回写失败：{}", record.getOut_trade_no(), e.getMessage());
        }
    }

    /**
     * 商户订单号：P + 4 位服务单号 + 时间戳 + 4 位随机数，共 23 位（支付宝要求 ≤64）
     */
    private String buildTradeNo(int serviceId) {
        String ts = LocalDateTime.now().format(TRADE_NO_DATE);
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "P" + String.format("%04d", serviceId) + ts + random;
    }
}

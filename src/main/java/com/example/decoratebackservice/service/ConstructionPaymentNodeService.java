package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.ConstructionPaymentNode;
import com.example.decoratebackservice.entity.ConstructionStage;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.mapper.ConstructionPaymentNodeMapper;
import com.example.decoratebackservice.mapper.ConstructionStageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 分期付款节点业务层
 * <p>
 * 节点规则（金额按合同/报价总额计算）：
 * <pre>
 * DEPOSIT 定金     签约前已支付，金额取设计单上的定金
 * TILING  瓷砖完成 30%  ，贴瓷砖阶段业主验收通过后可付
 * CUSTOM  全屋定制 30%  ，全屋定制阶段业主验收通过后可付
 * FINAL   保洁尾款 余额，保洁阶段业主验收通过后可付，付完即订单完成
 * </pre>
 * 尾款用「总额 - 其余三期」倒算，保证四期合计等于合同总额。
 */
@Service
public class ConstructionPaymentNodeService {

    public static final String NODE_DEPOSIT = "DEPOSIT";
    public static final String NODE_TILING = "TILING";
    public static final String NODE_CUSTOM = "CUSTOM";
    public static final String NODE_FINAL = "FINAL";

    public static final int PAY_WAIT = 0;
    public static final int PAY_DONE = 1;
    public static final int PAY_REFUNDED = 2;

    private static final BigDecimal PERCENT_30 = new BigDecimal("30");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private ConstructionPaymentNodeMapper paymentNodeMapper;

    @Autowired
    private ConstructionStageMapper constructionStageMapper;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    /**
     * 按合同金额生成付款节点（幂等）
     */
    public List<ConstructionPaymentNode> initNodes(int serviceId, BigDecimal totalAmount) {
        List<ConstructionPaymentNode> exist = paymentNodeMapper.findByService(serviceId);
        if (exist != null && !exist.isEmpty()) {
            return exist;
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal deposit = order.getDeposit_amount() == null
                ? BigDecimal.ZERO : order.getDeposit_amount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal tiling = total.multiply(PERCENT_30).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal custom = tiling;
        BigDecimal finalAmount = total.subtract(deposit).subtract(tiling).subtract(custom);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        insertNode(serviceId, NODE_DEPOSIT, "定金", ratio(deposit, total), deposit, null,
                order.getDeposit_time() != null ? PAY_DONE : PAY_WAIT, order.getDeposit_time(), "平面图确认后签订");
        insertNode(serviceId, NODE_TILING, "瓷砖完成一期款", PERCENT_30, tiling, "tiling",
                PAY_WAIT, null, "贴瓷砖阶段验收通过后可付");
        insertNode(serviceId, NODE_CUSTOM, "全屋定制二期款", PERCENT_30, custom, "custom",
                PAY_WAIT, null, "全屋定制阶段验收通过后可付");
        insertNode(serviceId, NODE_FINAL, "保洁完成尾款", ratio(finalAmount, total), finalAmount, "cleaning",
                PAY_WAIT, null, "保洁阶段验收通过后付清");
        return paymentNodeMapper.findByService(serviceId);
    }

    private void insertNode(int serviceId, String code, String name, BigDecimal ratio, BigDecimal amount,
                            String triggerStageCode, int payStatus, LocalDateTime payTime, String remark) {
        ConstructionPaymentNode node = new ConstructionPaymentNode();
        node.setService_id(serviceId);
        node.setNode_code(code);
        node.setNode_name(name);
        node.setRatio(ratio);
        node.setAmount(amount);
        node.setTrigger_stage_code(triggerStageCode);
        node.setPay_status(payStatus);
        node.setPay_time(payTime);
        node.setRemark(remark);
        paymentNodeMapper.insert(node);
    }

    private BigDecimal ratio(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }

    public List<ConstructionPaymentNode> findByService(int serviceId) {
        return paymentNodeMapper.findByService(serviceId);
    }

    public ConstructionPaymentNode findById(int id) {
        return paymentNodeMapper.findById(id);
    }

    public BigDecimal sumPaid(int serviceId) {
        return paymentNodeMapper.sumPaid(serviceId);
    }

    /**
     * 业主支付某个节点
     */
    public ConstructionPaymentNode pay(int nodeId, int userId, String tradeNo) {
        ConstructionPaymentNode node = paymentNodeMapper.findById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("付款节点不存在");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(node.getService_id());
        if (order.getUser_id() != userId) {
            throw new IllegalArgumentException("只有业主可以支付该笔款项");
        }
        if (node.getPay_status() != PAY_WAIT) {
            throw new IllegalArgumentException("该节点已支付或已退款，请勿重复支付");
        }
        if (node.getTrigger_stage_code() != null) {
            ConstructionStage stage = constructionStageMapper
                    .findByServiceAndCode(node.getService_id(), node.getTrigger_stage_code());
            if (stage == null || stage.getStatus() != ConstructionStageService.STAGE_ACCEPTED) {
                throw new IllegalArgumentException("「" + node.getNode_name() + "」需该施工阶段通过业主验收后才能支付");
            }
        }
        String no = (tradeNo == null || tradeNo.isBlank())
                ? "PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 1000)
                : tradeNo;
        int rows = paymentNodeMapper.pay(nodeId, no, LocalDateTime.now());
        if (rows == 0) {
            throw new IllegalArgumentException("该节点已被支付，请刷新后重试");
        }
        if (NODE_FINAL.equals(node.getNode_code())) {
            designServiceOrderService.markFinished(node.getService_id());
        }
        return paymentNodeMapper.findById(nodeId);
    }
}

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
 * 节点规则（以「合同总额 - 已付定金」为分期基数）：
 * <pre>
 * DEPOSIT      定金     平面图确认后支付，金额取设计单上的定金
 * STAGE_xxx    阶段款   每个施工阶段业主验收通过后支付，默认 20%
 *                       「灯具与饰品」阶段只收 10%
 * FINAL        竣工尾款 剩下未收的部分（整装约 10%），最后一个阶段验收后付清
 * </pre>
 * 阶段款与尾款均由「余额 - 定金」按比例拆分并倒算尾款，
 * 保证「定金 + 各阶段款 + 尾款」恒等于合同总额，不会超收。
 */
@Service
public class ConstructionPaymentNodeService {

    public static final String NODE_DEPOSIT = "DEPOSIT";
    public static final String NODE_TILING = "TILING";
    public static final String NODE_CUSTOM = "CUSTOM";
    public static final String NODE_FINAL = "FINAL";
    /** 阶段款节点编码前缀，后接施工阶段编码（如 STAGE_structure） */
    public static final String NODE_STAGE_PREFIX = "STAGE_";

    public static final int PAY_WAIT = 0;
    public static final int PAY_DONE = 1;
    public static final int PAY_REFUNDED = 2;

    /** 普通阶段收款比例：20% */
    private static final BigDecimal PERCENT_STAGE = new BigDecimal("20");
    /** 灯具与饰品阶段收款比例：10% */
    private static final BigDecimal PERCENT_DECOR = new BigDecimal("10");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private ConstructionPaymentNodeMapper paymentNodeMapper;

    @Autowired
    private ConstructionStageMapper constructionStageMapper;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    /**
     * 按合同金额生成付款节点（幂等）
     * <p>
     * 每个施工阶段一笔阶段款，业主验收通过后才能支付；
     * 剩余部分作为竣工尾款，最后一个阶段验收后支付。
     */
    public List<ConstructionPaymentNode> initNodes(int serviceId, BigDecimal totalAmount) {
        List<ConstructionPaymentNode> exist = paymentNodeMapper.findByService(serviceId);
        boolean stageSchema = exist != null && exist.stream()
                .anyMatch(n -> n.getNode_code() != null && n.getNode_code().startsWith(NODE_STAGE_PREFIX));
        if (stageSchema) {
            return exist;
        }
        if (exist != null && !exist.isEmpty()) {
            // 旧的分期规则（瓷砖 30% / 全屋定制 30% / 保洁尾款）作废，已支付的节点保留
            paymentNodeMapper.deleteUnpaidByService(serviceId);
        }

        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal deposit = order.getDeposit_amount() == null
                ? BigDecimal.ZERO : order.getDeposit_amount().setScale(2, RoundingMode.HALF_UP);
        // 分期基数：合同总额扣除已付定金，后续各期都从这里拆分
        BigDecimal base = total.subtract(deposit);
        if (base.signum() < 0) {
            base = BigDecimal.ZERO;
        }

        // 定金：已存在（已支付）的节点直接沿用，避免重复插入
        if (paymentNodeMapper.findByServiceAndCode(serviceId, NODE_DEPOSIT) == null) {
            insertNode(serviceId, NODE_DEPOSIT, "定金", ratio(deposit, total), deposit, null,
                    order.getDeposit_time() != null ? PAY_DONE : PAY_WAIT, order.getDeposit_time(),
                    "平面图确认后签订");
        }

        // 每个施工阶段一笔：默认 20%，灯具与饰品 10%
        List<ConstructionStage> stages = constructionStageMapper.findByService(serviceId);
        BigDecimal used = BigDecimal.ZERO;
        for (ConstructionStage stage : stages) {
            BigDecimal percent = isDecorStage(stage) ? PERCENT_DECOR : PERCENT_STAGE;
            BigDecimal amount = base.multiply(percent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            if (used.add(amount).compareTo(base) > 0) {
                // 阶段较多时避免累计超过余额
                amount = base.subtract(used);
            }
            used = used.add(amount);
            insertNode(serviceId, NODE_STAGE_PREFIX + stage.getStage_code(),
                    stage.getStage_name() + "阶段款", percent, amount, stage.getStage_code(),
                    PAY_WAIT, null, stage.getStage_name() + "验收通过后支付");
        }

        // 尾款：余额里剩下的部分（整装 = 10%），最后一个阶段验收通过后才可付
        BigDecimal finalAmount = base.subtract(used);
        if (finalAmount.signum() < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        String lastStageCode = stages.isEmpty() ? null : stages.get(stages.size() - 1).getStage_code();
        // 比例与阶段款同口径（相对分期基数），整装即最后 10%
        insertNode(serviceId, NODE_FINAL, "竣工尾款", ratio(finalAmount, base), finalAmount, lastStageCode,
                PAY_WAIT, null, "全部阶段验收通过后付清");
        return paymentNodeMapper.findByService(serviceId);
    }

    /**
     * 是否「灯具与饰品」阶段（该阶段只收 10%）
     */
    private boolean isDecorStage(ConstructionStage stage) {
        String code = stage.getStage_code() == null ? "" : stage.getStage_code();
        if ("decor_fitting".equals(code) || "lighting".equals(code)) {
            return true;
        }
        return stage.getStage_name() != null && stage.getStage_name().contains("灯具");
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
     * 校验某个付款节点当前是否可以由该业主支付
     * <p>
     * 支付前（创建交易）与支付回调落库前都会调用，保证两边口径一致：
     * 节点存在 → 属于该业主 → 未支付 → 前置施工阶段已验收。
     */
    public ConstructionPaymentNode validatePayable(int nodeId, int userId) {
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
        return node;
    }

    /**
     * 支付成功落库：把节点标记为已支付（支付回调验签通过后调用）
     * <p>
     * 幂等依赖 Mapper 的「pay_status = 0 才更新」，重复回调只会成功一次；
     * 尾款付清后把订单推进为「已完工」。
     *
     * @param tradeNo 交易号，真实支付传支付宝 out_trade_no
     */
    public ConstructionPaymentNode pay(int nodeId, int userId, String tradeNo) {
        ConstructionPaymentNode node = validatePayable(nodeId, userId);
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

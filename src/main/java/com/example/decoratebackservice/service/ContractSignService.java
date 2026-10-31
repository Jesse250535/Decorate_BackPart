package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DesignContract;
import com.example.decoratebackservice.entity.DesignQuote;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.mapper.DesignContractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业主线上签订合同（装修服务合同）。
 * <p>
 * 流程：业主支付定金后 → 生成合同草稿（金额取生效报价总额）→ 手写签名 →
 * 支付首付款（合同金额扣除定金后余额的 15%）→ 上传合同回执。
 * 签约成功即初始化施工阶段与付款节点，项目经理端可据此实时展示施工进度。
 * <p>
 * 说明：签名图片由前端 Canvas 手写后上传到 {@code /file/upload}，签名地址写入
 * {@code design_contract.signature_url}；如需换成第三方电子签平台（e签宝 / 法大大 /
 * 上上签），只需前端把该平台返回的签署文件地址传入 {@code signature_url} 与
 * {@code file_url}，本服务逻辑无需变动。
 */
@Service
public class ContractSignService {

    /** 首付款比例：合同金额扣除定金后余额的 15% */
    public static final BigDecimal FIRST_PAY_RATE = new BigDecimal("0.15");

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    @Autowired
    private DesignQuoteService designQuoteService;

    @Autowired
    private DesignContractMapper designContractMapper;

    @Autowired
    private ConstructionStageService constructionStageService;

    @Autowired
    private ConstructionPaymentNodeService paymentNodeService;

    /**
     * 合同草稿：金额、定金、首付款、开工日期、已签/已付状态，供前端渲染合同正文
     */
    public Map<String, Object> draft(int serviceId, int userId) {
        DesignServiceOrder order = requireOwner(serviceId, userId);
        DesignQuote quote = designQuoteService.findActive(serviceId);

        BigDecimal amount = totalAmount(order, quote);
        BigDecimal deposit = nvl(order.getDeposit_amount());
        BigDecimal firstPayment = firstPayment(amount, deposit);

        DesignContract contract = designContractMapper.findByService(serviceId);
        if (contract == null && order.getStatus() >= DesignServiceOrderService.STATUS_DEPOSIT_PAID) {
            // 老数据没有合同草稿时补一条并落库，保证合同编号进入页面即固定
            contract = designServiceOrderService.ensureContract(order, amount);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("quote", quote);
        data.put("contract", contract);
        data.put("contract_no", contract == null ? previewNo() : contract.getContract_no());
        data.put("contract_amount", amount);
        data.put("deposit_amount", deposit);
        data.put("first_payment_rate", 15);
        data.put("first_payment_amount", firstPayment);
        data.put("start_date", contract != null && contract.getStart_date() != null
                ? contract.getStart_date() : LocalDate.now().plusDays(3));
        data.put("signature_url", contract == null ? null : contract.getSignature_url());
        data.put("first_paid", contract != null && contract.getFirst_payment_time() != null);
        data.put("receipt_url", contract == null ? null : contract.getReceipt_url());
        data.put("signed", contract != null && contract.getSign_status() == 1);
        data.put("can_sign", contract == null || contract.getSign_status() == 0);
        return data;
    }

    /**
     * 业主签署合同：写入签名、生成合同记录并推进为「已签合同」
     */
    public DesignContract sign(int serviceId, int userId, Map<String, Object> body) {
        DesignServiceOrder order = requireOwner(serviceId, userId);
        if (order.getStatus() < DesignServiceOrderService.STATUS_DEPOSIT_PAID
                || order.getStatus() > DesignServiceOrderService.STATUS_QUOTE_CONFIRMED) {
            throw new IllegalArgumentException("请先支付定金，再签订合同");
        }

        String signature = str(body == null ? null : body.get("signature_url"));
        if (signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("请先手写签名后再提交");
        }
        String fileUrl = str(body.get("file_url"));
        LocalDate startDate = date(body.get("start_date"));
        if (startDate == null) {
            startDate = LocalDate.now().plusDays(3);
        }

        DesignQuote quote = designQuoteService.findActive(serviceId);
        BigDecimal amount = totalAmount(order, quote);
        BigDecimal deposit = nvl(order.getDeposit_amount());
        BigDecimal firstPayment = firstPayment(amount, deposit);

        DesignContract exist = designContractMapper.findByService(serviceId);
        DesignContract contract;
        if (exist == null) {
            contract = new DesignContract();
            contract.setContract_no("DC" + LocalDateTime.now().format(NO_FORMAT)
                    + ThreadLocalRandom.current().nextInt(1000, 10000));
            contract.setService_id(serviceId);
            contract.setUser_id(order.getUser_id());
            contract.setDesigner_id(order.getDesigner_id());
            // 尚未指派项目经理时以 0 占位，项目经理接单后可回写
            contract.setManager_id(order.getManager_id() == null ? 0 : order.getManager_id());
            contract.setContract_amount(amount);
            contract.setDecoration_mode(order.getDecoration_mode());
            contract.setSign_status(1);
            contract.setSign_date(LocalDateTime.now());
            contract.setStart_date(startDate);
            contract.setFile_url(fileUrl);
            contract.setSignature_url(signature);
            contract.setFirst_payment_amount(firstPayment);
            contract.setRemark("业主在线签署");
            contract.setIs_deleted(0);
            designContractMapper.insert(contract);
        } else {
            if (exist.getSign_status() == 1) {
                throw new IllegalArgumentException("合同已签订，无需重复签署");
            }
            int rows = designContractMapper.updateOwnerSign(exist.getId(), amount, firstPayment,
                    signature, fileUrl, startDate, "业主在线签署", LocalDateTime.now());
            if (rows == 0) {
                throw new IllegalArgumentException("合同已被处理，请刷新后重试");
            }
            contract = designContractMapper.findById(exist.getId());
        }

        Integer managerId = order.getManager_id();
        designServiceOrderService.markContractedByOwner(serviceId, contract.getId(), managerId, startDate);
        // 签约即排施工计划与付款节点，项目经理端据此展示施工进度
        constructionStageService.initStages(serviceId, order.getDecoration_mode(),
                managerId == null ? 0 : managerId, startDate);
        paymentNodeService.initNodes(serviceId, amount);

        // 量房阶段生成的报价单一并确认，避免后续尾款统计取到未确认报价
        if (quote != null && quote.getStatus() == DesignQuoteService.STATUS_WAIT_CONFIRM) {
            try {
                designQuoteService.confirm(quote.getId(), userId);
            } catch (RuntimeException ignored) {
                // 已被确认或作废时不影响签约
            }
        }
        return designContractMapper.findById(contract.getId());
    }

    /**
     * 业主支付首付款（模拟支付，实际对接收银台时在此处发起支付单）
     */
    public DesignContract payFirst(int serviceId, int userId) {
        requireOwner(serviceId, userId);
        DesignContract contract = designContractMapper.findByService(serviceId);
        if (contract == null || contract.getSign_status() != 1) {
            throw new IllegalArgumentException("请先签订合同");
        }
        if (contract.getFirst_payment_time() != null) {
            throw new IllegalArgumentException("首付款已支付，请勿重复操作");
        }
        if (nvl(contract.getFirst_payment_amount()).signum() <= 0) {
            throw new IllegalArgumentException("当前首付款金额为 0，无需支付");
        }
        int rows = designContractMapper.updateFirstPayment(contract.getId(), LocalDateTime.now());
        if (rows == 0) {
            throw new IllegalArgumentException("首付款已支付，请刷新后重试");
        }
        return designContractMapper.findById(contract.getId());
    }

    /**
     * 上传合同回执（纸质合同拍照 / 扫描件）
     */
    public DesignContract uploadReceipt(int serviceId, int userId, String receiptUrl) {
        requireOwner(serviceId, userId);
        if (receiptUrl == null || receiptUrl.isBlank()) {
            throw new IllegalArgumentException("请先上传合同回执");
        }
        DesignContract contract = designContractMapper.findByService(serviceId);
        if (contract == null || contract.getSign_status() != 1) {
            throw new IllegalArgumentException("合同尚未签订，无法上传回执");
        }
        designContractMapper.updateReceipt(contract.getId(), receiptUrl);
        return designContractMapper.findById(contract.getId());
    }

    /* ==================== 内部工具 ==================== */

    private DesignServiceOrder requireOwner(int serviceId, int userId) {
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        if (order.getUser_id() != userId) {
            throw new IllegalArgumentException("只有业主可以签订该合同");
        }
        return order;
    }

    /**
     * 合同金额：优先取生效报价总额，其次取业主填写的预算
     */
    private BigDecimal totalAmount(DesignServiceOrder order, DesignQuote quote) {
        if (quote != null && quote.getTotal_amount() != null && quote.getTotal_amount().signum() > 0) {
            return quote.getTotal_amount().setScale(2, RoundingMode.HALF_UP);
        }
        if (order.getBudget() != null && order.getBudget().signum() > 0) {
            return order.getBudget().setScale(2, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("尚无有效报价金额，请联系设计师完成报价");
    }

    /**
     * 首付款 =（合同金额 - 已付定金）× 15%
     */
    private BigDecimal firstPayment(BigDecimal amount, BigDecimal deposit) {
        BigDecimal rest = amount.subtract(deposit);
        if (rest.signum() < 0) {
            throw new IllegalArgumentException("定金已超过合同金额，请核对报价");
        }
        return rest.multiply(FIRST_PAY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private String previewNo() {
        return "DC" + LocalDateTime.now().format(NO_FORMAT);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDate date(Object value) {
        String text = str(value);
        return text == null || text.isBlank() ? null : LocalDate.parse(text);
    }
}

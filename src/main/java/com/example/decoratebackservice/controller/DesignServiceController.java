package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.ConstructionPaymentNode;
import com.example.decoratebackservice.entity.ConstructionStage;
import com.example.decoratebackservice.entity.DesignContract;
import com.example.decoratebackservice.entity.DesignQuote;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DesignContractMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.ConstructionPaymentNodeService;
import com.example.decoratebackservice.service.ConstructionStageService;
import com.example.decoratebackservice.service.DesignQuoteService;
import com.example.decoratebackservice.service.DesignServiceOrderService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 设计服务全流程接口：业主找设计师出效果图 → 量房 → 平面图 → 定金 → 效果图 → 报价 → 合同。
 * <p>
 * 阶段施工与付款节点分别见 {@link ConstructionStageController}、{@link PaymentNodeController}。
 * 角色准入：业主为 ROLE_USER，设计师为 ROLE_DESIGNER，签合同为 ROLE_MANAGER。
 */
@Tag(name = "设计服务-全流程接口")
@RestController
@RequestMapping("/design-service")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DesignServiceController {

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    @Autowired
    private DesignQuoteService designQuoteService;

    @Autowired
    private ConstructionStageService constructionStageService;

    @Autowired
    private ConstructionPaymentNodeService paymentNodeService;

    @Autowired
    private DesignContractMapper designContractMapper;

    @Autowired
    private UserService userService;

    /* ==================== 业主端 ==================== */

    @Operation(summary = "业主提交设计需求：选择设计师与装修方式，等待设计师接单")
    @PostMapping("/apply")
    public Result<DesignServiceOrder> apply(@RequestBody DesignServiceOrder order, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再预约设计师");
        }
        if (order == null) {
            return Result.validateFailed("请求体不能为空");
        }
        designServiceOrderService.create(order, userId);
        return Result.success("预约成功，等待设计师接单", order);
    }

    @Operation(summary = "业主查看自己的设计服务单")
    @GetMapping("/my")
    public Result<List<DesignServiceOrder>> my(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(designServiceOrderService.findByUser(userId));
    }

    @Operation(summary = "业主确认平面设计图：确认后进入待付定金")
    @PutMapping("/{id}/plan-confirm")
    public Result<Integer> confirmPlan(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success("平面图已确认，请支付定金", designServiceOrderService.confirmPlan(id, userId));
    }

    @Operation(summary = "业主支付定金：支付后设计师开始出效果图")
    @PutMapping("/{id}/pay-deposit")
    public Result<Integer> payDeposit(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success("定金支付成功", designServiceOrderService.payDeposit(id, userId));
    }

    @Operation(summary = "业主取消设计需求（合同签订后不可取消）")
    @PutMapping("/{id}/cancel")
    public Result<Integer> cancel(@PathVariable int id,
                                  @RequestParam(required = false) String reason,
                                  Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success("已取消", designServiceOrderService.cancel(id, userId, reason));
    }

    /* ==================== 设计师端 ==================== */

    @Operation(summary = "设计师查看派给自己的设计服务单")
    @GetMapping("/designer/mine")
    public Result<List<DesignServiceOrder>> designerMine(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可查看");
        }
        return Result.success(designServiceOrderService.findByDesigner(userId));
    }

    @Operation(summary = "设计师接单")
    @PutMapping("/{id}/accept")
    public Result<Integer> accept(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可接单");
        }
        return Result.success("接单成功", designServiceOrderService.accept(id, userId));
    }

    @Operation(summary = "设计师登记免费量房完成")
    @PutMapping("/{id}/measure")
    public Result<Integer> measure(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可登记量房");
        }
        return Result.success("量房完成，请出平面设计图", designServiceOrderService.measure(id, userId));
    }

    @Operation(summary = "设计师提交平面设计图与定金金额")
    @PostMapping("/{id}/plan-drawing")
    public Result<Integer> submitPlanDrawing(@PathVariable int id,
                                             @RequestBody Map<String, Object> body,
                                             Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可提交平面图");
        }
        String drawings = str(body.get("drawings"));
        BigDecimal deposit = decimal(body.get("deposit_amount"));
        return Result.success("平面图已提交，等待业主确认",
                designServiceOrderService.submitPlanDrawing(id, userId, drawings, deposit));
    }

    @Operation(summary = "设计师在量房阶段录入硬装 / 软装报价（总额与 10% 定金由页面实时计算）")
    @PostMapping("/{id}/measure-quote")
    public Result<DesignQuote> submitMeasureQuote(@PathVariable int id,
                                                  @RequestBody Map<String, Object> body,
                                                  Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可提交报价");
        }
        return Result.success("报价已保存，可继续上传平面设计图",
                designQuoteService.submitMeasureQuote(id, userId,
                        decimal(body.get("hard_amount")),
                        decimal(body.get("soft_amount")),
                        str(body.get("remark"))));
    }

    @Operation(summary = "设计师提交效果图（业主付定金后）")
    @PostMapping("/{id}/effect-drawing")
    public Result<Integer> submitEffectDrawing(@PathVariable int id,
                                               @RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可提交效果图");
        }
        return Result.success("效果图已提交，请随后提交报价单",
                designServiceOrderService.submitEffectDrawing(id, userId, str(body.get("drawings"))));
    }

    /* ==================== 项目经理端 ==================== */

    @Operation(summary = "项目经理查看待签合同的设计服务单")
    @GetMapping("/manager/wait-contract")
    public Result<List<DesignServiceOrder>> waitContract(Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可查看");
        }
        return Result.success(designServiceOrderService.findWaitContract());
    }

    @Operation(summary = "项目经理查看自己负责的项目")
    @GetMapping("/manager/mine")
    public Result<List<DesignServiceOrder>> managerMine(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可查看");
        }
        return Result.success(designServiceOrderService.findByManager(userId));
    }

    @Operation(summary = "项目经理签订合同：自动生成施工阶段计划与分期付款节点")
    @PostMapping("/{id}/contract")
    public Result<DesignContract> signContract(@PathVariable int id,
                                               @RequestBody(required = false) Map<String, Object> body,
                                               Authentication authentication) {
        Integer managerId = currentUserId(authentication);
        if (managerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可签订合同");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(id);
        if (order.getStatus() != DesignServiceOrderService.STATUS_QUOTE_CONFIRMED) {
            return Result.validateFailed("业主尚未确认报价，不能签合同");
        }
        DesignQuote quote = designQuoteService.findActive(id);
        if (quote == null || quote.getStatus() != DesignQuoteService.STATUS_CONFIRMED) {
            return Result.validateFailed("未找到已确认的报价单");
        }
        Map<String, Object> data = body == null ? new HashMap<>() : body;
        LocalDate startDate = date(data.get("start_date"));
        if (startDate == null) {
            startDate = LocalDate.now().plusDays(3);
        }

        DesignContract contract = new DesignContract();
        contract.setContract_no("DC" + LocalDateTime.now().format(NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(1000, 10000));
        contract.setService_id(id);
        contract.setUser_id(order.getUser_id());
        contract.setDesigner_id(order.getDesigner_id());
        contract.setManager_id(managerId);
        contract.setContract_amount(quote.getTotal_amount());
        contract.setDecoration_mode(order.getDecoration_mode());
        contract.setSign_status(1);
        contract.setSign_date(LocalDateTime.now());
        contract.setStart_date(startDate);
        contract.setFile_url(str(data.get("file_url")));
        contract.setRemark(str(data.get("remark")));
        contract.setIs_deleted(0);
        designContractMapper.insert(contract);

        designServiceOrderService.markContracted(id, contract.getId(), managerId, startDate);
        constructionStageService.initStages(id, order.getDecoration_mode(), managerId, startDate);
        paymentNodeService.initNodes(id, quote.getTotal_amount());
        return Result.success("合同已签订，施工计划与付款节点已生成", contract);
    }

    /* ==================== 详情 ==================== */

    @Operation(summary = "设计服务单详情：含报价、施工阶段、付款节点、合同")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(id);
        boolean owner = order.getUser_id() == userId;
        boolean designer = order.getDesigner_id() == userId;
        boolean manager = StaffRole.isManager(authentication);
        if (!owner && !designer && !manager) {
            return Result.forbidden("无权查看该设计服务单");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        DesignQuote quote = designQuoteService.findActive(id);
        data.put("quote", quote);
        data.put("quote_items", quote == null ? List.of() : designQuoteService.findItems(quote.getId()));
        List<ConstructionStage> stages = constructionStageService.findByService(id);
        data.put("stages", stages);
        List<ConstructionPaymentNode> nodes = paymentNodeService.findByService(id);
        data.put("payment_nodes", nodes);
        data.put("paid_amount", paymentNodeService.sumPaid(id));
        DesignContract contract = designContractMapper.findByService(id);
        data.put("contract", contract);
        data.put("decoration_mode_text", modeText(order.getDecoration_mode()));
        return Result.success(data);
    }

    private String modeText(int mode) {
        return switch (mode) {
            case 1 -> "硬装";
            case 2 -> "软装";
            case 3 -> "硬装+软装整装";
            default -> "未知";
        };
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal decimal(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDate date(Object value) {
        String text = str(value);
        return text == null || text.isBlank() ? null : LocalDate.parse(text);
    }

    /**
     * 从 Security 会话解析当前登录用户ID（以用户名反查 sys_user.id）
     */
    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}

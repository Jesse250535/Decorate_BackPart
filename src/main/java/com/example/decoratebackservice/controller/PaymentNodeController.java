package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.ConstructionPaymentNode;
import com.example.decoratebackservice.entity.DesignQuote;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.ConstructionPaymentNodeService;
import com.example.decoratebackservice.service.DesignQuoteService;
import com.example.decoratebackservice.service.DesignServiceOrderService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分期付款接口：定金 → 瓷砖完成 → 全屋定制完成 → 保洁尾款。
 * <p>
 * 中间两期必须等对应施工阶段通过业主验收后才能支付，尾款付清即订单完成。
 */
@Tag(name = "设计服务-分期付款接口")
@RestController
@RequestMapping("/payment-node")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentNodeController {

    @Autowired
    private ConstructionPaymentNodeService paymentNodeService;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    @Autowired
    private DesignQuoteService designQuoteService;

    @Autowired
    private UserService userService;

    @Operation(summary = "查看某设计服务单的付款节点与已付金额")
    @GetMapping("/service/{serviceId}")
    public Result<Map<String, Object>> listByService(@PathVariable int serviceId, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        if (order.getUser_id() != userId && order.getDesigner_id() != userId
                && !StaffRole.isManager(authentication)) {
            return Result.forbidden("无权查看该项目的付款节点");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("nodes", paymentNodeService.findByService(serviceId));
        data.put("paid_amount", paymentNodeService.sumPaid(serviceId));
        return Result.success(data);
    }

    @Operation(summary = "项目经理按报价总额重新生成/校准付款节点")
    @PostMapping("/service/{serviceId}/init")
    public Result<List<ConstructionPaymentNode>> init(@PathVariable int serviceId, Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可维护付款节点");
        }
        DesignQuote quote = designQuoteService.findActive(serviceId);
        BigDecimal total = quote == null ? BigDecimal.ZERO : quote.getTotal_amount();
        return Result.success("付款节点已生成", paymentNodeService.initNodes(serviceId, total));
    }

    @Operation(summary = "业主支付某个付款节点（需前置阶段已验收通过）")
    @PutMapping("/{id}/pay")
    public Result<ConstructionPaymentNode> pay(@PathVariable int id,
                                               @RequestBody(required = false) Map<String, Object> body,
                                               Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        String tradeNo = body == null || body.get("trade_no") == null ? null : String.valueOf(body.get("trade_no"));
        return Result.success("支付成功", paymentNodeService.pay(id, userId, tradeNo));
    }

    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}

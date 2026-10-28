package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.DecorationMaterialService;
import com.example.decoratebackservice.service.DecorationOrderService;
import com.example.decoratebackservice.service.DecorationPlanService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单管理接口
 */
@Tag(name = "订单管理接口")
@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecorationOrderController {

    @Autowired
    private DecorationOrderService decorationOrderService;

    @Autowired
    private DecorationPlanService decorationPlanService;

    @Autowired
    private DecorationMaterialService decorationMaterialService;

    @Autowired
    private UserService userService;

    @Operation(summary = "添加订单")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody DecorationOrder order) {
        return Result.success(decorationOrderService.insert(order));
    }

    @Operation(summary = "根据ID查询订单")
    @GetMapping("/findById/{id}")
    public Result<DecorationOrder> findById(@PathVariable int id) {
        return Result.success(decorationOrderService.findById(id));
    }

    @Operation(summary = "更新订单")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody DecorationOrder order) {
        return Result.success(decorationOrderService.update(order));
    }

    @Operation(summary = "根据ID删除订单")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(decorationOrderService.delete(id));
    }

    @Operation(summary = "查询所有订单")
    @GetMapping("/findAll")
    public Result<List<DecorationOrder>> findAll() {
        return Result.success(decorationOrderService.findAll());
    }

    @Operation(summary = "根据订单编号修改合同签订状态")
    @PutMapping("/updateContract")
    public Result<Integer> updateContract(@RequestParam String order_no,
                                          @RequestParam int contract_status) {
        return Result.success(decorationOrderService.updateContractByOrderNo(order_no, contract_status));
    }

    @Operation(summary = "根据ID修改订单状态（0待支付 1已支付 2已完成 3已退款）")
    @PutMapping("/updateStatus/{id}/{order_status}")
    public Result<Integer> updateStatus(@PathVariable int id, @PathVariable int order_status) {
        if (order_status < 0 || order_status > 3) {
            return Result.validateFailed("订单状态只能是 0待支付、1已支付、2已完成、3已退款");
        }
        return Result.success(decorationOrderService.updateOrderStatus(id, order_status));
    }

    @Operation(summary = "根据订单编号修改订单状态（0待支付 1已支付 2已完成 3已退款）")
    @PutMapping("/updateStatusByOrderNo/{order_no}/{order_status}")
    public Result<Integer> updateStatusByOrderNo(@PathVariable String order_no,
                                                 @PathVariable int order_status) {
        if (order_status < 0 || order_status > 3) {
            return Result.validateFailed("订单状态只能是 0待支付、1已支付、2已完成、3已退款");
        }
        return Result.success(decorationOrderService.updateOrderStatusByOrderNo(order_no, order_status));
    }

    @Operation(summary = "用户申请退款：直接把订单状态改为已退款（3）")
    @PutMapping("/refund/{order_no}")
    public Result<Integer> refund(@PathVariable String order_no) {
        int rows = decorationOrderService.refundByOrderNo(order_no);
        if (rows <= 0) {
            return Result.error(404, "退款失败：订单编号不存在");
        }
        return Result.success("退款成功", rows);
    }

    /* ==================== 用户自助下单 / 支付 ==================== */

    @Operation(summary = "当前登录用户下单：落库为待支付订单，订单号由后端生成")
    @PostMapping("/my")
    public Result<DecorationOrder> createMyOrder(@RequestBody DecorationOrder order,
                                                Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再下单");
        }
        if (order == null) {
            return Result.validateFailed("请求体不能为空");
        }
        if (order.getOrder_type() != 1 && order.getOrder_type() != 2) {
            return Result.validateFailed("订单类型只能是 1购买装修方案 / 2购买装修材料");
        }
        if (order.getTarget_id() <= 0) {
            return Result.validateFailed("请选择要购买的商品");
        }
        if (order.getTotal_amount() == null || order.getTotal_amount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.validateFailed("订单金额必须大于 0");
        }
        // 商品必须真实存在，否则个人中心会出现找不到名称的订单
        if (order.getOrder_type() == 1 && decorationPlanService.findById(order.getTarget_id()) == null) {
            return Result.error(404, "购买的装修方案不存在");
        }
        if (order.getOrder_type() == 2 && decorationMaterialService.findById(order.getTarget_id()) == null) {
            return Result.error(404, "购买的装修材料不存在");
        }
        // order_no 有唯一索引：同一秒内极小概率撞号，重试几次即可
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                DecorationOrder created = decorationOrderService.createUnpaidOrder(userId, order);
                return Result.success("下单成功", created);
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    break;
                }
            }
        }
        return Result.error("下单失败：订单号生成冲突，请重试");
    }

    @Operation(summary = "支付当前登录用户的待支付订单（写入支付时间）")
    @PutMapping("/my/pay/{order_no}")
    public Result<Integer> payMyOrder(@PathVariable String order_no, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再支付");
        }
        int rows = decorationOrderService.payOrder(order_no, userId);
        if (rows <= 0) {
            return Result.error(404, "支付失败：订单不存在或已不是待支付状态");
        }
        return Result.success("支付成功", rows);
    }

    /**
     * 从 Security 会话解析当前登录用户ID（以用户名反查 sys_user.id，前端无法伪造）
     *
     * @param authentication 由 Spring Security 注入
     * @return sys_user.id；未登录或用户不存在时返回 null
     */
    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}

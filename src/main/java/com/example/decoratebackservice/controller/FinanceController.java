package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.mapper.DecorationOrderMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationOrderService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务人员工作台：核对与管理合同的签订金额。
 * <p>
 * 只开放「金额」这一个口子：订单状态、合同状态、退款状态都属于其它岗位的职责，
 * 财务改金额不应顺带改状态，因此使用专用 UPDATE 语句。
 */
@Tag(name = "财务人员-合同金额接口")
@RestController
@RequestMapping("/finance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinanceController {

    /** 单笔合同金额上限，防止误输入天价数字把统计打乱 */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private final DecorationOrderService orderService;
    private final DecorationOrderMapper orderMapper;
    private final UserService userService;

    public FinanceController(DecorationOrderService orderService,
                             DecorationOrderMapper orderMapper,
                             UserService userService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.userService = userService;
    }

    @Operation(summary = "财务查看全部订单合同（含金额、合同状态）")
    @GetMapping("/contracts")
    public Result<List<Map<String, Object>>> contracts(Authentication authentication) {
        if (!StaffRole.isFinance(authentication)) {
            return Result.forbidden("仅财务人员可管理合同金额");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (DecorationOrder order : orderService.findAll()) {
            if (order.getIs_deleted() != 0) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("order", order);
            row.put("buyer_name", userService.findUsernameById(order.getUser_id()));
            result.add(row);
        }
        return Result.success(result);
    }

    @Operation(summary = "财务核对合同签订金额（只改金额，不动订单状态）")
    @PutMapping("/contracts/{order_no}/amount")
    public Result<Integer> updateAmount(@PathVariable String order_no,
                                        @RequestParam("total_amount") BigDecimal total_amount,
                                        Authentication authentication) {
        if (!StaffRole.isFinance(authentication)) {
            return Result.forbidden("仅财务人员可管理合同金额");
        }
        if (total_amount == null || total_amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error(400, "合同金额必须大于 0");
        }
        if (total_amount.compareTo(MAX_AMOUNT) > 0) {
            return Result.error(400, "合同金额超出可录入范围");
        }
        DecorationOrder order = orderMapper.findByOrderNo(order_no);
        if (order == null || order.getIs_deleted() != 0) {
            return Result.error(404, "订单合同不存在");
        }
        int rows = orderMapper.updateAmountByOrderNo(order_no, total_amount);
        return Result.success("合同金额已更新", rows);
    }
}

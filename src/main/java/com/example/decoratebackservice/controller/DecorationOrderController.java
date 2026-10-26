package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.service.DecorationOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

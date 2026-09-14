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
}

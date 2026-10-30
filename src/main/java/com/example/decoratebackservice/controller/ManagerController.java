package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.mapper.DecorationOrderMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目经理工作台：查看所有已签订合同的商城订单项目。
 * <p>
 * 施工进度管理已迁移到 construction_stage（绑定 design_service_order），
 * 统一由 ConstructionStageController 提供（生成计划 / 开工 / 报完工 / 业主验收 / 返工）。
 */
@Tag(name = "项目经理-合同项目接口")
@RestController
@RequestMapping("/manager")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ManagerController {

    /** 合同已签订 */
    private static final int CONTRACT_SIGNED = 1;

    private final DecorationOrderMapper orderMapper;
    private final UserService userService;

    public ManagerController(DecorationOrderMapper orderMapper,
                             UserService userService) {
        this.orderMapper = orderMapper;
        this.userService = userService;
    }

    @Operation(summary = "项目经理查看所有已签订合同的订单项目")
    @GetMapping("/projects")
    public Result<List<Map<String, Object>>> projects(Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可查看合同项目");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (DecorationOrder order : orderMapper.findByContractStatus(CONTRACT_SIGNED)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("order", order);
            row.put("buyer_name", userService.findUsernameById(order.getUser_id()));
            result.add(row);
        }
        return Result.success(result);
    }
}

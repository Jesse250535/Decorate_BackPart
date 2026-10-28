package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.mapper.DecorationMaterialMapper;
import com.example.decoratebackservice.mapper.DecorationPlanMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationMaterialService;
import com.example.decoratebackservice.service.DecorationPlanService;
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

import java.util.List;

/**
 * 审核中心：把「谁审核谁」在后端写死，前端藏按钮不作数。
 * <ul>
 *     <li>材料供应商上传的材料 → 库管人员（ROLE_STOCK）审核</li>
 *     <li>装修方案提供商上传的方案 → 设计师（ROLE_DESIGNER）审核</li>
 * </ul>
 */
@Tag(name = "审核中心-材料/方案审核接口")
@RestController
@RequestMapping("/audit")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

    /**
     * 审核状态：待审核 / 通过 / 驳回
     */
    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_PASS = 1;
    private static final int AUDIT_REJECT = 2;

    private final DecorationMaterialService materialService;
    private final DecorationMaterialMapper materialMapper;
    private final DecorationPlanService planService;
    private final DecorationPlanMapper planMapper;

    public AuditController(DecorationMaterialService materialService,
                           DecorationMaterialMapper materialMapper,
                           DecorationPlanService planService,
                           DecorationPlanMapper planMapper) {
        this.materialService = materialService;
        this.materialMapper = materialMapper;
        this.planService = planService;
        this.planMapper = planMapper;
    }

    // ==================== 库管：材料审核 ====================

    @Operation(summary = "库管查看全部材料（含待审核，用于审核）")
    @GetMapping("/materials")
    public Result<List<DecorationMaterial>> materials(Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可审核材料");
        }
        List<DecorationMaterial> list = materialService.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0)
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "库管审核材料：通过后自动上架，驳回则保持下架")
    @PutMapping("/materials/{id}")
    public Result<Integer> auditMaterial(@PathVariable int id,
                                         @RequestParam("audit_status") int audit_status,
                                         @RequestParam(value = "audit_remark", required = false) String audit_remark,
                                         Authentication authentication) {
        if (!StaffRole.isStock(authentication)) {
            return Result.forbidden("仅库管人员可审核材料");
        }
        String error = checkAuditParams(audit_status, audit_remark);
        if (error != null) {
            return Result.error(400, error);
        }
        DecorationMaterial material = materialService.findById(id);
        if (material == null || material.getIs_deleted() != 0) {
            return Result.error(404, "材料不存在");
        }
        int rows = materialMapper.audit(id, audit_status, audit_remark);
        // 审核结论同时决定是否可售：通过才上架，驳回回到下架，待审核也保持下架
        materialService.updateStatus(id, audit_status == AUDIT_PASS ? 1 : 0);
        return Result.success(audit_status == AUDIT_PASS ? "已通过并上架" : "已驳回", rows);
    }

    // ==================== 设计师：方案审核 ====================

    @Operation(summary = "设计师查看全部方案（含待审核）")
    @GetMapping("/plans")
    public Result<List<DecorationPlan>> plans(Authentication authentication) {
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可审核装修方案");
        }
        List<DecorationPlan> list = planService.findAll().stream()
                .filter(item -> item.getIs_deleted() == 0)
                .toList();
        return Result.success(list);
    }

    @Operation(summary = "设计师审核方案：通过后自动上架，驳回则保持下架")
    @PutMapping("/plans/{id}")
    public Result<Integer> auditPlan(@PathVariable int id,
                                     @RequestParam("audit_status") int audit_status,
                                     @RequestParam(value = "audit_remark", required = false) String audit_remark,
                                     Authentication authentication) {
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可审核装修方案");
        }
        String error = checkAuditParams(audit_status, audit_remark);
        if (error != null) {
            return Result.error(400, error);
        }
        DecorationPlan plan = planService.findById(id);
        if (plan == null || plan.getIs_deleted() != 0) {
            return Result.error(404, "方案不存在");
        }
        int rows = planMapper.audit(id, audit_status, audit_remark);
        // 审核结论同时决定能否对外展示：通过才上架，驳回 / 待审核保持下架
        planMapper.updateStatus(id, audit_status == AUDIT_PASS ? 1 : 0);
        return Result.success(audit_status == AUDIT_PASS ? "已通过并上架" : "已驳回", rows);
    }

    /**
     * 审核参数校验：只允许 1通过 / 2驳回；驳回必须写清原因
     *
     * @return 错误信息；校验通过返回 null
     */
    private String checkAuditParams(int audit_status, String audit_remark) {
        if (audit_status != AUDIT_PASS && audit_status != AUDIT_REJECT) {
            return "审核结果只能是 1通过 / 2驳回";
        }
        if (audit_status == AUDIT_REJECT && (audit_remark == null || audit_remark.isBlank())) {
            return "驳回请填写审核意见";
        }
        if (audit_status == AUDIT_PENDING) {
            return "审核结果只能是 1通过 / 2驳回";
        }
        return null;
    }
}

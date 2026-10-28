package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.mapper.DecorationOrderMapper;
import com.example.decoratebackservice.mapper.DecorationProgressMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationOrderService;
import com.example.decoratebackservice.service.DecorationProgressService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目经理工作台：统管「所有已签订合同的项目」，并审核施工人员的进度上报。
 * <p>
 * 施工人员每上报一条进度都是「待审核」，只有项目经理通过后才对客户可见
 * （/progress/my 只返回 audit_status = 1 的记录）。
 */
@Tag(name = "项目经理-合同项目与进度审核接口")
@RestController
@RequestMapping("/manager")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ManagerController {

    /** 合同已签订 */
    private static final int CONTRACT_SIGNED = 1;

    /** 审核状态：待审核 / 通过 / 驳回 */
    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_PASS = 1;
    private static final int AUDIT_REJECT = 2;

    private final DecorationOrderService orderService;
    private final DecorationOrderMapper orderMapper;
    private final DecorationProgressService progressService;
    private final DecorationProgressMapper progressMapper;
    private final UserService userService;

    public ManagerController(DecorationOrderService orderService,
                             DecorationOrderMapper orderMapper,
                             DecorationProgressService progressService,
                             DecorationProgressMapper progressMapper,
                             UserService userService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.progressService = progressService;
        this.progressMapper = progressMapper;
        this.userService = userService;
    }

    @Operation(summary = "项目经理查看所有已签订合同的订单项目")
    @GetMapping("/projects")
    public Result<List<Map<String, Object>>> projects(Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可查看合同项目");
        }
        List<DecorationProgress> allProgress = progressService.findAll();
        Map<Integer, Integer> pendingByOrder = new HashMap<>();
        for (DecorationProgress progress : allProgress) {
            if (progress.getIs_deleted() == 0 && progress.getAudit_status() == AUDIT_PENDING) {
                pendingByOrder.merge(progress.getOrder_id(), 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (DecorationOrder order : orderMapper.findByContractStatus(CONTRACT_SIGNED)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("order", order);
            row.put("buyer_name", userService.findUsernameById(order.getUser_id()));
            // 待审核进度条数：项目经理一眼看出哪些项目还压着待审的上报
            row.put("pending_progress", pendingByOrder.getOrDefault(order.getId(), 0));
            result.add(row);
        }
        return Result.success(result);
    }

    @Operation(summary = "项目经理查看全部施工进度上报（含待审核/已通过/已驳回）")
    @GetMapping("/progress")
    public Result<List<Map<String, Object>>> progress(Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可审核施工进度");
        }
        Map<Integer, String> orderNoById = new HashMap<>();
        for (DecorationOrder order : orderService.findAll()) {
            orderNoById.put(order.getId(), order.getOrder_no());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (DecorationProgress progress : progressService.findAll()) {
            if (progress.getIs_deleted() != 0) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", progress.getId());
            row.put("order_id", progress.getOrder_id());
            row.put("order_no", orderNoById.get(progress.getOrder_id()));
            row.put("progress_status", progress.getProgress_status());
            row.put("status_desc", progress.getStatus_desc());
            row.put("progress_images", progress.getProgress_images());
            row.put("report_user_id", progress.getReport_user_id());
            row.put("reporter_name", userService.findUsernameById(progress.getReport_user_id()));
            row.put("audit_status", progress.getAudit_status());
            row.put("audit_user_id", progress.getAudit_user_id());
            row.put("audit_remark", progress.getAudit_remark());
            row.put("audit_time", progress.getAudit_time());
            row.put("create_time", progress.getCreate_time());
            result.add(row);
        }
        return Result.success(result);
    }

    @Operation(summary = "项目经理审核施工进度：1通过 2驳回（驳回必须写意见）")
    @PutMapping("/progress/{id}")
    public Result<Integer> auditProgress(@PathVariable int id,
                                         @RequestParam("audit_status") int audit_status,
                                         @RequestParam(value = "audit_remark", required = false) String audit_remark,
                                         Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可审核施工进度");
        }
        if (audit_status != AUDIT_PASS && audit_status != AUDIT_REJECT) {
            return Result.error(400, "审核结果只能是 1通过 / 2驳回");
        }
        if (audit_status == AUDIT_REJECT && (audit_remark == null || audit_remark.isBlank())) {
            return Result.error(400, "驳回请填写审核意见");
        }
        DecorationProgress exists = progressService.findById(id);
        if (exists == null || exists.getIs_deleted() != 0) {
            return Result.error(404, "进度记录不存在");
        }
        int userId = currentUserId(authentication);
        int rows = progressMapper.audit(id, audit_status, userId, audit_remark, LocalDateTime.now());
        return Result.success(audit_status == AUDIT_PASS ? "审核通过" : "已驳回", rows);
    }

    /**
     * 取当前登录用户ID（审核人留痕用）
     *
     * @return 找不到时返回 0
     */
    private int currentUserId(Authentication authentication) {
        var user = userService.findByUsernameForStaff(StaffRole.username(authentication));
        return user == null ? 0 : user.getId();
    }
}

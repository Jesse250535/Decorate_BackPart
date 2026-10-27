package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationOrderService;
import com.example.decoratebackservice.service.DecorationProgressService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 施工人员的进度上报接口。
 * <p>
 * 关键约束：report_user_id 一律由后端填入当前登录用户，不接受前端传入。
 * 否则施工人员可以伪造成别人上报，进度记录会失去可追溯性。
 */
@Tag(name = "施工人员-进度上报接口")
@RestController
@RequestMapping("/worker")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkerController {

    /**
     * 允许上报进度的订单状态：1 已支付、2 施工中
     */
    private static final int ORDER_PAID = 1;

    private static final int ORDER_CONSTRUCTING = 2;

    /**
     * 合同已签订。未签订合同的订单不允许上报进度：
     * 合同没签说明施工范围、金额尚未确认，此时上报的进度没有依据，也无法据此结算。
     */
    private static final int CONTRACT_SIGNED = 1;

    private final DecorationProgressService progressService;
    private final DecorationOrderService orderService;
    private final UserService userService;

    public WorkerController(DecorationProgressService progressService,
                            DecorationOrderService orderService,
                            UserService userService) {
        this.progressService = progressService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @Operation(summary = "施工人员查询可上报进度的订单（已支付 / 施工中，且合同已签订）")
    @GetMapping("/orders")
    public Result<List<DecorationOrder>> orders(Authentication authentication) {
        if (!StaffRole.isWorker(authentication)) {
            return Result.forbidden("仅施工人员可上报施工进度");
        }
        List<DecorationOrder> result = new ArrayList<>();
        for (DecorationOrder order : orderService.findAll()) {
            if (order.getOrder_status() != ORDER_PAID && order.getOrder_status() != ORDER_CONSTRUCTING) {
                continue;
            }
            // 合同未签订的订单不出现在下拉里，从源头上减少施工人员选到不可上报的订单
            if (order.getContract_status() != CONTRACT_SIGNED) {
                continue;
            }
            result.add(order);
        }
        return Result.success(result);
    }

    @Operation(summary = "施工人员查询自己上报过的进度记录")
    @GetMapping("/progress")
    public Result<List<DecorationProgress>> list(Authentication authentication) {
        if (!StaffRole.isWorker(authentication)) {
            return Result.forbidden("仅施工人员可查看施工进度");
        }
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("未找到当前登录用户");
        }
        return Result.success(progressService.findByReportUserId(userId));
    }

    @Operation(summary = "施工人员上报施工进度")
    @PostMapping("/progress")
    public Result<Integer> insert(@RequestBody DecorationProgress progress, Authentication authentication) {
        if (!StaffRole.isWorker(authentication)) {
            return Result.forbidden("仅施工人员可上报施工进度");
        }
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("未找到当前登录用户");
        }
        // 新增进度必须要求合同已签订
        String error = validate(progress, true);
        if (error != null) {
            return Result.error(400, error);
        }
        // 上报人强制取当前登录用户，忽略前端传入的任何值
        progress.setReport_user_id(userId);
        progress.setIs_deleted(0);
        if (progress.getProgress_images() == null) {
            // 该列 NOT NULL，没有图片时写入空串而不是 NULL
            progress.setProgress_images("");
        }
        return Result.success("进度上报成功", progressService.insert(progress));
    }

    @Operation(summary = "施工人员修改自己上报的进度")
    @PutMapping("/progress")
    public Result<Integer> update(@RequestBody DecorationProgress progress, Authentication authentication) {
        if (!StaffRole.isWorker(authentication)) {
            return Result.forbidden("仅施工人员可修改施工进度");
        }
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("未找到当前登录用户");
        }
        if (progress.getId() <= 0) {
            return Result.error(400, "缺少进度记录ID");
        }
        DecorationProgress existing = progressService.findById(progress.getId());
        if (existing == null) {
            return Result.error(404, "进度记录不存在");
        }
        // 只能改自己的记录，防止越权修改他人的施工进度
        if (existing.getReport_user_id() != userId) {
            return Result.forbidden("只能修改自己上报的进度");
        }
        // 修改历史记录不卡合同状态：合同未签订期间产生的旧记录仍需要能改
        String error = validate(progress, false);
        if (error != null) {
            return Result.error(400, error);
        }
        progress.setReport_user_id(userId);
        progress.setIs_deleted(existing.getIs_deleted());
        return Result.success("修改成功", progressService.update(progress));
    }

    @Operation(summary = "施工人员删除自己上报的进度")
    @DeleteMapping("/progress/{id}")
    public Result<Integer> delete(@PathVariable int id, Authentication authentication) {
        if (!StaffRole.isWorker(authentication)) {
            return Result.forbidden("仅施工人员可删除施工进度");
        }
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("未找到当前登录用户");
        }
        DecorationProgress existing = progressService.findById(id);
        if (existing == null) {
            return Result.error(404, "进度记录不存在");
        }
        if (existing.getReport_user_id() != userId) {
            return Result.forbidden("只能删除自己上报的进度");
        }
        return Result.success("删除成功", progressService.delete(id));
    }

    /**
     * 取当前登录用户在 sys_user 中的主键
     *
     * @return 用户ID；找不到返回 null
     */
    private Integer currentUserId(Authentication authentication) {
        String username = StaffRole.username(authentication);
        if (username == null) {
            return null;
        }
        User user = userService.findByUsernameForStaff(username);
        return user == null ? null : user.getId();
    }

    /**
     * 上报前的校验
     *
     * @param requireSignedContract 是否要求订单合同已签订。
     *                              新增时true——合同未签订不允许上报；
     *                              修改时false——早期已存在的历史记录仍允许修正，避免改不动旧数据
     * @return 错误信息；校验通过返回 null
     */
    private String validate(DecorationProgress progress, boolean requireSignedContract) {
        if (progress == null) {
            return "请求体不能为空";
        }
        if (progress.getOrder_id() <= 0) {
            return "请选择关联订单";
        }
        DecorationOrder order = orderService.findById(progress.getOrder_id());
        if (order == null) {
            return "关联订单不存在";
        }
        if (requireSignedContract && order.getContract_status() != CONTRACT_SIGNED) {
            return "该订单合同尚未签订，暂不能上报施工进度";
        }
        if (progress.getProgress_status() < 1 || progress.getProgress_status() > 5) {
            return "施工阶段只能是 1-5";
        }
        if (progress.getStatus_desc() == null || progress.getStatus_desc().isBlank()) {
            return "请填写进度说明";
        }
        return null;
    }
}

package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationCategory;
import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DecorationCategoryMapper;
import com.example.decoratebackservice.mapper.DecorationPlanMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DecorationPlanService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 装修方案工作台：上传 / 修改自己的方案。
 * <p>
 * 准入身份由 {@link StaffRole#isPlanProvider} 判定：ROLE_PLAN_PROVIDER 与
 * ROLE_DESIGNER 都放行（设计师已囊括装修方案提供商职能）。
 * <p>
 * 方案上传后为「待审核」，由设计师审核（见 {@link AuditController}），
 * 审核通过才允许上架展示，上传方自己无法直接让方案出现在商城。
 */
@Tag(name = "设计师/装修方案提供商-方案上传接口")
@RestController
@RequestMapping("/plan-portal")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlanPortalController {

    /**
     * 分类类型：1 装修方案
     */
    private static final int CATEGORY_TYPE_PLAN = 1;

    /**
     * 审核状态：待审核
     */
    private static final int AUDIT_PENDING = 0;

    private final DecorationPlanService planService;
    private final DecorationPlanMapper planMapper;
    private final DecorationCategoryMapper categoryMapper;
    private final UserService userService;

    public PlanPortalController(DecorationPlanService planService,
                                DecorationPlanMapper planMapper,
                                DecorationCategoryMapper categoryMapper,
                                UserService userService) {
        this.planService = planService;
        this.planMapper = planMapper;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
    }

    @Operation(summary = "装修方案提供商上传方案所需下拉数据：方案分类")
    @GetMapping("/options")
    public Result<Map<String, Object>> options(Authentication authentication) {
        if (!StaffRole.isPlanProvider(authentication)) {
            return Result.forbidden("仅设计师 / 装修方案提供商可上传方案");
        }
        List<DecorationCategory> categories = categoryMapper.findByType(CATEGORY_TYPE_PLAN);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        return Result.success(data);
    }

    @Operation(summary = "装修方案提供商查询自己上传的方案")
    @GetMapping("/plans")
    public Result<List<DecorationPlan>> plans(Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅设计师 / 装修方案提供商可上传方案");
        }
        return Result.success(planMapper.findByCreateUserId(user.getId()));
    }

    @Operation(summary = "装修方案提供商上传方案（落库为待审核，由设计师审核）")
    @PostMapping("/plans")
    public Result<Integer> insert(@RequestBody DecorationPlan plan, Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅设计师 / 装修方案提供商可上传方案");
        }
        String error = validate(plan);
        if (error != null) {
            return Result.error(400, error);
        }
        plan.setCreate_user_id(user.getId());
        // 未审核前一律下架，避免绕过设计师直接对外售卖
        plan.setAudit_status(AUDIT_PENDING);
        plan.setAudit_remark(null);
        plan.setStatus(0);
        plan.setSales_count(0);
        plan.setView_count(0);
        plan.setCollect_count(0);
        plan.setIs_deleted(0);
        return Result.success("方案已提交，等待设计师审核", planService.insert(plan));
    }

    @Operation(summary = "装修方案提供商修改自己上传的方案（修改后重新回到待审核）")
    @PutMapping("/plans")
    public Result<Integer> update(@RequestBody DecorationPlan plan, Authentication authentication) {
        User user = currentUser(authentication);
        if (user == null) {
            return Result.forbidden("仅设计师 / 装修方案提供商可上传方案");
        }
        if (plan.getId() <= 0) {
            return Result.error(400, "缺少方案ID");
        }
        DecorationPlan exists = planService.findById(plan.getId());
        if (exists == null || exists.getIs_deleted() != 0) {
            return Result.error(404, "方案不存在");
        }
        if (exists.getCreate_user_id() != user.getId()) {
            return Result.forbidden("只能修改自己上传的方案");
        }
        if (exists.getAudit_status() == 1) {
            return Result.error(400, "该方案已审核通过，如需修改请联系设计师下架后再改");
        }
        String error = validate(plan);
        if (error != null) {
            return Result.error(400, error);
        }
        // update 是全字段覆盖，销量/浏览量等运营数据必须沿用库里已有值，不能被清零
        plan.setCreate_user_id(exists.getCreate_user_id());
        plan.setSales_count(exists.getSales_count());
        plan.setView_count(exists.getView_count());
        plan.setCollect_count(exists.getCollect_count());
        plan.setStatus(0);
        plan.setAudit_status(AUDIT_PENDING);
        plan.setAudit_remark(null);
        plan.setIs_deleted(0);
        int rows = planService.update(plan);
        planMapper.audit(plan.getId(), AUDIT_PENDING, null);
        return Result.success("修改成功，已重新提交审核", rows);
    }

    /**
     * 上传/修改前的必填校验
     *
     * @return 错误信息；校验通过返回 null
     */
    private String validate(DecorationPlan plan) {
        if (plan == null) {
            return "请求体不能为空";
        }
        if (plan.getPlan_name() == null || plan.getPlan_name().isBlank()) {
            return "请填写方案名称";
        }
        if (plan.getPrice() == null) {
            return "请填写方案价格";
        }
        if (plan.getDescription() == null || plan.getDescription().isBlank()) {
            return "请填写方案说明";
        }
        if (plan.getCategory_id() <= 0) {
            return "请选择方案分类";
        }
        if (plan.getArea_min() > 0 && plan.getArea_max() > 0 && plan.getArea_min() > plan.getArea_max()) {
            return "最小面积不能大于最大面积";
        }
        return null;
    }

    /**
     * 取当前登录账号
     *
     * @return 非装修方案提供商 / 未登录返回 null
     */
    private User currentUser(Authentication authentication) {
        if (!StaffRole.isPlanProvider(authentication)) {
            return null;
        }
        return userService.findByUsernameForStaff(StaffRole.username(authentication));
    }
}

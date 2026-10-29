package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.ConstructionStage;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.ConstructionStageService;
import com.example.decoratebackservice.service.DesignServiceOrderService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 施工阶段接口：拆墙 / 开槽 / 水电改造 / 贴瓷砖 / 做吊顶 / 刮腻子 / 全屋定制 / 保洁 / 软装。
 * <p>
 * 项目经理负责开工与报完工，业主负责逐阶段验收；
 * 验收不通过则进入返工，需登记预计返工天数，返工完成后重新提交验收。
 */
@Tag(name = "设计服务-施工阶段与验收接口")
@RestController
@RequestMapping("/construction")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConstructionStageController {

    @Autowired
    private ConstructionStageService constructionStageService;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    @Autowired
    private UserService userService;

    @Operation(summary = "查看某设计服务单的施工阶段看板（业主/设计师/项目经理）")
    @GetMapping("/service/{serviceId}")
    public Result<List<ConstructionStage>> listByService(@PathVariable int serviceId, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        if (!canView(order, userId, authentication)) {
            return Result.forbidden("无权查看该项目的施工进度");
        }
        return Result.success(constructionStageService.findByService(serviceId));
    }

    @Operation(summary = "项目经理生成施工阶段计划（按装修方式自动排期）")
    @PostMapping("/service/{serviceId}/init")
    public Result<List<ConstructionStage>> init(@PathVariable int serviceId,
                                                @RequestBody(required = false) Map<String, Object> body,
                                                Authentication authentication) {
        Integer managerId = currentUserId(authentication);
        if (managerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可生成施工计划");
        }
        DesignServiceOrder order = designServiceOrderService.requireOrder(serviceId);
        LocalDate startDate = body == null ? null : parseDate(body.get("start_date"));
        return Result.success("施工计划已生成",
                constructionStageService.initStages(serviceId, order.getDecoration_mode(), managerId, startDate));
    }

    @Operation(summary = "项目经理开工（前序阶段必须已通过业主验收）")
    @PutMapping("/{id}/start")
    public Result<ConstructionStage> start(@PathVariable int id, Authentication authentication) {
        Integer managerId = currentUserId(authentication);
        if (managerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可开工");
        }
        return Result.success("已开工", constructionStageService.start(id, managerId));
    }

    @Operation(summary = "项目经理报完工：自动计算实际工期、是否按时与超期天数")
    @PutMapping("/{id}/finish")
    public Result<ConstructionStage> finish(@PathVariable int id,
                                            @RequestBody(required = false) Map<String, Object> body,
                                            Authentication authentication) {
        Integer managerId = currentUserId(authentication);
        if (managerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可报完工");
        }
        Map<String, Object> data = body == null ? Map.of() : body;
        return Result.success("已报完工，等待业主验收", constructionStageService.finish(id,
                str(data.get("images")), str(data.get("remark"))));
    }

    @Operation(summary = "业主验收：通过则阶段完成，不通过则进入返工并登记返工天数")
    @PutMapping("/{id}/accept")
    public Result<ConstructionStage> accept(@PathVariable int id,
                                            @RequestBody Map<String, Object> body,
                                            Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        if (body == null || body.get("pass") == null) {
            return Result.validateFailed("请给出验收结论 pass=true/false");
        }
        boolean pass = Boolean.parseBoolean(String.valueOf(body.get("pass")));
        Integer reworkDays = body.get("rework_days") == null ? null
                : Integer.valueOf(String.valueOf(body.get("rework_days")));
        String remark = str(body.get("remark"));
        return Result.success(pass ? "验收通过" : "已提交返工要求",
                constructionStageService.accept(id, userId, pass, remark, reworkDays));
    }

    @Operation(summary = "项目经理登记返工完成，重新提交业主验收")
    @PutMapping("/{id}/rework-done")
    public Result<ConstructionStage> reworkDone(@PathVariable int id,
                                                @RequestBody(required = false) Map<String, Object> body,
                                                Authentication authentication) {
        Integer managerId = currentUserId(authentication);
        if (managerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可登记返工完成");
        }
        LocalDate endDate = body == null ? null : parseDate(body.get("end_date"));
        return Result.success("返工完成，等待业主复验", constructionStageService.reworkDone(id, endDate));
    }

    @Operation(summary = "项目经理调整阶段计划起止日期")
    @PutMapping("/{id}/plan")
    public Result<ConstructionStage> updatePlan(@PathVariable int id,
                                                @RequestBody Map<String, Object> body,
                                                Authentication authentication) {
        if (!StaffRole.isManager(authentication)) {
            return Result.forbidden("仅项目经理可调整计划");
        }
        return Result.success("计划已更新",
                constructionStageService.updatePlan(id, parseDate(body.get("start_date")),
                        parseDate(body.get("end_date"))));
    }

    private boolean canView(DesignServiceOrder order, int userId, Authentication authentication) {
        return order.getUser_id() == userId || order.getDesigner_id() == userId
                || StaffRole.isManager(authentication);
    }

    private LocalDate parseDate(Object value) {
        String text = str(value);
        return text == null || text.isBlank() ? null : LocalDate.parse(text);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}

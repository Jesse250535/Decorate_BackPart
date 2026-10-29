package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DesignQuote;
import com.example.decoratebackservice.entity.DesignQuoteItem;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.DesignQuoteService;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报价单接口：设计师按施工阶段逐项报价 → 业主确认 → 合同金额取报价总额。
 * <p>
 * 硬装、软装明细分开（item_type），业主可以据此选择只做硬装、只做软装或整装。
 */
@Tag(name = "设计服务-报价单接口")
@RestController
@RequestMapping("/design-quote")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DesignQuoteController {

    @Autowired
    private DesignQuoteService designQuoteService;

    @Autowired
    private UserService userService;

    @Operation(summary = "设计师提交报价单（整体覆盖，自动作废旧报价）")
    @PostMapping("/submit")
    public Result<DesignQuote> submit(@RequestBody QuoteRequest request, Authentication authentication) {
        Integer designerId = currentUserId(authentication);
        if (designerId == null) {
            return Result.unauthorized("请先登录");
        }
        if (!StaffRole.isDesigner(authentication)) {
            return Result.forbidden("仅设计师可提交报价");
        }
        if (request == null || request.service_id <= 0) {
            return Result.validateFailed("请指定设计服务单");
        }
        return Result.success("报价已提交，等待业主确认",
                designQuoteService.submit(request.service_id, designerId, request.items, request.remark));
    }

    @Operation(summary = "报价单详情（含明细）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable int id) {
        DesignQuote quote = designQuoteService.findById(id);
        if (quote == null) {
            return Result.error(404, "报价单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("quote", quote);
        data.put("items", designQuoteService.findItems(id));
        return Result.success(data);
    }

    @Operation(summary = "查看某设计服务单下的所有报价单")
    @GetMapping("/service/{serviceId}")
    public Result<List<DesignQuote>> listByService(@PathVariable int serviceId) {
        return Result.success(designQuoteService.findByService(serviceId));
    }

    @Operation(summary = "业主确认报价：确认后进入待签合同")
    @PutMapping("/{id}/confirm")
    public Result<DesignQuote> confirm(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success("报价已确认，等待项目经理签订合同", designQuoteService.confirm(id, userId));
    }

    /**
     * 报价请求体：service_id + 明细列表 + 说明
     */
    public static class QuoteRequest {
        public int service_id;
        public String remark;
        public List<DesignQuoteItem> items;
    }

    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }
}

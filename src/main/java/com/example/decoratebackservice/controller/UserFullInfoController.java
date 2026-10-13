package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserFullInfo;
import com.example.decoratebackservice.security.LoginUser;
import com.example.decoratebackservice.service.UserFullInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户完整信息查询接口：返回 sys_user 的全部字段
 * <p>
 * 非侵入式实现：新增独立文件，不改动既有 UserController / UserService / UserMapper。
 * 接口需登录后访问（沿用全局 anyRequest().authenticated() 策略）
 * <p>
 * 新增 /current 与 /findByUsername 两个接口，用于替代前端「拉取全部用户再按用户名筛选」的做法：
 * 前者只返回当前登录用户自己的信息，后者按用户名精确查询，均为非侵入式增量。
 */
@Tag(name = "用户完整信息接口")
@RestController
@RequestMapping("/user/full")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserFullInfoController {

    private final UserFullInfoService userFullInfoService;

    public UserFullInfoController(UserFullInfoService userFullInfoService) {
        this.userFullInfoService = userFullInfoService;
    }

    @Operation(summary = "按ID查询单个用户完整信息（含 email、create_time、update_time 等全部字段）")
    @GetMapping("/findById/{id}")
    public Result<UserFullInfo> findById(@PathVariable int id) {
        return Result.success(userFullInfoService.findById(id));
    }

    @Operation(summary = "查询所有用户完整信息（含 email、create_time、update_time 等全部字段）")
    @GetMapping("/findAll")
    public Result<List<UserFullInfo>> findAll() {
        return Result.success(userFullInfoService.findAll());
    }

    @Operation(summary = "按用户名查询单个用户完整信息（含 email、create_time、update_time 等全部字段）")
    @GetMapping("/findByUsername")
    public Result<UserFullInfo> findByUsername(@RequestParam("username") String username) {
        UserFullInfo user = userFullInfoService.findByUsername(username);
        if (user == null) {
            return Result.error("未找到用户名为 " + username + " 的用户");
        }
        return Result.success(user);
    }

    /**
     * 查询当前登录用户自己的完整信息。
     * <p>
     * /auth/me 只认 LoginUser（管理员），普通用户会话一律被判为 401，前端因此无法获取
     * 「当前用户」。本接口直接以会话 principal 的用户名反查 sys_user，普通用户与管理员通用，
     * 只返回当前用户本人数据，不需要前端拉取全量用户后自行筛选。
     */
    @Operation(summary = "查询当前登录用户自己的完整信息（普通用户与管理员通用）")
    @GetMapping("/current")
    public Result<UserFullInfo> current() {
        String username = resolveCurrentUsername();
        if (username == null) {
            return Result.unauthorized("未登录或会话已过期");
        }
        UserFullInfo user = userFullInfoService.findByUsername(username);
        if (user == null) {
            // 管理员账号走 /auth/admin/login，其用户名不在 sys_user 表中
            return Result.error("当前会话不是普通用户，未在 sys_user 中找到对应账号");
        }
        return Result.success(user);
    }

    /**
     * 从当前安全上下文中解析登录用户名。
     *
     * @return 用户名；未登录或无法识别时返回 null
     */
    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}

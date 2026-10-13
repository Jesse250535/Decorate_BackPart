package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.AccountLoginRequest;
import com.example.decoratebackservice.config.AdminLoginRequest;
import com.example.decoratebackservice.config.AdminLoginResponse;
import com.example.decoratebackservice.config.LoginRequest;
import com.example.decoratebackservice.config.UsernameLoginRequest;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.security.LoginUser;
import com.example.decoratebackservice.service.AdminLoginService;
import com.example.decoratebackservice.service.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 认证接口：基于 HttpSession 的会话登录
 * <p>
 * 统一承载管理员登录与用户登录，避免登录逻辑分散在多个 Controller 中。
 * 所有对外 URL 与整合前完全一致，前端与 SecurityConfig 放行配置无需改动：
 * <ul>
 *     <li>POST /auth/login              通用登录（用户名 + 密码，走 AuthenticationManager）</li>
 *     <li>POST /auth/admin/login        管理员登录（支持用户名 / 昵称 / 手机号）</li>
 *     <li>POST /auth/login-by-account   用户登录（昵称 / 邮箱）</li>
 *     <li>POST /auth/login-by-username  用户登录（用户名）</li>
 *     <li>GET  /auth/me                 获取当前登录管理员</li>
 *     <li>POST /auth/logout             退出登录</li>
 * </ul>
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AdminLoginService adminLoginService;

    private final UserLoginService userLoginService;

    /**
     * 会话上下文仓库，登录成功后把认证信息写入 HttpSession
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager,
                          AdminLoginService adminLoginService,
                          UserLoginService userLoginService) {
        this.authenticationManager = authenticationManager;
        this.adminLoginService = adminLoginService;
        this.userLoginService = userLoginService;
    }

    // ==================== 通用登录 ====================

    @Operation(summary = "通用登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginRequest loginRequest,
                                HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            saveAuthentication(authentication, request, response);
            return Result.success("登录成功", authentication.getName());
        } catch (AuthenticationException e) {
            return Result.unauthorized("用户名或密码错误");
        }
    }

    // ==================== 管理员登录 ====================

    @Operation(summary = "管理员登录（支持用户名/昵称/手机号），返回管理员相关字段与角色权限")
    @PostMapping("/admin/login")
    public Result<AdminLoginResponse> adminLogin(@RequestBody @Valid AdminLoginRequest request,
                                                 HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SysAdmin admin = adminLoginService.authenticate(request.getAccount(), request.getPassword());
        if (admin == null) {
            return Result.unauthorized("账号或密码错误");
        }

        Collection<? extends GrantedAuthority> authorities = adminLoginService.buildAuthorities(admin.getRole_id());

        // 以 LoginUser 作为 principal，写入会话后可被 /auth/me 等接口识别
        LoginUser principal = new LoginUser(admin, authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        saveAuthentication(authentication, httpRequest, httpResponse);

        return Result.success("登录成功", buildAdminResponse(admin, authorities));
    }

    /**
     * 组装返回给前端的管理员相关字段与角色权限
     */
    private AdminLoginResponse buildAdminResponse(SysAdmin admin, Collection<? extends GrantedAuthority> authorities) {
        AdminLoginResponse response = new AdminLoginResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setPhone(admin.getPhone());
        response.setNickname(admin.getNickname());
        response.setAvatar(admin.getAvatar());
        response.setGender(admin.getGender());
        response.setUser_type(admin.getUser_type());
        response.setRole_id(admin.getRole_id());
        response.setProfile(admin.getProfile());
        response.setViolation_count(admin.getViolation_count());
        response.setStatus(admin.getStatus());

        Role role = adminLoginService.findRole(admin.getRole_id());
        if (role != null) {
            response.setRole_name(role.getRole_name());
            response.setRole_code(role.getRole_code());
        }

        List<String> authorityNames = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            authorityNames.add(authority.getAuthority());
        }
        response.setAuthorities(authorityNames);
        return response;
    }

    // ==================== 用户登录 ====================

    @Operation(summary = "昵称/邮箱登录")
    @PostMapping("/login-by-account")
    public Result<String> loginByAccount(@RequestBody @Valid AccountLoginRequest request,
                                         HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserDetails userDetails = userLoginService.authenticate(request.getAccount(), request.getPassword());
        if (userDetails == null) {
            return Result.unauthorized("昵称/邮箱或密码错误");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        saveAuthentication(authentication, httpRequest, httpResponse);
        return Result.success("登录成功", userDetails.getUsername());
    }

    @Operation(summary = "用户名登录")
    @PostMapping("/login-by-username")
    public Result<String> loginByUsername(@RequestBody @Valid UsernameLoginRequest request,
                                          HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserDetails userDetails = userLoginService.authenticateByUsername(request.getUsername(), request.getPassword());
        if (userDetails == null) {
            return Result.unauthorized("用户名或密码错误");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        saveAuthentication(authentication, httpRequest, httpResponse);
        return Result.success("登录成功", userDetails.getUsername());
    }

    // ==================== 会话相关 ====================

    @Operation(summary = "获取当前登录管理员")
    @GetMapping("/me")
    public Result<SysAdmin> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return Result.unauthorized("未登录");
        }
        SysAdmin admin = loginUser.getAdmin();
        admin.setPassword(null);
        return Result.success(admin);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    /**
     * 把认证信息写入 SecurityContext 并持久化到 HttpSession。
     * <p>
     * 三个登录入口原本各自重复这段逻辑，整合后统一收口到这里，行为与原先完全一致。
     */
    private void saveAuthentication(Authentication authentication,
                                    HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}

package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.AccountLoginRequest;
import com.example.decoratebackservice.config.AdminLoginRequest;
import com.example.decoratebackservice.config.AdminLoginResponse;
import com.example.decoratebackservice.config.LoginRequest;
import com.example.decoratebackservice.config.UsernameLoginRequest;
import com.example.decoratebackservice.config.UserTypeRequest;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.mapper.UserMapper;
import com.example.decoratebackservice.security.LoginUser;
import com.example.decoratebackservice.service.AdminLoginService;
import com.example.decoratebackservice.service.RoleService;
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
                          UserLoginService userLoginService,
                          RoleService roleService,
                          UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.adminLoginService = adminLoginService;
        this.userLoginService = userLoginService;
        this.roleService = roleService;
        this.userMapper = userMapper;
    }

    /**
     * 注册页角色列表与「首次登录」身份类型回写所需的依赖
     */
    private final RoleService roleService;

    private final UserMapper userMapper;

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
            // 增量：密码正确但账号被封禁时返回 423，前端据此弹出「账号已封禁」窗口
            if (userLoginService.isBannedByAccount(request.getAccount(), request.getPassword())) {
                return Result.error(423, "你的账号已被封禁，暂时无法登录");
            }
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
            // 增量：密码正确但账号被封禁时返回 423，前端据此弹出「账号已封禁」窗口
            if (userLoginService.isBannedByUsername(request.getUsername(), request.getPassword())) {
                return Result.error(423, "你的账号已被封禁，暂时无法登录");
            }
            return Result.unauthorized("用户名或密码错误");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        saveAuthentication(authentication, httpRequest, httpResponse);
        return Result.success("登录成功", userDetails.getUsername());
    }

    // ==================== 注册页 / 首次登录 ====================

    @Operation(summary = "注册页可选身份类型（sys_role 全量角色）")
    @GetMapping("/roles")
    public Result<List<Role>> roles() {
        // 注册页要在未登录状态下填表，因此该接口在 SecurityConfig 中放行
        return Result.success(roleService.findAll());
    }

    @Operation(summary = "首次登录弹窗：确认当前用户的身份类型（1客户2装修爱好者3购房者4家具爱好者5其他）")
    @PostMapping("/user-type")
    public Result<Integer> updateUserType(@RequestBody UserTypeRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.unauthorized("未登录或会话已过期");
        }
        if (request == null || request.getUser_type() == null
                || request.getUser_type() < USER_TYPE_MIN || request.getUser_type() > USER_TYPE_MAX) {
            return Result.validateFailed("用户类型只能是 1客户 2装修爱好者 3购房者 4家具爱好者 5其他");
        }
        int rows = userMapper.updateUserType(authentication.getName(), request.getUser_type());
        if (rows <= 0) {
            return Result.error(404, "当前账号不是前台用户，无法设置身份类型");
        }
        return Result.success("身份类型已保存", rows);
    }

    /**
     * 用户身份类型的取值区间：1客户 2装修爱好者 3购房者 4家具爱好者 5其他
     */
    private static final int USER_TYPE_MIN = 1;

    private static final int USER_TYPE_MAX = 5;

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

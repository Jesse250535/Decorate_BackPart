package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.AdminLoginRequest;
import com.example.decoratebackservice.config.AdminLoginResponse;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.security.LoginUser;
import com.example.decoratebackservice.service.AdminLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 管理员登录接口：读取 sys_admin 相关字段完成登录校验，并返回管理员信息与角色权限
 * <p>
 * 非侵入式实现：新增独立文件，不改动既有 AuthController，仅在 SecurityConfig 中新增放行配置。
 * 登录成功后写入 HttpSession（principal 为 LoginUser），因此 /auth/me 等既有接口同样可用
 */
@Tag(name = "管理员登录接口")
@RestController
@RequestMapping("/auth/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminLoginController {

    private final AdminLoginService adminLoginService;

    /**
     * 会话上下文仓库，登录成功后把认证信息写入 HttpSession
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AdminLoginController(AdminLoginService adminLoginService) {
        this.adminLoginService = adminLoginService;
    }

    @Operation(summary = "管理员登录（支持用户名/昵称/手机号），返回管理员相关字段与角色权限")
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody @Valid AdminLoginRequest request,
                                            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SysAdmin admin = adminLoginService.authenticate(request.getAccount(), request.getPassword());
        if (admin == null) {
            return Result.unauthorized("账号或密码错误");
        }

        Collection<? extends GrantedAuthority> authorities = adminLoginService.buildAuthorities(admin.getRole_id());

        // 以 LoginUser 作为 principal，写入会话后可被既有 /auth/me 等接口识别
        LoginUser principal = new LoginUser(admin, authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return Result.success("登录成功", buildResponse(admin, authorities));
    }

    /**
     * 组装返回给前端的管理员相关字段与角色权限
     */
    private AdminLoginResponse buildResponse(SysAdmin admin, Collection<? extends GrantedAuthority> authorities) {
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
}

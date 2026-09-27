package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.AccountLoginRequest;
import com.example.decoratebackservice.service.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用户登录接口：支持昵称或邮箱登录
 */
@Tag(name = "用户登录接口")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserAccountLoginController {

    private final UserLoginService userLoginService;

    /**
     * 会话上下文仓库，登录成功后把认证信息写入 HttpSession
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public UserAccountLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

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
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return Result.success("登录成功", userDetails.getUsername());
    }
}

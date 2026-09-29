package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.config.GiteeLoginProperties;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.GiteeLoginOAuthService;
import com.example.decoratebackservice.service.GiteeOAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Gitee 账号「直接登录」接口：仅登录已绑定账号，不自动注册
 * <p>
 * 独立于既有「Gitee 注册」流程（GiteeLoginController），新增文件实现，不改动原有代码
 */
@Tag(name = "Gitee 直接登录接口")
@RestController
@RequestMapping("/auth/gitee/login")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GiteeDirectLoginController {

    private final GiteeLoginOAuthService giteeLoginOAuthService;
    private final GiteeOAuthService giteeOAuthService;
    private final GiteeLoginProperties giteeLoginProperties;

    /**
     * 会话上下文仓库，登录成功后把认证信息写入 HttpSession
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public GiteeDirectLoginController(GiteeLoginOAuthService giteeLoginOAuthService,
                                      GiteeOAuthService giteeOAuthService,
                                      GiteeLoginProperties giteeLoginProperties) {
        this.giteeLoginOAuthService = giteeLoginOAuthService;
        this.giteeOAuthService = giteeOAuthService;
        this.giteeLoginProperties = giteeLoginProperties;
    }

    @Operation(summary = "跳转到 Gitee 授权页（登录入口）")
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(giteeLoginOAuthService.buildLoginAuthorizeUrl());
    }

    @Operation(summary = "Gitee 登录回调（未绑定账号不建号）")
    @GetMapping("/callback")
    public void callback(@RequestParam(value = "code", required = false) String code,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (code == null || code.isBlank()) {
            response.sendRedirect(redirect(giteeLoginProperties.getPageUrl(), "giteeLogin", "fail"));
            return;
        }
        String accessToken = giteeLoginOAuthService.exchangeAccessToken(code);
        JsonNode giteeUser = giteeOAuthService.fetchGiteeUser(accessToken);
        String login = giteeUser.path("login").asText(null);

        User user = giteeOAuthService.findByGiteeLogin(login);
        if (user == null) {
            // 登录入口：未绑定账号时不自动注册，提示前往注册
            response.sendRedirect(redirect(giteeLoginProperties.getPageUrl(), "giteeLogin", "not_registered"));
            return;
        }
        loginAndSave(user, request, response);
        response.sendRedirect(redirect(giteeLoginProperties.getSuccessUrl(), "giteeLogin", "success"));
    }

    /**
     * 构建认证信息并写入 HttpSession
     */
    private void loginAndSave(User user, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = giteeOAuthService.buildUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    /**
     * 拼接带参数的重定向地址
     */
    private String redirect(String url, String name, String value) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + name + "=" + URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}

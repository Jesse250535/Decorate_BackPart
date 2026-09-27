package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.config.GiteeIdentityRequest;
import com.example.decoratebackservice.config.GiteeProperties;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.GiteeOAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
 * Gitee 第三方登录接口：授权 → 回调 → 未注册时选择身份并自动注册
 */
@Tag(name = "Gitee 第三方登录接口")
@RestController
@RequestMapping("/auth/gitee")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GiteeLoginController {

    /**
     * 暂存 Gitee 用户信息的 Session 属性名
     */
    private static final String SESSION_GITEE_USER = "GITEE_TEMP_USER";

    private final GiteeOAuthService giteeOAuthService;
    private final GiteeProperties giteeProperties;

    /**
     * 会话上下文仓库，登录成功后把认证信息写入 HttpSession
     */
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public GiteeLoginController(GiteeOAuthService giteeOAuthService, GiteeProperties giteeProperties) {
        this.giteeOAuthService = giteeOAuthService;
        this.giteeProperties = giteeProperties;
    }

    @Operation(summary = "跳转到 Gitee 授权页")
    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(giteeOAuthService.buildAuthorizeUrl());
    }

    @Operation(summary = "Gitee 授权回调")
    @GetMapping("/callback")
    public void callback(@RequestParam(value = "code", required = false) String code,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (code == null || code.isBlank()) {
            response.sendRedirect(redirect(giteeProperties.getSuccessUrl(), "giteeLogin", "fail"));
            return;
        }
        String accessToken = giteeOAuthService.exchangeAccessToken(code);
        JsonNode giteeUser = giteeOAuthService.fetchGiteeUser(accessToken);
        String login = giteeUser.path("login").asText(null);

        User user = giteeOAuthService.findByGiteeLogin(login);
        if (user == null) {
            // 未注册：暂存 Gitee 用户信息，跳转到“选择身份”页面
            request.getSession(true).setAttribute(SESSION_GITEE_USER, giteeUser.toString());
            response.sendRedirect(giteeProperties.getIdentityPageUrl());
            return;
        }
        // 已注册：直接建立会话
        loginAndSave(user, request, response);
        response.sendRedirect(redirect(giteeProperties.getSuccessUrl(), "giteeLogin", "success"));
    }

    @Operation(summary = "选择身份并完成自动注册")
    @PostMapping("/complete-register")
    public void completeRegister(@ModelAttribute GiteeIdentityRequest identityRequest,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object temp = session == null ? null : session.getAttribute(SESSION_GITEE_USER);
        if (temp == null) {
            response.sendRedirect(redirect(giteeProperties.getSuccessUrl(), "giteeLogin", "expired"));
            return;
        }
        UserIdentityType identityType;
        try {
            identityType = UserIdentityType.fromText(identityRequest.getIdentityType());
        } catch (IllegalArgumentException e) {
            response.sendRedirect(redirect(giteeProperties.getIdentityPageUrl(), "error", e.getMessage()));
            return;
        }
        User user = giteeOAuthService.createUser(temp.toString(), identityType);
        session.removeAttribute(SESSION_GITEE_USER);
        loginAndSave(user, request, response);
        response.sendRedirect(redirect(giteeProperties.getSuccessUrl(), "giteeLogin", "success"));
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

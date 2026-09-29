package com.example.decoratebackservice.service;

import com.example.decoratebackservice.config.GiteeLoginProperties;
import com.example.decoratebackservice.config.GiteeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Gitee「直接登录」授权业务层
 * <p>
 * 与注册流程隔离：仅负责「授权地址构建」与「授权码换 token」，
 * 用户查询 / 用户信息获取 / 权限装配复用既有 GiteeOAuthService，避免改动原有代码
 */
@Service
public class GiteeLoginOAuthService {

    private static final String AUTHORIZE_URL = "https://gitee.com/oauth/authorize";
    private static final String TOKEN_URL = "https://gitee.com/oauth/token";
    private static final String SCOPE = "user_info";

    private final GiteeProperties giteeProperties;
    private final GiteeLoginProperties giteeLoginProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GiteeLoginOAuthService(GiteeProperties giteeProperties,
                                  GiteeLoginProperties giteeLoginProperties,
                                  ObjectMapper objectMapper) {
        this.giteeProperties = giteeProperties;
        this.giteeLoginProperties = giteeLoginProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 构建登录用授权地址（redirect_uri 使用登录专用回调地址）
     */
    public String buildLoginAuthorizeUrl() {
        return AUTHORIZE_URL
                + "?client_id=" + urlEncode(giteeProperties.getClientId())
                + "&redirect_uri=" + urlEncode(giteeLoginProperties.getRedirectUri())
                + "&response_type=code"
                + "&scope=" + urlEncode(SCOPE);
    }

    /**
     * 使用登录专用 redirect_uri 将授权码换取 access_token
     */
    public String exchangeAccessToken(String code) {
        String form = "grant_type=authorization_code"
                + "&code=" + urlEncode(code)
                + "&client_id=" + urlEncode(giteeProperties.getClientId())
                + "&client_secret=" + urlEncode(giteeProperties.getClientSecret())
                + "&redirect_uri=" + urlEncode(giteeLoginProperties.getRedirectUri());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        JsonNode node = send(request);
        String accessToken = textOrNull(node, "access_token");
        if (accessToken == null) {
            throw new IllegalStateException("Gitee 授权失败，未获取到 access_token：" + node);
        }
        return accessToken;
    }

    private JsonNode send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Gitee 接口调用失败，状态码：" + response.statusCode() + "，响应：" + response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gitee 接口调用被中断", e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Gitee 接口调用异常：" + e.getMessage(), e);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isEmpty() ? null : text;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}

package com.example.decoratebackservice.service;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.config.GiteeProperties;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Gitee OAuth2 第三方登录业务层
 * <p>
 * 复用本站既有的 sys_user 表：以 Gitee 登录名（login）作为本站 username 进行绑定
 */
@Service
public class GiteeOAuthService {

    private static final String AUTHORIZE_URL = "https://gitee.com/oauth/authorize";
    private static final String TOKEN_URL = "https://gitee.com/oauth/token";
    private static final String USER_INFO_URL = "https://gitee.com/api/v5/user";
    private static final String SCOPE = "user_info";

    private final GiteeProperties giteeProperties;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public GiteeOAuthService(GiteeProperties giteeProperties, UserMapper userMapper,
                             RoleService roleService, PasswordEncoder passwordEncoder,
                             ObjectMapper objectMapper) {
        this.giteeProperties = giteeProperties;
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * 构建 Gitee 授权地址
     */
    public String buildAuthorizeUrl() {
        return AUTHORIZE_URL
                + "?client_id=" + urlEncode(giteeProperties.getClientId())
                + "&redirect_uri=" + urlEncode(giteeProperties.getRedirectUri())
                + "&response_type=code"
                + "&scope=" + urlEncode(SCOPE);
    }

    /**
     * 使用授权码换取 access_token
     */
    public String exchangeAccessToken(String code) {
        String form = "grant_type=authorization_code"
                + "&code=" + urlEncode(code)
                + "&client_id=" + urlEncode(giteeProperties.getClientId())
                + "&client_secret=" + urlEncode(giteeProperties.getClientSecret())
                + "&redirect_uri=" + urlEncode(giteeProperties.getRedirectUri());
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

    /**
     * 获取 Gitee 用户信息
     */
    public JsonNode fetchGiteeUser(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL + "?access_token=" + urlEncode(accessToken)))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return send(request);
    }

    /**
     * 按 Gitee 登录名查找本站账号
     */
    public User findByGiteeLogin(String login) {
        if (login == null || login.isBlank()) {
            return null;
        }
        return userMapper.findByUsername(login.trim());
    }

    /**
     * 依据 Gitee 用户信息与所选身份类型创建本站账号
     *
     * @param giteeUserJson Gitee 用户信息 JSON（暂存于 Session）
     * @param identityType  用户选择的身份类型
     * @return 创建后的用户
     */
    public User createUser(String giteeUserJson, UserIdentityType identityType) {
        JsonNode giteeUser;
        try {
            giteeUser = objectMapper.readTree(giteeUserJson);
        } catch (Exception e) {
            throw new IllegalStateException("解析 Gitee 用户信息失败", e);
        }
        String login = resolveUsername(giteeUser);
        int identityCode = identityType.getCode();
        String avatar = textOrNull(giteeUser, "avatar_url");
        String nickname = textOrNull(giteeUser, "name");
        // 使用随机占位密码，禁止第三方账号通过账号密码登录
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        userMapper.insert(
                login,
                null,
                randomPassword,
                avatar,
                nickname,
                "0",
                String.valueOf(identityCode),
                identityCode,
                null,
                0,
                "0",
                0);
        return userMapper.findByUsername(login);
    }

    /**
     * 构建 Spring Security 用户信息（含角色权限）
     */
    public UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getIs_deleted() == 0,
                true,
                true,
                user.getStatus() == 0,
                buildAuthorities(user.getRole_id()));
    }

    /**
     * 按角色装配权限：角色标识（如 ROLE_USER）与权限标识集合
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(int roleId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = roleService.findById(roleId);
        if (role == null) {
            return authorities;
        }
        if (role.getRole_code() != null && !role.getRole_code().isBlank()) {
            authorities.add(new SimpleGrantedAuthority(role.getRole_code()));
        }
        if (role.getPermissions() != null && !role.getPermissions().isBlank()) {
            for (String permission : role.getPermissions().split(",")) {
                String trimmed = permission.trim();
                if (!trimmed.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(trimmed));
                }
            }
        }
        return authorities;
    }

    /**
     * 解析本站用户名：优先使用 Gitee 登录名，缺失时回退为 gitee_<id>
     */
    private String resolveUsername(JsonNode giteeUser) {
        String login = textOrNull(giteeUser, "login");
        if (login == null) {
            login = "gitee_" + giteeUser.path("id").asText();
        }
        login = login.trim();
        return login.length() > 50 ? login.substring(0, 50) : login;
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

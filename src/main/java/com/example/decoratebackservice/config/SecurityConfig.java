package com.example.decoratebackservice.config;

import com.example.decoratebackservice.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Spring Security 配置类
 * 采用基于 HttpSession 的会话认证：登录后认证信息写入 Session，
 * 未登录请求统一返回 JSON 格式的 401，无权限返回 JSON 格式的 403
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 密码编码器，当前为临时联调阶段，直接使用明文密码比对（暂不加密）
     * 注意：仅用于临时联调，恢复加密时改回 BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword == null ? null : rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && encodedPassword != null
                        && encodedPassword.contentEquals(rawPassword);
            }
        };
    }

    /**
     * 认证管理器，由 Spring Security 依据 UserDetailsService 与 PasswordEncoder 自动装配
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 前后端分离场景不使用表单令牌，此处保留禁用状态
            .csrf(csrf -> csrf.disable())
            // 配置跨域
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 关闭默认表单登录与 HTTP Basic，统一走 /auth/login
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 放行登录接口与 Swagger 相关接口
                .requestMatchers(
                    "/auth/login",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/error"
                ).permitAll()
                // 其余接口均需登录后访问
                .anyRequest().authenticated()
            )
            // 会话策略：按需创建，登录成功后由 /auth/login 写入
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            // 未登录 / 无权限时返回 JSON，而非重定向到登录页
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, ex) ->
                    writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.unauthorized("未登录或会话已过期")))
                .accessDeniedHandler((request, response, ex) ->
                    writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.forbidden("无访问权限")))
            );

        return http.build();
    }

    /**
     * 输出统一格式的 JSON 响应
     */
    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 会话认证依赖 Cookie，必须允许携带凭证
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

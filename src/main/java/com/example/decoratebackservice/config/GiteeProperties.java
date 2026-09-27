package com.example.decoratebackservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gitee OAuth2 第三方登录配置，对应 application.properties 中的 gitee.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "gitee")
public class GiteeProperties {

    /** 应用 client_id */
    private String clientId;

    /** 应用 client_secret */
    private String clientSecret;

    /** 授权回调地址，需与 Gitee 应用配置完全一致 */
    private String redirectUri;

    /** 未注册用户选择身份的页面地址 */
    private String identityPageUrl;

    /** 登录成功后前端跳转地址 */
    private String successUrl;
}

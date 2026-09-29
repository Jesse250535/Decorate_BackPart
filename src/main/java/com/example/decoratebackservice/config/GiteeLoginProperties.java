package com.example.decoratebackservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gitee「直接登录」专用配置（与注册流程隔离），对应 application.properties 中的 gitee.login.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "gitee.login")
public class GiteeLoginProperties {

    /** 登录专用回调地址，需与 Gitee 应用配置中的回调地址一致 */
    private String redirectUri;

    /** 未绑定账号被拒时跳转的前端登录页地址 */
    private String pageUrl;

    /** 登录成功后前端跳转地址 */
    private String successUrl;
}

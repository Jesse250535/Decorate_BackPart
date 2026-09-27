package com.example.decoratebackservice.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送业务层：封装邮箱验证码邮件的发送
 * <p>
 * 依赖 spring-boot-starter-mail，发送参数来自 application.properties 的 spring.mail.* 配置
 */
@Service
public class EmailSendService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    /**
     * 发件人地址，取配置的邮箱账号
     */
    @Value("${spring.mail.username:}")
    private String from;

    public EmailSendService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * 发送注册验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("邮件服务未配置，请先在 application.properties 中配置 spring.mail.*");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject("【Decorate】邮箱注册验证码");
        message.setText("您正在使用邮箱注册 Decorate 账号，本次验证码为：" + code
                + "，5 分钟内有效。请勿将验证码泄露给他人。若非本人操作，请忽略本邮件。");
        mailSender.send(message);
    }
}

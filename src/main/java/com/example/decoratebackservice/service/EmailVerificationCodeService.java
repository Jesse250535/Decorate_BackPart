package com.example.decoratebackservice.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码业务层：负责验证码的生成、暂存、校验与过期管理
 * <p>
 * 采用进程内内存存储（ConcurrentHashMap），不依赖外部中间件，独立于既有代码
 */
@Service
public class EmailVerificationCodeService {

    /**
     * 验证码有效期
     */
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    /**
     * 同一邮箱发送间隔，避免频繁发送
     */
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);

    /**
     * 验证码位数
     */
    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    /**
     * key：规范化后的邮箱（小写去空格）；value：验证码及生成时间
     */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    /**
     * 为指定邮箱生成验证码并暂存，返回生成的验证码
     *
     * @param email 目标邮箱
     * @return 6 位数字验证码
     */
    public String generate(String email) {
        String key = normalize(email);
        CodeEntry existing = codeStore.get(key);
        if (existing != null && Instant.now().isBefore(existing.createdAt().plus(RESEND_INTERVAL))) {
            long wait = Math.max(
                    RESEND_INTERVAL.minus(Duration.between(existing.createdAt(), Instant.now())).getSeconds(), 1);
            throw new IllegalArgumentException("验证码发送过于频繁，请 " + wait + " 秒后再试");
        }
        String code = randomCode();
        codeStore.put(key, new CodeEntry(code, Instant.now()));
        return code;
    }

    /**
     * 校验验证码是否正确且未过期；校验通过后立即失效（一次性使用）
     *
     * @param email 目标邮箱
     * @param code  用户提交的验证码
     */
    public void verify(String email, String code) {
        String key = normalize(email);
        CodeEntry entry = codeStore.get(key);
        if (entry == null) {
            throw new IllegalArgumentException("请先获取邮箱验证码");
        }
        if (Instant.now().isAfter(entry.createdAt().plus(CODE_TTL))) {
            codeStore.remove(key);
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (code == null || !entry.code().equals(code.trim())) {
            throw new IllegalArgumentException("验证码不正确");
        }
        // 一次性使用，校验通过后立即移除
        codeStore.remove(key);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /**
     * 验证码条目：记录验证码与生成时间
     */
    private record CodeEntry(String code, Instant createdAt) {
    }
}

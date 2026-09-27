package com.example.decoratebackservice.service;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.config.EmailRegisterRequest;
import com.example.decoratebackservice.mapper.EmailUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 邮箱注册业务层：校验邮箱验证码通过后创建前台用户
 * <p>
 * 复用既有「身份类型 → user_type/role_id 直映」约定，独立于 UserRegisterService
 */
@Service
public class EmailRegisterService {

    /**
     * 用户名最大长度，与 sys_user.username varchar(50) 保持一致
     */
    private static final int USERNAME_MAX_LENGTH = 50;

    private final EmailUserMapper emailUserMapper;
    private final EmailVerificationCodeService codeService;
    private final PasswordEncoder passwordEncoder;

    public EmailRegisterService(EmailUserMapper emailUserMapper,
                                EmailVerificationCodeService codeService,
                                PasswordEncoder passwordEncoder) {
        this.emailUserMapper = emailUserMapper;
        this.codeService = codeService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 校验验证码并注册用户
     *
     * @param request 邮箱注册请求
     * @return 注册成功的用户名
     */
    public String register(EmailRegisterRequest request) {
        String email = request.getEmail().trim();
        // 1. 校验邮箱验证码（校验通过即失效）
        codeService.verify(email, request.getCode());
        // 2. 邮箱唯一性校验
        if (emailUserMapper.countByEmail(email) > 0) {
            throw new IllegalArgumentException("该邮箱已被注册：" + email);
        }
        // 3. 解析用户名：未填写时由邮箱自动生成
        String username = resolveUsername(request.getUsername(), email);
        if (emailUserMapper.countByUsername(username) > 0) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }
        // 4. 解析身份类型，未填写时默认「客户」
        UserIdentityType identityType = request.getIdentityType() == null || request.getIdentityType().isBlank()
                ? UserIdentityType.CLIENT
                : UserIdentityType.fromText(request.getIdentityType());
        int identityCode = identityType.getCode();
        String nickname = request.getNickname() == null || request.getNickname().isBlank()
                ? null : request.getNickname().trim();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. 写入用户：user_type 与 role_id 取同一身份编号（与既有注册约定一致）
        emailUserMapper.insert(
                username,
                null,
                email,
                encodedPassword,
                null,
                nickname,
                "0",
                String.valueOf(identityCode),
                identityCode,
                null,
                0,
                "0",
                0);
        return username;
    }

    /**
     * 解析用户名：优先使用请求值，缺省时取邮箱 @ 前缀并裁剪到合法长度
     */
    private String resolveUsername(String username, String email) {
        String value = username == null ? "" : username.trim();
        if (value.isEmpty()) {
            int at = email.indexOf('@');
            value = at > 0 ? email.substring(0, at) : email;
        }
        return value.length() > USERNAME_MAX_LENGTH ? value.substring(0, USERNAME_MAX_LENGTH) : value;
    }
}

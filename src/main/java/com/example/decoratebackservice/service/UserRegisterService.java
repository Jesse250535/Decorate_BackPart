package com.example.decoratebackservice.service;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 前台用户注册业务层：依据所选身份类型自动分配 user_type 与 role_id
 */
@Service
public class UserRegisterService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserRegisterService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册用户：身份类型文字解析为编号（1-5），并将同一编号注入 role_id
     *
     * @param username     用户名
     * @param rawPassword  明文密码
     * @param identityType 身份类型文字，如“购房者”
     * @param phone        手机号（可选）
     * @param nickname     昵称（可选）
     * @param gender       性别（可选）
     * @return 影响行数
     */
    public int register(String username, String rawPassword, String identityType,
                        String phone, String nickname, Integer gender) {
        // 用户名唯一性校验
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }
        // 按文字描述匹配身份类型，非法文字将抛出参数非法异常
        UserIdentityType type = UserIdentityType.fromText(identityType);
        int identityCode = type.getCode();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        int genderValue = gender == null ? 0 : gender;

        // user_type 与 role_id 采用同一身份编号（1-5），实现身份到角色的自动注入
        return userMapper.insert(
                username,
                phone,
                encodedPassword,
                null,
                nickname,
                String.valueOf(genderValue),
                String.valueOf(identityCode),
                identityCode,
                null,
                0,
                "0",
                0);
    }
}

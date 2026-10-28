package com.example.decoratebackservice.service;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.config.EmailRegisterRequest;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.mapper.EmailUserMapper;
import com.example.decoratebackservice.mapper.UserMapper;
import com.example.decoratebackservice.security.StaffRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 邮箱注册业务层：校验邮箱验证码通过后创建前台用户
 * <p>
 * 身份类型与账号注册（{@link UserRegisterService}）保持一致：
 * 优先按前端提交的 role_code 换算 sys_role.id 写入 role_id，
 * user_type 统一默认 1 客户（客户画像由首次登录弹窗回写）；
 * 未传 role_code 的旧请求仍按「身份文字 → 编号」的历史规则兜底。
 */
@Service
public class EmailRegisterService {

    /**
     * 用户名最大长度，与 sys_user.username varchar(50) 保持一致
     */
    private static final int USERNAME_MAX_LENGTH = 50;

    /**
     * 默认用户身份类型：客户
     */
    private static final int DEFAULT_USER_TYPE = 1;

    private final EmailUserMapper emailUserMapper;
    private final UserMapper userMapper;
    private final EmailVerificationCodeService codeService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public EmailRegisterService(EmailUserMapper emailUserMapper,
                                UserMapper userMapper,
                                EmailVerificationCodeService codeService,
                                PasswordEncoder passwordEncoder,
                                RoleService roleService) {
        this.emailUserMapper = emailUserMapper;
        this.userMapper = userMapper;
        this.codeService = codeService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
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
        // 4. 解析身份：与账号注册同一套 sys_role
        String roleCode = request.getRole_code() == null ? null : request.getRole_code().trim();
        int userType;
        int roleId;
        if (roleCode != null && !roleCode.isEmpty()) {
            Role role = roleService.findByCode(roleCode);
            if (role == null) {
                throw new IllegalArgumentException("身份类型不存在：" + roleCode);
            }
            roleId = role.getId();
            // 与账号注册一致：客户画像统一默认 1 客户，由首次登录弹窗确认
            userType = DEFAULT_USER_TYPE;
        } else {
            // 兼容旧前端：只传身份文字时，编号同时作为 user_type 与 role_id
            UserIdentityType identityType =
                    request.getIdentityType() == null || request.getIdentityType().isBlank()
                            ? UserIdentityType.CLIENT
                            : UserIdentityType.fromText(request.getIdentityType());
            userType = identityType.getCode();
            roleId = userType;
        }
        String nickname = request.getNickname() == null || request.getNickname().isBlank()
                ? null : request.getNickname().trim();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. 写入用户
        emailUserMapper.insert(
                username,
                null,
                email,
                encodedPassword,
                null,
                nickname,
                "0",
                String.valueOf(userType),
                roleId,
                null,
                0,
                "0",
                0);

        // 6. 与账号注册保持一致：库管 / 施工属于岗位账号，登录后不再弹身份选择
        if (StaffRole.STOCK.equals(roleCode) || StaffRole.WORKER.equals(roleCode)) {
            userMapper.markUserTypeChosen(username);
        }
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

package com.example.decoratebackservice.service;

import com.example.decoratebackservice.common.UserIdentityType;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.mapper.UserMapper;
import com.example.decoratebackservice.security.StaffRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 前台用户注册业务层：依据注册时选择的「系统角色」自动分配 role_id 与 user_type
 * <p>
 * 两套概念要分清：
 * <ul>
 *     <li>role_id → 系统角色（sys_role）：决定登录后进哪个管理页，如 ROLE_SUPPLIER</li>
 *     <li>user_type → 用户身份类型（1客户2装修爱好者3购房者4家具爱好者5其他）：客户画像，
 *         注册时统一默认 1 客户，由「首次登录弹窗」确认后回写</li>
 * </ul>
 */
@Service
public class UserRegisterService {

    /** 普通用户角色：注册时没选角色时落到这里 */
    private static final String DEFAULT_ROLE_CODE = "ROLE_USER";

    /** 默认用户身份类型：客户 */
    private static final int DEFAULT_USER_TYPE = 1;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserRegisterService(UserMapper userMapper, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    /**
     * 注册用户
     *
     * @param username     用户名
     * @param rawPassword  明文密码
     * @param identityType 身份类型文字（旧字段，roleCode 为空时才作为兜底角色依据）
     * @param roleCode     注册页选择的系统角色，如 ROLE_SUPPLIER；为空则按普通用户注册
     * @param phone        手机号（可选）
     * @param nickname     昵称（可选）
     * @param gender       性别（可选）
     * @return 影响行数
     */
    public int register(String username, String rawPassword, String identityType, String roleCode,
                        String phone, String nickname, Integer gender) {
        // 用户名唯一性校验
        if (userMapper.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在：" + username);
        }

        String finalRoleCode = resolveRoleCode(roleCode, identityType);
        Role role = roleService.findByCode(finalRoleCode);
        if (role == null) {
            throw new IllegalArgumentException("身份类型不存在：" + finalRoleCode);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        int genderValue = gender == null ? 0 : gender;

        int rows = userMapper.insert(
                username,
                phone,
                encodedPassword,
                null,
                nickname,
                String.valueOf(genderValue),
                String.valueOf(DEFAULT_USER_TYPE),
                role.getId(),
                null,
                0,
                "0",
                0);

        // 库管 / 施工人员属于「岗位账号」：系统已经用角色区分身份，
        // 登录后不再追着问客户画像，直接标记为已确认，避免一进管理页就被弹窗打断
        if (StaffRole.STOCK.equals(finalRoleCode) || StaffRole.WORKER.equals(finalRoleCode)) {
            userMapper.markUserTypeChosen(username);
        }
        return rows;
    }

    /**
     * 兼容旧前端的注册调用（不带 role_code）
     */
    public int register(String username, String rawPassword, String identityType,
                        String phone, String nickname, Integer gender) {
        return register(username, rawPassword, identityType, null, phone, nickname, gender);
    }

    /**
     * 解析最终角色：优先用注册页选中的 role_code；老前端只传身份类型文字时，
     * 按历史规则映射（1客户→ROLE_USER、2装修爱好者→ROLE_PLAN_PROVIDER ...），保证接口向后兼容
     */
    private String resolveRoleCode(String roleCode, String identityType) {
        if (roleCode != null && !roleCode.isBlank()) {
            return roleCode.trim();
        }
        if (identityType != null && !identityType.isBlank()) {
            List<Role> roles = roleService.findAll();
            int code;
            try {
                code = UserIdentityType.fromText(identityType).getCode();
            } catch (IllegalArgumentException e) {
                return DEFAULT_ROLE_CODE;
            }
            if (code > 0 && code <= roles.size()) {
                // 历史数据里 identityCode 恰好等于 sys_role.id（1~5），保持原行为
                for (Role role : roles) {
                    if (role.getId() == code) {
                        return role.getRole_code();
                    }
                }
            }
        }
        return DEFAULT_ROLE_CODE;
    }
}

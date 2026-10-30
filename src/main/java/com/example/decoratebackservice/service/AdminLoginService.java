package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.mapper.AdminLoginMapper;
import com.example.decoratebackservice.mapper.AdminRoleMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员登录业务层：支持用户名 / 昵称 / 手机号 + 密码登录，
 * 并从 sys_role 读取管理员的角色与权限（sys_user.role_id 的外键目标表）
 */
@Service
public class AdminLoginService {

    private final AdminLoginMapper adminLoginMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final SysAdminService sysAdminService;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginService(AdminLoginMapper adminLoginMapper, AdminRoleMapper adminRoleMapper,
                             SysAdminService sysAdminService, PasswordEncoder passwordEncoder) {
        this.adminLoginMapper = adminLoginMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.sysAdminService = sysAdminService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 依据用户名 / 昵称 / 手机号校验管理员账号密码
     *
     * @param account     用户名 / 昵称 / 手机号
     * @param rawPassword 明文密码
     * @return 校验通过返回管理员实体，账号不存在 / 被禁用 / 密码错误返回 null
     */
    public SysAdmin authenticate(String account, String rawPassword) {
        if (account == null || account.trim().isEmpty() || rawPassword == null) {
            return null;
        }
        String value = account.trim();

        // 优先使用 Mapper 层新增的「账户+密码」精确匹配（用户名登录）
        SysAdmin admin = sysAdminService.findByUsernameAndPassword(value, rawPassword);

        if (admin == null) {
            // 回退：兼容昵称 / 手机号登录，按账号查出候选后，再用密码编码器比对
            Map<Integer, SysAdmin> distinct = new LinkedHashMap<>();
            addIfPresent(distinct, adminLoginMapper.findByUsername(value));
            addIfPresent(distinct, adminLoginMapper.findByNickname(value));
            addIfPresent(distinct, adminLoginMapper.findByPhone(value));
            List<SysAdmin> admins = new ArrayList<>(distinct.values());

            if (admins.isEmpty()) {
                // 账号不存在，保持模糊提示，避免暴露账号信息
                return null;
            }
            if (admins.size() > 1) {
                // 昵称不唯一时无法安全定位账号
                throw new IllegalArgumentException("该账号关联了多个管理员，请使用用户名登录");
            }

            SysAdmin candidate = admins.get(0);
            if (!passwordEncoder.matches(rawPassword, candidate.getPassword())) {
                return null;
            }
            admin = candidate;
        }

        // status 0正常1封禁，is_deleted 0未删1已删
        if (admin.getStatus() != 0 || admin.getIs_deleted() != 0) {
            return null;
        }
        return admin;
    }

    /**
     * 按角色读取管理员权限：角色标识（如 ROLE_ADMIN）与权限标识集合（如 plan:view）
     */
    public Collection<? extends GrantedAuthority> buildAuthorities(int roleId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = adminRoleMapper.findById(roleId);
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
     * 读取管理员角色信息（sys_role）
     */
    public Role findRole(int roleId) {
        return adminRoleMapper.findById(roleId);
    }

    private void addIfPresent(Map<Integer, SysAdmin> distinct, SysAdmin admin) {
        if (admin != null) {
            distinct.putIfAbsent(admin.getId(), admin);
        }
    }
}

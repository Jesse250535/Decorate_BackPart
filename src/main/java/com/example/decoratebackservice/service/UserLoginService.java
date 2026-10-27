package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台用户登录业务层：支持使用昵称或邮箱 + 密码登录
 */
@Service
public class UserLoginService {

    private final UserAccountMapper userAccountMapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserLoginService(UserAccountMapper userAccountMapper, RoleService roleService,
                            PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 依据昵称或邮箱校验账号密码
     *
     * @param account     昵称或邮箱
     * @param rawPassword 明文密码
     * @return 校验通过返回 UserDetails，账号不存在/被禁用/密码错误返回 null
     */
    public UserDetails authenticate(String account, String rawPassword) {
        if (account == null || account.trim().isEmpty()) {
            return null;
        }
        String value = account.trim();

        // 同时按邮箱与昵称匹配，兼容两种登录方式，并按用户 id 去重
        Map<Integer, User> distinct = new LinkedHashMap<>();
        for (User user : userAccountMapper.findByEmail(value)) {
            distinct.putIfAbsent(user.getId(), user);
        }
        for (User user : userAccountMapper.findByNickname(value)) {
            distinct.putIfAbsent(user.getId(), user);
        }
        List<User> users = new ArrayList<>(distinct.values());

        if (users.isEmpty()) {
            // 账号不存在，保持模糊提示，避免暴露账号信息
            return null;
        }
        if (users.size() > 1) {
            // 昵称/邮箱不唯一时无法安全定位账号
            throw new IllegalArgumentException("该昵称或邮箱关联了多个账号，请使用用户名登录");
        }

        User user = users.get(0);
        // status 0正常1封禁
        if (user.getStatus() != 0) {
            return null;
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }
        return buildUserDetails(user);
    }

    /**
     * 依据用户名校验账号密码
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 校验通过返回 UserDetails，账号不存在/被禁用/密码错误返回 null
     */
    public UserDetails authenticateByUsername(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String value = username.trim();

        List<User> users = userAccountMapper.findByUsername(value);
        if (users.isEmpty()) {
            // 账号不存在，保持模糊提示，避免暴露账号信息
            return null;
        }
        if (users.size() > 1) {
            // 用户名不唯一时无法安全定位账号
            throw new IllegalArgumentException("该用户名关联了多个账号，请联系管理员");
        }

        User user = users.get(0);
        // status 0正常1封禁
        if (user.getStatus() != 0) {
            return null;
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }
        return buildUserDetails(user);
    }

    /* ==================== 封禁状态复核（增量，不影响以上登录校验逻辑） ==================== */

    /**
     * 用户名登录失败后复核：密码正确但账号处于封禁状态时返回 true。
     * <p>
     * 仅用于把「用户名或密码错误」细化为「账号已封禁」，让前端能弹出封禁提示窗口。
     * 必须先校验密码，避免不知道密码的人探知某个账号是否存在、是否被封禁。
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 密码正确且 status != 0 时返回 true
     */
    public boolean isBannedByUsername(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty() || rawPassword == null) {
            return false;
        }
        return isBannedWithValidPassword(userAccountMapper.findByUsername(username.trim()), rawPassword);
    }

    /**
     * 昵称 / 邮箱登录失败后复核：密码正确但账号处于封禁状态时返回 true
     *
     * @param account     昵称或邮箱
     * @param rawPassword 明文密码
     * @return 密码正确且 status != 0 时返回 true
     */
    public boolean isBannedByAccount(String account, String rawPassword) {
        if (account == null || account.trim().isEmpty() || rawPassword == null) {
            return false;
        }
        String value = account.trim();
        Map<Integer, User> distinct = new LinkedHashMap<>();
        for (User user : userAccountMapper.findByEmail(value)) {
            distinct.putIfAbsent(user.getId(), user);
        }
        for (User user : userAccountMapper.findByNickname(value)) {
            distinct.putIfAbsent(user.getId(), user);
        }
        return isBannedWithValidPassword(new ArrayList<>(distinct.values()), rawPassword);
    }

    /**
     * 账号唯一命中且密码正确时，判断该账号是否已被封禁
     *
     * @param users       按用户名 / 昵称 / 邮箱查到的账号列表
     * @param rawPassword 明文密码
     * @return 账号唯一、密码正确且 status != 0 时返回 true
     */
    private boolean isBannedWithValidPassword(List<User> users, String rawPassword) {
        if (users.size() != 1) {
            return false;
        }
        User user = users.get(0);
        return passwordEncoder.matches(rawPassword, user.getPassword()) && user.getStatus() != 0;
    }

    /**
     * 构建 Spring Security 用户信息（含角色权限）
     */
    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getIs_deleted() == 0,
                true,
                true,
                user.getStatus() == 0,
                buildAuthorities(user.getRole_id()));
    }

    /**
     * 按角色装配权限：角色标识（如 ROLE_USER）与权限标识集合（如 plan:view）
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(int roleId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Role role = roleService.findById(roleId);
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
}

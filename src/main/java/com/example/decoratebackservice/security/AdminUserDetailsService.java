package com.example.decoratebackservice.security;

import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.RoleService;
import com.example.decoratebackservice.service.SysAdminService;
import com.example.decoratebackservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 管理员认证信息加载服务，查询 sys_admin 并装配角色权限
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private SysAdminService sysAdminService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    /**
     * 根据用户名加载管理员账号，并装配其角色对应权限
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 优先按管理员账号（sys_admin）认证
        SysAdmin admin = sysAdminService.findByUsername(username);
        if (admin != null) {
            return new LoginUser(admin, buildAuthorities(admin.getRole_id()));
        }
        // 管理表中不存在时，回退到普通用户账号（sys_user），支持以 username 登录
        User user = userService.findByUsername(username);
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getIs_deleted() == 0,
                    true,
                    true,
                    user.getStatus() == 0,
                    buildAuthorities(user.getRole_id()));
        }
        throw new UsernameNotFoundException("账号不存在: " + username);
    }

    /**
     * 按角色装配权限：角色标识（如 ROLE_ADMIN）与权限标识集合（如 plan:view）
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

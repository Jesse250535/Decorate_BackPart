package com.example.decoratebackservice.security;

import com.example.decoratebackservice.entity.SysAdmin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 登录用户信息，封装 sys_admin 账号与角色权限
 */
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 管理员账号信息
     */
    private final transient SysAdmin admin;

    /**
     * 权限集合（角色标识 + 权限标识）
     */
    private final Collection<? extends GrantedAuthority> authorities;

    public LoginUser(SysAdmin admin, Collection<? extends GrantedAuthority> authorities) {
        this.admin = admin;
        this.authorities = authorities;
    }

    public SysAdmin getAdmin() {
        return admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    /**
     * 账号是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定，status 0正常1封禁
     */
    @Override
    public boolean isAccountNonLocked() {
        return admin.getStatus() == 0;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否可用，is_deleted 0未删1已删
     */
    @Override
    public boolean isEnabled() {
        return admin.getIs_deleted() == 0;
    }
}

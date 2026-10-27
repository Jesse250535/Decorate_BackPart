package com.example.decoratebackservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * 员工身份判定工具。
 * <p>
 * 普通用户登录时，{@link AdminUserDetailsService#loadUserByUsername} 已经把
 * sys_role.role_code（如 ROLE_STOCK）与 permissions（如 stock:operate）装配成
 * GrantedAuthority 写入 HttpSession，因此这里直接读会话，不需要再查数据库。
 * <p>
 * 角色取值来源 sys_role 表：
 * <ul>
 *     <li>4 库管人员 → ROLE_STOCK</li>
 *     <li>5 施工人员 → ROLE_WORKER</li>
 * </ul>
 */
public final class StaffRole {

    /**
     * 库管人员角色标识（sys_role.id = 4）
     */
    public static final String STOCK = "ROLE_STOCK";

    /**
     * 施工人员角色标识（sys_role.id = 5）
     */
    public static final String WORKER = "ROLE_WORKER";

    private StaffRole() {
    }

    /**
     * 判断当前会话是否持有指定角色
     *
     * @param authentication 安全上下文
     * @param roleCode       角色标识，如 ROLE_STOCK
     * @return 持有返回 true
     */
    public static boolean has(Authentication authentication, String roleCode) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleCode::equals);
    }

    /**
     * 是否库管人员
     */
    public static boolean isStock(Authentication authentication) {
        return has(authentication, STOCK);
    }

    /**
     * 是否施工人员
     */
    public static boolean isWorker(Authentication authentication) {
        return has(authentication, WORKER);
    }

    /**
     * 取当前登录用户名（管理员与普通用户通用）
     */
    public static String username(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}

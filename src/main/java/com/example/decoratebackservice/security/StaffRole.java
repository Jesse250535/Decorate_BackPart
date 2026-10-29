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
 *     <li>1 普通用户 → ROLE_USER</li>
 *     <li>2 设计师 → ROLE_DESIGNER（囊括原装修方案提供商职能）</li>
 *     <li>3 项目经理 → ROLE_MANAGER</li>
 * </ul>
 * 历史岗位角色（ROLE_STOCK / ROLE_WORKER / ROLE_SUPPLIER / ROLE_FINANCE_STAFF）常量保留，
 * 用于兼容旧数据，不再作为新注册的可选项。
 */
public final class StaffRole {

    /**
     * 库管人员角色标识（sys_role.id = 4）
     */
    public static final String STOCK = "ROLE_STOCK";

    /**
     * 装修方案提供商角色标识（sys_role.id = 2）
     */
    public static final String PLAN_PROVIDER = "ROLE_PLAN_PROVIDER";

    /**
     * 材料供应商角色标识（sys_role.id = 3）
     */
    public static final String SUPPLIER = "ROLE_SUPPLIER";

    /**
     * 设计师角色标识（sys_role.id = 6）
     */
    public static final String DESIGNER = "ROLE_DESIGNER";

    /**
     * 项目经理角色标识（sys_role.id = 7）
     */
    public static final String MANAGER = "ROLE_MANAGER";

    /**
     * 财务人员角色标识（sys_role.id = 8）
     */
    public static final String FINANCE = "ROLE_FINANCE_STAFF";

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
     * 是否可上传 / 维护装修方案。
     * <p>
     * 设计师已囊括装修方案提供商（ROLE_PLAN_PROVIDER）的全部职能，因此这里把
     * ROLE_DESIGNER 也视为方案提供方；否则设计师登录后调用 /plan-portal/* 会被拒。
     * 方案上传后仍是「待审核」状态，审核入口见 {@link #isDesigner}。
     */
    public static boolean isPlanProvider(Authentication authentication) {
        return has(authentication, PLAN_PROVIDER) || has(authentication, DESIGNER);
    }

    /**
     * 是否材料供应商
     */
    public static boolean isSupplier(Authentication authentication) {
        return has(authentication, SUPPLIER);
    }

    /**
     * 是否设计师
     */
    public static boolean isDesigner(Authentication authentication) {
        return has(authentication, DESIGNER);
    }

    /**
     * 是否项目经理
     */
    public static boolean isManager(Authentication authentication) {
        return has(authentication, MANAGER);
    }

    /**
     * 是否财务人员
     */
    public static boolean isFinance(Authentication authentication) {
        return has(authentication, FINANCE);
    }

    /**
     * 取当前登录用户名（管理员与普通用户通用）
     */
    public static String username(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}

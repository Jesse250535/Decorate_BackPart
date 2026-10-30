package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.RoleService;
import com.example.decoratebackservice.service.UserService;
import com.example.decoratebackservice.vo.StaffIdentityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 员工身份接口。
 * <p>
 * 前端登录后需要知道「我是不是库管 / 施工」，才能决定是否展示对应的管理入口。
 * 现有 /user/full/current 只返回 sys_user 的字段（含 role_id），但拿不到 role_code，
 * 也不便于判断；本接口专门回答这一个问题，不改动任何既有接口。
 */
@Tag(name = "员工身份接口")
@RestController
@RequestMapping("/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffController {

    private final UserService userService;
    private final RoleService roleService;

    public StaffController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Operation(summary = "查询当前登录用户的角色身份（是否库管 / 是否施工）")
    @GetMapping("/me")
    public Result<StaffIdentityVO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.unauthorized("未登录或会话已过期");
        }
        String username = StaffRole.username(authentication);
        StaffIdentityVO vo = new StaffIdentityVO();
        vo.setUsername(username);
        // 是否具备某角色，直接读会话中的 GrantedAuthority，比按 role_id 猜更可靠
        vo.setIs_stock(StaffRole.isStock(authentication) ? 1 : 0);
        vo.setIs_worker(StaffRole.isWorker(authentication) ? 1 : 0);
        // 新增的各岗位主页判定：前端据此决定展示哪个管理入口
        vo.setIs_supplier(StaffRole.isSupplier(authentication) ? 1 : 0);
        vo.setIs_plan_provider(StaffRole.isPlanProvider(authentication) ? 1 : 0);
        vo.setIs_designer(StaffRole.isDesigner(authentication) ? 1 : 0);
        vo.setIs_manager(StaffRole.isManager(authentication) ? 1 : 0);
        vo.setIs_finance(StaffRole.isFinance(authentication) ? 1 : 0);

        User user = userService.findByUsernameForStaff(username);
        if (user == null) {
            // 管理员账号 is_admin=1，不在前台用户（is_admin=0）查询结果中；返回空角色信息即可
            vo.setRole_code("");
            vo.setRole_name("");
            vo.setPermissions(Collections.emptyList());
            return Result.success("当前账号不是前台用户", vo);
        }

        vo.setUser_id(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRole_id(user.getRole_id());
        vo.setUser_type(user.getUser_type());
        vo.setUser_type_chosen(user.getUser_type_chosen());

        Role role = roleService.findById(user.getRole_id());
        if (role == null) {
            // role_id 指向了已删除的角色（历史脏数据），不影响登录，只是没有角色信息
            vo.setRole_code("");
            vo.setRole_name("");
            vo.setPermissions(Collections.emptyList());
            return Result.success(vo);
        }

        vo.setRole_code(role.getRole_code());
        vo.setRole_name(role.getRole_name());
        vo.setPermissions(splitPermissions(role.getPermissions()));
        return Result.success(vo);
    }

    /**
     * 拆分 permissions 字段（数据库中为逗号分隔字符串）
     */
    private List<String> splitPermissions(String permissions) {
        List<String> list = new ArrayList<>();
        if (permissions == null || permissions.isBlank()) {
            return list;
        }
        for (String item : permissions.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}

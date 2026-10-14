package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.security.LoginUser;
import com.example.decoratebackservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;

/**
 * 用户资料修改接口：当前登录用户修改自己的手机号、邮箱、昵称、性别、用户类型
 * <p>
 * 非侵入式实现：新增独立文件，不改动既有 UserController / UserFullInfoController。
 * <p>
 * 目标用户由服务端从会话 principal 解析，接口不接收 id / username 参数，
 * 也就无法通过传参改写他人资料，越权面从设计上消除。
 * <p>
 * 每个接口只更新一个字段。既有的 /user/update 是「全字段覆盖」，若只传部分字段，
 * 未传的列会被写成 null/0，因此单字段修改必须走本控制器。
 * <p>
 * 接口需登录后访问（未列入 SecurityConfig 放行名单，落在 anyRequest().authenticated()，
 * 未登录会被过滤器链直接拦成 401）。各字段取值校验集中在 UserProfileService，
 * 非法参数由 GlobalExceptionHandler 转成 code 400。
 */
@Tag(name = "用户资料修改接口")
@RestController
@RequestMapping("/user/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(summary = "修改当前登录用户的手机号（需唯一，不能与他人重复）")
    @PutMapping("/phone")
    public Result<Void> updatePhone(@RequestParam(value = "phone", required = false) String phone) {
        return forCurrentUser(username -> userProfileService.updatePhone(username, phone), "手机号修改成功");
    }

    @Operation(summary = "修改当前登录用户的邮箱")
    @PutMapping("/email")
    public Result<Void> updateEmail(@RequestParam(value = "email", required = false) String email) {
        return forCurrentUser(username -> userProfileService.updateEmail(username, email), "邮箱修改成功");
    }

    @Operation(summary = "修改当前登录用户的昵称")
    @PutMapping("/nickname")
    public Result<Void> updateNickname(@RequestParam(value = "nickname", required = false) String nickname) {
        return forCurrentUser(username -> userProfileService.updateNickname(username, nickname), "昵称修改成功");
    }

    @Operation(summary = "修改当前登录用户的性别（0未知 1男 2女）")
    @PutMapping("/gender")
    public Result<Void> updateGender(@RequestParam(value = "gender", required = false) Integer gender) {
        return forCurrentUser(username -> userProfileService.updateGender(username, gender), "性别修改成功");
    }

    @Operation(summary = "修改当前登录用户的用户类型（1客户 2装修爱好者 3购房者 4家具爱好者 5其他）")
    @PutMapping("/userType")
    public Result<Void> updateUserType(@RequestParam(value = "userType", required = false) Integer userType) {
        return forCurrentUser(username -> userProfileService.updateUserType(username, userType), "用户类型修改成功");
    }

    /**
     * 解析当前登录用户名后执行修改动作。
     * <p>
     * 把「未登录」分支收拢到一处，保证 5 个接口的 401 行为完全一致。
     *
     * @param action         以当前登录用户名为入参的修改动作
     * @param successMessage 修改成功提示
     * @return 未登录返回 401，否则返回修改结果
     */
    private Result<Void> forCurrentUser(Consumer<String> action, String successMessage) {
        String username = resolveCurrentUsername();
        if (username == null) {
            return Result.unauthorized("未登录或会话已过期");
        }
        action.accept(username);
        return Result.success(successMessage, null);
    }

    /**
     * 从当前安全上下文中解析登录用户名。
     * <p>
     * 与 UserFullInfoController 保持同一套判定：普通用户登录后 principal 是 UserDetails
     * （AuthController 的 /login-by-username、/login-by-account 及 Gitee 登录均如此），
     * 管理员登录后 principal 是 LoginUser，两种都要能取到用户名。
     *
     * @return 用户名；未登录或无法识别时返回 null
     */
    private String resolveCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}

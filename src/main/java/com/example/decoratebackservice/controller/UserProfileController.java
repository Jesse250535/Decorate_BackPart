package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料修改接口：手机号、邮箱、昵称、性别、用户类型
 * <p>
 * 非侵入式实现：新增独立文件，不改动既有 UserController / UserFullInfoController。
 * <p>
 * 每个接口只更新一个字段。既有的 /user/update 是「全字段覆盖」，若只传部分字段，
 * 未传的列会被写成 null/0，因此单字段修改必须走本控制器。
 * <p>
 * 接口需登录后访问（沿用全局 anyRequest().authenticated() 策略），未加入 SecurityConfig 放行名单。
 * <p>
 * 各字段的取值校验集中在 UserProfileService，非法参数由 GlobalExceptionHandler 转成 code 400。
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

    @Operation(summary = "修改手机号（需唯一，不能与他人重复）")
    @PutMapping("/{id}/phone")
    public Result<Void> updatePhone(@PathVariable int id,
                                    @RequestParam(value = "phone", required = false) String phone) {
        userProfileService.updatePhone(id, phone);
        return Result.success("手机号修改成功", null);
    }

    @Operation(summary = "修改邮箱")
    @PutMapping("/{id}/email")
    public Result<Void> updateEmail(@PathVariable int id,
                                    @RequestParam(value = "email", required = false) String email) {
        userProfileService.updateEmail(id, email);
        return Result.success("邮箱修改成功", null);
    }

    @Operation(summary = "修改昵称")
    @PutMapping("/{id}/nickname")
    public Result<Void> updateNickname(@PathVariable int id,
                                       @RequestParam(value = "nickname", required = false) String nickname) {
        userProfileService.updateNickname(id, nickname);
        return Result.success("昵称修改成功", null);
    }

    @Operation(summary = "修改性别（0未知 1男 2女）")
    @PutMapping("/{id}/gender")
    public Result<Void> updateGender(@PathVariable int id,
                                     @RequestParam(value = "gender", required = false) Integer gender) {
        userProfileService.updateGender(id, gender);
        return Result.success("性别修改成功", null);
    }

    @Operation(summary = "修改用户类型（1客户 2装修爱好者 3购房者 4家具爱好者 5其他）")
    @PutMapping("/{id}/userType")
    public Result<Void> updateUserType(@PathVariable int id,
                                       @RequestParam(value = "userType", required = false) Integer userType) {
        userProfileService.updateUserType(id, userType);
        return Result.success("用户类型修改成功", null);
    }
}

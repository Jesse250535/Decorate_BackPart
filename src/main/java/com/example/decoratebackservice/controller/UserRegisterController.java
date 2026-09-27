package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.RegisterRequest;
import com.example.decoratebackservice.service.UserRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用户注册接口
 */
@Tag(name = "用户注册接口")
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserRegisterController {

    private final UserRegisterService userRegisterService;

    public UserRegisterController(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    @Operation(summary = "用户注册（按所选身份类型自动分配角色）")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterRequest request) {
        userRegisterService.register(
                request.getUsername(),
                request.getPassword(),
                request.getIdentityType(),
                request.getPhone(),
                request.getNickname(),
                request.getGender());
        return Result.success("注册成功", request.getUsername());
    }
}

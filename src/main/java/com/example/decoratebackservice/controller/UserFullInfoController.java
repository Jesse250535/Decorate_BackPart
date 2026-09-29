package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserFullInfo;
import com.example.decoratebackservice.service.UserFullInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户完整信息查询接口：返回 sys_user 的全部字段
 * <p>
 * 非侵入式实现：新增独立文件，不改动既有 UserController / UserService / UserMapper。
 * 接口需登录后访问（沿用全局 anyRequest().authenticated() 策略）
 */
@Tag(name = "用户完整信息接口")
@RestController
@RequestMapping("/user/full")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserFullInfoController {

    private final UserFullInfoService userFullInfoService;

    public UserFullInfoController(UserFullInfoService userFullInfoService) {
        this.userFullInfoService = userFullInfoService;
    }

    @Operation(summary = "按ID查询单个用户完整信息（含 email、create_time、update_time 等全部字段）")
    @GetMapping("/findById/{id}")
    public Result<UserFullInfo> findById(@PathVariable int id) {
        return Result.success(userFullInfoService.findById(id));
    }

    @Operation(summary = "查询所有用户完整信息（含 email、create_time、update_time 等全部字段）")
    @GetMapping("/findAll")
    public Result<List<UserFullInfo>> findAll() {
        return Result.success(userFullInfoService.findAll());
    }
}

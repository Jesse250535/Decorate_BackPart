package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.controller
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 17:17:36
 */

@Tag(name = "用户管理接口")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "添加用户信息")
    @PostMapping("/insert")
    public String insert(@RequestBody User user) {
        // @RequestBody注解用来绑定通过http请求中application/json类型上传的数据
        return userService.insert(user.getUsername(), user.getPhone(), user.getPassword(), user.getAvatar(), user.getNickname(), String.valueOf(user.getGender()), String.valueOf(user.getUser_type()), user.getRole_id(), user.getProfile(), user.getViolation_count(), String.valueOf(user.getStatus()), user.getIs_deleted());
    }

    @Operation(summary = "根据ID查询用户信息")
    @GetMapping("/findById/{id}")
    public User findById(@PathVariable int id) {
        return userService.findById(id);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public void update(@RequestBody User user) {
        userService.update(user);
    }

    @Operation(summary = "根据ID删除用户")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.delete(id);
    }


}

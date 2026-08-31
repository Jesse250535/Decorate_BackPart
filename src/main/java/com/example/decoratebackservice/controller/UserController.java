package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.controller
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 17:17:36
 */

@Api(tags = "API接口")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    @Autowired
    private UserService userService;

    @ApiOperation("添加完整信息")
    @PostMapping("/insert")
    public String insert(@RequestBody User user) {
        // @RequestBody注解用来绑定通过http请求中application/json类型上传的数据
        return userService.insert(user.getUsername(), user.getPhone(), user.getPassword(), user.getAvatar(), user.getNickname(), user.getGender(), user.getUser_type(), user.getRole_id(), user.getProfile(), user.getViolation_count(), user.getStatus(), user.getIs_deleted());
    }

    @ApiOperation("查询id对应的信息")
    @GetMapping("/findById/{id}")
    public User findById(@PathVariable int id) {
        return userService.findById(id);
    }

    @ApiOperation("更新信息")
    @PutMapping("/update")
    public void update(@RequestBody User user) {
        userService.update(user);
    }

    @ApiOperation("删除指定id的信息")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.delete(id);
    }

}

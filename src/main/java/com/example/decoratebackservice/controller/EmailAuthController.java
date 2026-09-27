package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.EmailCodeRequest;
import com.example.decoratebackservice.config.EmailRegisterRequest;
import com.example.decoratebackservice.mapper.EmailUserMapper;
import com.example.decoratebackservice.service.EmailRegisterService;
import com.example.decoratebackservice.service.EmailSendService;
import com.example.decoratebackservice.service.EmailVerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 邮箱验证码注册接口：发送验证码 → 校验验证码完成注册
 * <p>
 * 独立于既有注册/登录接口，新增文件实现，不改动原有业务代码
 */
@Tag(name = "邮箱验证码注册接口")
@RestController
@RequestMapping("/auth/email")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailAuthController {

    private final EmailVerificationCodeService codeService;
    private final EmailSendService emailSendService;
    private final EmailRegisterService emailRegisterService;
    private final EmailUserMapper emailUserMapper;

    public EmailAuthController(EmailVerificationCodeService codeService,
                               EmailSendService emailSendService,
                               EmailRegisterService emailRegisterService,
                               EmailUserMapper emailUserMapper) {
        this.codeService = codeService;
        this.emailSendService = emailSendService;
        this.emailRegisterService = emailRegisterService;
        this.emailUserMapper = emailUserMapper;
    }

    @Operation(summary = "发送邮箱注册验证码")
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody @Valid EmailCodeRequest request) {
        String email = request.getEmail().trim();
        // 已注册邮箱无需再发验证码
        if (emailUserMapper.countByEmail(email) > 0) {
            throw new IllegalArgumentException("该邮箱已被注册：" + email);
        }
        String code = codeService.generate(email);
        emailSendService.sendVerificationCode(email, code);
        return Result.success("验证码已发送，请注意查收", null);
    }

    @Operation(summary = "邮箱验证码注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid EmailRegisterRequest request) {
        String username = emailRegisterService.register(request);
        return Result.success("注册成功", username);
    }
}

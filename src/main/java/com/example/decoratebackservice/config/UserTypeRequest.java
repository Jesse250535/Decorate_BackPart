package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首次登录弹窗提交的用户身份类型
 */
@Data
@Schema(description = "用户身份类型请求")
public class UserTypeRequest {

    @Schema(description = "用户身份类型 1客户 2装修爱好者 3购房者 4家具爱好者 5其他", example = "1")
    private Integer user_type;
}

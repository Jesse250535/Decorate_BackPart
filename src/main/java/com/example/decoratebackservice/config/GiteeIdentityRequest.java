package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Gitee 授权后选择身份请求
 */
@Data
@Schema(description = "Gitee 授权后选择身份请求")
public class GiteeIdentityRequest {

    @Schema(description = "身份类型文字：客户/装修爱好者/购房者/家具爱好者/其他", example = "客户")
    private String identityType;
}

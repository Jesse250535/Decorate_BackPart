package com.example.decoratebackservice.common;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 前台用户身份类型，与 sys_user.user_type 一一对应，仅允许 1-5
 * <p>
 * 1客户 2装修爱好者 3购房者 4家具爱好者 5其他
 */
public enum UserIdentityType {

    CLIENT(1, "客户"),
    DECOR_LOVER(2, "装修爱好者"),
    HOUSE_BUYER(3, "购房者"),
    FURNITURE_LOVER(4, "家具爱好者"),
    OTHER(5, "其他");

    /**
     * 身份编号，对应 sys_user.user_type
     */
    private final int code;

    /**
     * 身份文字描述
     */
    private final String text;

    UserIdentityType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    /**
     * 按文字描述解析身份类型（同时兼容传入编号 1-5），无法匹配时抛出参数非法异常
     *
     * @param value 用户选择的身份文字，如“购房者”
     * @return 匹配到的身份类型
     */
    public static UserIdentityType fromText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("身份类型不能为空");
        }
        String trimmed = value.trim();
        for (UserIdentityType type : values()) {
            if (type.text.equals(trimmed) || String.valueOf(type.code).equals(trimmed)) {
                return type;
            }
        }
        throw new IllegalArgumentException("身份类型仅允许：" + allDescriptions() + "（或对应编号 1-5）");
    }

    /**
     * 拼接全部可选身份文字，用于提示信息
     */
    private static String allDescriptions() {
        return Arrays.stream(values())
                .map(type -> type.code + "-" + type.text)
                .collect(Collectors.joining("、"));
    }
}

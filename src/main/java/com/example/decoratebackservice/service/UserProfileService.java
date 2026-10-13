package com.example.decoratebackservice.service;

import com.example.decoratebackservice.mapper.UserProfileMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 用户资料修改业务层：手机号、邮箱、昵称、性别、用户类型的单字段修改
 * <p>
 * 新增独立文件，未改动既有 UserService / UserFullInfoService，遵循非侵入式约定。
 * <p>
 * 校验失败统一抛 IllegalArgumentException，由 GlobalExceptionHandler 转成 code 400 返回，
 * 无需在本层拼装 Result。
 */
@Service
public class UserProfileService {

    /** 中国大陆手机号：1 开头，第二位 3-9，共 11 位 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 常规邮箱格式：本地部分@域名，域名至少含一个点 */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    /** 长度上限，与 sys_user 建表列宽严格对齐 */
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int NICKNAME_MAX_LENGTH = 50;

    /** 性别取值 0未知1男2女 */
    private static final int GENDER_MIN = 0;
    private static final int GENDER_MAX = 2;

    /** 用户类型取值，与建表时的 chk_sys_user_user_type 约束（between 1 and 5）一致 */
    private static final int USER_TYPE_MIN = 1;
    private static final int USER_TYPE_MAX = 5;

    private final UserProfileMapper userProfileMapper;

    public UserProfileService(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * 修改手机号
     *
     * @param id    用户主键
     * @param phone 新手机号（必填）
     * @throws IllegalArgumentException 参数非法或号码已被占用
     */
    public void updatePhone(int id, String phone) {
        String value = requireText(phone, "手机号");
        if (value.length() > PHONE_MAX_LENGTH) {
            throw new IllegalArgumentException("手机号长度不能超过 " + PHONE_MAX_LENGTH + " 个字符");
        }
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("手机号格式不正确，应为 1 开头的 11 位数字");
        }
        requireExistingUser(id);
        // phone 列上有唯一索引 uk_phone，先查重以给出可读提示
        if (userProfileMapper.countByPhoneExcludeId(value, id) > 0) {
            throw new IllegalArgumentException("该手机号已被其他账号使用");
        }
        try {
            userProfileMapper.updatePhone(id, value);
        } catch (DuplicateKeyException e) {
            // 查重与更新之间存在时间差，并发写入仍可能撞上唯一索引，兜底成同样的提示
            throw new IllegalArgumentException("该手机号已被其他账号使用");
        }
    }

    /**
     * 修改邮箱
     * <p>
     * 注：sys_user 未对 email 建唯一索引，因此本方法只校验格式，不做查重。
     *
     * @param id    用户主键
     * @param email 新邮箱（必填）
     * @throws IllegalArgumentException 参数非法
     */
    public void updateEmail(int id, String email) {
        String value = requireText(email, "邮箱");
        if (value.length() > EMAIL_MAX_LENGTH) {
            throw new IllegalArgumentException("邮箱长度不能超过 " + EMAIL_MAX_LENGTH + " 个字符");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        requireExistingUser(id);
        userProfileMapper.updateEmail(id, value);
    }

    /**
     * 修改昵称
     *
     * @param id       用户主键
     * @param nickname 新昵称（必填）
     * @throws IllegalArgumentException 参数非法
     */
    public void updateNickname(int id, String nickname) {
        String value = requireText(nickname, "昵称");
        if (value.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("昵称长度不能超过 " + NICKNAME_MAX_LENGTH + " 个字符");
        }
        requireExistingUser(id);
        userProfileMapper.updateNickname(id, value);
    }

    /**
     * 修改性别
     *
     * @param id     用户主键
     * @param gender 0未知 1男 2女
     * @throws IllegalArgumentException 参数非法
     */
    public void updateGender(int id, Integer gender) {
        if (gender == null) {
            throw new IllegalArgumentException("性别不能为空");
        }
        if (gender < GENDER_MIN || gender > GENDER_MAX) {
            throw new IllegalArgumentException("性别取值只能是 0(未知)、1(男)、2(女)");
        }
        requireExistingUser(id);
        userProfileMapper.updateGender(id, gender);
    }

    /**
     * 修改用户类型
     *
     * @param id       用户主键
     * @param userType 1客户 2装修爱好者 3购房者 4家具爱好者 5其他
     * @throws IllegalArgumentException 参数非法
     */
    public void updateUserType(int id, Integer userType) {
        if (userType == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
        if (userType < USER_TYPE_MIN || userType > USER_TYPE_MAX) {
            throw new IllegalArgumentException("用户类型只能是 1-5（1客户 2装修爱好者 3购房者 4家具爱好者 5其他）");
        }
        requireExistingUser(id);
        userProfileMapper.updateUserType(id, userType);
    }

    /**
     * 校验目标用户存在且未被逻辑删除
     */
    private void requireExistingUser(int id) {
        if (userProfileMapper.countById(id) == 0) {
            throw new IllegalArgumentException("用户不存在或已被删除（id=" + id + "）");
        }
    }

    /**
     * 校验字符串必填并去除首尾空白
     *
     * @return 去除首尾空白后的值
     */
    private String requireText(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return raw.trim();
    }
}

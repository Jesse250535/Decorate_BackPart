package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.entity.UserFeedback;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import com.example.decoratebackservice.mapper.UserFeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户意见反馈业务层
 */
@Service
public class UserFeedbackService {

    /** 反馈内容最大长度（与前端输入限制保持一致） */
    public static final int MAX_CONTENT_LENGTH = 1000;

    /** 联系方式最大长度（user_feedback.contact_info varchar(100)） */
    public static final int MAX_CONTACT_LENGTH = 100;

    @Autowired
    private UserFeedbackMapper userFeedbackMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    public int insert(UserFeedback feedback) {
        return userFeedbackMapper.insert(feedback);
    }

    public UserFeedback findById(int id) {
        return userFeedbackMapper.findById(id);
    }

    public List<UserFeedback> findAll() {
        return userFeedbackMapper.findAll();
    }

    public int update(UserFeedback feedback) {
        return userFeedbackMapper.update(feedback);
    }

    public int delete(int id) {
        return userFeedbackMapper.delete(id);
    }

    /* ==================== 以下为前台用户提交 / 查看自己的反馈 ==================== */

    /**
     * 按用户名反查用户ID（前台用户登录时 principal 只有用户名）
     *
     * @param username 用户名
     * @return sys_user.id；用户不存在或用户名为空时返回 null
     */
    public Integer findUserIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        List<User> users = userAccountMapper.findByUsername(username.trim());
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0).getId();
    }

    /**
     * 提交意见反馈。
     * <p>
     * user_id、handle_status、handle_remark 全部由后端决定，前端只能提交内容与联系方式，
     * 避免伪造他人反馈或直接把状态改成「已处理」。
     *
     * @param userId      当前登录用户ID
     * @param content     反馈内容
     * @param contactInfo 联系方式（可为空）
     * @return 保存后的反馈记录；参数不合法返回 null
     */
    public UserFeedback submit(int userId, String content, String contactInfo) {
        if (userId <= 0 || content == null || content.isBlank()) {
            return null;
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT_LENGTH) {
            return null;
        }
        String contact = contactInfo == null ? null : contactInfo.trim();
        if (contact != null && contact.isEmpty()) {
            contact = null;
        }
        if (contact != null && contact.length() > MAX_CONTACT_LENGTH) {
            return null;
        }

        UserFeedback feedback = new UserFeedback();
        feedback.setUser_id(userId);
        feedback.setContent(text);
        feedback.setContact_info(contact);
        feedback.setHandle_status(0);
        feedback.setHandle_remark(null);
        feedback.setIs_deleted(0);
        userFeedbackMapper.insert(feedback);

        // 回填主键后再查一次，保证返回给前端的数据与库里一致（含 create_time）
        return feedback.getId() > 0 ? userFeedbackMapper.findById(feedback.getId()) : null;
    }

    /**
     * 查询当前用户自己提交过的反馈
     *
     * @param userId 当前登录用户ID
     * @return 反馈列表，按提交时间倒序
     */
    public List<UserFeedback> findMyFeedbacks(int userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        return userFeedbackMapper.findByUserId(userId);
    }
}

package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DecorationCommentMapper;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论业务层
 */
@Service
public class DecorationCommentService {

    @Autowired
    private DecorationCommentMapper decorationCommentMapper;

    /**
     * 按用户名反查 sys_user.id，用于确定当前登录用户
     */
    @Autowired
    private UserAccountMapper userAccountMapper;

    public int insert(DecorationComment comment) {
        return decorationCommentMapper.insert(comment);
    }

    public DecorationComment findById(int id) {
        return decorationCommentMapper.findById(id);
    }

    public List<DecorationComment> findAll() {
        return decorationCommentMapper.findAll();
    }

    public int update(DecorationComment comment) {
        return decorationCommentMapper.update(comment);
    }

    public int delete(int id) {
        return decorationCommentMapper.delete(id);
    }

    /* ==================== 以下为「我的评价」相关新增逻辑 ==================== */

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
     * 查询当前用户自己的评论记录（不含已删除）
     *
     * @param user_id 当前登录用户ID
     * @return 评论列表，用户ID非法时返回空列表
     */
    public List<DecorationComment> findMyComments(int user_id) {
        if (user_id <= 0) {
            return new ArrayList<>();
        }
        return decorationCommentMapper.findByUserId(user_id);
    }

    /**
     * 修改自己的评论：只允许改内容与图片
     * <p>
     * user_id 作为 SQL 条件一起传入，因此天然只能改到自己的评论，
     * 返回 0 表示评论不存在 / 不属于自己 / 已被删除。
     *
     * @param user_id 当前登录用户ID
     * @param id      评论主键
     * @param content 新内容（不能为空白）
     * @param images  新图片，可为 null
     * @return 受影响行数
     */
    public int updateMyComment(int user_id, int id, String content, String images) {
        if (user_id <= 0 || id <= 0 || content == null || content.isBlank()) {
            return 0;
        }
        return decorationCommentMapper.updateContentByIdAndUser(id, user_id, content.trim(), images);
    }

    /**
     * 删除自己的评论（逻辑删除）
     *
     * @param user_id 当前登录用户ID
     * @param id      评论主键
     * @return 受影响行数，0 表示评论不存在 / 不属于自己 / 已被删除
     */
    public int deleteMyComment(int user_id, int id) {
        if (user_id <= 0 || id <= 0) {
            return 0;
        }
        return decorationCommentMapper.softDeleteByIdAndUser(id, user_id);
    }
}

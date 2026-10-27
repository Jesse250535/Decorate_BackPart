package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.entity.UserLikeCollect;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import com.example.decoratebackservice.mapper.UserLikeCollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户点赞收藏业务层
 */
@Service
public class UserLikeCollectService {

    /** 操作类型：2 收藏（1 为点赞，本次功能只用收藏） */
    public static final int OPERATE_COLLECT = 2;

    /** 对象类型：1 装修方案 2 装修材料 3 评论 */
    private static final int TARGET_PLAN = 1;
    private static final int TARGET_MATERIAL = 2;
    private static final int TARGET_COMMENT = 3;

    @Autowired
    private UserLikeCollectMapper userLikeCollectMapper;

    /**
     * 按用户名反查 sys_user.id，用于确定当前登录用户
     */
    @Autowired
    private UserAccountMapper userAccountMapper;

    public int insert(UserLikeCollect likeCollect) {
        return userLikeCollectMapper.insert(likeCollect);
    }

    public UserLikeCollect findById(int id) {
        return userLikeCollectMapper.findById(id);
    }

    public List<UserLikeCollect> findAll() {
        return userLikeCollectMapper.findAll();
    }

    public int update(UserLikeCollect likeCollect) {
        return userLikeCollectMapper.update(likeCollect);
    }

    public int delete(int id) {
        return userLikeCollectMapper.delete(id);
    }

    /* ==================== 以下为「我的收藏」相关新增逻辑 ==================== */

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
     * 查询当前用户自己的收藏记录（不含已取消的）
     *
     * @param user_id 当前登录用户ID
     * @return 收藏记录，用户ID非法时返回空列表
     */
    public List<UserLikeCollect> findMyCollects(int user_id) {
        if (user_id <= 0) {
            return new ArrayList<>();
        }
        return userLikeCollectMapper.findByUserAndOperate(user_id, OPERATE_COLLECT);
    }

    /**
     * 收藏一个方案 / 材料 / 评论
     * <p>
     * user_id 由会话解析后传入，前端无法替他人收藏；
     * 目标类型只接受 1-3，避免写入无法解析的脏数据。
     *
     * @param user_id     当前登录用户ID
     * @param target_type 对象类型 1装修方案 2装修材料 3评论
     * @param target_id   对象ID
     * @return 受影响行数；参数非法时返回 0
     */
    public int collect(int user_id, int target_type, int target_id) {
        if (user_id <= 0 || target_id <= 0 || !isValidTargetType(target_type)) {
            return 0;
        }
        return userLikeCollectMapper.upsertCollect(user_id, OPERATE_COLLECT, target_type, target_id);
    }

    /**
     * 取消收藏（逻辑删除）
     *
     * @param user_id     当前登录用户ID
     * @param target_type 对象类型
     * @param target_id   对象ID
     * @return 受影响行数，0 表示本来就没收藏过
     */
    public int cancel(int user_id, int target_type, int target_id) {
        if (user_id <= 0 || target_id <= 0 || !isValidTargetType(target_type)) {
            return 0;
        }
        return userLikeCollectMapper.cancelCollect(user_id, OPERATE_COLLECT, target_type, target_id);
    }

    /**
     * 判断当前用户是否已收藏某个对象（详情页初始化收藏按钮状态）
     *
     * @param user_id     当前登录用户ID
     * @param target_type 对象类型
     * @param target_id   对象ID
     * @return 已收藏返回 true
     */
    public boolean isCollected(int user_id, int target_type, int target_id) {
        if (user_id <= 0 || target_id <= 0 || !isValidTargetType(target_type)) {
            return false;
        }
        return userLikeCollectMapper.countCollected(user_id, OPERATE_COLLECT, target_type, target_id) > 0;
    }

    /**
     * 目标类型是否合法（1 装修方案 2 装修材料 3 评论）
     */
    private boolean isValidTargetType(int target_type) {
        return target_type == TARGET_PLAN || target_type == TARGET_MATERIAL || target_type == TARGET_COMMENT;
    }
}

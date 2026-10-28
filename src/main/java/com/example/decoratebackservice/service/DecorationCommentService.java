package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.entity.DecorationMaterial;
import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.DecorationCommentMapper;
import com.example.decoratebackservice.mapper.DecorationMaterialMapper;
import com.example.decoratebackservice.mapper.DecorationPlanMapper;
import com.example.decoratebackservice.mapper.UserAccountMapper;
import com.example.decoratebackservice.mapper.UserLikeCollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** 点赞记录：评论点赞复用 user_like_collect（operate_type=1, target_type=3） */
    @Autowired
    private UserLikeCollectMapper userLikeCollectMapper;

    /** 校验评论目标（方案 / 材料）是否真实存在 */
    @Autowired
    private DecorationPlanMapper planMapper;

    @Autowired
    private DecorationMaterialMapper materialMapper;

    /** 对象类型：1 装修方案 2 装修材料 */
    public static final int TARGET_PLAN = 1;
    public static final int TARGET_MATERIAL = 2;

    /** 评论内容最大长度（与前端输入限制保持一致） */
    public static final int MAX_CONTENT_LENGTH = 500;

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

    /* ==================== 以下为详情页评论区：列表 / 发表 / 点赞 ==================== */

    /**
     * 查询某个方案 / 材料下的评论，组装成「主评论 + 回复」两级结构
     *
     * @param target_type   1装修方案 2装修材料
     * @param target_id     方案 / 材料ID
     * @param currentUserId 当前登录用户ID，未登录传 null（此时 liked 恒为 false）
     * @return 主评论列表，按发布时间倒序
     */
    public List<DecorationComment> listByTarget(int target_type, int target_id, Integer currentUserId) {
        if (!isValidTargetType(target_type) || target_id <= 0) {
            return new ArrayList<>();
        }
        List<DecorationComment> all = decorationCommentMapper.findByTarget(target_type, target_id);
        Set<Integer> likedIds = new HashSet<>();
        if (currentUserId != null && currentUserId > 0) {
            likedIds.addAll(userLikeCollectMapper.findLikedCommentIds(currentUserId));
        }

        Map<Integer, User> userCache = new HashMap<>();
        Map<Integer, DecorationComment> rootMap = new LinkedHashMap<>();
        List<DecorationComment> roots = new ArrayList<>();
        for (DecorationComment comment : all) {
            fillUser(comment, userCache);
            comment.setLiked(likedIds.contains(comment.getId()));
            comment.setReplies(new ArrayList<>());
            if (comment.getParent_id() == 0) {
                roots.add(comment);
                rootMap.put(comment.getId(), comment);
            }
        }
        // findByTarget 是发布时间倒序；回复列表按时间正序阅读更自然
        List<DecorationComment> orphans = new ArrayList<>();
        for (DecorationComment comment : all) {
            if (comment.getParent_id() == 0) {
                continue;
            }
            DecorationComment root = rootMap.get(comment.getParent_id());
            if (root != null) {
                root.getReplies().add(comment);
            } else {
                // 父评论已被删除时，把回复挂到一级，避免评论凭空消失
                orphans.add(comment);
            }
        }
        for (DecorationComment root : roots) {
            Collections.reverse(root.getReplies());
        }
        Collections.reverse(orphans);
        roots.addAll(orphans);
        return roots;
    }

    /**
     * 发表评论 / 回复
     *
     * @param userId      当前登录用户ID
     * @param targetType  1装修方案 2装修材料
     * @param targetId    方案 / 材料ID
     * @param parentId    父评论ID，0 表示主评论，否则为对该评论的回复
     * @param content     评论内容
     * @return 新评论（已填充昵称），失败返回 null
     */
    public DecorationComment publish(int userId, int targetType, int targetId, int parentId, String content) {
        if (userId <= 0 || content == null || content.isBlank()) {
            return null;
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT_LENGTH) {
            return null;
        }
        if (!isValidTargetType(targetType) || targetId <= 0) {
            return null;
        }
        if (!targetExists(targetType, targetId)) {
            return null;
        }
        // 回复必须指向同一目标下的有效主评论，避免跨对象串评论
        if (parentId > 0) {
            DecorationComment parent = decorationCommentMapper.findById(parentId);
            if (parent == null
                    || parent.getIs_deleted() != 0
                    || parent.getStatus() != 0
                    || parent.getTarget_type() != targetType
                    || parent.getTarget_id() != targetId) {
                return null;
            }
            // 只支持两级：回复的回复统一挂到主评论下
            if (parent.getParent_id() != 0) {
                parentId = parent.getParent_id();
            }
        }

        DecorationComment comment = new DecorationComment();
        comment.setUser_id(userId);
        comment.setTarget_type(targetType);
        comment.setTarget_id(targetId);
        comment.setParent_id(parentId);
        comment.setContent(text);
        comment.setImages(null);
        comment.setLike_count(0);
        comment.setStatus(0);
        comment.setIs_deleted(0);
        decorationCommentMapper.insert(comment);

        // 回填主键后再查一次，保证返回给前端的数据与库里完全一致
        DecorationComment saved = comment.getId() > 0
                ? decorationCommentMapper.findById(comment.getId())
                : null;
        if (saved != null) {
            saved.setLiked(false);
            saved.setReplies(new ArrayList<>());
            fillUser(saved, new HashMap<>());
        }
        return saved;
    }

    /**
     * 点赞 / 取消点赞（同一个接口切换）
     * <p>
     * 是否已点赞以 user_like_collect 中的有效记录为准，点赞数同步维护在
     * decoration_comment.like_count 上，两者要么一起成功，要么都不变。
     *
     * @param userId    当前登录用户ID
     * @param commentId 评论ID
     * @return {liked, like_count}；评论不存在时返回 null
     */
    public Map<String, Object> toggleCommentLike(int userId, int commentId) {
        if (userId <= 0 || commentId <= 0) {
            return null;
        }
        if (decorationCommentMapper.countValidById(commentId) <= 0) {
            return null;
        }
        boolean liked = userLikeCollectMapper.countCommentLiked(userId, commentId) > 0;
        if (liked) {
            userLikeCollectMapper.cancelCommentLike(userId, commentId);
            decorationCommentMapper.addLikeCount(commentId, -1);
        } else {
            userLikeCollectMapper.upsertCommentLike(userId, commentId);
            decorationCommentMapper.addLikeCount(commentId, 1);
        }
        DecorationComment comment = decorationCommentMapper.findById(commentId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("liked", !liked);
        data.put("like_count", comment == null ? 0 : comment.getLike_count());
        return data;
    }

    /**
     * 评论目标是否存在（未逻辑删除的方案 / 材料）
     */
    private boolean targetExists(int targetType, int targetId) {
        if (targetType == TARGET_PLAN) {
            DecorationPlan plan = planMapper.findById(targetId);
            return plan != null && plan.getIs_deleted() == 0;
        }
        if (targetType == TARGET_MATERIAL) {
            DecorationMaterial material = materialMapper.findById(targetId);
            return material != null && material.getIs_deleted() == 0;
        }
        return false;
    }

    private boolean isValidTargetType(int targetType) {
        return targetType == TARGET_PLAN || targetType == TARGET_MATERIAL;
    }

    /**
     * 用 sys_user 的昵称 / 头像填充展示字段，同一次请求内做缓存避免重复查库
     */
    private void fillUser(DecorationComment comment, Map<Integer, User> cache) {
        User user = cache.computeIfAbsent(comment.getUser_id(), id -> userAccountMapper.findById(id));
        if (user == null) {
            comment.setUser_name("匿名用户");
            comment.setUser_avatar(null);
            return;
        }
        String nickname = user.getNickname();
        comment.setUser_name(nickname != null && !nickname.isBlank() ? nickname : user.getUsername());
        comment.setUser_avatar(user.getAvatar());
    }
}

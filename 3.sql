/*
 装饰公司系统建表脚本

 Source Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001
 日期：2026-10-30

 说明：
 1. 本文件为合并后的单一建表脚本（21 张表），新库只需执行：3.sql → 4_seed_data.sql。
 2. 角色收敛为 3 类前台角色 + 1 类后台管理员：
      ROLE_USER 普通用户 / ROLE_DESIGNER 设计师 / ROLE_MANAGER 项目经理 / ROLE_ADMIN 管理员
    权限判断依据是 role_code，不依赖 id。
 3. 施工进度统一由 construction_stage（绑定 design_service_order）管理，
    已移除旧的 decoration_progress（绑定商城订单的装修进度表）。
 4. 量房、效果图、平面设计图功能已整合进 design_service_order
    （measure_date / plan_drawings / effect_drawings 字段），
    移除独立的设计图纸表 design_drawing 与量房记录表 measure_record。
 5. 设计师展示信息整合到 sys_user（头像 avatar / 简介 profile），
    移除设计师档案表 designer_profile；验证码走内存实现，移除 email_verification_code 表。
 6. 移除 Java 后端未使用的冗余字段（部分 create_time / update_time 审计字段、
    decoration_order 的量房 / 下单入口等字段）。
 7. order_payment 付款计划表已合并进 decoration_order 主表，
    order_payment 表已删除；材料订单默认全款（payment_type=5），
    尾款金额与尾款施工项目分别用 tail_amount / tail_items 记录。
    */
create database decoration_company_system;
use decoration_company_system;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_role（前台角色权限表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '权限标识集合',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色备注',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user（用户表：前台用户 + 后台管理员，is_admin 区分）
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别 0未知1男2女',
  `user_type` tinyint NOT NULL DEFAULT 1 COMMENT '用户身份类型 1客户2装修爱好者3购房者4家具爱好者5其他，仅允许1-5',
  `user_type_chosen` tinyint NOT NULL DEFAULT 0,
  `role_id` bigint NOT NULL COMMENT '关联角色ID',
  `is_admin` tinyint NOT NULL DEFAULT 0 COMMENT '是否后台管理员 0否1是',
  `profile` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人简介',
  `violation_count` int NOT NULL DEFAULT 0 COMMENT '违规次数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '账号状态 0正常1封禁',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_sys_user_user_type` CHECK (`user_type` between 1 and 5)
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表（前台用户 + 后台管理员）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for supplier（材料供应商档案）
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商地址',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '合作状态 0停用1正常合作',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for decoration_category（分类标签表）
-- ----------------------------
DROP TABLE IF EXISTS `decoration_category`;
CREATE TABLE `decoration_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `category_type` tinyint NOT NULL COMMENT '分类类型 1装修方案分类2装修材料分类',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门标签 0否1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for decoration_plan（装修方案表，绑定设计师）
-- ----------------------------
DROP TABLE IF EXISTS `decoration_plan`;
CREATE TABLE `decoration_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方案名称',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图',
  `detail_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '实景案例图集，多图逗号分隔',
  `price` decimal(10, 2) NOT NULL COMMENT '方案价格',
  `area_min` int NULL DEFAULT NULL COMMENT '适用最小面积(平米)',
  `area_max` int NULL DEFAULT NULL COMMENT '适用最大面积(平米)',
  `style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格描述',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方案详细描述',
  `category_id` bigint NOT NULL COMMENT '关联分类ID',
  `create_user_id` bigint NOT NULL COMMENT '上传者用户ID',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核1通过2驳回',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
  `sales_count` int NOT NULL DEFAULT 0 COMMENT '购买销量',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '上架状态 0下架1上架',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_name`(`plan_name` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_create_user`(`create_user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  CONSTRAINT `fk_plan_category` FOREIGN KEY (`category_id`) REFERENCES `decoration_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_plan_user` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修方案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for decoration_material（装修材料表）
--   stock 仅作商品展示库存，不再记录出入库流水（material_stock_record 已移除）
-- ----------------------------
DROP TABLE IF EXISTS `decoration_material`;
CREATE TABLE `decoration_material`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料名称',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外观图',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `spec_long` decimal(10, 2) NULL DEFAULT NULL COMMENT '长度参数(毫米)',
  `spec_width` decimal(10, 2) NULL DEFAULT NULL COMMENT '宽度参数(毫米)',
  `spec_height` decimal(10, 2) NULL DEFAULT NULL COMMENT '高度参数(毫米)',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计量单位',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量（仅展示，不记录流水）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料参数详情',
  `category_id` bigint NOT NULL COMMENT '关联分类ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `create_user_id` bigint NULL DEFAULT NULL,
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核1通过2驳回',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
  `sales_count` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '上架状态 0下架1上架',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_material_name`(`material_name` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  INDEX `idx_material_create_user`(`create_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_material_category` FOREIGN KEY (`category_id`) REFERENCES `decoration_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_material_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修材料表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for decoration_order（订单主表）
-- ----------------------------
DROP TABLE IF EXISTS `decoration_order`;
CREATE TABLE `decoration_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `order_type` tinyint NOT NULL COMMENT '订单类型 1购买装修方案2购买装修材料',
  `target_id` bigint NOT NULL COMMENT '关联资源ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态 0待支付1已支付2已完成3已退款',
  `contract_status` tinyint NOT NULL DEFAULT 0 COMMENT '合同状态 0未签订1已签订',
  `refund_status` tinyint NOT NULL DEFAULT 0 COMMENT '退款状态 0无退款1待审核2已退款3驳回',
  `payment_type` tinyint NOT NULL DEFAULT 5 COMMENT '款项类型 1定金2首付3期付4尾付5全款（材料订单默认全款）',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '本期应付金额',
  `ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '占总金额比例(%)',
  `due_date` date NULL DEFAULT NULL COMMENT '应付日期',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态 0待支付1已支付2已退款',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付流水号',
  `tail_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '尾款金额（含施工项目费用）',
  `tail_items` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '尾款包含的施工项目，逗号分隔：拆墙,开槽,水电,贴瓷砖,做吊顶,刮腻子,全屋定制',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户订单备注',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `contract_time` datetime NULL DEFAULT NULL COMMENT '合同签订时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '订单完成时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表（含付款计划与尾款）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_conversation（设计师-客户会话表，一对一会话）
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversation`;
CREATE TABLE `chat_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '客户 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '设计师 sys_user.id',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '围绕的方案ID（可空）',
  `last_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `user_unread` int NOT NULL DEFAULT 0 COMMENT '客户未读数',
  `designer_unread` int NOT NULL DEFAULT 0 COMMENT '设计师未读数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0关闭1正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chat_user_designer`(`user_id` ASC, `designer_id` ASC) USING BTREE,
  INDEX `idx_chat_designer`(`designer_id` ASC) USING BTREE,
  CONSTRAINT `fk_chat_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_designer` FOREIGN KEY (`designer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_chat_plan` FOREIGN KEY (`plan_id`) REFERENCES `decoration_plan` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计师客户会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_message（设计师-客户消息表）
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者 sys_user.id',
  `sender_role` tinyint NOT NULL COMMENT '发送者身份 1客户 2设计师',
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型 1文字 2图片 3方案卡片',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文字内容',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片地址',
  `target_id` bigint NULL DEFAULT NULL COMMENT '卡片关联对象ID（msg_type=3 时为方案ID）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '对方是否已读 0未读1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_conversation`(`conversation_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_msg_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计师客户消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for decoration_comment（评论表）
-- ----------------------------
DROP TABLE IF EXISTS `decoration_comment`;
CREATE TABLE `decoration_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `target_type` tinyint NOT NULL COMMENT '评论对象类型 1装修方案2装修材料',
  `target_id` bigint NOT NULL COMMENT '关联对象ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父评论ID 0代表主评论',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论图片，多图逗号分隔',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0正常1违规删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_like_collect（用户点赞收藏表）
-- ----------------------------
DROP TABLE IF EXISTS `user_like_collect`;
CREATE TABLE `user_like_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `operate_type` tinyint NOT NULL COMMENT '操作类型 1点赞2收藏',
  `target_type` tinyint NOT NULL COMMENT '对象类型 1装修方案2装修材料3评论',
  `target_id` bigint NOT NULL COMMENT '关联对象ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_operate`(`user_id` ASC, `operate_type` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户点赞收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_feedback（用户意见反馈表）
-- ----------------------------
DROP TABLE IF EXISTS `user_feedback`;
CREATE TABLE `user_feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '提交用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户联系方式',
  `handle_status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态 0待处理1已处理',
  `handle_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理回复',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_feedback_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户意见反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_home_config（首页配置表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_home_config`;
CREATE TABLE `sys_home_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_type` tinyint NOT NULL COMMENT '配置类型 1轮播图2精选成功案例图3首页推荐位',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `jump_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点击跳转链接',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '展示状态 0隐藏1显示',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notice（系统公告表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告详情',
  `show_position` tinyint NOT NULL COMMENT '展示位置 1首页顶部2用户中心3后台首页',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '发布状态 0草稿1已发布2已下线',
  `create_user_id` bigint NOT NULL COMMENT '发布人ID（sys_user.id）',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_notice_admin`(`create_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_notice_admin` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_visit_stat（系统访问统计表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_visit_stat`;
CREATE TABLE `sys_visit_stat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `pv` int NOT NULL DEFAULT 0 COMMENT '页面浏览量',
  `uv` int NOT NULL DEFAULT 0 COMMENT '独立访客数',
  `new_user_count` int NOT NULL DEFAULT 0 COMMENT '新增注册用户数',
  `create_plan_count` int NOT NULL DEFAULT 0 COMMENT '新增上传方案数',
  `create_material_count` int NOT NULL DEFAULT 0 COMMENT '新增上传材料数',
  `finish_order_count` int NOT NULL DEFAULT 0 COMMENT '完成订单数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 设计服务全流程（业主-设计师）
--   status 状态机：
--     0 待设计师接单  1 待量房(已接单)  2 已量房(待出平面图)
--     3 平面图待业主确认  4 平面图已确认(待付定金)  5 已付定金(待出效果图)
--     6 效果图已出(待报价)  7 已报价(待业主确认)  8 报价已确认(待签合同)
--     9 已签合同(待开工)  10 施工中  11 已完工(待付尾款)  12 已完成  13 已取消
-- ----------------------------
DROP TABLE IF EXISTS `design_service_order`;
CREATE TABLE `design_service_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设计服务单号',
  `user_id` bigint NOT NULL COMMENT '业主 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '设计师 sys_user.id',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '项目经理 sys_user.id',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '关联方案ID（可空）',
  `decoration_mode` tinyint NOT NULL DEFAULT 1 COMMENT '装修方式 1硬装 2软装 3硬装+软装整装',
  `house_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '户型，如三室两厅',
  `house_area` decimal(8, 2) NULL DEFAULT NULL COMMENT '建筑面积(平米)',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目地址',
  `style` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期望风格',
  `budget` decimal(12, 2) NULL DEFAULT NULL COMMENT '预算(元)',
  `expect_date` datetime NULL DEFAULT NULL COMMENT '期望上门量房时间',
  `requirement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '业主需求描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态，见上方状态机',
  `measure_date` datetime NULL DEFAULT NULL COMMENT '免费量房完成时间',
  `plan_drawing_time` datetime NULL DEFAULT NULL COMMENT '平面设计图提交时间',
  `plan_drawings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '平面设计图地址，多图逗号分隔',
  `plan_confirm_time` datetime NULL DEFAULT NULL COMMENT '业主确认平面图时间',
  `deposit_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '定金金额',
  `deposit_time` datetime NULL DEFAULT NULL COMMENT '定金支付时间',
  `effect_drawing_time` datetime NULL DEFAULT NULL COMMENT '效果图提交时间',
  `effect_drawings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '效果图地址，多图逗号分隔',
  `quote_id` bigint NULL DEFAULT NULL COMMENT '当前生效报价单ID',
  `quote_time` datetime NULL DEFAULT NULL COMMENT '报价单提交时间',
  `contract_id` bigint NULL DEFAULT NULL COMMENT '合同ID',
  `contract_time` datetime NULL DEFAULT NULL COMMENT '合同签订时间',
  `start_date` date NULL DEFAULT NULL COMMENT '计划开工日期',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '整体完工时间',
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_service_no`(`service_no` ASC) USING BTREE,
  INDEX `idx_dso_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_dso_designer`(`designer_id` ASC) USING BTREE,
  INDEX `idx_dso_manager`(`manager_id` ASC) USING BTREE,
  INDEX `idx_dso_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_dso_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dso_designer` FOREIGN KEY (`designer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_dso_manager` FOREIGN KEY (`manager_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_dso_plan` FOREIGN KEY (`plan_id`) REFERENCES `decoration_plan` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计服务单（业主-设计师全流程主表）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 装修报价单（硬装/软装金额分开）
--   status：0 草稿  1 待业主确认  2 已确认  3 已作废
-- ----------------------------
DROP TABLE IF EXISTS `design_quote`;
CREATE TABLE `design_quote`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报价单号',
  `service_id` bigint NOT NULL COMMENT '关联设计服务单ID',
  `user_id` bigint NOT NULL COMMENT '业主 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '报价设计师 sys_user.id',
  `hard_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '硬装合计',
  `soft_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '软装合计',
  `total_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '报价总额',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0草稿1待业主确认2已确认3已作废',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报价说明',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT '业主确认时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_quote_no`(`quote_no` ASC) USING BTREE,
  INDEX `idx_quote_service`(`service_id` ASC) USING BTREE,
  CONSTRAINT `fk_quote_service` FOREIGN KEY (`service_id`) REFERENCES `design_service_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修报价单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 报价明细（按施工阶段归类，硬/软装分开）
-- ----------------------------
DROP TABLE IF EXISTS `design_quote_item`;
CREATE TABLE `design_quote_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quote_id` bigint NOT NULL COMMENT '关联报价单ID',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '1硬装 2软装',
  `stage_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '归属施工阶段编码',
  `item_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `spec` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格/工艺说明',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `quantity` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '数量',
  `unit_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '小计',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌/型号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_qi_quote`(`quote_id` ASC) USING BTREE,
  CONSTRAINT `fk_qi_quote` FOREIGN KEY (`quote_id`) REFERENCES `design_quote` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报价单明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 装修合同（设计服务流程）
--   sign_status：0 待签  1 已签  2 已作废
-- ----------------------------
DROP TABLE IF EXISTS `design_contract`;
CREATE TABLE `design_contract`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `service_id` bigint NOT NULL COMMENT '关联设计服务单ID',
  `user_id` bigint NOT NULL COMMENT '业主 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '设计师 sys_user.id',
  `manager_id` bigint NOT NULL COMMENT '签订合同的项目经理 sys_user.id',
  `contract_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '合同金额（取报价总额）',
  `decoration_mode` tinyint NOT NULL DEFAULT 1 COMMENT '装修方式 1硬装2软装3整装',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待签1已签2已作废',
  `sign_date` datetime NULL DEFAULT NULL COMMENT '签订时间',
  `start_date` date NULL DEFAULT NULL COMMENT '计划开工日期',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同附件地址',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dc_no`(`contract_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_dc_service`(`service_id` ASC) USING BTREE,
  CONSTRAINT `fk_dc_service` FOREIGN KEY (`service_id`) REFERENCES `design_service_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修合同（设计服务流程）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 施工阶段（进度表：计划/实际起止、是否按时、业主验收、返工）
--   status：0 未开始  1 进行中  2 已完工(待验收)  3 已验收通过  4 返工中
--   accept_status：0 待验收  1 验收通过  2 验收不通过
--   on_time：0 未判定  1 按时完成  2 超期完成
--   stage_code：硬装 demolition 拆墙 / slotting 开槽 / plumbing 水电改造 / tiling 贴瓷砖
--          ceiling 做吊顶 / putty 刮腻子 / custom 全屋定制 / cleaning 保洁
--          软装 furniture 家具进场 / curtain 窗帘布艺 / lighting 灯具安装 / decor 软装饰品
-- ----------------------------
DROP TABLE IF EXISTS `construction_stage`;
CREATE TABLE `construction_stage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_id` bigint NOT NULL COMMENT '关联设计服务单ID',
  `stage_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '阶段编码',
  `stage_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '阶段名称',
  `stage_type` tinyint NOT NULL DEFAULT 1 COMMENT '1硬装 2软装',
  `seq` int NOT NULL DEFAULT 0 COMMENT '施工顺序',
  `plan_start_date` date NULL DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` date NULL DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` date NULL DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date NULL DEFAULT NULL COMMENT '实际结束日期',
  `plan_days` int NOT NULL DEFAULT 0 COMMENT '计划工期(天)',
  `actual_days` int NOT NULL DEFAULT 0 COMMENT '实际工期(天)',
  `delay_days` int NOT NULL DEFAULT 0 COMMENT '超期天数',
  `on_time` tinyint NOT NULL DEFAULT 0 COMMENT '是否按时 0未判定1按时2超期',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0未开始1进行中2已完工3已验收4返工中',
  `accept_status` tinyint NOT NULL DEFAULT 0 COMMENT '业主验收 0待验收1通过2不通过',
  `accept_user_id` bigint NULL DEFAULT NULL COMMENT '验收业主 sys_user.id',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '验收时间',
  `accept_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验收意见/不通过原因',
  `rework_status` tinyint NOT NULL DEFAULT 0 COMMENT '返工状态 0无需返工1返工中2返工完成',
  `rework_days` int NOT NULL DEFAULT 0 COMMENT '返工工期(天)',
  `rework_start_date` date NULL DEFAULT NULL COMMENT '返工开始日期',
  `rework_end_date` date NULL DEFAULT NULL COMMENT '返工完成日期',
  `rework_count` int NOT NULL DEFAULT 0 COMMENT '返工次数',
  `manager_id` bigint NULL DEFAULT NULL COMMENT '负责的项目经理 sys_user.id',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '现场实拍图，多图逗号分隔',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stage_service_code`(`service_id` ASC, `stage_code` ASC) USING BTREE,
  INDEX `idx_stage_service_seq`(`service_id` ASC, `seq` ASC) USING BTREE,
  CONSTRAINT `fk_stage_service` FOREIGN KEY (`service_id`) REFERENCES `design_service_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '施工阶段进度与验收表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 分期付款节点（定金 / 瓷砖完成 / 全屋定制完成 / 保洁完成尾款）
--   node_code：DEPOSIT 定金 / TILING 瓷砖完成 / CUSTOM 全屋定制完成 / FINAL 保洁尾款
--   pay_status：0 待支付  1 已支付  2 已退款
--   trigger_stage_code：允许付款的前置阶段，该阶段业主验收通过后才可付
-- ----------------------------
DROP TABLE IF EXISTS `construction_payment_node`;
CREATE TABLE `construction_payment_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_id` bigint NOT NULL COMMENT '关联设计服务单ID',
  `node_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点编码 DEPOSIT/TILING/CUSTOM/FINAL',
  `node_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `ratio` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '占总价比例(%)',
  `amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '本期应付金额',
  `trigger_stage_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前置阶段编码，空表示签约即付',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付1已支付2已退款',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付流水号',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pnode_service_code`(`service_id` ASC, `node_code` ASC) USING BTREE,
  CONSTRAINT `fk_pnode_service` FOREIGN KEY (`service_id`) REFERENCES `design_service_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分期付款节点表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

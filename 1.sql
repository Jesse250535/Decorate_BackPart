/*
 Navicat Premium Data Transfer

 Source Server         : adminjesse
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : decoration_company_system

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 28/10/2026 19:16:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for decoration_category
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
-- Records of decoration_category
-- ----------------------------
INSERT INTO `decoration_category` VALUES (1, '现代简约', 1, 0, 1, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_category` VALUES (2, '北欧风格', 1, 0, 2, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_category` VALUES (3, '瓷砖石材', 2, 0, 1, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_category` VALUES (4, '其他', 2, 0, 0, 1, '2026-10-28 16:59:20', 0);
INSERT INTO `decoration_category` VALUES (5, '其他', 1, 0, 0, 1, '2026-10-28 16:59:34', 0);
INSERT INTO `decoration_category` VALUES (6, '装修器械工具', 2, 0, 1, 1, '2026-10-28 17:24:11', 0);

-- ----------------------------
-- Table structure for decoration_comment
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
-- Records of decoration_comment
-- ----------------------------
INSERT INTO `decoration_comment` VALUES (1, 5, 1, 1, 0, '这个现代简约方案很不错，空间利用得很合理！', NULL, 13, 0, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_comment` VALUES (2, 7, 2, 1, 0, '瓷砖质量很好，铺贴效果和图片一致。', 'https://example.com/comment/2_1.jpg', 6, 0, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_comment` VALUES (3, 10, 1, 2, 0, '北欧风格很适合小家庭，收藏了！点了！', NULL, 9, 0, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_comment` VALUES (4, 7, 2, 2, 0, '舒服！', NULL, 0, 0, '2026-10-28 17:42:43', 0);
INSERT INTO `decoration_comment` VALUES (5, 7, 2, 2, 0, '好用！', NULL, 0, 0, '2026-10-28 17:42:46', 0);
INSERT INTO `decoration_comment` VALUES (6, 7, 1, 1, 0, '值得一瓶！', NULL, 1, 0, '2026-10-28 17:43:16', 0);
INSERT INTO `decoration_comment` VALUES (7, 7, 1, 1, 1, 'god', NULL, 0, 0, '2026-10-28 17:43:25', 0);
INSERT INTO `decoration_comment` VALUES (8, 7, 1, 1, 6, 'good', NULL, 0, 0, '2026-10-28 17:43:31', 0);

-- ----------------------------
-- Table structure for decoration_material
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
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料参数详情',
  `category_id` bigint NOT NULL COMMENT '关联分类ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `create_user_id` bigint NULL DEFAULT NULL,
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核1通过2驳回',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核驳回原因',
  `sales_count` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '上架状态 0下架1上架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
-- Records of decoration_material
-- ----------------------------
INSERT INTO `decoration_material` VALUES (1, '通体大理石纹瓷砖', '/api/uploads/2026/10/27/cz.webp', 89.00, 800.00, 800.00, 10.00, '片', 500, '800x800mm通体大理石纹瓷砖，防滑耐磨', 3, 1, NULL, 1, NULL, 120, 1, '2026-09-29 10:48:02', '2026-10-27 17:20:17', 0);
INSERT INTO `decoration_material` VALUES (2, '环保净味内墙乳胶漆', '/api/uploads/2026/10/27/rjq.webp', 260.00, 300.00, 200.00, 250.00, '桶', 300, '18L环保净味乳胶漆，遮盖力强', 3, 2, NULL, 1, NULL, 210, 1, '2026-09-29 10:48:02', '2026-10-27 16:48:34', 0);
INSERT INTO `decoration_material` VALUES (3, '实木复合地板', '/api/uploads/2026/10/27/floor.webp', 168.00, 900.00, 125.00, 15.00, '平方米', 800, '实木复合地板，脚感舒适，环保耐磨', 3, 3, NULL, 1, NULL, 0, 1, '2026-09-29 10:48:02', '2026-10-27 16:51:48', 0);
INSERT INTO `decoration_material` VALUES (8, 'new1', '/g', 34.00, NULL, NULL, NULL, '桶', 23, 'good', 4, 4, 16, 1, NULL, 0, 1, '2026-10-28 17:05:48', '2026-10-28 17:09:05', 0);
INSERT INTO `decoration_material` VALUES (9, '电钻', 'gt', 399.00, NULL, NULL, NULL, '个', 40, '电钻', 4, 4, 16, 1, NULL, 0, 1, '2026-10-28 17:25:39', '2026-10-28 17:26:09', 0);

-- ----------------------------
-- Table structure for decoration_order
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
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户订单备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `contract_time` datetime NULL DEFAULT NULL COMMENT '合同签订时间',
  `finish_time` datetime NULL DEFAULT NULL COMMENT '订单完成时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_order
-- ----------------------------
INSERT INTO `decoration_order` VALUES (1, 'DO202609010001', 5, 1, 1, 88000.00, 1, 1, 0, '希望尽快安排施工', '2026-09-29 10:48:02', '2026-09-01 10:20:00', NULL, NULL, '2026-09-29 10:48:02', 0);
INSERT INTO `decoration_order` VALUES (2, 'DO202609020002', 7, 2, 1, 890.00, 1, 0, 0, '购买瓷砖10片', '2026-09-29 10:48:02', '2026-09-02 14:35:00', '2026-10-26 16:56:34', NULL, '2026-10-27 14:53:27', 0);
INSERT INTO `decoration_order` VALUES (3, 'DO202609030003', 10, 1, 2, 128000.00, 1, 0, 0, '待确认方案细节', '2026-09-29 10:48:02', NULL, NULL, NULL, '2026-10-27 15:01:37', 0);
INSERT INTO `decoration_order` VALUES (5, 'DD202610281539331192', 10, 1, 3, 58000.00, 1, 0, 0, NULL, '2026-10-28 15:39:33', '2026-10-28 15:39:36', NULL, NULL, '2026-10-28 15:39:35', 0);
INSERT INTO `decoration_order` VALUES (6, 'DD202610281540543430', 2, 1, 2, 384000.00, 3, 0, 0, NULL, '2026-10-28 15:40:54', '2026-10-28 15:40:55', NULL, NULL, '2026-10-28 15:41:06', 0);
INSERT INTO `decoration_order` VALUES (7, 'DD202610281541458106', 2, 2, 3, 1680.00, 1, 0, 0, NULL, '2026-10-28 15:41:45', '2026-10-28 15:42:06', NULL, NULL, '2026-10-28 15:42:05', 0);
INSERT INTO `decoration_order` VALUES (8, 'DD202610281649023289', 10, 1, 4, 69999.00, 3, 0, 0, NULL, '2026-10-28 16:49:02', '2026-10-28 16:49:03', NULL, NULL, '2026-10-28 16:49:09', 0);
INSERT INTO `decoration_order` VALUES (9, 'DD202610281700395652', 7, 2, 2, 260.00, 1, 0, 0, NULL, '2026-10-28 17:00:39', '2026-10-28 17:00:41', NULL, NULL, '2026-10-28 17:00:40', 0);
INSERT INTO `decoration_order` VALUES (10, 'DD202610281701476777', 7, 2, 2, 260.00, 1, 0, 0, NULL, '2026-10-28 17:01:47', '2026-10-28 17:01:48', NULL, NULL, '2026-10-28 17:01:47', 0);

-- ----------------------------
-- Table structure for decoration_plan
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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
-- Records of decoration_plan
-- ----------------------------
INSERT INTO `decoration_plan` VALUES (1, '现代简约二居室整装方案', '/api/uploads/2026/10/27/modern01.png', '/api/uploads/2026/10/27/modern01.png', 80000.00, 70, 90, '现代简约', '以白色与浅木色为主色调，打造简洁通透的二居室空间', 1, 2, 1, NULL, 15, 320, 45, 1, '2026-09-29 10:48:02', '2026-10-27 16:41:29', 0);
INSERT INTO `decoration_plan` VALUES (2, '北欧风格三居室整装方案', '/api/uploads/2026/10/27/franch01.jpg', '/api/uploads/2026/10/27/franch01.jpg', 128000.00, 90, 120, '北欧风格', '主打自然原木与清新绿植，营造温馨舒适的居家氛围', 2, 11, 1, '', 0, 0, 0, 1, '2026-09-29 10:48:02', '2026-10-27 16:42:42', 0);
INSERT INTO `decoration_plan` VALUES (3, '现代简约小户型改造方案', '/api/uploads/2026/10/27/xdjy.jpg', NULL, 58000.00, 40, 60, '现代简约', '针对小户型优化收纳与动线，提升空间利用率', 1, 1, 1, '', 0, 0, 0, 1, '2026-09-29 10:48:02', '2026-10-27 16:52:46', 0);
INSERT INTO `decoration_plan` VALUES (4, 'fgwt', NULL, NULL, 69999.00, 78, 123, NULL, 'confort', 2, 14, 1, NULL, 0, 0, 0, 1, '2026-10-28 16:47:54', '2026-10-28 16:48:51', 0);

-- ----------------------------
-- Table structure for decoration_progress
-- ----------------------------
DROP TABLE IF EXISTS `decoration_progress`;
CREATE TABLE `decoration_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `progress_status` tinyint NOT NULL COMMENT '装修状态 1待开工2施工中3待竣工验收4已完成5施工异常',
  `status_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '当前进度描述',
  `progress_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '现场实拍图，多图逗号分隔',
  `report_user_id` bigint NOT NULL COMMENT '上报施工人员ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  `audit_status` tinyint NOT NULL DEFAULT 0,
  `audit_user_id` bigint NULL DEFAULT NULL,
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `audit_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `fk_progress_user`(`report_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_progress_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_progress_user` FOREIGN KEY (`report_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修进度跟踪表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_progress
-- ----------------------------
INSERT INTO `decoration_progress` VALUES (1, 1, 2, '水电改造已完成，进入泥木阶段', 'https://example.com/progress/1_1.jpg,https://example.com/progress/1_2.jpg', 9, '2026-09-29 10:48:02', 0, 1, NULL, NULL, '2026-09-29 10:48:02');
INSERT INTO `decoration_progress` VALUES (2, 2, 2, '材料已全部交付并验收完成', 'https://example.com/progress/2_1.jpg', 6, '2026-09-29 10:48:02', 0, 1, NULL, NULL, '2026-09-29 10:48:02');
INSERT INTO `decoration_progress` VALUES (3, 3, 1, '已完成现场测量，等待开工', 'https://example.com/progress/3_1.jpg', 4, '2026-09-29 10:48:02', 0, 1, NULL, NULL, '2026-09-29 10:48:02');
INSERT INTO `decoration_progress` VALUES (6, 1, 4, 'ok', '/api/uploads/2026/10/28/f305f5cf04014440a7ecdee25a831fe5.png', 1, '2026-10-27 23:57:05', 0, 1, NULL, NULL, '2026-10-27 23:57:05');

-- ----------------------------
-- Table structure for email_verification_code
-- ----------------------------
DROP TABLE IF EXISTS `email_verification_code`;
CREATE TABLE `email_verification_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验证码',
  `scene` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'register' COMMENT '使用场景：register 注册',
  `expired_at` datetime NOT NULL COMMENT '过期时间',
  `used` tinyint NOT NULL DEFAULT 0 COMMENT '是否已使用：0 否 1 是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_email_scene`(`email` ASC, `scene` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮箱验证码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of email_verification_code
-- ----------------------------
INSERT INTO `email_verification_code` VALUES (1, 'zhangsan@example.com', '483920', 'register', '2026-09-29 12:00:00', 1, '2026-09-29 10:48:02');
INSERT INTO `email_verification_code` VALUES (2, 'lisi@example.com', '751204', 'register', '2026-09-29 12:05:00', 0, '2026-09-29 10:48:02');
INSERT INTO `email_verification_code` VALUES (3, 'wangwu@example.com', '620517', 'register', '2026-09-29 12:10:00', 0, '2026-09-29 10:48:02');

-- ----------------------------
-- Table structure for material_stock_record
-- ----------------------------
DROP TABLE IF EXISTS `material_stock_record`;
CREATE TABLE `material_stock_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '关联材料ID',
  `operate_type` tinyint NOT NULL COMMENT '操作类型 1采购入库2订单出库3库存盘点调整',
  `change_num` int NOT NULL COMMENT '变动数量 正数为入库负数为出库',
  `before_stock` int NOT NULL COMMENT '变动前库存',
  `after_stock` int NOT NULL COMMENT '变动后库存',
  `operate_user_id` bigint NOT NULL COMMENT '操作库管ID',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出入库备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_material_id`(`material_id` ASC) USING BTREE,
  INDEX `fk_stock_user`(`operate_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_stock_material` FOREIGN KEY (`material_id`) REFERENCES `decoration_material` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_stock_user` FOREIGN KEY (`operate_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料出入库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material_stock_record
-- ----------------------------
INSERT INTO `material_stock_record` VALUES (1, 1, 1, 500, 0, 500, 6, '采购入库通体大理石纹瓷砖500片', '2026-09-29 10:48:02', 0);
INSERT INTO `material_stock_record` VALUES (2, 2, 2, -20, 320, 300, 9, '订单出库环保乳胶漆20桶', '2026-09-29 10:48:02', 0);
INSERT INTO `material_stock_record` VALUES (3, 3, 3, -10, 810, 800, 4, '库存盘点调整实木复合地板10平方米', '2026-09-29 10:48:02', 0);

-- ----------------------------
-- Table structure for supplier
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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of supplier
-- ----------------------------
INSERT INTO `supplier` VALUES (1, '东鹏瓷砖供应有限公司', '陈经理', '020-88880001', '广东省佛山市禅城区南庄镇工业大道1号', '主营瓷砖、地砖，供货稳定', 1, '2026-09-29 10:48:02', 0);
INSERT INTO `supplier` VALUES (2, '立邦涂料华南代理', '刘经理', '020-88880002', '广东省广州市天河区科韵路12号', '主营环保内墙涂料', 1, '2026-09-29 10:48:02', 0);
INSERT INTO `supplier` VALUES (3, '圣象地板广州仓储中心', '黄经理', '020-88880003', '广东省广州市白云区太和镇仓储路8号', '主营实木复合地板', 1, '2026-09-29 10:48:02', 0);
INSERT INTO `supplier` VALUES (4, 'clgy123', 'clgy123', '00000000000', NULL, '材料供应商账号上传材料时自动创建', 1, '2026-10-28 17:05:30', 0);

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像地址',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别 0未知1男2女',
  `user_type` tinyint GENERATED ALWAYS AS (`role_id`) STORED COMMENT '用户身份类型，自动等于role_id' NOT NULL,
  `role_id` bigint NOT NULL COMMENT '关联角色ID',
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
  CONSTRAINT `sys_admin_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_adminrole` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'admin', '12333', '12345677', NULL, NULL, 0, DEFAULT, 6, NULL, 0, 0, '2026-09-21 18:21:53', '2026-09-24 19:11:53', 0);
INSERT INTO `sys_admin` VALUES (3, 'operation', '13900000001', '123456', NULL, '运营小助手', 1, DEFAULT, 7, '负责平台用户与内容运营', 0, 0, '2026-09-29 10:48:02', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_admin` VALUES (4, 'finance', '13900000002', '123456', NULL, '财务小张', 2, DEFAULT, 8, '负责平台订单与财务核对', 0, 0, '2026-09-29 10:48:02', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_admin` VALUES (5, 'service', '13900000003', '123456', NULL, '客服小李', 2, DEFAULT, 9, '负责处理用户反馈与评论', 0, 0, '2026-09-29 10:48:02', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_admin` VALUES (6, 'jesse', NULL, '1234567', NULL, NULL, 0, DEFAULT, 6, NULL, 0, 0, '2026-10-11 20:53:55', '2026-10-11 20:53:55', 0);

-- ----------------------------
-- Table structure for sys_adminrole
-- ----------------------------
DROP TABLE IF EXISTS `sys_adminrole`;
CREATE TABLE `sys_adminrole`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '权限标识集合',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_adminrole_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_adminrole
-- ----------------------------
INSERT INTO `sys_adminrole` VALUES (6, '管理员', 'ROLE_ADMIN', '*', '系统最高权限管理员', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_adminrole` VALUES (7, '运营管理员', 'ROLE_OPERATION', 'user:manage,material:audit,plan:audit', '负责用户与内容运营管理', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_adminrole` VALUES (8, '财务管理员', 'ROLE_FINANCE', 'order:manage,refund:audit,stat:view', '负责订单与财务相关管理', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_adminrole` VALUES (9, '客服管理员', 'ROLE_CS', 'feedback:handle,comment:manage,notice:manage', '负责用户反馈与评论处理', '2026-09-29 10:48:02', 0);

-- ----------------------------
-- Table structure for sys_home_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_home_config`;
CREATE TABLE `sys_home_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_type` tinyint NOT NULL COMMENT '配置类型 1轮播图2精选成功案例图3首页推荐位',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `jump_link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点击跳转链接',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '展示状态 0隐藏1显示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_home_config
-- ----------------------------
INSERT INTO `sys_home_config` VALUES (1, 1, '/banner/1.jpg', '/plan/list', 1, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `sys_home_config` VALUES (2, 1, '/banner/2.jpg', '/material/list', 2, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `sys_home_config` VALUES (3, 2, '/banner/3.jpg', '/plan/1', 1, 1, '2026-09-29 10:48:02', 0);
INSERT INTO `sys_home_config` VALUES (4, 1, '/banner/3.jpg', '/plan/list', 3, 1, '2026-10-28 18:35:55', 0);
INSERT INTO `sys_home_config` VALUES (5, 1, '/banner/4.jpg', '/material/list', 4, 1, '2026-10-28 18:35:55', 0);

-- ----------------------------
-- Table structure for sys_notice
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
  `create_user_id` bigint NOT NULL COMMENT '发布管理员ID',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_notice_admin`(`create_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_notice_admin` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '平台系统升级公告', '平台将于今晚23点进行系统升级，期间部分功能暂停使用，请提前做好准备。', 1, '2026-09-20 09:00:00', '2026-10-20 09:00:00', 1, 1, 0);
INSERT INTO `sys_notice` VALUES (2, '国庆装修优惠活动', '国庆期间下单装修方案享9折优惠，活动数量有限，先到先得。', 2, '2026-09-28 10:00:00', '2026-10-08 23:59:59', 1, 2, 0);
INSERT INTO `sys_notice` VALUES (3, '后台运维通知', '请各管理员及时处理待审核的材料与方案申请，避免积压。', 2, '2026-09-29 08:30:00', NULL, 1, 3, 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '权限标识集合',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '普通用户', 'ROLE_USER', 'plan:view,material:view,comment:write,order:operate', '前台普通注册用户', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (2, '装修方案提供商', 'ROLE_PLAN_PROVIDER', 'plan:upload,plan:view,my:resource', '可上传装修方案申请上架', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (3, '材料供应商', 'ROLE_SUPPLIER', 'material:upload,material:view,my:resource', '可上传装修材料申请上架', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (4, '库管人员', 'ROLE_STOCK', 'stock:operate,stock:view', '负责材料出入库管理', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (5, '施工人员', 'ROLE_WORKER', 'progress:report,progress:view', '负责上报装修施工进度', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (6, '设计师', 'ROLE_DESIGNER', 'plan:upload,plan:view,my:resource', '可上传装修方案设计', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_role` VALUES (7, '项目经理', 'ROLE_MANAGER', 'progress:audit,order:view,my:resource', '负责装修项目管理', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_role` VALUES (8, '财务人员', 'ROLE_FINANCE_STAFF', 'order:view,stat:view', '负责订单财务核对', '2026-09-29 10:48:02', 0);

-- ----------------------------
-- Table structure for sys_user
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
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'gert', '13877886657', 'kecpqrem6667@mail.com', '123456', NULL, 'bulitk', 1, 3, 0, 5, '热爱生活，向往理想的家。', 0, 0, '2026-09-23 18:08:53', '2026-10-24 14:51:15', 0);
INSERT INTO `sys_user` VALUES (2, 'geeee', '18177886655', 'nbk2we@io.org', '12345678io', NULL, 'drtwe', 0, 1, 0, 2, '热爱生活，向往理想的家。', 0, 0, '2026-09-23 18:32:23', '2026-10-14 09:38:59', 0);
INSERT INTO `sys_user` VALUES (3, 'dver', NULL, NULL, '123456uu', NULL, NULL, 0, 1, 0, 5, NULL, 0, 0, '2026-09-23 21:14:18', '2026-09-23 21:14:18', 0);
INSERT INTO `sys_user` VALUES (4, 'ada', NULL, NULL, '123456', NULL, NULL, 0, 1, 0, 5, NULL, 0, 0, '2026-09-24 18:51:49', '2026-09-24 18:51:49', 0);
INSERT INTO `sys_user` VALUES (5, 'test01', '13800000000', NULL, '123456', NULL, '小张', 1, 3, 0, 3, NULL, 0, 0, '2026-09-24 19:26:40', '2026-09-24 19:26:40', 0);
INSERT INTO `sys_user` VALUES (6, 'bertg', NULL, NULL, 'vvbbaacc', NULL, NULL, 0, 4, 0, 4, NULL, 0, 0, '2026-09-24 19:28:08', '2026-09-24 19:28:08', 0);
INSERT INTO `sys_user` VALUES (7, 'bertg1', NULL, NULL, 'ffddcc', '/api/uploads/2026/10/27/f90c33bad58543cf9f7d8c7cd1028d09.jpg', 'bertg1', 0, 3, 1, 1, '热爱生活，向往理想的家。', 0, 0, '2026-09-24 19:29:01', '2026-10-28 17:00:13', 0);
INSERT INTO `sys_user` VALUES (9, 'test', NULL, NULL, '12345678', NULL, NULL, 0, 4, 0, 4, NULL, 0, 1, '2026-09-26 16:15:03', '2026-10-26 15:15:27', 0);
INSERT INTO `sys_user` VALUES (10, 'zhangsan', '13800000001', 'zhangsan@example1.com', '123456', '/api/uploads/2026/10/27/1.png', '张三', 1, 1, 1, 1, '喜欢现代简约风格的装修', 0, 0, '2026-09-29 10:48:02', '2026-10-28 16:48:34', 0);
INSERT INTO `sys_user` VALUES (11, 'lisi', '13800000002', 'lisi@example.com', '123456', NULL, '李四', 2, 2, 0, 2, '专注北欧风装修方案设计', 0, 0, '2026-09-29 10:48:02', '2026-09-29 10:48:02', 0);
INSERT INTO `sys_user` VALUES (13, 'plj', NULL, NULL, '123456', NULL, NULL, 0, 1, 0, 6, NULL, 0, 0, '2026-10-11 21:09:28', '2026-10-11 21:09:28', 0);
INSERT INTO `sys_user` VALUES (14, 'zxfa123', NULL, NULL, '123456', NULL, NULL, 0, 1, 0, 2, NULL, 0, 0, '2026-10-28 16:46:54', '2026-10-28 16:46:54', 0);
INSERT INTO `sys_user` VALUES (15, 'des123', NULL, NULL, '123456', NULL, NULL, 0, 1, 0, 6, NULL, 0, 0, '2026-10-28 16:48:09', '2026-10-28 16:48:09', 0);
INSERT INTO `sys_user` VALUES (16, 'clgy123', NULL, NULL, '123456yyds', NULL, NULL, 0, 1, 0, 3, NULL, 0, 0, '2026-10-28 16:50:36', '2026-10-28 16:50:36', 0);
INSERT INTO `sys_user` VALUES (17, 'kg123', NULL, NULL, '123456', NULL, NULL, 0, 1, 1, 4, NULL, 0, 0, '2026-10-28 17:07:56', '2026-10-28 17:07:56', 0);

-- ----------------------------
-- Table structure for sys_visit_stat
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
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '统计生成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_visit_stat
-- ----------------------------
INSERT INTO `sys_visit_stat` VALUES (1, '2026-09-27', 1250, 320, 5, 2, 3, 1, '2026-09-29 10:48:02');
INSERT INTO `sys_visit_stat` VALUES (2, '2026-09-28', 1430, 356, 3, 1, 2, 2, '2026-09-29 10:48:02');
INSERT INTO `sys_visit_stat` VALUES (3, '2026-09-29', 980, 245, 4, 3, 1, 1, '2026-09-29 10:48:02');

-- ----------------------------
-- Table structure for user_feedback
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
-- Records of user_feedback
-- ----------------------------
INSERT INTO `user_feedback` VALUES (1, 5, '希望增加装修材料的价格对比功能', '13800000000', 1, '已记录，后续版本增加该功能', '2026-09-29 10:48:02', 0);
INSERT INTO `user_feedback` VALUES (2, 7, '装修方案详情页图片加载较慢', 'lisi@example.com', 0, NULL, '2026-09-29 10:48:02', 0);
INSERT INTO `user_feedback` VALUES (3, 10, '建议支持订单在线签订合同', '13800000001', 1, '已反馈至产品团队评估', '2026-09-29 10:48:02', 0);
INSERT INTO `user_feedback` VALUES (4, 7, '网站稳定性差', '4433', 0, NULL, '2026-10-28 18:02:29', 0);

-- ----------------------------
-- Table structure for user_like_collect
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
-- Records of user_like_collect
-- ----------------------------
INSERT INTO `user_like_collect` VALUES (10, 5, 2, 1, 1, '2026-10-28 09:41:25', 0);
INSERT INTO `user_like_collect` VALUES (11, 5, 2, 2, 2, '2026-10-28 09:43:33', 0);
INSERT INTO `user_like_collect` VALUES (30, 7, 2, 2, 2, '2026-10-28 17:00:48', 1);
INSERT INTO `user_like_collect` VALUES (42, 7, 2, 2, 1, '2026-10-28 17:01:09', 1);
INSERT INTO `user_like_collect` VALUES (46, 7, 2, 2, 3, '2026-10-28 17:01:19', 1);
INSERT INTO `user_like_collect` VALUES (58, 7, 1, 3, 1, '2026-10-28 17:43:19', 0);
INSERT INTO `user_like_collect` VALUES (59, 7, 1, 3, 6, '2026-10-28 17:43:20', 0);

SET FOREIGN_KEY_CHECKS = 1;

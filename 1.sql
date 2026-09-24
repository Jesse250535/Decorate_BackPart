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

 Date: 24/09/2026 15:58:10
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_category
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_comment
-- ----------------------------

-- ----------------------------
-- Table structure for decoration_material
-- ----------------------------
DROP TABLE IF EXISTS `decoration_material`;
CREATE TABLE `decoration_material`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料名称',
                                        `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外观图',
                                        `price` decimal(10, 2) NOT NULL COMMENT '单价',
                                        `spec_long` decimal(10, 2) NULL DEFAULT NULL COMMENT '长度参数(毫米)',
                                        `spec_width` decimal(10, 2) NULL DEFAULT NULL COMMENT '宽度参数(毫米)',
                                        `spec_height` decimal(10, 2) NULL DEFAULT NULL COMMENT '高度参数(毫米)',
                                        `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计量单位',
                                        `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
                                        `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '材料参数详情',
                                        `category_id` bigint NOT NULL COMMENT '关联分类ID',
                                        `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
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
                                        CONSTRAINT `fk_material_category` FOREIGN KEY (`category_id`) REFERENCES `decoration_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                        CONSTRAINT `fk_material_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修材料表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_material
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_order
-- ----------------------------

-- ----------------------------
-- Table structure for decoration_plan
-- ----------------------------
DROP TABLE IF EXISTS `decoration_plan`;
CREATE TABLE `decoration_plan`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `plan_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方案名称',
                                    `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封面图',
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修方案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_plan
-- ----------------------------

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
                                        PRIMARY KEY (`id`) USING BTREE,
                                        INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
                                        INDEX `fk_progress_user`(`report_user_id` ASC) USING BTREE,
                                        CONSTRAINT `fk_progress_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                        CONSTRAINT `fk_progress_user` FOREIGN KEY (`report_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修进度跟踪表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of decoration_progress
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料出入库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of material_stock_record
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '材料供应商表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of supplier
-- ----------------------------

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
                              `user_type` tinyint NOT NULL DEFAULT 6 COMMENT '用户身份类型 6管理员（后台端专用）',
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
                              CONSTRAINT `sys_admin_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_adminrole` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `chk_sys_admin_user_type` CHECK (`user_type` = 6)
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
INSERT INTO `sys_admin` VALUES (1, 'admin', '12333', '123456', NULL, NULL, 0, 6, 6, NULL, 0, 0, '2026-09-21 18:21:53', '2026-09-23 18:30:00', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台管理员角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_adminrole
-- ----------------------------
INSERT INTO `sys_adminrole` VALUES (6, '管理员', 'ROLE_ADMIN', '*', '系统最高权限管理员', '2026-09-10 21:05:52', 0);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '首页配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_home_config
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台角色权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '普通用户', 'ROLE_USER', 'plan:view,material:view,comment:write,order:operate', '前台普通注册用户', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (2, '装修方案提供者', 'ROLE_PLAN_PROVIDER', 'plan:upload,plan:view,my:resource', '可上传装修方案申请上架', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (3, '材料供应商', 'ROLE_SUPPLIER', 'material:upload,material:view,my:resource', '可上传装修材料申请上架', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (4, '库管人员', 'ROLE_STOCK', 'stock:operate,stock:view', '负责材料出入库管理', '2026-09-10 21:05:52', 0);
INSERT INTO `sys_role` VALUES (5, '施工人员', 'ROLE_WORKER', 'progress:report,progress:view', '负责上报装修施工进度', '2026-09-10 21:05:52', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '前台用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'gert', NULL, NULL, '123456', NULL, NULL, 0, 4, 5, NULL, 0, 0, '2026-09-23 18:08:53', '2026-09-23 18:31:06', 0);
INSERT INTO `sys_user` VALUES (2, 'geeee', NULL, NULL, '12345678io', NULL, NULL, 0, 1, 2, NULL, 0, 0, '2026-09-23 18:32:23', '2026-09-23 18:32:23', 0);
INSERT INTO `sys_user` VALUES (3, 'dver', NULL, NULL, '123456uu', NULL, NULL, 0, 1, 5, NULL, 0, 0, '2026-09-23 21:14:18', '2026-09-23 21:14:18', 0);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_visit_stat
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户意见反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_feedback
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户点赞收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_like_collect
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

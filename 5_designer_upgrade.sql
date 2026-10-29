/*
  5_designer_upgrade.sql —— 角色收敛 / 设计师 / 项目经理 / 付款阶段 的结构增量
  ==================================================================
  执行顺序：3.sql（基础结构） → 本文件（增量结构） → 4_seed_data.sql（种子数据）

  本文件只做「CREATE TABLE IF NOT EXISTS」和「ADD COLUMN」，不修改、不删除任何
  已有表和已有字段；新增字段全部可空或带默认值，因此现有 Java / Vue 代码
  保持原样即可正常运行（老 SQL 插入语句依然成立）。

  业务约定（与代码保持一致：判断依据始终是 role_code，不是 id）：
    ROLE_USER      普通用户
    ROLE_DESIGNER  设计师：囊括原「装修方案提供商」职能
                   —— 上传方案、出效果图/平面设计图、客户沟通、上门量房
    ROLE_MANAGER   项目经理：进度管理 + 合同签订
    ROLE_ADMIN     管理后台（存 sys_adminrole，不占用 sys_role）

  两套下单入口（source_scene 字段区分）：
    1 按方案下单：先选方案 → 方案底下有设计师 → 再约量房
    2 按设计师下单：先量房 → 与设计师直接沟通 → 再看方案下单
  ==================================================================
*/

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 0. 幂等加列工具：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，
--    用存储过程查 information_schema 兜底，重复执行本文件不会报 1060。
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `add_column_if_absent`;
DELIMITER //
CREATE PROCEDURE `add_column_if_absent`(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_ddl VARCHAR(1000))
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

-- ------------------------------------------------------------
-- 1. 装修方案：绑定设计师、设计费、是否支持先量房
--    按方案查找时，方案详情页据此展示「本案设计师」并可发起沟通/量房
-- ------------------------------------------------------------
CALL add_column_if_absent('decoration_plan', 'designer_id',
  '`designer_id` bigint NULL COMMENT ''方案所属设计师（sys_user.id，role_code=ROLE_DESIGNER）'' , ADD INDEX `idx_plan_designer`(`designer_id` ASC)');
CALL add_column_if_absent('decoration_plan', 'design_fee',
  '`design_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT ''设计费（元/平米），效果图与平面设计图单独计费''');
CALL add_column_if_absent('decoration_plan', 'measure_first',
  '`measure_first` tinyint NOT NULL DEFAULT 0 COMMENT ''是否支持先量房后选方案 0否1是''');

-- ------------------------------------------------------------
-- 2. 订单：设计师/项目经理、付款方式、量房状态、下单入口
-- ------------------------------------------------------------
CALL add_column_if_absent('decoration_order', 'designer_id',
  '`designer_id` bigint NULL COMMENT ''承接该订单的设计师 sys_user.id'' , ADD INDEX `idx_order_designer`(`designer_id` ASC)');
CALL add_column_if_absent('decoration_order', 'manager_id',
  '`manager_id` bigint NULL COMMENT ''负责该订单的项目经理 sys_user.id（管进度、签合同）'' , ADD INDEX `idx_order_manager`(`manager_id` ASC)');
CALL add_column_if_absent('decoration_order', 'payment_type',
  '`payment_type` tinyint NULL DEFAULT NULL COMMENT ''付款方式 1定金 2首付 3期付 4尾付 5全款''');
CALL add_column_if_absent('decoration_order', 'paid_amount',
  '`paid_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT ''已付金额''');
CALL add_column_if_absent('decoration_order', 'payment_status',
  '`payment_status` tinyint NOT NULL DEFAULT 0 COMMENT ''付款进度 0未付款1已付定金2已付首付3已付期款4已付尾款5已结清''');
CALL add_column_if_absent('decoration_order', 'need_measure',
  '`need_measure` tinyint NOT NULL DEFAULT 0 COMMENT ''是否需要上门量房 0否1是''');
CALL add_column_if_absent('decoration_order', 'measure_status',
  '`measure_status` tinyint NOT NULL DEFAULT 0 COMMENT ''量房状态 0未安排1已预约2已量房3已取消''');
CALL add_column_if_absent('decoration_order', 'source_scene',
  '`source_scene` tinyint NOT NULL DEFAULT 1 COMMENT ''下单入口 1按方案(先选方案后量房) 2按设计师(先量房后选方案)''');

DROP PROCEDURE IF EXISTS `add_column_if_absent`;

-- ------------------------------------------------------------
-- 3. 设计师档案：囊括「装修方案提供商」的展示信息
--    is_accept_measure=1 表示愿意接受「先量房后选方案」
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `designer_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '设计师账号 sys_user.id（角色 ROLE_DESIGNER）',
  `real_name` varchar(50) NOT NULL COMMENT '设计师姓名',
  `avatar` varchar(255) NULL DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) NULL DEFAULT NULL COMMENT '联系电话',
  `city` varchar(50) NULL DEFAULT NULL COMMENT '服务城市',
  `style_tags` varchar(255) NULL DEFAULT NULL COMMENT '擅长风格，逗号分隔',
  `years_exp` int NOT NULL DEFAULT 0 COMMENT '从业年限',
  `design_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '设计费（元/平米）',
  `rating` decimal(2, 1) NOT NULL DEFAULT 5.0 COMMENT '评分',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '成交订单数',
  `is_accept_measure` tinyint NOT NULL DEFAULT 1 COMMENT '是否支持先量房后选方案 0否1是',
  `intro` text NULL COMMENT '设计理念/简介',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0停用1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_designer_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_designer_profile_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计师档案表' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 4. 设计图纸：效果图 + 平面设计图（设计师产出）
--    plan_id 可空：按设计师下单、尚未选方案时也能先出图
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `design_drawing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '所属方案ID（可空，按设计师下单时先出图）',
  `designer_id` bigint NOT NULL COMMENT '设计师 sys_user.id',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（可空）',
  `drawing_type` tinyint NOT NULL COMMENT '图纸类型 1效果图 2平面设计图 3施工图',
  `title` varchar(100) NOT NULL COMMENT '图纸名称',
  `room` varchar(50) NULL DEFAULT NULL COMMENT '空间/房间，如客厅、主卧',
  `image_url` varchar(255) NOT NULL COMMENT '图片地址',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0隐藏1展示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_drawing_plan`(`plan_id` ASC) USING BTREE,
  INDEX `idx_drawing_designer`(`designer_id` ASC) USING BTREE,
  INDEX `idx_drawing_order`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_drawing_plan` FOREIGN KEY (`plan_id`) REFERENCES `decoration_plan` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_drawing_designer` FOREIGN KEY (`designer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_drawing_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计图纸表（效果图/平面设计图）' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 5. 装修合同：由项目经理签订
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `decoration_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `contract_no` varchar(50) NOT NULL COMMENT '合同编号',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `user_id` bigint NOT NULL COMMENT '签约用户（业主）',
  `designer_id` bigint NULL DEFAULT NULL COMMENT '方案设计师',
  `manager_id` bigint NOT NULL COMMENT '签订该合同的项目经理',
  `contract_amount` decimal(10, 2) NOT NULL COMMENT '合同金额',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签订状态 0待签1已签2已作废',
  `sign_date` datetime NULL DEFAULT NULL COMMENT '签订时间',
  `file_url` varchar(255) NULL DEFAULT NULL COMMENT '合同附件地址',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no`(`contract_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_contract_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_contract_manager`(`manager_id` ASC) USING BTREE,
  CONSTRAINT `fk_contract_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_contract_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_contract_designer` FOREIGN KEY (`designer_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_contract_manager` FOREIGN KEY (`manager_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '装修合同表（项目经理签订）' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 6. 订单付款计划/记录：定金、首付、期付、尾付、全款
--    一个订单可以有多条（等额分期：定金10% / 首付30% / 期付30% / 尾付30%）
--    payment_type 与 decoration_order.payment_type 取值一致
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `payment_type` tinyint NOT NULL COMMENT '款项类型 1定金 2首付 3期付 4尾付 5全款',
  `amount` decimal(10, 2) NOT NULL COMMENT '本期金额',
  `ratio` decimal(5, 2) NULL DEFAULT NULL COMMENT '占总金额比例(%)',
  `due_date` date NULL DEFAULT NULL COMMENT '应付日期',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态 0待支付1已支付2已退款',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `trade_no` varchar(64) NULL DEFAULT NULL COMMENT '支付流水号',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_payment_type`(`order_id` ASC, `payment_type` ASC) USING BTREE,
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单付款计划表（定金/首付/期付/尾付/全款）' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 7. 量房记录：两种入口都能落在同一张表
--    scene=1 先选方案后量房（plan_id 必有）
--    scene=2 先量房后选方案（plan_id 为空，量房后再推荐方案）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `measure_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `measure_no` varchar(50) NOT NULL COMMENT '量房单号',
  `user_id` bigint NOT NULL COMMENT '客户 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '上门设计师 sys_user.id',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '关联方案ID（先选方案后量房时必填）',
  `scene` tinyint NOT NULL DEFAULT 1 COMMENT '入口 1按方案(先选方案后量房) 2按设计师(先量房后选方案)',
  `address` varchar(255) NOT NULL COMMENT '量房地址',
  `house_type` varchar(50) NULL DEFAULT NULL COMMENT '户型，如三室两厅',
  `area` decimal(8, 2) NULL DEFAULT NULL COMMENT '实测面积(平米)',
  `expect_date` datetime NULL DEFAULT NULL COMMENT '期望上门时间',
  `measure_date` datetime NULL DEFAULT NULL COMMENT '实际量房时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0待安排1已预约2已量房3已取消',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_measure_no`(`measure_no` ASC) USING BTREE,
  INDEX `idx_measure_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_measure_designer`(`designer_id` ASC) USING BTREE,
  INDEX `idx_measure_plan`(`plan_id` ASC) USING BTREE,
  CONSTRAINT `fk_measure_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_measure_designer` FOREIGN KEY (`designer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_measure_plan` FOREIGN KEY (`plan_id`) REFERENCES `decoration_plan` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '量房记录表' ROW_FORMAT = DYNAMIC;

-- ------------------------------------------------------------
-- 8. 设计师-客户会话（按设计师查找 → 直接对话）
--    plan_id 可空：还没选方案时也能先聊
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '客户 sys_user.id',
  `designer_id` bigint NOT NULL COMMENT '设计师 sys_user.id',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '围绕的方案ID（可空）',
  `last_message` varchar(255) NULL DEFAULT NULL COMMENT '最后一条消息摘要',
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

CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者 sys_user.id',
  `sender_role` tinyint NOT NULL COMMENT '发送者身份 1客户 2设计师',
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型 1文字 2图片 3方案卡片',
  `content` text NULL COMMENT '文字内容',
  `image_url` varchar(255) NULL DEFAULT NULL COMMENT '图片地址',
  `target_id` bigint NULL DEFAULT NULL COMMENT '卡片关联对象ID（msg_type=3 时为方案ID）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '对方是否已读 0未读1已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删1已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_conversation`(`conversation_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_msg_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设计师客户消息表' ROW_FORMAT = DYNAMIC;

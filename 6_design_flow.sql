/*
 设计服务全流程增量脚本（幂等，可重复执行）

 重要：新库建库请直接执行 3.sql（本 6 张表已并入其中），无需再执行本脚本。
       本脚本仅用于「已有旧库」的增量升级（IF NOT EXISTS，不会改动旧表）。

 覆盖业务流程：
   业主在官网选设计师 → 提交设计需求
   → 设计师免费量房 → 出平面设计图 → 业主确认
   → 签定金 → 设计师出效果图 → 出报价单 → 业主确认报价
   → 项目经理签合同（生成施工阶段 + 分期付款节点）
   → 按阶段施工（拆墙/开槽/水电/贴瓷砖/做吊顶/刮腻子/全屋定制/保洁/软装）
   → 每阶段记录计划与实际起止时间、是否按时、业主验收、返工天数
   → 贴瓷砖完成可付一期、全屋定制完成可付二期、保洁完成付尾款

 说明：
   1. 本脚本只新增表，不改动旧库中已存在的表，现有接口不受影响。
   2. 装修方式 decoration_mode：1 硬装、2 软装、3 硬装+软装整装。
   3. 阶段类型 stage_type：1 硬装、2 软装。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 设计服务单（全流程主表）
--   status 状态机：
--     0 待设计师接单     1 待量房(已接单)    2 已量房(待出平面图)
--     3 平面图待业主确认 4 平面图已确认(待付定金) 5 已付定金(待出效果图)
--     6 效果图已出(待报价) 7 已报价(待业主确认)  8 报价已确认(待签合同)
--     9 已签合同(待开工)  10 施工中           11 已完工(待付尾款)
--     12 已完成           13 已取消
-- ----------------------------
CREATE TABLE IF NOT EXISTS `design_service_order`  (
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
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态，见脚本头部状态机',
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
-- 报价单（硬装/软装金额分开，便于业主选择装修方式）
--   status：0 草稿  1 待业主确认  2 已确认  3 已作废
-- ----------------------------
CREATE TABLE IF NOT EXISTS `design_quote`  (
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
CREATE TABLE IF NOT EXISTS `design_quote_item`  (
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
-- 装修合同（新流程专用）
--   为什么不复用 decoration_contract：那张表通过 order_id 强绑 decoration_order
--   （购买方案/材料的商城订单），而本流程以 design_service_order 为主线，
--   强绑会让「设计服务」在旧订单表里凭空多出一条无意义订单。
--   sign_status：0 待签  1 已签  2 已作废
-- ----------------------------
CREATE TABLE IF NOT EXISTS `design_contract`  (
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
--   stage_code 约定：
--     硬装 demolition 拆墙 / slotting 开槽 / plumbing 水电改造 / tiling 贴瓷砖
--          ceiling 做吊顶 / putty 刮腻子 / custom 全屋定制 / cleaning 保洁
--     软装 furniture 家具进场 / curtain 窗帘布艺 / lighting 灯具安装 / decor 软装饰品
-- ----------------------------
CREATE TABLE IF NOT EXISTS `construction_stage`  (
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
CREATE TABLE IF NOT EXISTS `construction_payment_node`  (
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

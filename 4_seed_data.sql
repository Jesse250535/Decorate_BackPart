/*
  4_seed_data.sql —— 结构（3.sql）之后的种子数据
  ==================================================================
  执行顺序：
    1) 3.sql   建全部 21 张表（design_contract 已含业主签约字段）
    2) 本文件  写入种子数据（只在空库执行一次，主键固定）
               文件末尾附带 design_contract 签约字段兜底 DDL，
               即原 7_contract_sign.sql，已合并进来，不必再单独执行

  注：material_stock_record（材料出入库流水）已从结构里移除，
      本文件不再写入该表数据，材料库存只体现在 decoration_material.stock。

  角色收敛（只保留 3 类 + 管理后台）：
    1 普通用户 ROLE_USER      逛商城、下单、与设计师沟通、申请量房
    2 设计师   ROLE_DESIGNER  上传方案、接受咨询、上门量房、审核方案
    3 项目经理 ROLE_MANAGER   施工阶段管理、签订合同
    管理后台：ROLE_ADMIN(id=6, 权限 *)

  代码约束（不能违反）：
    · 权限判断依据是 role_code 不是 id，角色 id 可按 1/2/3 排列
    · sys_notice.create_user_id、decoration_plan.create_user_id 等外键都指向 sys_user
    · sys_user.user_type 受 CHECK 限制在 1~5，本脚本统一写 1
    · 当前 PasswordEncoder 为明文比对，密码直接写 123456
    · 图片全部指向 uploads/2026/10/27、10/28 里已存在的真实文件

  账号一览（密码都是 123456）：
    test01 / test02          普通用户
    designer01 / designer02  设计师
    manager01                项目经理
    admin                    管理后台
  ==================================================================
*/

-- 目标库（与 3.sql 保持一致，便于单独执行本文件）
USE `decoration_company_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 角色：前台（普通用户/设计师/项目经理）+ 后台（管理员）
--    sys_adminrole 已合并进 sys_role，管理员角色沿用原 id=6
-- ============================================================
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `permissions`, `remark`) VALUES
(1, '普通用户', 'ROLE_USER',
 'plan:view,material:view,comment:write,order:operate,chat:send,measure:apply',
 '前台注册客户：浏览方案/材料、下单、与设计师沟通、申请量房'),
(2, '设计师', 'ROLE_DESIGNER',
 'plan:view,plan:upload,plan:audit,drawing:manage,chat:reply,measure:manage,order:view',
 '囊括原装修方案提供商职能：上传方案、出效果图与平面设计图、接单量房、审核方案'),
(3, '项目经理', 'ROLE_MANAGER',
 'progress:manage,progress:audit,order:view,contract:manage,contract:sign',
 '负责施工进度管理（上报审核）与装修合同签订'),
(6, '管理员', 'ROLE_ADMIN', '*', '系统最高权限管理员，可管理用户/方案/材料/公告/订单');

-- ============================================================
-- 2. 用户：前台用户 is_admin=0，后台管理员 is_admin=1
--    sys_admin 已合并进 sys_user，管理员沿用原 role_id=6（ROLE_ADMIN）
-- ============================================================
INSERT INTO `sys_user`
(`id`, `username`, `phone`, `email`, `password`, `nickname`, `gender`, `user_type`, `user_type_chosen`, `role_id`, `is_admin`, `profile`) VALUES
(1, 'test01',     '13800000001', 'test01@example.com',     '123456', '小张',        1, 1, 0, 1, 0, '普通客户，喜欢现代简约风，已量房'),
(2, 'test02',     '13800000002', 'test02@example.com',     '123456', '小李',        2, 1, 1, 1, 0, '普通客户，正在对比材料价格'),
(3, 'designer01', '13800000003', 'designer01@example.com', '123456', '设计师林一',  1, 1, 1, 2, 0, '8 年经验，擅长现代简约与北欧风，可上门量房'),
(4, 'designer02', '13800000004', 'designer02@example.com', '123456', '设计师苏岑',  2, 1, 1, 2, 0, '6 年经验，擅长新中式与轻奢，支持先量房后选方案'),
(5, 'manager01',  '13800000005', 'manager01@example.com',  '123456', '项目经理老赵', 1, 1, 1, 3, 0, '负责施工进度管理与合同签订'),
(6, 'admin',      '13900000001', NULL,                     '123456', '超级管理员',   1, 1, 1, 6, 1, '平台最高权限，负责内容审核与运营');

-- ============================================================
-- 3. 供应商（材料表外键依赖；仅作为供货档案，不对应登录角色）
-- ============================================================
INSERT INTO `supplier` (`id`, `supplier_name`, `contact_person`, `contact_phone`, `address`, `remark`, `status`) VALUES
(1, '东鹏瓷砖供应有限公司', '陈经理', '020-88880001', '广东省佛山市禅城区南庄镇工业大道1号', '主营瓷砖、地砖', 1),
(2, '立邦涂料华南代理',     '刘经理', '020-88880002', '广东省广州市天河区科韵路12号',       '主营环保内墙涂料', 1),
(3, '圣象地板广州仓储中心', '黄经理', '020-88880003', '广东省广州市白云区太和镇仓储路8号',  '主营实木复合地板', 1);

-- ============================================================
-- 4. 分类（1~3 方案分类，4~5 材料分类）
-- ============================================================
INSERT INTO `decoration_category` (`id`, `category_name`, `category_type`, `parent_id`, `sort`, `is_hot`) VALUES
(1, '现代简约', 1, 0, 1, 1),
(2, '北欧风格', 1, 0, 2, 1),
(3, '新中式',   1, 0, 3, 0),
(4, '瓷砖石材', 2, 0, 1, 1),
(5, '涂料壁纸', 2, 0, 2, 0);

-- ============================================================
-- 4. 装修方案（create_user_id 为方案所属设计师账号）
--    首页/商城只展示 audit_status=1 且 status=1
-- ============================================================
INSERT INTO `decoration_plan`
(`id`, `plan_name`, `cover_image`, `detail_images`, `price`, `area_min`, `area_max`, `style`, `description`,
 `category_id`, `create_user_id`, `audit_status`,
 `sales_count`, `view_count`, `collect_count`, `status`) VALUES
(1, '现代简约二居室整装方案', '/api/uploads/2026/10/27/modern01.png',
 '/api/uploads/2026/10/27/modern01.png,/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png',
 88000.00, 70, 90, '现代简约', '白色与浅木色为主调，客餐厅一体，适合 70-90㎡ 两居，可先选方案后约量房',
 1, 3, 1, 15, 320, 45, 1),
(2, '北欧风格三居室整装方案', '/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png',
 '/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png,/api/uploads/2026/10/27/xdjy.jpg',
 128000.00, 90, 120, '北欧风格', '自然原木配清新绿植，主卧带独立衣帽间，适合 90-120㎡ 三居',
 2, 4, 1, 10, 260, 38, 1),
(3, '新中式小户型改造方案', '/api/uploads/2026/10/27/franch01.jpg',
 '/api/uploads/2026/10/27/franch01.jpg,/api/uploads/2026/10/27/cz.webp',
 58000.00, 40, 60, '新中式', '针对 40-60㎡ 小户型优化收纳与动线，屏风与木格栅做半开放分区',
 3, 3, 1, 20, 410, 52, 1);

-- ============================================================
-- 4. 装修材料（category_id 指向 category_type=2；无供应商角色账号，create_user_id 置空）
-- ============================================================
INSERT INTO `decoration_material`
(`id`, `material_name`, `cover_image`, `price`, `spec_long`, `spec_width`, `spec_height`, `unit`, `stock`,
 `description`, `category_id`, `supplier_id`, `create_user_id`, `audit_status`, `sales_count`, `status`) VALUES
(1, '通体大理石纹瓷砖',   '/api/uploads/2026/10/27/floor.webp', 89.00,  800.00, 800.00, 10.00, '片',     500,
 '800x800mm 通体大理石纹瓷砖，防滑耐磨',          4, 1, NULL, 1, 120, 1),
(2, '环保净味内墙乳胶漆', '/api/uploads/2026/10/27/rjq.webp',   260.00, 300.00, 200.00, 250.00, '桶',    300,
 '18L 环保净味乳胶漆，遮盖力强',                  5, 2, NULL, 1, 210, 1),
(3, '实木复合地板',       '/api/uploads/2026/10/27/cz.webp',    168.00, 900.00, 125.00, 15.00, '平方米', 800,
 '实木复合地板，脚感舒适，环保耐磨',              4, 3, NULL, 1,  95, 1);

-- ============================================================
-- 3. 首页配置（config_type=1 轮播，2 精选案例）
-- ============================================================
INSERT INTO `sys_home_config` (`id`, `config_type`, `image_url`, `jump_link`, `sort`, `status`) VALUES
(1, 1, '/api/uploads/2026/10/27/modern01.png', '/shop', 1, 1),
(2, 1, '/api/uploads/2026/10/27/franch01.jpg', '/shop', 2, 1),
(3, 1, '/api/uploads/2026/10/27/xdjy.jpg', '/shop', 3, 1),
(4, 1, '/api/uploads/2026/10/27/763073098b0a465c81fcb1ad4cd48d35.jpg', '/shop', 4, 1),
(5, 2, '/api/uploads/2026/10/27/f90c33bad58543cf9f7d8c7cd1028d09.jpg', '/shop', 1, 1),
(6, 2, '/api/uploads/2026/10/28/a6ad452b11ef4b7ea7c3aec06e2ce8b6.jpg', '/shop', 2, 1);

-- ============================================================
-- 4. 系统公告（create_user_id → sys_user.id，用项目经理账号发布）
-- ============================================================
INSERT INTO `sys_notice` (`id`, `title`, `content`, `show_position`, `publish_time`, `expire_time`, `status`, `create_user_id`) VALUES
(1, '平台系统升级公告',   '平台将于今晚 23:00-24:00 升级，期间部分功能暂停，请提前保存操作。', 1, '2026-10-24 09:00:00', NULL, 1, 5),
(2, '秋季装修优惠活动',   '10 月下单装修方案享 9 折，材料满 2000 减 200，活动名额有限。',       2, '2026-10-25 10:30:00', NULL, 1, 5),
(3, '量房与合同流程说明', '按方案下单可先约设计师上门量房；按设计师下单可先量房再选方案，合同由项目经理统一签订。', 3, '2026-10-26 08:30:00', NULL, 1, 5);

-- ============================================================
-- 3. 访问统计（stat_date 唯一）
-- ============================================================
INSERT INTO `sys_visit_stat`
(`stat_date`, `pv`, `uv`, `new_user_count`, `create_plan_count`, `create_material_count`, `finish_order_count`) VALUES
('2026-10-26', 1200, 300, 2, 1, 1, 0),
('2026-10-27', 1500, 360, 3, 2, 1, 1),
('2026-10-28',  900, 240, 1, 1, 0, 0);

-- ============================================================
-- 4. 订单（payment_type 1定金2首付3期付4尾付5全款，材料订单默认全款；
--     tail_amount 尾款金额、tail_items 尾款施工项目，由项目经理购买后计入尾款）
-- ============================================================
INSERT INTO `decoration_order`
(`id`, `order_no`, `user_id`, `order_type`, `target_id`, `total_amount`, `order_status`, `contract_status`, `refund_status`,
 `payment_type`, `amount`, `ratio`, `due_date`, `pay_status`, `trade_no`, `tail_amount`, `tail_items`,
 `remark`, `pay_time`, `contract_time`) VALUES
(1, 'DO202610280001', 1, 1, 1,  88000.00, 1, 1, 0,
 1,  8800.00,  10.00, '2026-10-28', 1, 'PAY202610280001', 26400.00, '拆墙,开槽,水电,贴瓷砖,做吊顶,刮腻子,全屋定制',
 '先选了现代简约方案，已量房，定金已付', '2026-10-28 10:20:00', '2026-10-28 16:00:00'),
(2, 'DO202610280002', 2, 2, 1,    890.00, 1, 0, 0,
 5,   890.00, 100.00, '2026-10-28', 1, 'PAY202610280002', 0.00, NULL,
 '购买瓷砖 10 片，材料订单全款支付', '2026-10-28 14:35:00', NULL),
(3, 'DO202610290003', 1, 1, 2, 128000.00, 0, 0, 0,
 1, 12800.00,  10.00, '2026-10-31', 0, NULL, 38400.00, '拆墙,开槽,水电,贴瓷砖,做吊顶,刮腻子,全屋定制',
 '还没定方案，先约苏设计师量房，定金待付', NULL, NULL);

-- ============================================================
-- 3. 设计师-客户会话与消息（按设计师查找 → 直接对话）
-- ============================================================
INSERT INTO `chat_conversation`
(`id`, `user_id`, `designer_id`, `plan_id`, `last_message`, `last_time`, `user_unread`, `designer_unread`, `status`) VALUES
(1, 1, 3, 1,    '效果图和平面图都传到方案里了，你先看下客厅那版', '2026-10-28 17:20:00', 1, 0, 1),
(2, 1, 4, NULL, '好的，周六上午十点上门量房，量完再给你推荐方案', '2026-10-29 09:15:00', 1, 0, 1);

INSERT INTO `chat_message`
(`id`, `conversation_id`, `sender_id`, `sender_role`, `msg_type`, `content`, `image_url`, `target_id`, `is_read`, `create_time`) VALUES
(1, 1, 1, 1, 1, '你好，现代简约二居室这套 88000 是含主材的吗？', NULL, NULL, 1, '2026-10-27 09:35:00'),
(2, 1, 3, 2, 1, '含主材和施工，设计费 80 元/㎡ 另计，方案里已附效果图和平面布置图。', NULL, NULL, 1, '2026-10-27 10:10:00'),
(3, 1, 3, 2, 3, '给你推荐这套方案，可以先看效果图再约量房', NULL, 1, 1, '2026-10-28 17:20:00'),
(4, 2, 1, 1, 1, '我还没选方案，可以先上门量房吗？', NULL, NULL, 1, '2026-10-29 09:05:00'),
(5, 2, 4, 2, 1, '可以的，量房免费，量完按户型和预算给你推荐 2-3 套', NULL, NULL, 1, '2026-10-29 09:15:00');

-- ============================================================
-- 4. 评论 / 点赞收藏 / 意见反馈
--     target_type 1=方案 2=材料
-- ============================================================
INSERT INTO `decoration_comment`
(`id`, `user_id`, `target_type`, `target_id`, `parent_id`, `content`, `like_count`, `status`) VALUES
(1, 1, 1, 1, 0, '方案很详细，收纳设计很实用，效果图和平面图都很清楚。', 6, 0),
(2, 2, 2, 1, 0, '瓷砖质量不错，铺贴效果满意。', 3, 0);

INSERT INTO `user_like_collect` (`id`, `user_id`, `operate_type`, `target_type`, `target_id`) VALUES
(1, 1, 1, 1, 1),
(2, 1, 2, 1, 1),
(3, 1, 2, 1, 2);

INSERT INTO `user_feedback`
(`id`, `user_id`, `content`, `contact_info`, `handle_status`, `handle_remark`) VALUES
(1, 1, '希望能增加设计师上门量房的预约提醒。', '13800000001', 1, '已记录，下个版本加入短信提醒');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 结构兜底：业主线上签订合同（原 7_contract_sign.sql，已并入本文件）
--   3.sql 的 design_contract 建表已含这 4 列，这里先查 information_schema
--   再决定是否 ALTER：老库自动补齐，新库不会报 Duplicate column name。
--   sign_status：0 待签 1 已签 2 已作废
-- ============================================================

-- 1) 业主手写签名图片
SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `design_contract` ADD COLUMN `signature_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT ''业主签名图片地址'' AFTER `file_url`',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'design_contract' AND COLUMN_NAME = 'signature_url');
PREPARE ddl_stmt FROM @ddl;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

-- 2) 首付款金额（(合同金额 - 定金) × 15%）
SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `design_contract` ADD COLUMN `first_payment_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT ''首付款金额（(合同金额-定金)×15%）'' AFTER `signature_url`',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'design_contract' AND COLUMN_NAME = 'first_payment_amount');
PREPARE ddl_stmt FROM @ddl;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

-- 3) 首付款支付时间
SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `design_contract` ADD COLUMN `first_payment_time` datetime NULL DEFAULT NULL COMMENT ''首付款支付时间'' AFTER `first_payment_amount`',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'design_contract' AND COLUMN_NAME = 'first_payment_time');
PREPARE ddl_stmt FROM @ddl;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

-- 4) 合同回执（纸质合同拍照 / 扫描件）
SET @ddl = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `design_contract` ADD COLUMN `receipt_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT ''合同回执地址'' AFTER `first_payment_time`',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'design_contract' AND COLUMN_NAME = 'receipt_url');
PREPARE ddl_stmt FROM @ddl;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;


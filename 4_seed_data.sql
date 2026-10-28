/*
  4_seed_data.sql —— 只导入结构（3.sql）之后的种子数据
  ------------------------------------------------------------------
  用法：先执行 3.sql 建表，再执行本文件；只在空库执行一次（主键固定，重复执行会报主键冲突）
  原则：
    1. 按外键依赖顺序写入：角色 → 用户 → 供应商/分类 → 方案/材料 → 订单 → 进度/库存/评论
    2. sys_role 的 id 被后端 StaffRole.java / 前端 staff.ts 硬编码（4库管 5施工 6设计师 7项目经理 8财务），不能改
    3. sys_admin.user_type 是生成列（=role_id），插入时不能出现该字段
    4. sys_notice.create_user_id 外键指向 sys_user（不是 sys_admin）
    5. 当前 PasswordEncoder 是明文比对，所以密码直接写明文
  ------------------------------------------------------------------
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 前台角色（id 必须与代码一致）
-- ============================================================
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `permissions`, `remark`) VALUES
(1, '普通用户',     'ROLE_USER',          'plan:view,material:view,comment:write,order:operate', '前台普通注册用户'),
(2, '装修方案提供者', 'ROLE_PLAN_PROVIDER', 'plan:upload,plan:view,my:resource',                   '可上传装修方案'),
(3, '材料供应商',   'ROLE_SUPPLIER',      'material:upload,material:view,my:resource',           '可上传装修材料'),
(4, '库管人员',     'ROLE_STOCK',         'stock:operate,stock:view',                            '负责材料出入库'),
(5, '施工人员',     'ROLE_WORKER',        'progress:report,progress:view',                       '负责施工进度上报'),
(6, '设计师',       'ROLE_DESIGNER',      'plan:audit,plan:view',                                '负责方案审核'),
(7, '项目经理',     'ROLE_MANAGER',       'progress:audit,order:view,contract:manage',           '负责合同与进度审核'),
(8, '财务人员',     'ROLE_FINANCE_STAFF', 'order:view,stat:view,contract:amount',                '负责合同金额与对账');

-- ============================================================
-- 2. 后台管理员角色（/auth/admin/login 从 sys_adminrole 读权限）
-- ============================================================
INSERT INTO `sys_adminrole` (`id`, `role_name`, `role_code`, `permissions`, `remark`) VALUES
(6, '管理员',     'ROLE_ADMIN',     '*',                                        '系统最高权限管理员'),
(7, '运营管理员', 'ROLE_OPERATION', 'user:manage,material:audit,plan:audit',     '负责用户与内容运营'),
(8, '财务管理员', 'ROLE_FINANCE',   'order:manage,refund:audit,stat:view',       '负责订单与财务'),
(9, '客服管理员', 'ROLE_CS',        'feedback:handle,comment:manage,notice:manage', '负责反馈与评论处理');

-- ============================================================
-- 3. 前台用户（覆盖 1~8 号角色，密码统一 123456）
--    user_type 受 CHECK 约束限制为 1~5；user_type_chosen=1 表示不再弹首次选身份
-- ============================================================
INSERT INTO `sys_user`
(`id`, `username`, `phone`, `email`, `password`, `nickname`, `gender`, `user_type`, `user_type_chosen`, `role_id`, `profile`) VALUES
(1, 'test01',     '13800000001', 'test01@example.com',     '123456', '小张',       1, 1, 0, 1, '普通客户，喜欢现代简约风'),
(2, 'planprov01', '13800000002', 'planprov01@example.com', '123456', '方案商老李', 1, 2, 1, 2, '可上传装修方案'),
(3, 'supplier01', '13800000003', 'supplier01@example.com', '123456', '供应商老王', 1, 3, 1, 3, '经营建材供应'),
(4, 'stock01',    '13800000004', 'stock01@example.com',    '123456', '库管小刘',   2, 1, 1, 4, '负责材料出入库'),
(5, 'worker01',   '13800000005', 'worker01@example.com',   '123456', '施工小陈',   1, 1, 1, 5, '负责现场施工与进度上报'),
(6, 'designer01', '13800000006', 'designer01@example.com', '123456', '设计师小林', 2, 1, 1, 6, '负责方案审核'),
(7, 'manager01',  '13800000007', 'manager01@example.com',  '123456', '项目经理老赵', 1, 1, 1, 7, '负责合同与进度审核'),
(8, 'finance01',  '13800000008', 'finance01@example.com',  '123456', '财务小周',   2, 1, 1, 8, '负责合同金额与对账');

-- ============================================================
-- 4. 后台管理员（role_id → sys_adminrole.id；不写 user_type 生成列）
-- ============================================================
INSERT INTO `sys_admin` (`id`, `username`, `phone`, `password`, `nickname`, `gender`, `role_id`, `profile`) VALUES
(1, 'admin',     '13900000001', '123456', '超级管理员', 1, 6, '平台最高权限'),
(2, 'operation', '13900000002', '123456', '运营小助手', 2, 7, '负责用户与内容运营');

-- ============================================================
-- 5. 供应商（材料表外键依赖）
-- ============================================================
INSERT INTO `supplier` (`id`, `supplier_name`, `contact_person`, `contact_phone`, `address`, `remark`, `status`) VALUES
(1, '东鹏瓷砖供应有限公司',   '陈经理', '020-88880001', '广东省佛山市禅城区南庄镇工业大道1号', '主营瓷砖、地砖', 1),
(2, '立邦涂料华南代理',       '刘经理', '020-88880002', '广东省广州市天河区科韵路12号',       '主营环保内墙涂料', 1),
(3, '圣象地板广州仓储中心',   '黄经理', '020-88880003', '广东省广州市白云区太和镇仓储路8号',  '主营实木复合地板', 1);

-- ============================================================
-- 6. 分类（1~3 方案分类，4~5 材料分类；type 与页面下拉框联动）
-- ============================================================
INSERT INTO `decoration_category` (`id`, `category_name`, `category_type`, `parent_id`, `sort`, `is_hot`) VALUES
(1, '现代简约', 1, 0, 1, 1),
(2, '北欧风格', 1, 0, 2, 1),
(3, '新中式',   1, 0, 3, 0),
(4, '瓷砖石材', 2, 0, 1, 1),
(5, '涂料壁纸', 2, 0, 2, 0);

-- ============================================================
-- 7. 装修方案（首页/商城只展示 audit_status=1 且 status=1）
--    create_user_id 必须是 sys_user.id（外键），用方案商/设计师账号
--    图片用项目 uploads 目录里已有的真实文件
-- ============================================================
INSERT INTO `decoration_plan`
(`id`, `plan_name`, `cover_image`, `detail_images`, `price`, `area_min`, `area_max`, `style`, `description`,
 `category_id`, `create_user_id`, `audit_status`, `sales_count`, `view_count`, `collect_count`, `status`) VALUES
(1, '现代简约二居室整装方案', '/api/uploads/2026/10/27/modern01.png',
 '/api/uploads/2026/10/27/modern01.png,/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png',
 88000.00, 70, 90, '现代简约', '以白色与浅木色为主色调，打造简洁通透的二居室空间', 1, 2, 1, 15, 320, 45, 1),
(2, '北欧风格三居室整装方案', '/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png',
 '/api/uploads/2026/10/27/f305f5cf04014440a7ecdee25a831fe5.png',
 128000.00, 90, 120, '北欧风格', '主打自然原木与清新绿植，营造温馨舒适的居家氛围', 2, 6, 1, 10, 260, 38, 1),
(3, '新中式小户型改造方案', '/api/uploads/2026/10/27/franch01.jpg',
 '/api/uploads/2026/10/27/franch01.jpg',
 58000.00, 40, 60, '新中式', '针对小户型优化收纳与动线，提升空间利用率', 3, 2, 1, 20, 410, 52, 1);

-- ============================================================
-- 8. 装修材料（category_id 必须指向 category_type=2 的分类）
-- ============================================================
INSERT INTO `decoration_material`
(`id`, `material_name`, `cover_image`, `price`, `spec_long`, `spec_width`, `spec_height`, `unit`, `stock`,
 `description`, `category_id`, `supplier_id`, `create_user_id`, `audit_status`, `sales_count`, `status`) VALUES
(1, '通体大理石纹瓷砖', '/api/uploads/2026/10/27/floor.webp', 89.00, 800.00, 800.00, 10.00, '片', 500,
 '800x800mm 通体大理石纹瓷砖，防滑耐磨', 4, 1, 3, 1, 120, 1),
(2, '环保净味内墙乳胶漆', '/api/uploads/2026/10/27/rjq.webp', 260.00, 300.00, 200.00, 250.00, '桶', 300,
 '18L 环保净味乳胶漆，遮盖力强', 5, 2, 3, 1, 210, 1),
(3, '实木复合地板', '/api/uploads/2026/10/27/cz.webp', 168.00, 900.00, 125.00, 15.00, '平方米', 800,
 '实木复合地板，脚感舒适，环保耐磨', 4, 3, 3, 1, 95, 1);

-- ============================================================
-- 9. 首页配置（config_type=1 轮播图，有几条 status=1 就轮播几张；2 精选案例）
-- ============================================================
INSERT INTO `sys_home_config` (`id`, `config_type`, `image_url`, `jump_link`, `sort`, `status`) VALUES
(1, 1, '/api/uploads/2026/10/27/modern01.png', '/shop', 1, 1),
(2, 1, '/api/uploads/2026/10/27/franch01.jpg', '/shop', 2, 1),
(3, 1, '/api/uploads/2026/10/27/xdjy.jpg', '/shop', 3, 1),
(4, 1, '/api/uploads/2026/10/27/763073098b0a465c81fcb1ad4cd48d35.jpg', '/shop', 4, 1),
(5, 2, '/api/uploads/2026/10/27/f90c33bad58543cf9f7d8c7cd1028d09.jpg', '/shop', 1, 1),
(6, 2, '/api/uploads/2026/10/28/a6ad452b11ef4b7ea7c3aec06e2ce8b6.jpg', '/shop', 2, 1);

-- ============================================================
-- 10. 系统公告（create_user_id → sys_user.id，不是 sys_admin）
--     status=1 已发布；expire_time 给 NULL 避免一上线就"已过期"
-- ============================================================
INSERT INTO `sys_notice` (`id`, `title`, `content`, `show_position`, `publish_time`, `expire_time`, `status`, `create_user_id`) VALUES
(1, '平台系统升级公告', '平台将于今晚 23:00-24:00 升级，期间部分功能暂停，请提前保存操作。', 1, '2026-10-24 09:00:00', NULL, 1, 1),
(2, '秋季装修优惠活动', '10 月下单装修方案享 9 折，材料满 2000 减 200，活动名额有限。',       2, '2026-10-25 10:30:00', NULL, 1, 1),
(3, '材料审核规则调整', '自 11 月 1 日起，材料上架需同时提供质检报告与规格参数。',           3, '2026-10-26 08:30:00', NULL, 1, 1);

-- ============================================================
-- 11. 访问统计（stat_date 唯一，日期不要重复）
-- ============================================================
INSERT INTO `sys_visit_stat`
(`stat_date`, `pv`, `uv`, `new_user_count`, `create_plan_count`, `create_material_count`, `finish_order_count`) VALUES
('2026-10-26', 1200, 300, 2, 1, 1, 0),
('2026-10-27', 1500, 360, 3, 2, 1, 1),
('2026-10-28',  900, 240, 1, 1, 0, 0);

-- ============================================================
-- 12. 订单（user_id → sys_user.id；order_no 唯一）
-- ============================================================
INSERT INTO `decoration_order`
(`id`, `order_no`, `user_id`, `order_type`, `target_id`, `total_amount`, `order_status`, `contract_status`, `refund_status`, `remark`, `pay_time`) VALUES
(1, 'DO202610280001', 1, 1, 1,  88000.00, 1, 1, 0, '希望尽快安排施工', '2026-10-28 10:20:00'),
(2, 'DO202610280002', 1, 2, 1, 890.00,   2, 0, 0, '购买瓷砖 10 片',   '2026-10-28 14:35:00');

-- ============================================================
-- 13. 装修进度（order_id → decoration_order.id，report_user_id → 施工人员）
-- ============================================================
INSERT INTO `decoration_progress`
(`id`, `order_id`, `progress_status`, `status_desc`, `progress_images`, `report_user_id`, `audit_status`, `audit_user_id`, `audit_time`) VALUES
(1, 1, 2, '水电改造已完成，进入泥木阶段', '/api/uploads/2026/10/27/franch01.jpg', 5, 1, 7, '2026-10-28 11:00:00'),
(2, 2, 4, '材料已全部交付并验收完成',     '/api/uploads/2026/10/27/floor.webp',   5, 1, 7, '2026-10-28 15:00:00');

-- ============================================================
-- 14. 材料出入库（material_id → decoration_material.id，operate_user_id → 库管）
-- ============================================================
INSERT INTO `material_stock_record`
(`id`, `material_id`, `operate_type`, `change_num`, `before_stock`, `after_stock`, `operate_user_id`, `remark`) VALUES
(1, 1, 1,  500,   0, 500, 4, '采购入库通体大理石纹瓷砖 500 片'),
(2, 2, 2,  -20, 320, 300, 4, '订单出库环保乳胶漆 20 桶');

-- ============================================================
-- 15. 评论（user_id → sys_user.id；target_type 1方案 2材料）
-- ============================================================
INSERT INTO `decoration_comment`
(`id`, `user_id`, `target_type`, `target_id`, `parent_id`, `content`, `like_count`, `status`) VALUES
(1, 1, 1, 1, 0, '方案很详细，收纳设计很实用。', 6, 0),
(2, 1, 2, 1, 0, '瓷砖质量不错，铺贴效果满意。',   3, 0);

-- ============================================================
-- 16. 点赞收藏（唯一键：user_id + operate_type + target_type + target_id）
-- ============================================================
INSERT INTO `user_like_collect` (`id`, `user_id`, `operate_type`, `target_type`, `target_id`) VALUES
(1, 1, 1, 1, 1),
(2, 1, 2, 1, 1),
(3, 1, 2, 2, 1);

-- ============================================================
-- 17. 意见反馈（user_id → sys_user.id）
-- ============================================================
INSERT INTO `user_feedback` (`id`, `user_id`, `content`, `contact_info`, `handle_status`, `handle_remark`) VALUES
(1, 1, '希望增加订单导出功能。', 'test01@example.com', 0, NULL);

-- ============================================================
-- 18. 邮箱验证码：运行时由注册流程写入，不需要预置
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;

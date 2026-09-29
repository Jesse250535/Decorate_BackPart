/*
 Navicat Premium Data Transfer

 Source Server         : adminjesse
 Source Schema         : decoration_company_system

 说明：本文件为装饰公司管理系统各业务表补充的示例数据（元组），
       每张表新增 3 条记录，供开发/联调演示使用。
       依赖 1.sql 中已创建的表结构，请在导入 1.sql 之后再执行本文件。
 Date: 29/09/2026 10:44:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. Records of sys_adminrole（后台管理员角色权限表，新增 3 条）
-- ----------------------------
INSERT INTO `sys_adminrole` (`id`, `role_name`, `role_code`, `permissions`, `remark`) VALUES
(7, '运营管理员', 'ROLE_OPERATION', 'user:manage,material:audit,plan:audit', '负责用户与内容运营管理'),
(8, '财务管理员', 'ROLE_FINANCE', 'order:manage,refund:audit,stat:view', '负责订单与财务相关管理'),
(9, '客服管理员', 'ROLE_CS', 'feedback:handle,comment:manage,notice:manage', '负责用户反馈与评论处理');

-- ----------------------------
-- 2. Records of sys_role（前台角色权限表，新增 3 条）
-- ----------------------------
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `permissions`, `remark`) VALUES
(6, '设计师', 'ROLE_DESIGNER', 'plan:upload,plan:view,my:resource', '可上传装修方案设计'),
(7, '项目经理', 'ROLE_MANAGER', 'progress:audit,order:view,my:resource', '负责装修项目管理'),
(8, '财务人员', 'ROLE_FINANCE_STAFF', 'order:view,stat:view', '负责订单财务核对');

-- ----------------------------
-- 3. Records of sys_user（前台用户表，新增 3 条）
--    user_type 取值范围 1-5，受 CHECK 约束限制
-- ----------------------------
INSERT INTO `sys_user` (`id`, `username`, `phone`, `email`, `password`, `nickname`, `gender`, `user_type`, `role_id`, `profile`, `violation_count`, `status`) VALUES
(10, 'zhangsan', '13800000001', 'zhangsan@example.com', '123456', '张三', 1, 1, 1, '喜欢现代简约风格的装修', 0, 0),
(11, 'lisi', '13800000002', 'lisi@example.com', '123456', '李四', 2, 2, 2, '专注北欧风装修方案设计', 0, 0),
(12, 'wangwu', '13800000003', 'wangwu@example.com', '123456', '王五', 1, 3, 3, '经营建材供应业务', 0, 0);

-- ----------------------------
-- 4. Records of sys_admin（后台管理员表，新增 3 条）
--    user_type 为生成列（自动等于 role_id），插入时不指定
-- ----------------------------
INSERT INTO `sys_admin` (`id`, `username`, `phone`, `password`, `nickname`, `gender`, `role_id`, `profile`, `violation_count`, `status`) VALUES
(3, 'operation', '13900000001', '123456', '运营小助手', 1, 7, '负责平台用户与内容运营', 0, 0),
(4, 'finance', '13900000002', '123456', '财务小张', 2, 8, '负责平台订单与财务核对', 0, 0),
(5, 'service', '13900000003', '123456', '客服小李', 2, 9, '负责处理用户反馈与评论', 0, 0);

-- ----------------------------
-- 5. Records of supplier（材料供应商表，新增 3 条）
-- ----------------------------
INSERT INTO `supplier` (`id`, `supplier_name`, `contact_person`, `contact_phone`, `address`, `remark`, `status`) VALUES
(1, '东鹏瓷砖供应有限公司', '陈经理', '020-88880001', '广东省佛山市禅城区南庄镇工业大道1号', '主营瓷砖、地砖，供货稳定', 1),
(2, '立邦涂料华南代理', '刘经理', '020-88880002', '广东省广州市天河区科韵路12号', '主营环保内墙涂料', 1),
(3, '圣象地板广州仓储中心', '黄经理', '020-88880003', '广东省广州市白云区太和镇仓储路8号', '主营实木复合地板', 1);

-- ----------------------------
-- 6. Records of decoration_category（分类标签表，新增 3 条）
--    category_type 1装修方案分类 2装修材料分类
-- ----------------------------
INSERT INTO `decoration_category` (`id`, `category_name`, `category_type`, `parent_id`, `sort`, `is_hot`) VALUES
(1, '现代简约', 1, 0, 1, 1),
(2, '北欧风格', 1, 0, 2, 1),
(3, '瓷砖石材', 2, 0, 1, 1);

-- ----------------------------
-- 7. Records of decoration_material（装修材料表，新增 3 条）
-- ----------------------------
INSERT INTO `decoration_material` (`id`, `material_name`, `cover_image`, `price`, `spec_long`, `spec_width`, `spec_height`, `unit`, `stock`, `description`, `category_id`, `supplier_id`, `audit_status`, `sales_count`, `status`) VALUES
(1, '通体大理石纹瓷砖', 'https://example.com/material/tile01.jpg', 89.00, 800.00, 800.00, 10.00, '片', 500, '800x800mm通体大理石纹瓷砖，防滑耐磨', 3, 1, 1, 120, 1),
(2, '环保净味内墙乳胶漆', 'https://example.com/material/paint01.jpg', 260.00, 300.00, 200.00, 250.00, '桶', 300, '18L环保净味乳胶漆，遮盖力强', 3, 2, 1, 210, 1),
(3, '实木复合地板', 'https://example.com/material/floor01.jpg', 168.00, 900.00, 125.00, 15.00, '平方米', 800, '实木复合地板，脚感舒适，环保耐磨', 3, 3, 1, 95, 1);

-- ----------------------------
-- 8. Records of decoration_plan（装修方案表，新增 3 条）
-- ----------------------------
INSERT INTO `decoration_plan` (`id`, `plan_name`, `cover_image`, `detail_images`, `price`, `area_min`, `area_max`, `style`, `description`, `category_id`, `create_user_id`, `audit_status`, `sales_count`, `view_count`, `collect_count`, `status`) VALUES
(1, '现代简约二居室整装方案', 'https://example.com/plan/modern01.jpg', 'https://example.com/plan/modern01_1.jpg,https://example.com/plan/modern01_2.jpg', 88000.00, 70, 90, '现代简约', '以白色与浅木色为主色调，打造简洁通透的二居室空间', 1, 2, 1, 15, 320, 45, 1),
(2, '北欧风格三居室整装方案', 'https://example.com/plan/nordic01.jpg', 'https://example.com/plan/nordic01_1.jpg,https://example.com/plan/nordic01_2.jpg', 128000.00, 90, 120, '北欧风格', '主打自然原木与清新绿植，营造温馨舒适的居家氛围', 2, 11, 1, 10, 260, 38, 1),
(3, '现代简约小户型改造方案', 'https://example.com/plan/small01.jpg', 'https://example.com/plan/small01_1.jpg,https://example.com/plan/small01_2.jpg', 58000.00, 40, 60, '现代简约', '针对小户型优化收纳与动线，提升空间利用率', 1, 1, 1, 20, 410, 52, 1);

-- ----------------------------
-- 9. Records of decoration_order（订单主表，新增 3 条）
--    order_type 1购买装修方案2购买装修材料
-- ----------------------------
INSERT INTO `decoration_order` (`id`, `order_no`, `user_id`, `order_type`, `target_id`, `total_amount`, `order_status`, `contract_status`, `refund_status`, `remark`, `pay_time`) VALUES
(1, 'DO202609010001', 5, 1, 1, 88000.00, 1, 1, 0, '希望尽快安排施工', '2026-09-01 10:20:00'),
(2, 'DO202609020002', 7, 2, 1, 890.00, 2, 0, 0, '购买瓷砖10片', '2026-09-02 14:35:00'),
(3, 'DO202609030003', 10, 1, 2, 128000.00, 0, 0, 0, '待确认方案细节', NULL);

-- ----------------------------
-- 10. Records of decoration_progress（装修进度跟踪表，新增 3 条）
-- ----------------------------
INSERT INTO `decoration_progress` (`id`, `order_id`, `progress_status`, `status_desc`, `progress_images`, `report_user_id`) VALUES
(1, 1, 2, '水电改造已完成，进入泥木阶段', 'https://example.com/progress/1_1.jpg,https://example.com/progress/1_2.jpg', 9),
(2, 2, 4, '材料已全部交付并验收完成', 'https://example.com/progress/2_1.jpg', 6),
(3, 3, 1, '已完成现场测量，等待开工', 'https://example.com/progress/3_1.jpg', 4);

-- ----------------------------
-- 11. Records of material_stock_record（材料出入库记录表，新增 3 条）
--     operate_type 1采购入库2订单出库3库存盘点调整
-- ----------------------------
INSERT INTO `material_stock_record` (`id`, `material_id`, `operate_type`, `change_num`, `before_stock`, `after_stock`, `operate_user_id`, `remark`) VALUES
(1, 1, 1, 500, 0, 500, 6, '采购入库通体大理石纹瓷砖500片'),
(2, 2, 2, -20, 320, 300, 9, '订单出库环保乳胶漆20桶'),
(3, 3, 3, -10, 810, 800, 4, '库存盘点调整实木复合地板10平方米');

-- ----------------------------
-- 12. Records of email_verification_code（邮箱验证码表，新增 3 条）
-- ----------------------------
INSERT INTO `email_verification_code` (`id`, `email`, `code`, `scene`, `expired_at`, `used`) VALUES
(1, 'zhangsan@example.com', '483920', 'register', '2026-09-29 12:00:00', 1),
(2, 'lisi@example.com', '751204', 'register', '2026-09-29 12:05:00', 0),
(3, 'wangwu@example.com', '620517', 'register', '2026-09-29 12:10:00', 0);

-- ----------------------------
-- 13. Records of sys_home_config（首页配置表，新增 3 条）
--     config_type 1轮播图2精选成功案例图3首页推荐位
-- ----------------------------
INSERT INTO `sys_home_config` (`id`, `config_type`, `image_url`, `jump_link`, `sort`, `status`) VALUES
(1, 1, 'https://example.com/home/banner01.jpg', '/plan/list', 1, 1),
(2, 1, 'https://example.com/home/banner02.jpg', '/material/list', 2, 1),
(3, 2, 'https://example.com/home/case01.jpg', '/plan/1', 1, 1);

-- ----------------------------
-- 14. Records of sys_notice（系统公告表，新增 3 条）
--     show_position 1首页顶部2用户中心3后台首页
-- ----------------------------
INSERT INTO `sys_notice` (`id`, `title`, `content`, `show_position`, `publish_time`, `expire_time`, `status`, `create_user_id`) VALUES
(1, '平台系统升级公告', '平台将于今晚23点进行系统升级，期间部分功能暂停使用，请提前做好准备。', 1, '2026-09-20 09:00:00', '2026-10-20 09:00:00', 1, 1),
(2, '国庆装修优惠活动', '国庆期间下单装修方案享9折优惠，活动数量有限，先到先得。', 2, '2026-09-28 10:00:00', '2026-10-08 23:59:59', 1, 2),
(3, '后台运维通知', '请各管理员及时处理待审核的材料与方案申请，避免积压。', 3, '2026-09-29 08:30:00', NULL, 1, 3);

-- ----------------------------
-- 15. Records of sys_visit_stat（系统访问统计表，新增 3 条）
--     stat_date 唯一，需保证日期不重复
-- ----------------------------
INSERT INTO `sys_visit_stat` (`id`, `stat_date`, `pv`, `uv`, `new_user_count`, `create_plan_count`, `create_material_count`, `finish_order_count`) VALUES
(1, '2026-09-27', 1250, 320, 5, 2, 3, 1),
(2, '2026-09-28', 1430, 356, 3, 1, 2, 2),
(3, '2026-09-29', 980, 245, 4, 3, 1, 1);

-- ----------------------------
-- 16. Records of user_feedback（用户意见反馈表，新增 3 条）
-- ----------------------------
INSERT INTO `user_feedback` (`id`, `user_id`, `content`, `contact_info`, `handle_status`, `handle_remark`) VALUES
(1, 5, '希望增加装修材料的价格对比功能', '13800000000', 1, '已记录，后续版本增加该功能'),
(2, 7, '装修方案详情页图片加载较慢', 'lisi@example.com', 0, NULL),
(3, 10, '建议支持订单在线签订合同', '13800000001', 1, '已反馈至产品团队评估');

-- ----------------------------
-- 17. Records of user_like_collect（用户点赞收藏表，新增 3 条）
--     operate_type 1点赞2收藏；target_type 1装修方案2装修材料3评论
--     唯一索引 uk_user_operate(user_id, operate_type, target_type, target_id) 需保证组合不重复
-- ----------------------------
INSERT INTO `user_like_collect` (`id`, `user_id`, `operate_type`, `target_type`, `target_id`) VALUES
(1, 5, 1, 1, 1),
(2, 7, 2, 1, 2),
(3, 10, 2, 2, 1);

-- ----------------------------
-- 18. Records of decoration_comment（评论表，新增 3 条）
--     target_type 1装修方案2装修材料；parent_id 0 代表主评论
-- ----------------------------
INSERT INTO `decoration_comment` (`id`, `user_id`, `target_type`, `target_id`, `parent_id`, `content`, `images`, `like_count`, `status`) VALUES
(1, 5, 1, 1, 0, '这个现代简约方案很不错，空间利用得很合理！', NULL, 12, 0),
(2, 7, 2, 1, 0, '瓷砖质量很好，铺贴效果和图片一致。', 'https://example.com/comment/2_1.jpg', 6, 0),
(3, 10, 1, 2, 0, '北欧风格很适合小家庭，收藏了！', NULL, 9, 0);

SET FOREIGN_KEY_CHECKS = 1;

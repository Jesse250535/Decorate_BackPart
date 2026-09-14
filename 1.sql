-- 创建数据库并设置字符集
CREATE DATABASE IF NOT EXISTS decoration_company_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE decoration_company_system;

-- 1. 角色权限表 sys_role
CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `role_name` varchar(50) NOT NULL COMMENT '角色名称',
                            `role_code` varchar(50) NOT NULL COMMENT '角色标识',
                            `permissions` text COMMENT '权限标识集合',
                            `remark` varchar(255) DEFAULT NULL COMMENT '角色备注',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限表';

-- 2. 用户主表 sys_user
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `password` varchar(100) NOT NULL COMMENT '加密密码',
                            `avatar` varchar(255) DEFAULT NULL COMMENT '头像地址',
                            `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                            `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别 0未知1男2女',
                            `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户身份类型 1客户2装修爱好者3购房者4家具爱好者',
                            `role_id` bigint NOT NULL COMMENT '关联角色ID',
                            `profile` text COMMENT '个人简介',
                            `violation_count` int NOT NULL DEFAULT '0' COMMENT '违规次数',
                            `status` tinyint NOT NULL DEFAULT '0' COMMENT '账号状态 0正常1封禁',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            UNIQUE KEY `uk_phone` (`phone`),
                            KEY `idx_role_id` (`role_id`),
                            CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- 3. 分类标签表 decoration_category
CREATE TABLE `decoration_category` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `category_name` varchar(50) NOT NULL COMMENT '分类名称',
                                       `category_type` tinyint NOT NULL COMMENT '分类类型 1装修方案分类2装修材料分类',
                                       `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID',
                                       `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
                                       `is_hot` tinyint NOT NULL DEFAULT '0' COMMENT '是否热门标签 0否1是',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类标签表';

-- 4. 供应商表 supplier
CREATE TABLE `supplier` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `supplier_name` varchar(100) NOT NULL COMMENT '供应商名称',
                            `contact_person` varchar(50) NOT NULL COMMENT '联系人',
                            `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
                            `address` varchar(255) DEFAULT NULL COMMENT '供应商地址',
                            `remark` text COMMENT '备注信息',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '合作状态 0停用1正常合作',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料供应商表';

-- 5. 装修方案表 decoration_plan
CREATE TABLE `decoration_plan` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `plan_name` varchar(100) NOT NULL COMMENT '方案名称',
                                   `cover_image` varchar(255) NOT NULL COMMENT '封面图',
                                   `detail_images` text COMMENT '实景案例图集，多图逗号分隔',
                                   `price` decimal(10,2) NOT NULL COMMENT '方案价格',
                                   `area_min` int DEFAULT NULL COMMENT '适用最小面积(平米)',
                                   `area_max` int DEFAULT NULL COMMENT '适用最大面积(平米)',
                                   `style` varchar(50) DEFAULT NULL COMMENT '风格描述',
                                   `description` text NOT NULL COMMENT '方案详细描述',
                                   `category_id` bigint NOT NULL COMMENT '关联分类ID',
                                   `create_user_id` bigint NOT NULL COMMENT '上传者用户ID',
                                   `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核1通过2驳回',
                                   `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
                                   `sales_count` int NOT NULL DEFAULT '0' COMMENT '购买销量',
                                   `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览次数',
                                   `collect_count` int NOT NULL DEFAULT '0' COMMENT '收藏次数',
                                   `status` tinyint NOT NULL DEFAULT '0' COMMENT '上架状态 0下架1上架',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_plan_name` (`plan_name`),
                                   KEY `idx_category_id` (`category_id`),
                                   KEY `idx_create_user` (`create_user_id`),
                                   KEY `idx_audit_status` (`audit_status`),
                                   CONSTRAINT `fk_plan_category` FOREIGN KEY (`category_id`) REFERENCES `decoration_category` (`id`),
                                   CONSTRAINT `fk_plan_user` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修方案表';

-- 6. 装修材料表 decoration_material
CREATE TABLE `decoration_material` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `material_name` varchar(100) NOT NULL COMMENT '材料名称',
                                       `cover_image` varchar(255) NOT NULL COMMENT '外观图',
                                       `price` decimal(10,2) NOT NULL COMMENT '单价',
                                       `spec_long` decimal(10,2) DEFAULT NULL COMMENT '长度参数(毫米)',
                                       `spec_width` decimal(10,2) DEFAULT NULL COMMENT '宽度参数(毫米)',
                                       `spec_height` decimal(10,2) DEFAULT NULL COMMENT '高度参数(毫米)',
                                       `unit` varchar(20) DEFAULT NULL COMMENT '计量单位',
                                       `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
                                       `description` text NOT NULL COMMENT '材料参数详情',
                                       `category_id` bigint NOT NULL COMMENT '关联分类ID',
                                       `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
                                       `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态 0待审核1通过2驳回',
                                       `audit_remark` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
                                       `sales_count` int NOT NULL DEFAULT '0' COMMENT '销量',
                                       `status` tinyint NOT NULL DEFAULT '0' COMMENT '上架状态 0下架1上架',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_material_name` (`material_name`),
                                       KEY `idx_category_id` (`category_id`),
                                       KEY `idx_supplier_id` (`supplier_id`),
                                       KEY `idx_audit_status` (`audit_status`),
                                       CONSTRAINT `fk_material_category` FOREIGN KEY (`category_id`) REFERENCES `decoration_category` (`id`),
                                       CONSTRAINT `fk_material_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修材料表';

-- 7. 评论表 decoration_comment
CREATE TABLE `decoration_comment` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `user_id` bigint NOT NULL COMMENT '发布用户ID',
                                      `target_type` tinyint NOT NULL COMMENT '评论对象类型 1装修方案2装修材料',
                                      `target_id` bigint NOT NULL COMMENT '关联对象ID',
                                      `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父评论ID 0代表主评论',
                                      `content` text NOT NULL COMMENT '评论内容',
                                      `images` varchar(500) DEFAULT NULL COMMENT '评论图片，多图逗号分隔',
                                      `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
                                      `status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态 0正常1违规删除',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
                                      `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`),
                                      KEY `idx_target` (`target_type`,`target_id`),
                                      CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 8. 用户点赞收藏表 user_like_collect
CREATE TABLE `user_like_collect` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     `user_id` bigint NOT NULL COMMENT '用户ID',
                                     `operate_type` tinyint NOT NULL COMMENT '操作类型 1点赞2收藏',
                                     `target_type` tinyint NOT NULL COMMENT '对象类型 1装修方案2装修材料3评论',
                                     `target_id` bigint NOT NULL COMMENT '关联对象ID',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                     `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_user_operate` (`user_id`,`operate_type`,`target_type`,`target_id`),
                                     CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户点赞收藏表';

-- 9. 订单主表 decoration_order
CREATE TABLE `decoration_order` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `order_no` varchar(50) NOT NULL COMMENT '订单编号',
                                    `user_id` bigint NOT NULL COMMENT '下单用户ID',
                                    `order_type` tinyint NOT NULL COMMENT '订单类型 1购买装修方案2购买装修材料',
                                    `target_id` bigint NOT NULL COMMENT '关联资源ID',
                                    `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
                                    `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态 0待支付1已支付2已完成3已退款',
                                    `contract_status` tinyint NOT NULL DEFAULT '0' COMMENT '合同状态 0未签订1已签订',
                                    `refund_status` tinyint NOT NULL DEFAULT '0' COMMENT '退款状态 0无退款1待审核2已退款3驳回',
                                    `remark` varchar(255) DEFAULT NULL COMMENT '用户订单备注',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                                    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
                                    `contract_time` datetime DEFAULT NULL COMMENT '合同签订时间',
                                    `finish_time` datetime DEFAULT NULL COMMENT '订单完成时间',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_order_no` (`order_no`),
                                    KEY `idx_user_id` (`user_id`),
                                    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 10. 装修进度跟踪表 decoration_progress
CREATE TABLE `decoration_progress` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `order_id` bigint NOT NULL COMMENT '关联订单ID',
                                       `progress_status` tinyint NOT NULL COMMENT '装修状态 1待开工2施工中3待竣工验收4已完成5施工异常',
                                       `status_desc` varchar(255) NOT NULL COMMENT '当前进度描述',
                                       `progress_images` text NOT NULL COMMENT '现场实拍图，多图逗号分隔',
                                       `report_user_id` bigint NOT NULL COMMENT '上报施工人员ID',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
                                       `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_order_id` (`order_id`),
                                       CONSTRAINT `fk_progress_order` FOREIGN KEY (`order_id`) REFERENCES `decoration_order` (`id`),
                                       CONSTRAINT `fk_progress_user` FOREIGN KEY (`report_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装修进度跟踪表';

-- 11. 材料出入库记录表 material_stock_record
CREATE TABLE `material_stock_record` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `material_id` bigint NOT NULL COMMENT '关联材料ID',
                                         `operate_type` tinyint NOT NULL COMMENT '操作类型 1采购入库2订单出库3库存盘点调整',
                                         `change_num` int NOT NULL COMMENT '变动数量 正数为入库负数为出库',
                                         `before_stock` int NOT NULL COMMENT '变动前库存',
                                         `after_stock` int NOT NULL COMMENT '变动后库存',
                                         `operate_user_id` bigint NOT NULL COMMENT '操作库管ID',
                                         `remark` varchar(255) DEFAULT NULL COMMENT '出入库备注',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                         `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                         PRIMARY KEY (`id`),
                                         KEY `idx_material_id` (`material_id`),
                                         CONSTRAINT `fk_stock_material` FOREIGN KEY (`material_id`) REFERENCES `decoration_material` (`id`),
                                         CONSTRAINT `fk_stock_user` FOREIGN KEY (`operate_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料出入库记录表';

-- 12. 系统公告表 sys_notice
CREATE TABLE `sys_notice` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `title` varchar(100) NOT NULL COMMENT '公告标题',
                              `content` text NOT NULL COMMENT '公告详情',
                              `show_position` tinyint NOT NULL COMMENT '展示位置 1首页顶部2用户中心3后台首页',
                              `publish_time` datetime NOT NULL COMMENT '发布时间',
                              `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
                              `status` tinyint NOT NULL DEFAULT '0' COMMENT '发布状态 0草稿1已发布2已下线',
                              `create_user_id` bigint NOT NULL COMMENT '发布管理员ID',
                              `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                              PRIMARY KEY (`id`),
                              CONSTRAINT `fk_notice_admin` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 13. 首页配置表 sys_home_config
CREATE TABLE `sys_home_config` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `config_type` tinyint NOT NULL COMMENT '配置类型 1轮播图2精选成功案例图3首页推荐位',
                                   `image_url` varchar(255) NOT NULL COMMENT '图片地址',
                                   `jump_link` varchar(255) DEFAULT NULL COMMENT '点击跳转链接',
                                   `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
                                   `status` tinyint NOT NULL DEFAULT '1' COMMENT '展示状态 0隐藏1显示',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页配置表';

-- 14. 系统访问统计表 sys_visit_stat
CREATE TABLE `sys_visit_stat` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `stat_date` date NOT NULL COMMENT '统计日期',
                                  `pv` int NOT NULL DEFAULT '0' COMMENT '页面浏览量',
                                  `uv` int NOT NULL DEFAULT '0' COMMENT '独立访客数',
                                  `new_user_count` int NOT NULL DEFAULT '0' COMMENT '新增注册用户数',
                                  `create_plan_count` int NOT NULL DEFAULT '0' COMMENT '新增上传方案数',
                                  `create_material_count` int NOT NULL DEFAULT '0' COMMENT '新增上传材料数',
                                  `finish_order_count` int NOT NULL DEFAULT '0' COMMENT '完成订单数',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '统计生成时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统访问统计表';

-- 15. 用户意见反馈表 user_feedback
CREATE TABLE `user_feedback` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NOT NULL COMMENT '提交用户ID',
                                 `content` text NOT NULL COMMENT '反馈内容',
                                 `contact_info` varchar(100) DEFAULT NULL COMMENT '用户联系方式',
                                 `handle_status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态 0待处理1已处理',
                                 `handle_remark` varchar(255) DEFAULT NULL COMMENT '处理回复',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                                 `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删1已删',
                                 PRIMARY KEY (`id`),
                                 CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户意见反馈表';

-- 初始化基础角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `permissions`, `remark`) VALUES
                                                                               ('普通用户', 'ROLE_USER', 'plan:view,material:view,comment:write,order:operate', '前台普通注册用户'),
                                                                               ('装修方案提供者', 'ROLE_PLAN_PROVIDER', 'plan:upload,plan:view,my:resource', '可上传装修方案申请上架'),
                                                                               ('材料供应商', 'ROLE_SUPPLIER', 'material:upload,material:view,my:resource', '可上传装修材料申请上架'),
                                                                               ('库管人员', 'ROLE_STOCK', 'stock:operate,stock:view', '负责材料出入库管理'),
                                                                               ('施工人员', 'ROLE_WORKER', 'progress:report,progress:view', '负责上报装修施工进度'),
                                                                               ('管理员', 'ROLE_ADMIN', '*', '系统最高权限管理员');

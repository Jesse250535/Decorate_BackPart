-- =====================================================
-- 业主线上签订合同：手写签名 / 首付款 / 合同回执
-- 前置脚本：3.sql → 4_seed_data.sql → 6_design_flow.sql
-- 说明：已合并进 4_seed_data.sql 末尾（条件 ALTER，可重复执行）。
--       新库只需执行 3.sql → 4_seed_data.sql，本文件仅作留档或给老库单独补列时使用。
-- =====================================================

ALTER TABLE `design_contract`
    ADD COLUMN `signature_url` varchar(255) NULL DEFAULT NULL COMMENT '业主签名图片地址' AFTER `file_url`,
    ADD COLUMN `first_payment_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '首付款金额（(合同金额-定金)×15%）' AFTER `signature_url`,
    ADD COLUMN `first_payment_time` datetime NULL DEFAULT NULL COMMENT '首付款支付时间' AFTER `first_payment_amount`,
    ADD COLUMN `receipt_url` varchar(255) NULL DEFAULT NULL COMMENT '合同回执地址（纸质合同拍照/扫描件）' AFTER `first_payment_time`;

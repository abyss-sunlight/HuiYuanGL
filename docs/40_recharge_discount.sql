-- 充值折扣表初始化
-- 说明：对应后端实体 com.example.membersystem.discount.entity.RechargeDiscount

USE HuiYuanSql;

CREATE TABLE IF NOT EXISTS recharge_discount (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '折扣ID',
  recharge_amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  discount_rate DECIMAL(5,4) NOT NULL COMMENT '折扣率（0.0000-1.0000）',
  discount_percentage DECIMAL(5,2) NOT NULL COMMENT '折扣百分比（0.00-100.00）',
  effective_date DATE NOT NULL COMMENT '生效日期',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  created_by VARCHAR(50) NOT NULL COMMENT '创建人',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_recharge_amount (recharge_amount),
  INDEX idx_effective_date (effective_date),
  INDEX idx_is_active (is_active),
  INDEX idx_created_at (created_at)
) COMMENT='充值折扣表';

-- 示例数据
INSERT INTO recharge_discount (recharge_amount, discount_rate, discount_percentage, effective_date, is_active, created_by)
VALUES
  (100.00, 0.9500, 95.00, '2024-01-01', 1, 'admin'),
  (200.00, 0.9000, 90.00, '2024-01-01', 1, 'admin'),
  (500.00, 0.8500, 85.00, '2024-01-01', 1, 'admin'),
  (1000.00, 0.8000, 80.00, '2024-01-01', 1, 'admin'),
  (2000.00, 0.7500, 75.00, '2024-01-01', 1, 'admin');

-- 添加注释说明
ALTER TABLE recharge_discount COMMENT = '充值折扣配置表，用于设置不同充值金额对应的折扣率';

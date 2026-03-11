-- 消费记录表初始化
-- 说明：对应后端实体 com.example.membersystem.consume.entity.ConsumeRecord

USE HuiYuanSql;

CREATE TABLE IF NOT EXISTS consume_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  phone VARCHAR(11) NOT NULL COMMENT '会员手机号',
  last_name VARCHAR(50) NOT NULL COMMENT '会员姓氏',
  gender TINYINT NOT NULL DEFAULT 1 COMMENT '性别：1-男，2-女',
  balance DECIMAL(10,2) NOT NULL COMMENT '余额',
  consume_amount DECIMAL(10,2) NOT NULL COMMENT '消费金额',
  consume_item VARCHAR(100) NOT NULL COMMENT '消费项目（中文）',
  consume_date DATE NOT NULL COMMENT '消费日期',
  consume_type VARCHAR(10) NOT NULL COMMENT '消费类型：支出或充值',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_phone (phone),
  INDEX idx_consume_date (consume_date),
  INDEX idx_consume_type (consume_type),
  INDEX idx_created_at (created_at)
) COMMENT='会员消费记录表';

-- 示例数据（consume_type 使用：支出/充值，与前端选项保持一致）
INSERT INTO consume_records (phone, last_name, gender, balance, consume_amount, consume_item, consume_date, consume_type)
VALUES
  ('13800138001', '张', 1, 1000.00, 200.00, '睫毛护理', '2024-01-15', '支出'),
  ('13800138002', '李', 1, 500.00, 0.00, '账户充值', '2024-01-16', '充值'),
  ('13800138003', '王', 2, 800.00, 200.00, '睫毛嫁接', '2024-01-17', '支出'),
  ('13800138004', '刘', 2, 1200.00, 200.00, '美容护理', '2024-01-18', '支出'),
  ('13800138005', '陈', 1, 300.00, 500.00, '会员充值', '2024-01-19', '充值');
-- 睫毛记录表初始化
-- 说明：对应后端实体 com.example.membersystem.eyelash.entity.EyelashRecord

USE HuiYuanSql;

CREATE TABLE IF NOT EXISTS eyelash_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  phone VARCHAR(11) NOT NULL COMMENT '手机号',
  last_name VARCHAR(50) NOT NULL COMMENT '姓氏',
  gender TINYINT NOT NULL DEFAULT 1 COMMENT '性别：1-男，2-女',
  style VARCHAR(100) NOT NULL COMMENT '款式（中文）',
  model_number VARCHAR(50) NOT NULL COMMENT '型号（数字）',
  length DOUBLE NOT NULL COMMENT '睫毛长度（数字，单位：mm）',
  curl VARCHAR(10) NOT NULL COMMENT '翘度（英文，如：C、D、J等）',
  record_date DATE NOT NULL COMMENT '记录日期',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_phone (phone),
  INDEX idx_record_date (record_date),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睫毛记录表';

-- 示例数据
INSERT INTO eyelash_records (phone, last_name, gender, style, model_number, length, curl, record_date)
VALUES
  ('13800138001', '张', 1, '自然款', '001', 10.5, 'C', '2024-01-15'),
  ('13800138002', '李', 1, '浓密款', '002', 12.0, 'D', '2024-01-16'),
  ('13800138003', '王', 2, '猫眼款', '003', 11.5, 'J', '2024-01-17'),
  ('13800138004', '刘', 2, '仙女款', '004', 13.0, 'C', '2024-01-18'),
  ('13800138005', '陈', 1, '韩式款', '005', 9.5, 'D', '2024-01-19');
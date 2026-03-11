-- 用户表初始化
-- 说明：对应后端实体 com.example.membersystem.user.entity.User

USE HuiYuanSql;

CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
  username VARCHAR(64) NOT NULL COMMENT '用户名，默认手机号后4位',
  last_name VARCHAR(50) NOT NULL COMMENT '姓氏，必填',
  phone VARCHAR(32) NOT NULL UNIQUE COMMENT '手机号，唯一标识，可用于登录',
  password VARCHAR(128) NULL COMMENT '密码（BCrypt哈希），可为空（纯短信登录）',
  openid VARCHAR(128) NULL UNIQUE COMMENT '微信openid，可为空',
  gender TINYINT NOT NULL DEFAULT 1 COMMENT '性别：1-男，2-女，必填',
  member_no VARCHAR(20) NULL UNIQUE COMMENT '会员号（唯一值），格式：年月日时分秒+2位随机数',
  amount DECIMAL(10,2) NULL COMMENT '账户金额，可为空',
  discount DECIMAL(5,2) NULL COMMENT '折扣率，可为空',
  permission_level INT NOT NULL DEFAULT 4 COMMENT '权限等级：1-店长，2-员工，3-会员，4-游客',
  status INT NOT NULL DEFAULT 0 COMMENT '账户状态：0-正常，1-禁用',
  avatar_url VARCHAR(512) NULL COMMENT '头像URL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  INDEX idx_phone (phone),
  INDEX idx_openid (openid),
  INDEX idx_member_no (member_no),
  INDEX idx_permission_level (permission_level),
  INDEX idx_status (status)
) COMMENT='用户信息表';

-- 测试用户（密码：123456；BCrypt哈希，与你现有脚本保持一致）
INSERT INTO user (username, last_name, phone, password, gender, member_no, amount, discount, permission_level, status)
VALUES
  ('8001', '张', '13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 1, '202401011200001', 1000.00, 0.95, 1, 0),
  ('8002', '李', '13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 1, '202401011200012', 500.00, 0.90, 2, 0),
  ('8003', '王', '13800138003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 2, '202401011200023', 800.00, 0.85, 3, 0),
  ('8004', '赵', '13800138004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 1, '202401011200034', 0.00, NULL, 4, 0);

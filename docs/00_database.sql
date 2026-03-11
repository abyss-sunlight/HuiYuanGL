-- 数据库初始化（仅建库与选择库）
-- 说明：执行本文件后，再依次执行 10_user.sql / 20_eyelash_records.sql / 30_consume_records.sql

CREATE DATABASE IF NOT EXISTS HuiYuanSql
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE HuiYuanSql;

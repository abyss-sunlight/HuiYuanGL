-- 轮播内容表
-- 用于存储主页展示的轮播图和视频内容

USE HuiYuanSql;

CREATE TABLE IF NOT EXISTS carousel_content (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `video_url` varchar(500) DEFAULT NULL COMMENT '视频URL',
  `image1_url` varchar(500) DEFAULT NULL COMMENT '图片1 URL',
  `image2_url` varchar(500) DEFAULT NULL COMMENT '图片2 URL',
  `image3_url` varchar(500) DEFAULT NULL COMMENT '图片3 URL',
  `image4_url` varchar(500) DEFAULT NULL COMMENT '图片4 URL',
  `image5_url` varchar(500) DEFAULT NULL COMMENT '图片5 URL',
  `content1` text COMMENT '文案1',
  `content2` text COMMENT '文案2',
  `content3` text COMMENT '文案3',
  `content4` text COMMENT '文案4',
  `content5` text COMMENT '文案5',
  `is_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `upload_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传日期',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_is_enabled` (`is_enabled`),
  KEY `idx_upload_date` (`upload_date`),
  CONSTRAINT `fk_carousel_content_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播内容表';

-- 插入示例数据
INSERT INTO `carousel_content` (
  `title`, 
  `video_url`, 
  `image1_url`, 
  `image2_url`, 
  `image3_url`, 
  `image4_url`, 
  `image5_url`,
  `content1`, 
  `content2`, 
  `content3`, 
  `content4`, 
  `content5`,
  `is_enabled`,
  `upload_date`,
  `uploader_id`
) VALUES (
  '美容护理推广',
  'http://localhost:8080/videos/meijia.mp4',
  '/images/meijia1.jpeg',
  '/images/meijia2.jpeg',
  '/images/meijia3.jpeg',
  '/images/meijie1.jpeg',
  '/images/meijie2.jpeg',
  '专业美容护理，焕发自然之美',
  '精致护理服务，提升生活品质',
  '美丽从这里开始，自信由内而外',
  '专业团队，贴心服务',
  '美丽人生，从这里启航',
  1,
  '2024-03-14 10:00:00',
  1
);

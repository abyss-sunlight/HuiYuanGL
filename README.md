# 会员记录管理系统（Spring Boot + 微信小程序）

本项目用于门店日常业务记录管理：
- 后端：Spring Boot 提供登录认证、权限校验、记录数据接口
- 前端：微信小程序提供记录查询/新增/删除等操作

> 当前版本已移除 `member`（会员表/会员 CRUD）模块：
> 客户信息以 `手机号 + 姓氏` 直接存储在业务记录表中（睫毛记录/消费记录）。

## 功能概览

### 1) 登录与权限
- 手机号登录（基于 `user` 表）
- JWT 登录态
- 后端接口权限注解：`@RequirePermission(level)`
- 小程序端也会做权限提示

权限等级：
- 1：店长
- 2：员工
- 3：会员
- 4：游客

### 2) 睫毛记录（`eyelash_records`）
- 列表展示
- 手机号搜索
- 日期范围搜索
- 新增记录
- 删除记录（二次确认弹窗居中覆盖；员工及以上）

### 3) 消费记录（`consume_records`）
- 列表展示
- 手机号搜索
- 日期范围搜索
- 新增记录
- 删除记录（二次确认弹窗；员工及以上）

### 4) 记录页体验优化
- “添加/刷新”按钮纵向排列，宽度与搜索框对齐
- 每条记录右侧提供“添加”快捷按钮：自动带入该客户手机号与姓氏
- 添加页面点击“重置”仍保留该手机号与姓氏
- 右下角“回到顶部”浮窗按钮

## 技术栈

### 后端
- Spring Boot（Java）
- Spring Data JPA
- MySQL
- JWT

### 小程序
- 微信小程序原生（WXML/WXSS/JS）

## 项目结构

```
biyesji6/
├── backend/                      # 后端
│   └── src/main/java/com/example/membersystem/
│       ├── auth/                 # 认证、拦截、JWT
│       ├── user/                 # 用户与权限（登录基于 user 表）
│       ├── eyelash/              # 睫毛记录
│       ├── consume/              # 消费记录
│       └── common/               # 统一返回、异常处理
├── miniprogram/                  # 微信小程序
│   └── pages/
│       ├── index/                # 首页
│       ├── records/              # 记录页（睫毛/消费）
│       ├── profile/              # 我的/登录
│       ├── add-eyelash-record/   # 新增睫毛记录
│       └── add-consume-record/   # 新增消费记录
└── docs/                         # 数据库 SQL（仅 SQL）
```

## 快速开始

### 1) 初始化数据库

`docs/` 目录只存放 SQL，按顺序执行：
1. `docs/00_database.sql`
2. `docs/10_user.sql`
3. `docs/20_eyelash_records.sql`
4. `docs/30_consume_records.sql`

说明：
- 已不包含 `member` 表
- `consume_type` 统一使用：`支出` / `充值`

### 2) 启动后端

1. 配置后端数据库连接（`application.yml/properties`）
2. 启动 Spring Boot

后端接口统一前缀：`/api`

### 3) 运行小程序

使用微信开发者工具打开 `miniprogram/` 目录并运行。

## 主要接口（概览）

- 认证：`/api/auth/...`
- 睫毛记录：`/api/eyelash-records`
- 消费记录：`/api/consume-records`

## 常见问题

### 1) 为什么没有会员（member）管理功能？
为了避免与 `user` 表重复维护，系统已移除 `member` 模块；客户信息随记录保存，通过手机号查询聚合即可。

### 2) 为什么“快捷添加”手机号/姓氏不可改？
从记录列表右侧“添加”进入时，会自动带入该客户信息，并在重置时保留，避免误改。

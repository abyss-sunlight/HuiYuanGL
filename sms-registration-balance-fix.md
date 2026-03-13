# 短信注册余额默认值修复

## ✅ 问题描述

短信注册后，新用户的余额字段为空或null，而不是默认的0。这导致在消费记录功能中显示异常。

## 🔧 修复方案

### 1. 修改AuthService注册逻辑

#### 修改前
```java
// 创建新用户
user = new User();
user.setPhone(request.getPhone());
user.setLastName("用户");
user.setGender(1);
user.setPermissionLevel(4);
user.setStatus(0);
// 没有设置amount和discount
```

#### 修改后
```java
// 创建新用户
user = new User();
user.setPhone(request.getPhone());
user.setLastName("用户");
user.setGender(1);
user.setPermissionLevel(4);
user.setStatus(0);
user.setAmount(0.0); // 默认余额为0
user.setDiscount(1.0); // 默认折扣为1.0（无折扣）
```

### 2. 修改User实体类

#### amount字段定义
```java
/**
 * 账户金额
 * 
 * 会员账户的余额
 * 用于消费记录和充值记录
 * 
 * 注意：
 * - 默认值为0，新注册用户余额为0
 * - 金额单位为元，保留2位小数
 * 
 * 约束：最大10位整数，2位小数
 */
@Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
private BigDecimal amount;
```

#### prePersist方法增强
```java
@PrePersist
public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    
    // ... 其他默认值设置 ...
    
    // 设置默认账户状态为正常
    if (this.status == null) {
        this.status = 0;
    }
    
    // 设置默认余额为0
    if (this.amount == null) {
        this.amount = BigDecimal.ZERO;
    }
    
    // 设置默认折扣为1.0（无折扣）
    if (this.discount == null) {
        this.discount = BigDecimal.ONE;
    }
    
    // ... 其他逻辑 ...
}
```

### 3. 数据库迁移脚本

#### SQL脚本 (41_update_user_balance_default.sql)
```sql
-- 1. 修改amount字段的默认值
ALTER TABLE user 
MODIFY COLUMN amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额，默认0.00';

-- 2. 更新所有NULL的余额为0.00
UPDATE user 
SET amount = 0.00 
WHERE amount IS NULL;

-- 3. 修改discount字段的默认值
ALTER TABLE user 
MODIFY COLUMN discount DECIMAL(5,2) DEFAULT 1.00 COMMENT '折扣率，默认1.00（无折扣）';

-- 4. 更新所有NULL的折扣为1.00
UPDATE user 
SET discount = 1.00 
WHERE discount IS NULL;
```

## 🎯 修复效果

### 1. 新用户注册
- ✅ **余额**: 自动设置为0.00
- ✅ **折扣**: 自动设置为1.00（无折扣）
- ✅ **一致性**: 前端显示正常，无null值

### 2. 现有用户数据
- ✅ **NULL处理**: 自动更新为0.00
- ✅ **数据一致性**: 所有用户都有有效的余额值
- ✅ **向后兼容**: 不影响现有业务逻辑

### 3. 消费记录功能
- ✅ **游客充值**: 余额显示为0.00，不再是null
- ✅ **会员消费**: 余额正确显示实际金额
- ✅ **数据验证**: 表单验证正常工作

## 🧪 测试验证

### 1. 新用户注册测试
```bash
# 测试短信注册新用户
curl -X POST http://localhost:8080/api/auth/send-sms \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138999"}'

curl -X POST http://localhost:8080/api/auth/sms-login \
  -H "Content-Type: application/json" \
  -d '{
    "loginType": "sms",
    "phone": "13800138999",
    "code": "123456"
  }'
```

#### 预期结果
```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "userId": 123,
    "phone": "13800138999",
    "amount": 0.00,
    "discount": 1.00,
    "permissionLevel": 4,
    "permissionName": "游客"
  }
}
```

### 2. 数据库验证
```sql
-- 查看新注册用户的余额
SELECT id, phone, amount, discount, permission_level, created_at
FROM user 
WHERE phone = '13800138999';

-- 检查所有NULL余额的用户
SELECT COUNT(*) as null_balance_users
FROM user 
WHERE amount IS NULL;
```

#### 预期结果
- 新用户的amount字段为0.00
- discount字段为1.00
- 没有NULL余额的用户

### 3. 前端功能测试
1. **新用户注册**
   - 短信验证登录
   - 查看个人资料页面
   - 余额显示为¥0.00

2. **游客充值功能**
   - 点击充值按钮
   - 余额显示为¥0.00（不是null或空白）
   - 充值成功后余额更新

3. **会员消费功能**
   - 会员用户消费
   - 余额正确显示和扣减
   - 消费记录正常保存

## 📋 部署检查清单

### 1. 代码修改
- [x] AuthService.java - 添加amount和discount设置
- [x] User.java - 修改amount字段定义
- [x] User.java - 增强prePersist方法
- [x] 数据库迁移脚本创建

### 2. 数据库更新
- [ ] 执行迁移脚本41_update_user_balance_default.sql
- [ ] 验证数据更新结果
- [ ] 检查应用启动日志

### 3. 功能测试
- [ ] 新用户短信注册测试
- [ ] 余额显示验证
- [ ] 游客充值功能测试
- [ ] 会员消费功能测试
- [ ] 错误处理测试

### 4. 性能验证
- [ ] 注册响应时间正常
- [ ] 数据库查询性能正常
- [ ] 前端页面加载正常

## 🔄 回滚方案

如果出现问题，可以按以下步骤回滚：

### 1. 代码回滚
```bash
git revert <commit-hash>  # 回滚相关代码修改
```

### 2. 数据库回滚
```sql
-- 如果需要，可以恢复原有的NULL值（不推荐）
UPDATE user 
SET amount = NULL 
WHERE amount = 0.00 AND created_at < '2024-03-12';

-- 恢复字段定义
ALTER TABLE user 
MODIFY COLUMN amount DECIMAL(10,2) NULL COMMENT '账户金额';
```

## 📊 影响范围

### 1. 受影响的功能
- ✅ 短信注册流程
- ✅ 用户余额显示
- ✅ 游客充值功能
- ✅ 会员消费功能
- ✅ 消费记录列表

### 2. 不受影响的功能
- ✅ 密码登录
- ✅ 用户信息编辑
- ✅ 睫毛记录管理
- ✅ 充值折扣管理
- ✅ 权限控制

## 🎯 总结

通过这次修复，确保了：

1. **数据一致性**: 所有用户都有有效的余额值
2. **用户体验**: 新用户注册后余额正确显示
3. **业务逻辑**: 消费记录功能正常工作
4. **向后兼容**: 不影响现有用户的正常使用

现在短信注册的新用户余额将默认为0，不再出现null值问题！

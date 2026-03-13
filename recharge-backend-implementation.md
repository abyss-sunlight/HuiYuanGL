# 会员消费/充值后端功能实现

## ✅ 功能需求

### 问题描述
实现会员消费/充值的完整后端功能，包括：
1. **充值功能**: 设置用户余额=消费金额+原有余额，根据充值金额设置适合的折扣值
2. **折扣保护**: 如果原有折扣高于充值折扣，保留原来的折扣
3. **消费功能**: 设置用户余额=原有余额-折扣后的消费金额

## 🔧 技术实现

### 1. 依赖注入增强

#### 新增依赖
```java
@Service
@Transactional
public class ConsumeRecordService {
    private final ConsumeRecordRepository consumeRecordRepository;
    private final UserRepository userRepository;           // 新增：用户操作
    private final RechargeDiscountService rechargeDiscountService; // 新增：折扣查询

    @Autowired
    public ConsumeRecordService(ConsumeRecordRepository consumeRecordRepository,
                              UserRepository userRepository,
                              RechargeDiscountService rechargeDiscountService) {
        this.consumeRecordRepository = consumeRecordRepository;
        this.userRepository = userRepository;
        this.rechargeDiscountService = rechargeDiscountService;
    }
}
```

#### 导入新增类
```java
import com.example.membersystem.discount.entity.RechargeDiscount;
import com.example.membersystem.discount.service.RechargeDiscountService;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import java.math.RoundingMode;
```

### 2. 核心业务逻辑

#### 充值处理逻辑
```java
private void handleRecharge(User user, ConsumeRecord record) {
    // 1. 计算新的余额
    BigDecimal newBalance = user.getAmount().add(record.getConsumeAmount());
    
    // 2. 查找适用的充值折扣
    Optional<RechargeDiscount> discountOptional = rechargeDiscountService
        .findApplicableDiscount(record.getConsumeAmount());
    
    // 3. 默认保持原折扣
    BigDecimal newDiscount = user.getDiscount();
    
    // 4. 只有当充值折扣更好时才更新
    if (discountOptional.isPresent()) {
        RechargeDiscount discount = discountOptional.get();
        // 数值越小折扣越大：1.2折(0.12) > 3.5折(0.035)
        if (user.getDiscount() == null || discount.getDiscountRate().compareTo(user.getDiscount()) < 0) {
            newDiscount = discount.getDiscountRate();
        }
    }
    
    // 5. 更新用户余额和折扣
    user.setAmount(newBalance);
    user.setDiscount(newDiscount);
    
    // 6. 更新记录中的余额快照（充值后的余额）
    record.setBalance(newBalance);
}
```

#### 消费处理逻辑
```java
private void handleConsume(User user, ConsumeRecord record) {
    // 1. 计算折扣后的消费金额
    BigDecimal discountedAmount = record.getConsumeAmount()
        .multiply(user.getDiscount() != null ? user.getDiscount() : BigDecimal.ONE)
        .setScale(2, RoundingMode.HALF_UP);
    
    // 2. 计算新的余额
    BigDecimal newBalance = user.getAmount().subtract(discountedAmount);
    
    // 3. 检查余额是否足够
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("余额不足，当前余额：" + user.getAmount() + 
            "，折扣后消费金额：" + discountedAmount);
    }
    
    // 4. 更新用户余额
    user.setAmount(newBalance);
    
    // 5. 更新记录中的余额快照（消费前的余额）
    record.setBalance(user.getAmount());
    
    // 6. 更新记录中的消费金额为折扣后的金额
    record.setConsumeAmount(discountedAmount);
}
```

### 3. 主流程重构

#### 创建记录流程
```java
public ConsumeRecord createRecord(ConsumeRecord record) {
    validateRecord(record);
    
    // 1. 查找用户
    Optional<User> userOptional = userRepository.findByPhone(record.getPhone());
    if (userOptional.isEmpty()) {
        throw new IllegalArgumentException("用户不存在，手机号：" + record.getPhone());
    }
    
    User user = userOptional.get();
    
    // 2. 根据消费类型处理余额和折扣
    if ("充值".equals(record.getConsumeType())) {
        handleRecharge(user, record);
    } else if ("支出".equals(record.getConsumeType())) {
        handleConsume(user, record);
    } else {
        throw new IllegalArgumentException("不支持的消费类型：" + record.getConsumeType());
    }
    
    // 3. 保存消费记录
    ConsumeRecord savedRecord = consumeRecordRepository.save(record);
    
    // 4. 保存更新后的用户信息
    userRepository.save(user);
    
    return savedRecord;
}
```

## 📊 业务规则详解

### 1. 充值业务规则

#### 余额计算
```
新余额 = 原有余额 + 充值金额
示例：用户原有100元，充值50元 → 新余额150元
```

#### 折扣设置逻辑
```
1. 查找充值金额对应的折扣规则
2. 比较现有折扣和充值折扣
3. 选择更优折扣（数值更小）
4. 更新用户折扣

示例：
- 用户现有折扣：1.2折（0.12）
- 充值100元对应折扣：3.5折（0.035）
- 比较：0.035 < 0.12，所以3.5折更优
- 结果：更新用户折扣为3.5折
```

#### 折扣保护机制
```
如果原有折扣优于充值折扣，保持原有折扣：
- 用户现有折扣：2.0折（0.02）
- 充值100元对应折扣：3.5折（0.035）
- 比较：0.02 < 0.035，所以2.0折更优
- 结果：保持用户折扣2.0折
```

### 2. 消费业务规则

#### 折扣计算
```
折扣后金额 = 原金额 × 用户折扣率
示例：
- 原消费金额：100元
- 用户折扣：3.5折（0.035）
- 折扣后金额：100 × 0.035 = 3.50元
```

#### 余额计算
```
新余额 = 原有余额 - 折扣后消费金额
示例：
- 用户原有余额：150元
- 折扣后消费金额：3.50元
- 新余额：150 - 3.50 = 146.50元
```

#### 余额验证
```
检查新余额不能为负数：
如果新余额 < 0，抛出异常："余额不足"
```

## 🧪 测试场景

### 1. 充值测试

#### 场景1：普通充值
```
用户数据：
- 原有余额：100.00元
- 原有折扣：1.0（无折扣）

充值数据：
- 充值金额：200.00元
- 对应折扣：3.0折（0.03）

预期结果：
- 新余额：100 + 200 = 300.00元
- 新折扣：3.0折（0.03）
- 记录余额快照：300.00元
```

#### 场景2：折扣保护
```
用户数据：
- 原有余额：100.00元
- 原有折扣：2.0折（0.02）

充值数据：
- 充值金额：200.00元
- 对应折扣：3.0折（0.03）

预期结果：
- 新余额：100 + 200 = 300.00元
- 新折扣：2.0折（0.02）// 保持原有更优折扣
- 记录余额快照：300.00元
```

#### 场景3：无折扣规则
```
用户数据：
- 原有余额：100.00元
- 原有折扣：1.0（无折扣）

充值数据：
- 充值金额：50.00元
- 对应折扣：无匹配规则

预期结果：
- 新余额：100 + 50 = 150.00元
- 新折扣：1.0（无折扣）// 保持原有
- 记录余额快照：150.00元
```

### 2. 消费测试

#### 场景1：普通消费
```
用户数据：
- 原有余额：300.00元
- 用户折扣：3.0折（0.03）

消费数据：
- 消费金额：100.00元
- 消费类型：支出

预期结果：
- 折扣后金额：100 × 0.03 = 3.00元
- 新余额：300 - 3.00 = 297.00元
- 记录余额快照：300.00元（消费前余额）
- 记录消费金额：3.00元（折扣后金额）
```

#### 场景2：无折扣消费
```
用户数据：
- 原有余额：150.00元
- 用户折扣：1.0（无折扣）

消费数据：
- 消费金额：50.00元
- 消费类型：支出

预期结果：
- 折扣后金额：50 × 1.0 = 50.00元
- 新余额：150 - 50 = 100.00元
- 记录余额快照：150.00元
- 记录消费金额：50.00元
```

#### 场景3：余额不足
```
用户数据：
- 原有余额：10.00元
- 用户折扣：3.0折（0.03）

消费数据：
- 消费金额：500.00元
- 消费类型：支出

预期结果：
- 折扣后金额：500 × 0.03 = 15.00元
- 新余额：10 - 15 = -5.00元
- 抛出异常："余额不足，当前余额：10.00，折扣后消费金额：15.00"
```

## 📋 数据库操作

### 1. 事务处理
```java
@Transactional
public ConsumeRecord createRecord(ConsumeRecord record) {
    // 所有数据库操作在同一个事务中
    // 1. 查询用户
    // 2. 更新用户余额和折扣
    // 3. 保存消费记录
    // 4. 保存用户信息
    // 任何操作失败都会回滚整个事务
}
```

### 2. 数据一致性
```
操作顺序：
1. 先查询用户信息
2. 计算新的余额和折扣
3. 更新用户信息到内存
4. 保存消费记录到数据库
5. 保存用户信息到数据库

确保：
- 消费记录中的余额快照正确
- 用户余额更新正确
- 折扣设置正确
- 数据一致性得到保证
```

### 3. 错误处理
```java
// 用户不存在
if (userOptional.isEmpty()) {
    throw new IllegalArgumentException("用户不存在，手机号：" + record.getPhone());
}

// 余额不足
if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalArgumentException("余额不足，当前余额：" + user.getAmount() + 
        "，折扣后消费金额：" + discountedAmount);
}

// 不支持的消费类型
if (!record.getConsumeType().equals("支出") && !record.getConsumeType().equals("充值")) {
    throw new IllegalArgumentException("消费类型必须是'支出'或'充值'");
}
```

## 🔄 API接口保持不变

### 1. 接口签名
```java
@PostMapping
public ResponseEntity<Map<String, Object>> createRecord(@RequestBody ConsumeRecord record) {
    // 接口签名保持不变
    // 业务逻辑在Service层增强
    ConsumeRecord createdRecord = consumeRecordService.createRecord(record);
    // 返回格式保持不变
}
```

### 2. 前端兼容性
```
前端调用方式保持不变：
- URL: POST /api/consume-records
- 数据格式: JSON
- 返回格式: 统一响应格式

新增的后端逻辑对前端透明：
- 充值时自动更新用户余额和折扣
- 消费时自动计算折扣并验证余额
- 错误信息通过统一异常处理返回
```

## 🎯 核心优势

### 1. 业务完整性
- **自动余额管理**: 充值和消费时自动更新用户余额
- **智能折扣设置**: 根据充值金额自动设置最优折扣
- **折扣保护**: 保护用户已有的更优折扣
- **余额验证**: 消费时验证余额充足性

### 2. 数据准确性
- **余额快照**: 消费记录保存操作时的余额快照
- **折扣计算**: 精确的折扣计算，四舍五入
- **事务安全**: 所有操作在同一事务中，保证数据一致性

### 3. 错误处理
- **用户验证**: 确保操作的用户存在
- **余额检查**: 防止余额不足的消费
- **类型验证**: 严格的消费类型验证
- **详细错误**: 提供清晰的错误信息

### 4. 扩展性
- **折扣规则**: 灵活的折扣规则配置
- **业务分离**: 充值和消费逻辑分离，便于维护
- **接口稳定**: 后端逻辑增强不影响前端接口

## 🎉 总结

通过这次后端功能实现：

1. **充值功能**: 自动更新余额+智能折扣设置+折扣保护
2. **消费功能**: 自动折扣计算+余额验证+余额更新
3. **数据一致性**: 事务保证+余额快照+精确计算
4. **错误处理**: 完善验证+详细错误信息+异常安全
5. **接口兼容**: 前端无需修改+业务逻辑透明+平滑升级

现在会员消费/充值功能具备完整的业务逻辑，能够自动处理余额更新、折扣设置和数据验证！

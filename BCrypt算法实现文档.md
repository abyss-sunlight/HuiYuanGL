# BCrypt算法实现文档

## 项目概述

本项目使用Spring Security的BCrypt算法对用户密码进行加密存储和验证，确保用户密码的安全性。

## 依赖配置

### Maven依赖
在`backend/pom.xml`中配置了Spring Security Crypto依赖：

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

## BCrypt配置

### PasswordEncoderConfig类
位置：`backend/src/main/java/com/example/membersystem/auth/config/PasswordEncoderConfig.java`

```java
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

该配置类：
- 使用`@Configuration`注解标记为Spring配置类
- 通过`@Bean`注解将`BCryptPasswordEncoder`注册为Spring Bean
- 供整个应用程序的密码加密和验证使用

## BCrypt算法实现步骤

### 1. 密码加密实现

#### 在AuthService中的密码设置
位置：`backend/src/main/java/com/example/membersystem/auth/service/AuthService.java`

```java
// 设置密码时进行BCrypt加密
user.setPassword(passwordEncoder.encode(pwd));
userRepository.save(user);
```

**加密步骤：**
1. 接收用户输入的明文密码
2. 使用`passwordEncoder.encode()`方法进行BCrypt加密
3. 将加密后的密码哈希值存储到数据库
4. 保存用户信息

### 2. 密码验证实现

#### 在PasswordLoginService中的密码验证
位置：`backend/src/main/java/com/example/membersystem/auth/service/PasswordLoginService.java`

```java
private void validatePassword(User user, String inputPassword) {
    if (!StringUtils.hasText(user.getPassword())) {
        throw new BusinessException(40001, "未设置密码，请使用短信登录后设置密码");
    }

    boolean isPasswordValid;
    String storedPassword = user.getPassword();
    
    if (isBcryptPassword(storedPassword)) {
        // BCrypt密码验证
        isPasswordValid = passwordEncoder.matches(inputPassword, storedPassword);
    } else {
        // 兼容历史明文密码（如果数据库里有旧数据），校验通过后自动升级为BCrypt
        isPasswordValid = storedPassword.equals(inputPassword);
        if (isPasswordValid) {
            // 自动升级为BCrypt加密
            user.setPassword(passwordEncoder.encode(inputPassword));
            userRepository.save(user);
            System.out.println("密码已自动升级为BCrypt加密");
        }
    }

    if (!isPasswordValid) {
        System.out.println("密码错误");
        throw new BusinessException(40002, "密码错误");
    }
}
```

**验证步骤：**
1. 检查用户是否设置了密码
2. 判断存储的密码是否为BCrypt格式
3. 如果是BCrypt格式：使用`passwordEncoder.matches()`进行验证
4. 如果是历史明文密码：进行明文比较，验证通过后自动升级为BCrypt
5. 验证失败时抛出业务异常

### 3. BCrypt格式识别

```java
private boolean isBcryptPassword(String password) {
    return password.startsWith("$2a$") || 
           password.startsWith("$2b$") || 
           password.startsWith("$2y$");
}
```

**识别逻辑：**
- BCrypt哈希值以特定前缀开头
- 支持`$2a$`、`$2b$`、`$2y$`三种格式
- 通过前缀判断是否为BCrypt加密密码

## BCrypt算法特点

### 1. 安全特性
- **盐值集成**：BCrypt自动生成并集成盐值到哈希中
- **计算成本**：可配置的计算因子（默认10-12轮），增加暴力破解难度
- **抗彩虹表**：每个密码都有唯一的盐值，有效抵御彩虹表攻击

### 2. 哈希格式
```
$2a$10$N9qo8uLOickgx2ZMRZoMy.MrqJ3e3jz2jl6QXO5E5J5Q5Q5Q5Q5Q
│   │   │                           │
│   │   │                           └─ 哈希值（22字符）
│   │   └─ 计算成本（10轮）
│   └─ 版本标识（2a）
└─ 算法标识
```

### 3. 自动升级机制
项目实现了向后兼容的密码升级机制：
- 检测历史明文密码
- 验证通过后自动升级为BCrypt加密
- 无缝迁移，不影响用户体验

## 使用场景

### 1. 用户注册/设置密码
```java
// 新用户设置密码
user.setPassword(passwordEncoder.encode(plainPassword));
```

### 2. 用户登录验证
```java
// 登录时验证密码
boolean isValid = passwordEncoder.matches(inputPassword, storedHash);
```

### 3. 密码修改
```java
// 修改密码时重新加密
user.setPassword(passwordEncoder.encode(newPassword));
```

## 安全最佳实践

### 1. 配置建议
- 使用默认的计算成本（10轮）
- 可根据服务器性能调整到12轮以提高安全性

### 2. 存储建议
- 数据库密码字段长度建议设置为60字符以上
- 确保数据库连接和存储的安全性

### 3. 验证建议
- 始终使用`matches()`方法进行验证
- 不要自行实现BCrypt算法
- 避免密码在内存中停留过长时间

## 总结

本项目的BCrypt实现具有以下优势：

1. **安全性高**：使用业界标准的BCrypt算法
2. **向后兼容**：支持历史数据的平滑迁移
3. **自动升级**：明文密码自动升级为加密存储
4. **易于维护**：基于Spring Security的标准实现
5. **性能平衡**：在安全性和性能之间取得良好平衡

通过这套完整的BCrypt实现，项目确保了用户密码的安全存储和验证，为整个会员管理系统提供了可靠的安全基础。

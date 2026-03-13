package com.example.membersystem.user.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 用户实体类
 * 
 * 映射到数据库user表，存储用户的基本信息和权限数据
 * 
 * 权限等级定义：
 * 1 - 店长：拥有所有权限，可管理所有功能
 * 2 - 员工：拥有部分管理权限，可进行日常操作
 * 3 - 会员：普通用户权限，可查看和操作自己的数据
 * 4 - 游客：最低权限，只能进行基本查看
 * 
 * 账户状态定义：
 * 0 - 正常：账户正常，可以正常使用
 * 1 - 禁用：账户被禁用，无法登录和使用
 * 
 * 使用JPA注解进行ORM映射，支持自动时间戳管理
 */
@Entity
@Table(name = "user")
public class User {

    /**
     * 用户主键ID
     * 
     * 数据库自增主键，唯一标识用户记录
     * 使用IDENTITY策略，由数据库自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     * 
     * 系统生成的用户名，默认为手机号后4位
     * 用于系统内部标识和显示
     * 
     * 约束：不能为空，最大长度64字符
     */
    @Column(name = "username", nullable = false, length = 64)
    private String username;

    /**
     * 用户姓氏
     * 
     * 用户的真实姓氏，用于业务记录关联
     * 在睫毛记录和消费记录中使用
     * 
     * 约束：不能为空，最大长度50字符
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * 性别
     * 
     * 用户性别信息
     * 
     * 性别定义：
     * 1 - 男性
     * 2 - 女性
     * 
     * 约束：不能为空，默认值为1（男性）
     */
    @Column(nullable = false)
    private Integer gender;

    /**
     * 用户手机号
     * 
     * 用户的主要登录凭证和联系方式
     * 在系统中具有唯一性约束，用于区分不同用户
     * 
     * 约束：不能为空，最大长度32字符，唯一索引
     */
    @Column(nullable = false, length = 32, unique = true)
    private String phone;

    /**
     * 用户密码
     * 
     * 用于密码登录方式的身份验证
     * 实际应用中应存储加密后的密码
     * 
     * 注意：
     * - 可为空，支持纯短信登录用户
     * - 应使用强加密算法（如BCrypt）
     * - 传输时应使用HTTPS保护
     * 
     * 约束：最大长度128字符
     */
    @Column(length = 128)
    private String password;

    /**
     * 微信OpenID
     * 
     * 微信用户的唯一标识符
     * 用于微信登录方式的身份验证
     * 
     * 注意：
     * - 可为空，支持非微信登录用户
     * - 具有唯一性约束
     * - 由微信服务器生成和分配
     * 
     * 约束：最大长度128字符，唯一索引
     */
    @Column(length = 128, unique = true)
    private String openid;

    /**
     * 用户权限等级
     * 
     * 数值型权限标识，决定用户可访问的功能范围
     * 权限等级越高，可访问的功能越多
     * 
     * 权限定义：
     * 1 - 店长：系统管理员，拥有所有权限
     * 2 - 员工：操作员，拥有业务管理权限
     * 3 - 会员：普通用户，拥有基本使用权限
     * 4 - 游客：最低权限，只能查看公开信息
     * 
     * 约束：不能为空，默认值为4（游客）
     */
    @Column(nullable = false)
    private Integer permissionLevel;

    /**
     * 会员号
     * 
     * 会员的唯一标识号码
     * 格式：年月日时分秒+2位随机数
     * 
     * 注意：
     * - 可为空，非会员用户无会员号
     * - 具有唯一性约束
     * - 在用户创建时自动生成
     * 
     * 约束：最大长度20字符，唯一索引
     */
    @Column(name = "member_no", length = 20, unique = true)
    private String memberNo;

    /**
     * 账户金额
     * 
     * 会员账户的余额
     * 用于消费记录和充值记录
     * 
     * 注意:
     * - 默认值为0，新注册用户余额为0
     * - 金额单位为元，保留2位小数
     * 
     * 约束：最大10位整数，2位小数
     */
    @Column(precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal amount;

    /**
     * 折扣率
     * 
     * 会员享受的折扣比例
     * 用于计算优惠价格
     * 
     * 注意：
     * - 可为空，非会员用户无折扣
     * - 折扣率范围：0.00-1.00
     * - 0.95表示95折
     * 
     * 约束：最大3位整数，2位小数
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal discount;

    /**
     * 账户状态
     * 
     * 控制用户账户的可用性
     * 用于账户管理和安全控制
     * 
     * 状态定义：
     * 0 - 正常：账户正常，可以正常登录和使用
     * 1 - 禁用：账户被管理员禁用，无法登录
     * 
     * 约束：不能为空，默认值为0（正常）
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 用户头像URL
     * 
     * 用户头像图片的网络地址
     * 用于个人资料显示和界面美化
     * 
     * 注意：
     * - 可为空，使用系统默认头像
     * - 应存储完整的URL地址
     * - 建议使用CDN加速访问
     * 
     * 约束：最大长度512字符
     */
    @Column(length = 512)
    private String avatarUrl;

    /**
     * 账户创建时间
     * 
     * 记录用户注册的时间戳
     * 用于统计分析和用户管理
     * 
     * 约束：不能为空，由@PrePersist自动设置
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 账户更新时间
     * 
     * 记录用户信息最后修改的时间戳
     * 用于数据版本控制和同步检查
     * 
     * 约束：不能为空，由@PrePersist和@PreUpdate自动维护
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 实体持久化前的预处理
     * 
     * 在新用户记录插入数据库前自动调用
     * 用于设置默认值和时间戳
     * 
     * 功能：
     * - 自动设置创建时间
     * - 自动设置更新时间
     * - 设置默认权限等级（游客）
     * - 设置默认账户状态（正常）
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        
        // 设置创建时间（如果为空）
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        
        // 设置更新时间
        this.updatedAt = now;
        
        // 设置默认性别为男性
        if (this.gender == null) {
            this.gender = 1;
        }
        
        // 设置默认用户名（如果为空且手机号不为空）
        if (this.username == null && this.phone != null && this.phone.length() >= 4) {
            this.username = this.phone.substring(this.phone.length() - 4);
        }
        
        // 设置默认权限等级为游客
        if (this.permissionLevel == null) {
            this.permissionLevel = 4;
        }
        
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
        
        // 生成会员号（如果为空且权限等级为会员）
        if (this.memberNo == null && this.permissionLevel != null && this.permissionLevel == 3) {
            this.memberNo = generateMemberNo(now);
        }
    }

    /**
     * 实体更新前的预处理
     * 
     * 在用户记录更新前自动调用
     * 用于自动维护更新时间戳
     * 
     * 功能：
     * - 自动更新修改时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 生成会员号
     * 
     * 格式：年月日时分秒+2位随机数
     * 示例：202401011200001
     * 
     * @param createTime 创建时间
     * @return 生成的会员号
     */
    private String generateMemberNo(LocalDateTime createTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = createTime.format(formatter);
        
        Random random = new Random();
        int randomPart = random.nextInt(100); // 0-99的随机数
        
        return timePart + String.format("%02d", randomPart);
    }

    // Getters and Setters
    // 标准的JavaBean方法，用于属性访问和修改
    // 由IDE自动生成，遵循封装原则
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(String memberNo) {
        this.memberNo = memberNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}

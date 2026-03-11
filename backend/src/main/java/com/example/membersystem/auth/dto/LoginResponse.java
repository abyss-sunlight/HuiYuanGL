package com.example.membersystem.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 登录响应数据传输对象
 * 
 * 封装用户登录成功后返回给前端的数据，包括：
 * - JWT访问令牌，用于后续API调用的身份验证
 * - 用户基本信息（ID、昵称、手机号、头像等）
 * - 权限信息（等级和名称）
 * - 账户创建时间
 * 
 * 该对象会被序列化为JSON格式返回给客户端
 */
public class LoginResponse {

    /**
     * JWT访问令牌
     * 
     * 用于后续API请求的身份验证
     * 包含用户ID、手机号和权限等级信息
     * 有效期由JWT配置决定
     */
    private String token;

    /**
     * 用户唯一标识
     * 
     * 数据库主键，用于唯一标识用户
     * 在后续操作中作为用户身份的主要标识
     */
    private Long userId;

    /**
     * 用户名
     * 
     * 系统生成的用户名，默认为手机号后4位
     * 用于系统内部标识和显示
     */
    private String username;

    /**
     * 用户姓氏
     * 
     * 用户的真实姓氏，用于业务记录关联
     * 在睫毛记录和消费记录中使用
     */
    private String lastName;

    /**
     * 性别
     * 
     * 用户性别信息
     * 
     * 性别定义：
     * 1 - 男性
     * 2 - 女性
     */
    private Integer gender;

    /**
     * 会员号
     * 
     * 会员的唯一标识号码
     * 只有会员用户才有此值
     */
    private String memberNo;

    /**
     * 账户金额
     * 
     * 会员账户的余额
     * 只有会员用户才有此值
     */
    private BigDecimal amount;

    /**
     * 折扣率
     * 
     * 会员享受的折扣比例
     * 只有会员用户才有此值
     */
    private BigDecimal discount;

    /**
     * 用户手机号
     * 
     * 用户的登录凭证和联系方式
     * 在系统中具有唯一性约束
     */
    private String phone;

    /**
     * 权限等级
     * 
     * 数值型权限标识：
     * 1 - 店长：拥有所有权限，可管理所有功能
     * 2 - 员工：拥有部分管理权限，可进行日常操作
     * 3 - 会员：普通用户权限，可查看和操作自己的数据
     * 4 - 游客：最低权限，只能进行基本查看
     */
    private Integer permissionLevel;

    /**
     * 权限名称
     * 
     * 权限等级的文字描述，便于前端显示
     * 由permissionLevel自动映射生成
     */
    private String permissionName;

    /**
     * 用户头像URL
     * 
     * 用户头像图片的网络地址
     * 可为空，使用默认头像
     */
    private String avatarUrl;

    /**
     * 账户创建时间
     * 
     * 用户注册的时间戳
     * 用于显示账户注册时长等信息
     */
    private LocalDateTime createdAt;

    /**
     * 获取JWT访问令牌
     * 
     * @return JWT令牌字符串
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置JWT访问令牌
     * 
     * @param token JWT令牌字符串
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取用户ID
     * 
     * @return 用户唯一标识
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * 
     * @param userId 用户唯一标识
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名
     * 
     * @return 用户名字符串
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * 
     * @param username 用户名字符串
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户姓氏
     * 
     * @return 用户姓氏字符串
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * 设置用户姓氏
     * 
     * @param lastName 用户姓氏字符串
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 获取性别
     * 
     * @return 性别数字
     */
    public Integer getGender() {
        return gender;
    }

    /**
     * 设置性别
     * 
     * @param gender 性别数字
     */
    public void setGender(Integer gender) {
        this.gender = gender;
    }

    /**
     * 获取用户手机号
     * 
     * @return 手机号字符串
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置用户手机号
     * 
     * @param phone 手机号字符串
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取权限等级
     * 
     * @return 权限等级数字
     */
    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * 设置权限等级
     * 
     * 设置权限等级的同时自动设置对应的权限名称
     * 
     * @param permissionLevel 权限等级数字
     */
    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
        // 自动设置权限名称，确保数据一致性
        this.permissionName = getPermissionName(permissionLevel);
    }

    /**
     * 获取权限名称
     * 
     * @return 权限名称字符串
     */
    public String getPermissionName() {
        return permissionName;
    }

    /**
     * 设置权限名称
     * 
     * @param permissionName 权限名称字符串
     */
    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    /**
     * 获取头像URL
     * 
     * @return 头像URL字符串
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 设置头像URL
     * 
     * @param avatarUrl 头像URL字符串
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 获取账户创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置账户创建时间
     * 
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 根据权限等级获取权限名称
     * 
     * 私有方法，用于内部权限等级到名称的映射
     * 确保权限名称的一致性和准确性
     * 
     * @param level 权限等级数字
     * @return 对应的权限名称
     */
    private String getPermissionName(Integer level) {
        switch (level) {
            case 1: return "店长";
            case 2: return "员工";
            case 3: return "会员";
            case 4: return "游客";
            default: return "未知";
        }
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

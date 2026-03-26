package com.example.membersystem.user.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 用户创建请求DTO
 * 
 * 用于创建新用户的数据传输对象，包含所有必要的用户信息字段
 * 根据用户权限等级，某些字段为可选
 */
public class UserCreateRequest {

    /**
     * 权限等级
     * 
     * 权限定义：
     * 1 - 店长：系统管理员（不允许创建）
     * 2 - 员工：操作员
     * 3 - 会员：普通用户
     * 4 - 游客：最低权限
     * 
     * 约束：不能为空，不能为1（不能创建店长）
     */
    @NotNull(message = "权限等级不能为空")
    @Min(value = 2, message = "权限等级不能低于2")
    @Max(value = 4, message = "权限等级不能高于4")
    private Integer permissionLevel;

    /**
     * 用户姓氏
     * 
     * 用户的真实姓氏，用于业务记录关联
     * 
     * 约束：不能为空，最大长度50字符
     */
    @NotBlank(message = "姓氏不能为空")
    @Size(max = 50, message = "姓氏长度不能超过50个字符")
    private String lastName;

    /**
     * 用户名
     * 
     * 系统生成的用户名，可选字段
     * 如果未提供，系统将自动生成（默认为手机号后4位）
     * 
     * 约束：可选，最大长度64字符
     */
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    /**
     * 用户头像URL
     * 
     * 用户头像图片的网络地址，可选字段
     * 如果未提供，将使用系统默认头像
     * 
     * 约束：可选，最大长度512字符
     */
    @Size(max = 512, message = "头像URL长度不能超过512个字符")
    private String avatarUrl;

    /**
     * 性别
     * 
     * 用户性别信息
     * 
     * 性别定义：
     * 1 - 男性
     * 2 - 女性
     * 
     * 约束：不能为空
     */
    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;

    /**
     * 手机号
     * 
     * 用户的主要登录凭证和联系方式
     * 在系统中具有唯一性约束
     * 
     * 约束：不能为空，格式验证
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 账户初始余额
     * 
     * 仅会员用户可设置此字段
     * 用于初始化会员账户余额
     * 
     * 约束：仅会员，最小值0，最大值99999999.99
     */
    @DecimalMin(value = "0.00", message = "余额不能小于0")
    @DecimalMax(value = "99999999.99", message = "余额不能超过99999999.99")
    private BigDecimal amount;

    /**
     * 会员折扣率
     * 
     * 仅会员用户可设置此字段
     * 用于设置会员享受的折扣比例
     * 
     * 约束：仅会员，范围0.00-1.00
     */
    @DecimalMin(value = "0.00", message = "折扣率不能小于0")
    @DecimalMax(value = "1.00", message = "折扣率不能大于1")
    private BigDecimal discount;

    // Getters and Setters

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

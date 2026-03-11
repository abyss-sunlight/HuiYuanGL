package com.example.membersystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 密码登录请求数据传输对象
 * 
 * 封装客户端发送的密码登录请求参数，包括：
 * - 登录类型标识
 * - 用户手机号
 * - 登录密码
 * 
 * 使用Jakarta Validation注解进行参数验证
 * 确保传入数据的完整性和格式正确性
 */
public class PasswordLoginRequest {
    
    /**
     * 登录类型标识
     * 
     * 用于区分不同的登录方式
     * 固定值：password - 表示密码登录
     * 
     * 验证规则：不能为空
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    /**
     * 用户手机号
     * 
     * 用户的登录凭证，必须是在系统中已注册的手机号
     * 用于查找对应的用户账户
     * 
     * 验证规则：
     * - 不能为空
     * - 必须符合中国大陆手机号格式：1开头，第二位3-9，共11位数字
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 用户密码
     * 
     * 用户的登录密码，用于验证用户身份
     * 在数据库中存储的是加密后的密码
     * 
     * 验证规则：不能为空
     * 
     * 安全注意：
     * - 传输过程中应使用HTTPS加密
     * - 实际应用中密码应该经过加密存储
     * - 建议使用强密码策略
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 获取登录类型
     * 
     * @return 登录类型字符串
     */
    public String getLoginType() {
        return loginType;
    }

    /**
     * 设置登录类型
     * 
     * @param loginType 登录类型字符串
     */
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * 获取手机号
     * 
     * @return 手机号字符串
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号
     * 
     * @param phone 手机号字符串
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取密码
     * 
     * @return 密码字符串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * 
     * @param password 密码字符串
     */
    public void setPassword(String password) {
        this.password = password;
    }
}

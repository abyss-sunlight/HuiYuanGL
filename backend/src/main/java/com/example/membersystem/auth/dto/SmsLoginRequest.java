package com.example.membersystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 短信登录请求数据传输对象
 * 
 * 封装客户端发送的短信登录请求参数，包括：
 * - 登录类型标识
 * - 用户手机号
 * - 短信验证码
 * 
 * 使用Jakarta Validation注解进行参数验证
 * 确保传入数据的完整性和格式正确性
 */
public class SmsLoginRequest {
    
    /**
     * 登录类型标识
     * 
     * 用于区分不同的登录方式
     * 固定值：sms - 表示短信验证码登录
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
     * 短信验证码
     * 
     * 通过短信服务发送给用户的6位数字验证码
     * 用于验证用户对该手机号的控制权
     * 
     * 验证规则：不能为空
     * 
     * 注意：实际应用中还需要验证验证码的时效性和使用次数
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

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
     * 获取验证码
     * 
     * @return 验证码字符串
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置验证码
     * 
     * @param code 验证码字符串
     */
    public void setCode(String code) {
        this.code = code;
    }
}

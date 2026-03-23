package com.example.membersystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 修改手机号请求
 */
public class ChangePhoneRequest {
    
    @NotBlank(message = "原手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "原手机号格式不正确")
    private String originalPhone;
    
    @NotBlank(message = "原验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "原验证码格式不正确")
    private String originalCode;
    
    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式不正确")
    private String newPhone;
    
    @NotBlank(message = "新验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "新验证码格式不正确")
    private String newCode;

    public String getOriginalPhone() {
        return originalPhone;
    }

    public void setOriginalPhone(String originalPhone) {
        this.originalPhone = originalPhone;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public void setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
    }

    public String getNewPhone() {
        return newPhone;
    }

    public void setNewPhone(String newPhone) {
        this.newPhone = newPhone;
    }

    public String getNewCode() {
        return newCode;
    }

    public void setNewCode(String newCode) {
        this.newCode = newCode;
    }
}

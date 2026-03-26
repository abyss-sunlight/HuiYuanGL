package com.example.membersystem.user.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 用户状态更新请求DTO
 * 
 * 用于传递用户状态更新的请求参数
 * 包含状态验证和权限控制
 */
public class UserStatusUpdateRequest {

    /**
     * 用户状态
     * 
     * 状态定义：
     * 0 - 正常：账户正常，可以正常使用
     * 1 - 禁用：账户被禁用，无法登录
     * 
     * 验证规则：不能为空，只能是0或1
     */
    @NotNull(message = "用户状态不能为空")
    private Integer status;

    public UserStatusUpdateRequest() {
    }

    public UserStatusUpdateRequest(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 验证状态值是否有效
     * 
     * @return 是否为有效状态
     */
    public boolean isValidStatus() {
        return status != null && (status == 0 || status == 1);
    }

    /**
     * 获取状态描述
     * 
     * @return 状态描述文本
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知状态";
        }
        return status == 0 ? "正常" : "禁用";
    }
}

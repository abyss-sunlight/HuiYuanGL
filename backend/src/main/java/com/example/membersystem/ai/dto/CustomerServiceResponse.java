package com.example.membersystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI客服响应DTO
 * 
 * 用于封装AI客服的响应结果
 */
@Data
@Schema(description = "AI客服响应")
public class CustomerServiceResponse {
    
    @Schema(description = "响应状态", example = "success")
    private String status;
    
    @Schema(description = "AI回复内容")
    private String content;
    
    @Schema(description = "建议的操作类型", example = "navigate")
    private String actionType;
    
    @Schema(description = "操作参数")
    private Map<String, Object> actionParams;
    
    @Schema(description = "快捷问题建议")
    private List<String> quickQuestions;
    
    @Schema(description = "错误信息")
    private String errorMessage;
    
    @Schema(description = "会话ID")
    private String sessionId;
    
    @Schema(description = "是否需要联系店长")
    private Boolean needContactManager;
    
    @Schema(description = "店长联系方式")
    private String managerContact;
    
    // 手动添加getter/setter方法
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public Map<String, Object> getActionParams() {
        return actionParams;
    }
    
    public void setActionParams(Map<String, Object> actionParams) {
        this.actionParams = actionParams;
    }
    
    public List<String> getQuickQuestions() {
        return quickQuestions;
    }
    
    public void setQuickQuestions(List<String> quickQuestions) {
        this.quickQuestions = quickQuestions;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Boolean getNeedContactManager() {
        return needContactManager;
    }
    
    public void setNeedContactManager(Boolean needContactManager) {
        this.needContactManager = needContactManager;
    }
    
    public String getManagerContact() {
        return managerContact;
    }
    
    public void setManagerContact(String managerContact) {
        this.managerContact = managerContact;
    }
    
    /**
     * 创建成功响应
     */
    public static CustomerServiceResponse success(String content) {
        CustomerServiceResponse response = new CustomerServiceResponse();
        response.setStatus("success");
        response.setContent(content);
        response.setNeedContactManager(false);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static CustomerServiceResponse error(String errorMessage) {
        CustomerServiceResponse response = new CustomerServiceResponse();
        response.setStatus("error");
        response.setErrorMessage(errorMessage);
        response.setNeedContactManager(false);
        return response;
    }
    
    /**
     * 创建需要联系店长的响应
     */
    public static CustomerServiceResponse needContactManager(String content, String managerContact) {
        CustomerServiceResponse response = new CustomerServiceResponse();
        response.setStatus("success");
        response.setContent(content);
        response.setNeedContactManager(true);
        response.setManagerContact(managerContact);
        return response;
    }
    
    /**
     * 检查响应是否成功
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }
    
    /**
     * 检查是否需要联系店长
     */
    public boolean isNeedContactManager() {
        return Boolean.TRUE.equals(needContactManager);
    }
}

package com.example.membersystem.ai.dto;

/**
 * 元宝AI响应DTO
 */
public class YuanbaiAIResponse {
    
    /**
     * 响应状态
     */
    private String status = "success";
    
    /**
     * AI分析内容
     */
    private String content;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 使用token数量
     */
    private Integer usageTokens;
    
    public YuanbaiAIResponse() {
        // 默认构造函数
    }
    
    public YuanbaiAIResponse(String status, String content) {
        this.status = status;
        this.content = content;
    }
    
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Integer getUsageTokens() {
        return usageTokens;
    }
    
    public void setUsageTokens(Integer usageTokens) {
        this.usageTokens = usageTokens;
    }
    
    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return "success".equals(status);
    }
}

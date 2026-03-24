package com.example.membersystem.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI客服请求DTO
 * 
 * 用于封装AI客服的请求参数
 */
@Data
@Schema(description = "AI客服请求")
public class CustomerServiceRequest {
    
    @Schema(description = "用户消息", example = "如何成为会员？")
    private String message;
    
    @Schema(description = "用户信息")
    private UserInfo userInfo;
    
    @Schema(description = "对话历史记录")
    private List<ChatMessage> chatHistory;
    
    @Schema(description = "会话ID")
    private String sessionId;
    
    // 手动添加getter/setter方法
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    public List<ChatMessage> getChatHistory() {
        return chatHistory;
    }
    
    public void setChatHistory(List<ChatMessage> chatHistory) {
        this.chatHistory = chatHistory;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * 用户信息
     */
    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        
        @Schema(description = "用户ID")
        private Long userId;
        
        @Schema(description = "手机号")
        private String phone;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "权限等级", example = "3")
        private Integer permissionLevel;
        
        @Schema(description = "会员号")
        private String memberNo;
        
        @Schema(description = "账户余额")
        private Double amount;
        
        @Schema(description = "是否为会员")
        private Boolean isMember;
        
        // 手动添加getter/setter方法
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Integer getPermissionLevel() {
            return permissionLevel;
        }
        
        public void setPermissionLevel(Integer permissionLevel) {
            this.permissionLevel = permissionLevel;
        }
        
        public String getMemberNo() {
            return memberNo;
        }
        
        public void setMemberNo(String memberNo) {
            this.memberNo = memberNo;
        }
        
        public Double getAmount() {
            return amount;
        }
        
        public void setAmount(Double amount) {
            this.amount = amount;
        }
        
        public Boolean getIsMember() {
            return isMember;
        }
        
        public void setIsMember(Boolean isMember) {
            this.isMember = isMember;
        }
    }
    
    /**
     * 聊天消息
     */
    @Data
    @Schema(description = "聊天消息")
    public static class ChatMessage {
        
        @Schema(description = "消息角色", example = "user")
        private String role; // user, assistant
        
        @Schema(description = "消息内容")
        private String content;
        
        @Schema(description = "消息时间戳")
        private Long timestamp;
        
        // 手动添加getter/setter方法
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}

package com.example.membersystem.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户状态变更日志实体
 * 
 * 记录用户账户状态的变更历史，包括：
 * - 状态变更时间
 * - 操作者信息
 * - 变更前后的状态
 * - 变更原因等
 */
@Entity
@Table(name = "user_status_log")
public class UserStatusLog {

    /**
     * 日志主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 目标用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 操作者用户ID
     */
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 变更前状态
     * 0 - 正常，1 - 禁用
     */
    @Column(name = "old_status", nullable = false)
    private Integer oldStatus;

    /**
     * 变更后状态
     * 0 - 正常，1 - 禁用
     */
    @Column(name = "new_status", nullable = false)
    private Integer newStatus;

    /**
     * 变更原因
     */
    @Column(name = "reason", length = 255)
    private String reason;

    /**
     * 操作时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 实体持久化前的预处理
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(Integer oldStatus) {
        this.oldStatus = oldStatus;
    }

    public Integer getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Integer newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

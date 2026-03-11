package com.example.membersystem.discount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 充值折扣实体类
 * 
 * 存储不同充值金额对应的折扣信息
 */
@Entity
@Table(name = "recharge_discount")
public class RechargeDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 充值金额
     */
    @Column(name = "recharge_amount", nullable = false, precision = 10, scale = 2)
    @JsonProperty("rechargeAmount")
    private BigDecimal rechargeAmount;

    /**
     * 折扣率（0.0000-1.0000）
     */
    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 4)
    @JsonProperty("discountRate")
    private BigDecimal discountRate;

    /**
     * 折扣百分比（0.00-100.00）
     */
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    @JsonProperty("discountPercentage")
    private BigDecimal discountPercentage;

    /**
     * 生效日期
     */
    @Column(name = "effective_date", nullable = false)
    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active", nullable = false)
    @JsonProperty("isActive")
    private Boolean isActive;

    /**
     * 创建人
     */
    @Column(name = "created_by", nullable = false, length = 50)
    @JsonProperty("createdBy")
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * 默认构造器
     */
    public RechargeDiscount() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 带参构造器
     */
    public RechargeDiscount(BigDecimal rechargeAmount, BigDecimal discountRate, 
                          BigDecimal discountPercentage, LocalDate effectiveDate, 
                          Boolean isActive, String createdBy) {
        this();
        this.rechargeAmount = rechargeAmount;
        this.discountRate = discountRate;
        this.discountPercentage = discountPercentage;
        this.effectiveDate = effectiveDate;
        this.isActive = isActive;
        this.createdBy = createdBy;
    }

    // JPA生命周期回调
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(BigDecimal rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "RechargeDiscount{" +
                "id=" + id +
                ", rechargeAmount=" + rechargeAmount +
                ", discountRate=" + discountRate +
                ", discountPercentage=" + discountPercentage +
                ", effectiveDate=" + effectiveDate +
                ", isActive=" + isActive +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

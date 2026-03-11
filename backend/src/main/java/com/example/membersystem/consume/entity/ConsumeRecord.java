package com.example.membersystem.consume.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消费记录实体类
 * 
 * 存储会员的消费记录信息
 */
@Entity
@Table(name = "consume_records")
public class ConsumeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会员手机号
     */
    @Column(name = "phone", nullable = false, length = 11)
    @JsonProperty("phone")
    private String phone;

    /**
     * 会员姓氏
     */
    @Column(name = "last_name", nullable = false, length = 50)
    @JsonProperty("lastName")
    private String lastName;

    /**
     * 性别：1-男，2-女
     */
    @Column(name = "gender", nullable = false)
    @JsonProperty("gender")
    private Integer gender;

    /**
     * 余额
     */
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @JsonProperty("balance")
    private BigDecimal balance;

    /**
     * 消费金额
     */
    @Column(name = "consume_amount", nullable = false, precision = 10, scale = 2)
    @JsonProperty("consumeAmount")
    private BigDecimal consumeAmount;

    /**
     * 消费项目（中文）
     */
    @Column(name = "consume_item", nullable = false, length = 100)
    @JsonProperty("consumeItem")
    private String consumeItem;

    /**
     * 消费日期
     */
    @Column(name = "consume_date", nullable = false)
    @JsonProperty("consumeDate")
    private LocalDate consumeDate;

    /**
     * 消费类型：支出或充值
     */
    @Column(name = "consume_type", nullable = false, length = 10)
    @JsonProperty("consumeType")
    private String consumeType;

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
    public ConsumeRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完整构造器
     */
    public ConsumeRecord(String phone, String lastName, Integer gender, BigDecimal balance,
                         BigDecimal consumeAmount, String consumeItem, LocalDate consumeDate, String consumeType) {
        this();
        this.phone = phone;
        this.lastName = lastName;
        this.gender = gender;
        this.balance = balance;
        this.consumeAmount = consumeAmount;
        this.consumeItem = consumeItem;
        this.consumeDate = consumeDate;
        this.consumeType = consumeType;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getConsumeAmount() {
        return consumeAmount;
    }

    public void setConsumeAmount(BigDecimal consumeAmount) {
        this.consumeAmount = consumeAmount;
    }

    public String getConsumeItem() {
        return consumeItem;
    }

    public void setConsumeItem(String consumeItem) {
        this.consumeItem = consumeItem;
    }

    public LocalDate getConsumeDate() {
        return consumeDate;
    }

    public void setConsumeDate(LocalDate consumeDate) {
        this.consumeDate = consumeDate;
    }

    public String getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(String consumeType) {
        this.consumeType = consumeType;
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
        return "ConsumeRecord{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", balance=" + balance +
                ", consumeAmount=" + consumeAmount +
                ", consumeItem='" + consumeItem + '\'' +
                ", consumeDate=" + consumeDate +
                ", consumeType='" + consumeType + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

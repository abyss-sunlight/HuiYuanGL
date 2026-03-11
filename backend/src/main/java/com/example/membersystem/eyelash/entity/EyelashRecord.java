package com.example.membersystem.eyelash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 睫毛记录实体类
 * 
 * 存储会员的睫毛护理和嫁接服务记录
 */
@Entity
@Table(name = "eyelash_records")
public class EyelashRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 手机号
     */
    @Column(name = "phone", nullable = false, length = 11)
    @JsonProperty("phone")
    private String phone;

    /**
     * 姓氏
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
     * 款式（中文）
     */
    @Column(name = "style", nullable = false, length = 100)
    @JsonProperty("style")
    private String style;

    /**
     * 型号（数字）
     */
    @Column(name = "model_number", nullable = false, length = 50)
    @JsonProperty("modelNumber")
    private String modelNumber;

    /**
     * 睫毛长度（数字，单位：mm）
     */
    @Column(name = "length", nullable = false)
    @JsonProperty("length")
    private Double length;

    /**
     * 翘度（英文，如：C、D、J等）
     */
    @Column(name = "curl", nullable = false, length = 10)
    @JsonProperty("curl")
    private String curl;

    /**
     * 记录日期
     */
    @Column(name = "record_date", nullable = false)
    @JsonProperty("recordDate")
    private LocalDate recordDate;

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
    public EyelashRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完整构造器
     */
    public EyelashRecord(String phone, String lastName, Integer gender, 
                        String style, String modelNumber, Double length, 
                        String curl, LocalDate recordDate) {
        this();
        this.phone = phone;
        this.lastName = lastName;
        this.gender = gender;
        this.style = style;
        this.modelNumber = modelNumber;
        this.length = length;
        this.curl = curl;
        this.recordDate = recordDate;
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

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public String getCurl() {
        return curl;
    }

    public void setCurl(String curl) {
        this.curl = curl;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
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
        return "EyelashRecord{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", style='" + style + '\'' +
                ", modelNumber='" + modelNumber + '\'' +
                ", length=" + length +
                ", curl='" + curl + '\'' +
                ", recordDate=" + recordDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

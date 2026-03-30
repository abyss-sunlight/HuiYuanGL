package com.example.membersystem.security.hash.model;

import java.time.LocalDateTime;

/**
 * 哈希结果模型
 * 包含哈希值、算法信息、创建时间等
 */
public class HashResult {
    
    /**
     * 哈希值
     */
    private String hash;
    
    /**
     * 使用的算法
     */
    private HashAlgorithm algorithm;
    
    /**
     * 算法配置
     */
    private AlgorithmConfig config;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 盐值（如果适用）
     */
    private String salt;
    
    /**
     * 处理耗时（毫秒）
     */
    private long processingTimeMs;
    
    // 构造函数
    public HashResult() {
        this.createdAt = LocalDateTime.now();
    }
    
    public HashResult(String hash, HashAlgorithm algorithm, AlgorithmConfig config) {
        this.hash = hash;
        this.algorithm = algorithm;
        this.config = config;
        this.createdAt = LocalDateTime.now();
    }
    
    public HashResult(String hash, HashAlgorithm algorithm, AlgorithmConfig config, 
                     String salt, long processingTimeMs) {
        this.hash = hash;
        this.algorithm = algorithm;
        this.config = config;
        this.salt = salt;
        this.processingTimeMs = processingTimeMs;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(HashAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    public AlgorithmConfig getConfig() {
        return config;
    }
    
    public void setConfig(AlgorithmConfig config) {
        this.config = config;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    /**
     * 获取算法标识符
     * 用于在哈希值中嵌入算法信息
     */
    public String getAlgorithmIdentifier() {
        switch (algorithm) {
            case BCRYPT:
                return "BCRYPT";
            case ARGON2:
                return "ARGON2";
            case HYBRID:
                return "HYBRID";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * 生成完整的哈希字符串（包含算法信息）
     */
    public String getFullHashString() {
        if (hash == null) {
            return null;
        }
        
        // 如果哈希值已经包含算法前缀，直接返回
        if (hash.startsWith("$")) {
            return hash;
        }
        
        // 否则添加算法标识符
        return "$" + getAlgorithmIdentifier() + "$" + hash;
    }
    
    @Override
    public String toString() {
        return "HashResult{" +
                "algorithm=" + algorithm +
                ", createdAt=" + createdAt +
                ", processingTimeMs=" + processingTimeMs +
                '}';
    }
}

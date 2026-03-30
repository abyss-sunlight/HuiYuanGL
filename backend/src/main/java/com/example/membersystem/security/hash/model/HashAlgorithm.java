package com.example.membersystem.security.hash.model;

/**
 * 哈希算法枚举
 * 支持的哈希算法类型
 */
public enum HashAlgorithm {
    /**
     * BCrypt算法
     */
    BCRYPT("BCrypt", "经典的密码哈希算法，具有良好的抗彩虹表能力"),
    
    /**
     * Argon2算法
     */
    ARGON2("Argon2", "现代密码哈希算法，抗GPU/ASIC攻击能力强"),
    
    /**
     * 混合算法
     */
    HYBRID("Hybrid", "BCrypt + Argon2混合算法，提供更高安全性");
    
    private final String name;
    private final String description;
    
    HashAlgorithm(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}

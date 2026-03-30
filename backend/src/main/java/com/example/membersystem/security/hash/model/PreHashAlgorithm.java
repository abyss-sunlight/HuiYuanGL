package com.example.membersystem.security.hash.model;

/**
 * 预哈希算法枚举
 * 用于混合模式中的预处理步骤
 */
public enum PreHashAlgorithm {
    /**
     * SHA-256算法
     */
    SHA256("SHA-256", "安全哈希算法256位"),
    
    /**
     * SHA-512算法
     */
    SHA512("SHA-512", "安全哈希算法512位"),
    
    /**
     * 无预哈希
     */
    NONE("None", "不进行预哈希处理");
    
    private final String name;
    private final String description;
    
    PreHashAlgorithm(String name, String description) {
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

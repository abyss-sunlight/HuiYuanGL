package com.example.membersystem.security.hash.provider;

import com.example.membersystem.security.hash.model.AlgorithmConfig;
import com.example.membersystem.security.hash.model.HashAlgorithm;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * BCrypt哈希提供者
 * 实现BCrypt算法的哈希和验证功能
 */
@Component
public class BCryptHashProvider implements HashProvider {
    
    private final AlgorithmConfig config;
    
    /**
     * 构造函数
     * 
     * @param config 算法配置
     */
    public BCryptHashProvider(AlgorithmConfig config) {
        this.config = config;
    }
    
    /**
     * 默认构造函数，使用默认配置
     */
    public BCryptHashProvider() {
        this.config = AlgorithmConfig.createDefaultConfig();
    }
    
    @Override
    public String hash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            // 生成盐值并哈希
            String salt = BCrypt.gensalt(config.getBcryptCost());
            return BCrypt.hashpw(password, salt);
        } catch (Exception e) {
            throw new RuntimeException("BCrypt哈希失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verify(String password, String hash) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (hash == null || hash.trim().isEmpty()) {
            throw new IllegalArgumentException("哈希值不能为空");
        }
        
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            // 如果哈希格式不正确，返回false而不是抛出异常
            return false;
        }
    }
    
    @Override
    public HashAlgorithm getSupportedAlgorithm() {
        return HashAlgorithm.BCRYPT;
    }
    
    @Override
    public boolean supportsHashFormat(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        
        // BCrypt哈希格式: $2a$[cost]$[salt+hash]
        // 或者: $2b$[cost]$[salt+hash]
        // 或者: $2y$[cost]$[salt+hash]
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
    
    /**
     * 获取哈希的成本因子
     * 
     * @param hash BCrypt哈希值
     * @return 成本因子，如果无法解析则返回-1
     */
    public int extractCost(String hash) {
        if (!supportsHashFormat(hash)) {
            return -1;
        }
        
        try {
            String[] parts = hash.split("\\$");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return -1;
    }
    
    /**
     * 检查是否需要重新哈希
     * 
     * @param hash 现有的哈希值
     * @return 是否需要重新哈希
     */
    public boolean needsRehash(String hash) {
        int currentCost = extractCost(hash);
        return currentCost != config.getBcryptCost();
    }
    
    /**
     * 更新配置
     * 
     * @param config 新的配置
     */
    public void updateConfig(AlgorithmConfig config) {
        if (config != null) {
            // 注意：这里不能直接修改config对象的引用，因为其他组件可能还在使用原配置
            // 在实际应用中，应该考虑配置的不可变性
        }
    }
}

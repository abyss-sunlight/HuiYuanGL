package com.example.membersystem.security.hash.provider;

import com.example.membersystem.security.hash.model.AlgorithmConfig;
import com.example.membersystem.security.hash.model.HashAlgorithm;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/**
 * Argon2哈希提供者
 * 实现Argon2算法的哈希和验证功能
 */
@Component
public class Argon2HashProvider implements HashProvider {
    
    private final Argon2 argon2;
    private final AlgorithmConfig config;
    
    /**
     * 构造函数
     * 
     * @param config 算法配置
     */
    public Argon2HashProvider(AlgorithmConfig config) {
        this.config = config;
        this.argon2 = Argon2Factory.create(
            config.getArgon2Type(),
            32, // 盐值长度
            64  // 哈希长度
        );
    }
    
    /**
     * 默认构造函数，使用默认配置
     */
    public Argon2HashProvider() {
        this.config = AlgorithmConfig.createDefaultConfig();
        this.argon2 = Argon2Factory.create(
            config.getArgon2Type(),
            32, // 盐值长度
            64  // 哈希长度
        );
    }
    
    @Override
    public String hash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            return argon2.hash(
                config.getArgon2TimeCost(),
                config.getArgon2MemoryCost(),
                config.getArgon2Parallelism(),
                password.toCharArray()
            );
        } catch (Exception e) {
            throw new RuntimeException("Argon2哈希失败: " + e.getMessage(), e);
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
            return argon2.verify(hash, password.toCharArray());
        } catch (Exception e) {
            // 如果哈希格式不正确，返回false而不是抛出异常
            return false;
        }
    }
    
    @Override
    public HashAlgorithm getSupportedAlgorithm() {
        return HashAlgorithm.ARGON2;
    }
    
    @Override
    public boolean supportsHashFormat(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        
        // Argon2哈希格式: $argon2id$v=19$m=65536,t=3,p=4$[salt]$[hash]
        // 或者: $argon2i$v=19$m=65536,t=3,p=4$[salt]$[hash]
        // 或者: $argon2d$v=19$m=65536,t=3,p=4$[salt]$[hash]
        return hash.startsWith("$argon2id$") || 
               hash.startsWith("$argon2i$") || 
               hash.startsWith("$argon2d$");
    }
    
    /**
     * 提取Argon2参数
     * 
     * @param hash Argon2哈希值
     * @return 包含参数的对象，如果无法解析则返回null
     */
    public Argon2Parameters extractParameters(String hash) {
        if (!supportsHashFormat(hash)) {
            return null;
        }
        
        try {
            // 解析格式: $argon2id$v=19$m=65536,t=3,p=4$[salt]$[hash]
            String[] parts = hash.split("\\$");
            if (parts.length < 6) {
                return null;
            }
            
            Argon2Parameters params = new Argon2Parameters();
            
            // 解析参数部分
            String paramPart = parts[3]; // v=19,m=65536,t=3,p=4
            String[] paramPairs = paramPart.split(",");
            
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    int value = Integer.parseInt(keyValue[1]);
                    
                    switch (key) {
                        case "m":
                            params.memoryCost = value;
                            break;
                        case "t":
                            params.timeCost = value;
                            break;
                        case "p":
                            params.parallelism = value;
                            break;
                    }
                }
            }
            
            return params;
        } catch (Exception e) {
            // 解析失败，返回null
            return null;
        }
    }
    
    /**
     * 检查是否需要重新哈希
     * 
     * @param hash 现有的哈希值
     * @return 是否需要重新哈希
     */
    public boolean needsRehash(String hash) {
        Argon2Parameters currentParams = extractParameters(hash);
        if (currentParams == null) {
            return true;
        }
        
        return currentParams.timeCost != config.getArgon2TimeCost() ||
               currentParams.memoryCost != config.getArgon2MemoryCost() ||
               currentParams.parallelism != config.getArgon2Parallelism();
    }
    
    /**
     * Argon2参数类
     */
    public static class Argon2Parameters {
        public int timeCost;
        public int memoryCost;
        public int parallelism;
        
        @Override
        public String toString() {
            return "Argon2Parameters{" +
                    "timeCost=" + timeCost +
                    ", memoryCost=" + memoryCost +
                    ", parallelism=" + parallelism +
                    '}';
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (argon2 != null) {
            // Argon2库不需要手动清理，由GC自动处理
            // 这里可以添加其他清理逻辑
        }
    }
}

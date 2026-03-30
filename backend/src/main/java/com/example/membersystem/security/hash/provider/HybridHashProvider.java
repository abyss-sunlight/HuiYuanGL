package com.example.membersystem.security.hash.provider;

import com.example.membersystem.security.hash.model.AlgorithmConfig;
import com.example.membersystem.security.hash.model.HashAlgorithm;
import com.example.membersystem.security.hash.model.PreHashAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * 混合哈希提供者
 * 实现BCrypt + Argon2混合算法，支持预哈希和Pepper
 */
@Component
public class HybridHashProvider implements HashProvider {
    
    private final AlgorithmConfig config;
    private final BCryptHashProvider bcryptProvider;
    private final Argon2HashProvider argon2Provider;
    
    /**
     * 构造函数
     * 
     * @param config 算法配置
     * @param bcryptProvider BCrypt提供者
     * @param argon2Provider Argon2提供者
     */
    public HybridHashProvider(AlgorithmConfig config, 
                             BCryptHashProvider bcryptProvider, 
                             Argon2HashProvider argon2Provider) {
        this.config = config;
        this.bcryptProvider = bcryptProvider;
        this.argon2Provider = argon2Provider;
    }
    
    /**
     * 默认构造函数
     */
    public HybridHashProvider() {
        this.config = AlgorithmConfig.createDefaultConfig();
        this.bcryptProvider = new BCryptHashProvider(config);
        this.argon2Provider = new Argon2HashProvider(config);
    }
    
    @Override
    public String hash(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            String processedPassword = password;
            
            // 第一步：预哈希处理
            if (config.isEnablePreHash()) {
                processedPassword = preHash(processedPassword);
            }
            
            // 第二步：添加Pepper
            if (config.isEnablePepper()) {
                processedPassword = addPepper(processedPassword);
            }
            
            // 第三步：混合哈希处理
            return performHybridHash(processedPassword);
            
        } catch (Exception e) {
            throw new RuntimeException("混合哈希失败: " + e.getMessage(), e);
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
            // 检查是否是混合哈希格式
            if (!supportsHashFormat(hash)) {
                return false;
            }
            
            // 提取混合哈希的各个部分
            HybridHashComponents components = extractHybridHashComponents(hash);
            if (components == null) {
                return false;
            }
            
            // 重新处理密码
            String processedPassword = password;
            
            // 预哈希处理
            if (config.isEnablePreHash()) {
                processedPassword = preHash(processedPassword);
            }
            
            // 添加Pepper
            if (config.isEnablePepper()) {
                processedPassword = addPepper(processedPassword);
            }
            
            // 验证逻辑：由于BCrypt每次哈希结果不同，我们需要验证Argon2部分
            // 但是我们需要验证原始BCrypt哈希是否有效，然后验证Argon2部分
            // 首先验证BCrypt部分
            if (!bcryptProvider.verify(processedPassword, components.bcryptHash)) {
                return false;
            }
            
            // 然后验证Argon2部分是否匹配原始BCrypt哈希
            return argon2Provider.verify(components.bcryptHash, components.argon2Hash);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public HashAlgorithm getSupportedAlgorithm() {
        return HashAlgorithm.HYBRID;
    }
    
    @Override
    public boolean supportsHashFormat(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return false;
        }
        
        // 混合哈希格式: $HYBRID$[bcrypt_hash]$[argon2_hash]
        return hash.startsWith("$HYBRID$") && hash.contains("$$");
    }
    
    /**
     * 执行混合哈希
     * 
     * @param processedPassword 处理后的密码
     * @return 混合哈希值
     */
    private String performHybridHash(String processedPassword) {
        // 第一步：BCrypt哈希
        String bcryptHash = bcryptProvider.hash(processedPassword);
        
        // 第二步：Argon2哈希（对BCrypt结果进行哈希）
        String argon2Hash = argon2Provider.hash(bcryptHash);
        
        // 组合结果：$HYBRID$[bcrypt_hash]$[argon2_hash]
        return "$HYBRID$" + bcryptHash + "$" + argon2Hash;
    }
    
    /**
     * 预哈希处理
     * 
     * @param password 原始密码
     * @return 预哈希后的密码
     */
    private String preHash(String password) {
        switch (config.getPreHashAlgorithm()) {
            case SHA256:
                return DigestUtils.sha256Hex(password);
            case SHA512:
                return DigestUtils.sha512Hex(password);
            case NONE:
            default:
                return password;
        }
    }
    
    /**
     * 添加Pepper
     * 
     * @param password 密码
     * @return 添加Pepper后的密码
     */
    private String addPepper(String password) {
        String pepperValue = config.getPepper();
        if (pepperValue == null || pepperValue.trim().isEmpty()) {
            throw new IllegalStateException("Pepper已启用但值为空");
        }
        return DigestUtils.sha256Hex(password + pepperValue);
    }
    
    /**
     * 提取混合哈希组件
     * 
     * @param hash 混合哈希值
     * @return 哈希组件对象
     */
    public HybridHashComponents extractHybridHashComponents(String hash) {
        try {
            // 移除前缀 $HYBRID$
            String content = hash.substring(8); // 去掉 "$HYBRID$"
            
            // 分割BCrypt和Argon2部分
            int separatorIndex = content.lastIndexOf("$");
            if (separatorIndex == -1) {
                return null;
            }
            
            String bcryptHash = content.substring(0, separatorIndex);
            String argon2Hash = content.substring(separatorIndex + 1);
            
            return new HybridHashComponents(bcryptHash, argon2Hash);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 混合哈希组件
     */
    public static class HybridHashComponents {
        public final String bcryptHash;
        public final String argon2Hash;
        
        HybridHashComponents(String bcryptHash, String argon2Hash) {
            this.bcryptHash = bcryptHash;
            this.argon2Hash = argon2Hash;
        }
    }
    
    /**
     * 检查是否需要重新哈希
     * 
     * @param hash 现有的哈希值
     * @return 是否需要重新哈希
     */
    public boolean needsRehash(String hash) {
        HybridHashComponents components = extractHybridHashComponents(hash);
        if (components == null) {
            return true;
        }
        
        // 检查BCrypt部分是否需要重新哈希
        boolean bcryptNeedsRehash = bcryptProvider.needsRehash(components.bcryptHash);
        
        // 检查Argon2部分是否需要重新哈希
        boolean argon2NeedsRehash = argon2Provider.needsRehash(components.argon2Hash);
        
        return bcryptNeedsRehash || argon2NeedsRehash;
    }
    
    /**
     * 更新配置
     * 
     * @param config 新的配置
     */
    public void updateConfig(AlgorithmConfig config) {
        if (config != null) {
            // 重新初始化提供者
            // 注意：在实际应用中，应该考虑配置的不可变性和线程安全性
        }
    }
}

package com.example.membersystem.security.hash;

import com.example.membersystem.security.hash.model.*;
import com.example.membersystem.security.hash.provider.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 混合哈希服务实现类
 * 提供统一的哈希服务接口，支持多种算法
 */
@Service
public class HybridHashServiceImpl implements HybridHashService {
    
    private final Map<HashAlgorithm, HashProvider> providers;
    private final AlgorithmConfig defaultConfig;
    private final HybridHashProvider hybridProvider;
    
    /**
     * 构造函数
     * 
     * @param providers 哈希提供者列表
     */
    public HybridHashServiceImpl(List<HashProvider> providers) {
        this.providers = new HashMap<>();
        this.defaultConfig = AlgorithmConfig.createDefaultConfig();
        
        // 初始化提供者映射
        for (HashProvider provider : providers) {
            this.providers.put(provider.getSupportedAlgorithm(), provider);
        }
        
        // 获取混合提供者
        this.hybridProvider = (HybridHashProvider) this.providers.get(HashAlgorithm.HYBRID);
    }
    
    /**
     * 默认构造函数
     */
    public HybridHashServiceImpl() {
        this.providers = new HashMap<>();
        this.defaultConfig = AlgorithmConfig.createDefaultConfig();
        
        // 初始化默认提供者
        BCryptHashProvider bcryptProvider = new BCryptHashProvider(defaultConfig);
        Argon2HashProvider argon2Provider = new Argon2HashProvider(defaultConfig);
        this.hybridProvider = new HybridHashProvider(defaultConfig, bcryptProvider, argon2Provider);
        
        this.providers.put(HashAlgorithm.BCRYPT, bcryptProvider);
        this.providers.put(HashAlgorithm.ARGON2, argon2Provider);
        this.providers.put(HashAlgorithm.HYBRID, hybridProvider);
    }
    
    @Override
    public HashResult hashPassword(String password, HashAlgorithm algorithm, AlgorithmConfig config) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (algorithm == null) {
            algorithm = HashAlgorithm.BCRYPT; // 默认使用BCrypt
        }
        
        AlgorithmConfig effectiveConfig = config != null ? config : defaultConfig;
        HashProvider provider = getProvider(algorithm);
        
        long startTime = System.currentTimeMillis();
        String hash = provider.hash(password);
        long processingTime = System.currentTimeMillis() - startTime;
        
        return new HashResult(hash, algorithm, effectiveConfig, null, processingTime);
    }
    
    @Override
    public VerificationResult verifyPassword(String password, String hashedPassword) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("哈希值不能为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 自动识别算法
        HashAlgorithm algorithm = detectAlgorithm(hashedPassword);
        if (algorithm == null) {
            return VerificationResult.failure(null, "无法识别的哈希格式", 
                System.currentTimeMillis() - startTime);
        }
        
        HashProvider provider = getProvider(algorithm);
        boolean verified = provider.verify(password, hashedPassword);
        long processingTime = System.currentTimeMillis() - startTime;
        
        if (verified) {
            VerificationResult result = VerificationResult.success(algorithm, processingTime);
            
            // 检查是否需要重新哈希
            if (needsRehash(hashedPassword)) {
                result.setNeedsRehash(true);
                result.setRecommendedAlgorithm(getRecommendedAlgorithm());
            }
            
            return result;
        } else {
            return VerificationResult.failure(algorithm, "密码验证失败", processingTime);
        }
    }
    
    @Override
    public String migrateHash(String oldHash, HashAlgorithm newAlgorithm, AlgorithmConfig config) {
        if (oldHash == null || oldHash.trim().isEmpty()) {
            throw new IllegalArgumentException("旧哈希值不能为空");
        }
        
        if (newAlgorithm == null) {
            throw new IllegalArgumentException("新算法不能为空");
        }
        
        // 注意：这里无法从哈希值恢复原始密码
        // 实际的迁移需要在用户成功验证密码后进行
        // 这里只是提供接口，实际使用时需要在验证成功后调用
        
        HashProvider provider = getProvider(newAlgorithm);
        AlgorithmConfig effectiveConfig = config != null ? config : defaultConfig;
        
        // 这里返回一个特殊的迁移标记
        // 实际应用中，需要在用户登录验证成功后进行迁移
        return "MIGRATION_REQUIRED:" + newAlgorithm.name();
    }
    
    @Override
    public boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        
        HashAlgorithm algorithm = detectAlgorithm(hashedPassword);
        if (algorithm == null) {
            return true; // 无法识别的格式，需要重新哈希
        }
        
        HashProvider provider = getProvider(algorithm);
        
        // 检查是否需要重新哈希
        if (provider instanceof BCryptHashProvider) {
            return ((BCryptHashProvider) provider).needsRehash(hashedPassword);
        } else if (provider instanceof Argon2HashProvider) {
            return ((Argon2HashProvider) provider).needsRehash(hashedPassword);
        } else if (provider instanceof HybridHashProvider) {
            return ((HybridHashProvider) provider).needsRehash(hashedPassword);
        }
        
        return false;
    }
    
    @Override
    public HashAlgorithm getRecommendedAlgorithm() {
        // 根据当前安全策略推荐算法
        // 这里默认推荐混合算法以获得最高安全性
        return HashAlgorithm.HYBRID;
    }
    
    @Override
    public AlgorithmConfig getDefaultConfig() {
        return defaultConfig;
    }
    
    /**
     * 获取哈希提供者
     * 
     * @param algorithm 算法类型
     * @return 哈希提供者
     */
    private HashProvider getProvider(HashAlgorithm algorithm) {
        HashProvider provider = providers.get(algorithm);
        if (provider == null) {
            throw new IllegalArgumentException("不支持的算法: " + algorithm);
        }
        return provider;
    }
    
    /**
     * 检测哈希算法
     * 
     * @param hash 哈希值
     * @return 算法类型，如果无法识别则返回null
     */
    private HashAlgorithm detectAlgorithm(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return null;
        }
        
        // 检查各种算法的格式
        for (HashProvider provider : providers.values()) {
            if (provider.supportsHashFormat(hash)) {
                return provider.getSupportedAlgorithm();
            }
        }
        
        return null;
    }
    
    /**
     * 获取所有支持的算法
     * 
     * @return 支持的算法列表
     */
    public HashAlgorithm[] getSupportedAlgorithms() {
        return providers.keySet().toArray(new HashAlgorithm[0]);
    }
    
    /**
     * 更新默认配置
     * 
     * @param newConfig 新的默认配置
     */
    public void updateDefaultConfig(AlgorithmConfig newConfig) {
        if (newConfig != null) {
            // 注意：这里需要重新初始化提供者
            // 在实际应用中，应该考虑配置的不可变性和线程安全性
        }
    }
}

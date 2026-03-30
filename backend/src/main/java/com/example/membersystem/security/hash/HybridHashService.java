package com.example.membersystem.security.hash;

import com.example.membersystem.security.hash.model.AlgorithmConfig;
import com.example.membersystem.security.hash.model.HashAlgorithm;
import com.example.membersystem.security.hash.model.HashResult;
import com.example.membersystem.security.hash.model.VerificationResult;

/**
 * 混合哈希服务接口
 * 提供多种哈希算法的统一接口
 */
public interface HybridHashService {
    
    /**
     * 哈希密码
     * 
     * @param password 原始密码
     * @param algorithm 算法类型
     * @param config 算法配置，如果为null则使用默认配置
     * @return 哈希结果
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    HashResult hashPassword(String password, HashAlgorithm algorithm, AlgorithmConfig config);
    
    /**
     * 哈希密码（使用默认配置）
     * 
     * @param password 原始密码
     * @param algorithm 算法类型
     * @return 哈希结果
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    default HashResult hashPassword(String password, HashAlgorithm algorithm) {
        return hashPassword(password, algorithm, null);
    }
    
    /**
     * 验证密码
     * 自动识别哈希算法并验证
     * 
     * @param password 原始密码
     * @param hashedPassword 存储的哈希值
     * @return 验证结果
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    VerificationResult verifyPassword(String password, String hashedPassword);
    
    /**
     * 算法迁移
     * 将旧哈希值迁移到新算法
     * 
     * @param oldHash 旧哈希值
     * @param newAlgorithm 新算法
     * @param config 新算法配置
     * @return 新哈希值
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    String migrateHash(String oldHash, HashAlgorithm newAlgorithm, AlgorithmConfig config);
    
    /**
     * 算法迁移（使用默认配置）
     * 
     * @param oldHash 旧哈希值
     * @param newAlgorithm 新算法
     * @return 新哈希值
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    default String migrateHash(String oldHash, HashAlgorithm newAlgorithm) {
        return migrateHash(oldHash, newAlgorithm, null);
    }
    
    /**
     * 检查是否需要重新哈希
     * 用于算法升级场景
     * 
     * @param hashedPassword 存储的哈希值
     * @return 是否需要重新哈希
     */
    boolean needsRehash(String hashedPassword);
    
    /**
     * 获取推荐的算法
     * 根据当前安全策略推荐最适合的算法
     * 
     * @return 推荐的算法
     */
    HashAlgorithm getRecommendedAlgorithm();
    
    /**
     * 获取默认配置
     * 
     * @return 默认算法配置
     */
    AlgorithmConfig getDefaultConfig();
}

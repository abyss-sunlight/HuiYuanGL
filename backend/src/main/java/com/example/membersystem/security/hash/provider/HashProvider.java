package com.example.membersystem.security.hash.provider;

import com.example.membersystem.security.hash.model.HashAlgorithm;

/**
 * 哈希提供者接口
 * 定义具体算法实现的基本接口
 */
public interface HashProvider {
    
    /**
     * 哈希密码
     * 
     * @param password 原始密码
     * @return 哈希值
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    String hash(String password);
    
    /**
     * 验证密码
     * 
     * @param password 原始密码
     * @param hash 存储的哈希值
     * @return 验证结果
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    boolean verify(String password, String hash);
    
    /**
     * 获取支持的算法
     * 
     * @return 支持的算法类型
     */
    HashAlgorithm getSupportedAlgorithm();
    
    /**
     * 检查哈希格式是否支持
     * 
     * @param hash 哈希值
     * @return 是否支持该格式
     */
    boolean supportsHashFormat(String hash);
}

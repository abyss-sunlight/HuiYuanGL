package com.example.membersystem.security.hash;

import com.example.membersystem.security.hash.model.*;
import com.example.membersystem.security.hash.provider.*;

/**
 * 简单的混合哈希测试
 */
public class SimpleHybridTest2 {
    
    public static void main(String[] args) {
        String testPassword = "MySecurePassword123!";
        
        // 创建简单的配置（不启用Pepper和预哈希）
        AlgorithmConfig config = new AlgorithmConfig();
        config.setEnablePepper(false);
        config.setEnablePreHash(false);
        
        System.out.println("=== 配置信息 ===");
        System.out.println("启用Pepper: " + config.isEnablePepper());
        System.out.println("启用PreHash: " + config.isEnablePreHash());
        System.out.println("Pepper值: '" + config.getPepper() + "'");
        
        // 创建提供者
        BCryptHashProvider bcryptProvider = new BCryptHashProvider(config);
        Argon2HashProvider argon2Provider = new Argon2HashProvider(config);
        HybridHashProvider hybridProvider = new HybridHashProvider(config, bcryptProvider, argon2Provider);
        
        System.out.println("\n=== 混合哈希测试 ===");
        
        // 步骤1：创建混合哈希
        String hybridHash = hybridProvider.hash(testPassword);
        System.out.println("原始密码: " + testPassword);
        System.out.println("混合哈希: " + hybridHash);
        
        // 步骤2：验证
        boolean isValid = hybridProvider.verify(testPassword, hybridHash);
        System.out.println("验证结果: " + isValid);
        
        // 步骤3：手动验证
        System.out.println("\n=== 手动验证 ===");
        HybridHashProvider.HybridHashComponents components = hybridProvider.extractHybridHashComponents(hybridHash);
        System.out.println("BCrypt部分: " + components.bcryptHash);
        System.out.println("Argon2部分: " + components.argon2Hash);
        
        boolean bcryptValid = bcryptProvider.verify(testPassword, components.bcryptHash);
        boolean argon2Valid = argon2Provider.verify(components.bcryptHash, components.argon2Hash);
        System.out.println("BCrypt验证: " + bcryptValid);
        System.out.println("Argon2验证: " + argon2Valid);
        System.out.println("手动验证结果: " + (bcryptValid && argon2Valid));
    }
}

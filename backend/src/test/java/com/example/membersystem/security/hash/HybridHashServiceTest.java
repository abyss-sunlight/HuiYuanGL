package com.example.membersystem.security.hash;

import com.example.membersystem.security.hash.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合哈希服务测试类
 * 验证各种哈希算法的功能
 */
class HybridHashServiceTest {
    
    private HybridHashService hybridHashService;
    private String testPassword = "MySecurePassword123!";
    
    @BeforeEach
    void setUp() {
        hybridHashService = new HybridHashServiceImpl();
    }
    
    @Test
    @DisplayName("测试BCrypt算法哈希和验证")
    void testBCryptHashAndVerify() {
        // 哈希密码
        HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.BCRYPT);
        
        assertNotNull(result);
        assertNotNull(result.getHash());
        assertEquals(HashAlgorithm.BCRYPT, result.getAlgorithm());
        assertTrue(result.getProcessingTimeMs() >= 0);
        
        // 验证密码
        VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
        assertTrue(verifyResult.isVerified());
        assertEquals(HashAlgorithm.BCRYPT, verifyResult.getAlgorithm());
        
        // 验证错误密码
        VerificationResult wrongPasswordResult = hybridHashService.verifyPassword("WrongPassword", result.getHash());
        assertFalse(wrongPasswordResult.isVerified());
    }
    
    @Test
    @DisplayName("测试Argon2算法哈希和验证")
    void testArgon2HashAndVerify() {
        // 哈希密码
        HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.ARGON2);
        
        assertNotNull(result);
        assertNotNull(result.getHash());
        assertEquals(HashAlgorithm.ARGON2, result.getAlgorithm());
        assertTrue(result.getProcessingTimeMs() >= 0);
        
        // 验证密码
        VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
        assertTrue(verifyResult.isVerified());
        assertEquals(HashAlgorithm.ARGON2, verifyResult.getAlgorithm());
        
        // 验证错误密码
        VerificationResult wrongPasswordResult = hybridHashService.verifyPassword("WrongPassword", result.getHash());
        assertFalse(wrongPasswordResult.isVerified());
    }
    
    @Test
    @DisplayName("测试混合算法哈希和验证")
    void testHybridHashAndVerify() {
        // 创建高安全性配置（确保Pepper和预哈希都正确设置）
        AlgorithmConfig config = AlgorithmConfig.createHighSecurityConfig();
        config.setPepper("test-pepper-value-12345"); // 设置Pepper值
        
        // 哈希密码
        HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.HYBRID, config);
        
        assertNotNull(result);
        assertNotNull(result.getHash());
        assertEquals(HashAlgorithm.HYBRID, result.getAlgorithm());
        assertTrue(result.getProcessingTimeMs() >= 0);
        
        // 验证密码
        VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
        assertTrue(verifyResult.isVerified());
        assertEquals(HashAlgorithm.HYBRID, verifyResult.getAlgorithm());
        
        // 验证错误密码
        VerificationResult wrongPasswordResult = hybridHashService.verifyPassword("WrongPassword", result.getHash());
        assertFalse(wrongPasswordResult.isVerified());
    }
    
    @Test
    @DisplayName("测试算法自动识别")
    void testAlgorithmDetection() {
        // 创建不同算法的哈希
        String bcryptHash = hybridHashService.hashPassword(testPassword, HashAlgorithm.BCRYPT).getHash();
        String argon2Hash = hybridHashService.hashPassword(testPassword, HashAlgorithm.ARGON2).getHash();
        String hybridHash = hybridHashService.hashPassword(testPassword, HashAlgorithm.HYBRID).getHash();
        
        // 验证自动识别
        VerificationResult bcryptResult = hybridHashService.verifyPassword(testPassword, bcryptHash);
        assertEquals(HashAlgorithm.BCRYPT, bcryptResult.getAlgorithm());
        
        VerificationResult argon2Result = hybridHashService.verifyPassword(testPassword, argon2Hash);
        assertEquals(HashAlgorithm.ARGON2, argon2Result.getAlgorithm());
        
        VerificationResult hybridResult = hybridHashService.verifyPassword(testPassword, hybridHash);
        assertEquals(HashAlgorithm.HYBRID, hybridResult.getAlgorithm());
    }
    
    @Test
    @DisplayName("测试配置管理")
    void testConfigurationManagement() {
        // 测试默认配置
        AlgorithmConfig defaultConfig = hybridHashService.getDefaultConfig();
        assertNotNull(defaultConfig);
        assertEquals(12, defaultConfig.getBcryptCost());
        assertEquals(3, defaultConfig.getArgon2TimeCost());
        assertEquals(65536, defaultConfig.getArgon2MemoryCost());
        assertEquals(4, defaultConfig.getArgon2Parallelism());
        
        // 测试高安全性配置
        AlgorithmConfig highSecurityConfig = AlgorithmConfig.createHighSecurityConfig();
        assertEquals(14, highSecurityConfig.getBcryptCost());
        assertEquals(4, highSecurityConfig.getArgon2TimeCost());
        assertEquals(131072, highSecurityConfig.getArgon2MemoryCost());
        assertTrue(highSecurityConfig.isEnablePepper());
        assertTrue(highSecurityConfig.isEnablePreHash());
        
        // 测试高性能配置
        AlgorithmConfig highPerformanceConfig = AlgorithmConfig.createHighPerformanceConfig();
        assertEquals(10, highPerformanceConfig.getBcryptCost());
        assertEquals(2, highPerformanceConfig.getArgon2TimeCost());
        assertEquals(32768, highPerformanceConfig.getArgon2MemoryCost());
        assertFalse(highPerformanceConfig.isEnablePepper());
        assertFalse(highPerformanceConfig.isEnablePreHash());
    }
    
    @Test
    @DisplayName("测试推荐算法")
    void testRecommendedAlgorithm() {
        HashAlgorithm recommended = hybridHashService.getRecommendedAlgorithm();
        assertEquals(HashAlgorithm.HYBRID, recommended);
    }
    
    @Test
    @DisplayName("测试参数验证")
    void testParameterValidation() {
        // 测试空密码
        assertThrows(IllegalArgumentException.class, () -> {
            hybridHashService.hashPassword("", HashAlgorithm.BCRYPT);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            hybridHashService.hashPassword(null, HashAlgorithm.BCRYPT);
        });
        
        // 测试空哈希值
        assertThrows(IllegalArgumentException.class, () -> {
            hybridHashService.verifyPassword(testPassword, "");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            hybridHashService.verifyPassword(testPassword, null);
        });
    }
    
    @Test
    @DisplayName("测试性能基准")
    void testPerformanceBenchmark() {
        int iterations = 10;
        
        // BCrypt性能测试
        long bcryptTotalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.BCRYPT);
            bcryptTotalTime += (System.currentTimeMillis() - startTime);
            
            // 验证
            VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
            assertTrue(verifyResult.isVerified());
        }
        
        // Argon2性能测试
        long argon2TotalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.ARGON2);
            argon2TotalTime += (System.currentTimeMillis() - startTime);
            
            // 验证
            VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
            assertTrue(verifyResult.isVerified());
        }
        
        // 混合算法性能测试
        long hybridTotalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            HashResult result = hybridHashService.hashPassword(testPassword, HashAlgorithm.HYBRID);
            hybridTotalTime += (System.currentTimeMillis() - startTime);
            
            // 验证
            VerificationResult verifyResult = hybridHashService.verifyPassword(testPassword, result.getHash());
            assertTrue(verifyResult.isVerified());
        }
        
        System.out.println("性能测试结果 (" + iterations + " 次迭代):");
        System.out.println("BCrypt平均时间: " + (bcryptTotalTime / iterations) + "ms");
        System.out.println("Argon2平均时间: " + (argon2TotalTime / iterations) + "ms");
        System.out.println("混合算法平均时间: " + (hybridTotalTime / iterations) + "ms");
        
        // 基本性能断言（这些值可能需要根据实际环境调整）
        assertTrue(bcryptTotalTime / iterations < 1000, "BCrypt性能应该小于1秒");
        assertTrue(argon2TotalTime / iterations < 2000, "Argon2性能应该小于2秒");
        assertTrue(hybridTotalTime / iterations < 3000, "混合算法性能应该小于3秒");
    }
    
    @Test
    @DisplayName("测试哈希格式支持")
    void testHashFormatSupport() {
        // 测试各种哈希格式
        String bcryptHash = hybridHashService.hashPassword(testPassword, HashAlgorithm.BCRYPT).getHash();
        String argon2Hash = hybridHashService.hashPassword(testPassword, HashAlgorithm.ARGON2).getHash();
        String hybridHash = hybridHashService.hashPassword(testPassword, HashAlgorithm.HYBRID).getHash();
        
        // 验证格式
        assertTrue(bcryptHash.startsWith("$2"));
        assertTrue(argon2Hash.startsWith("$argon2"));
        assertTrue(hybridHash.startsWith("$HYBRID$"));
        
        // 测试不支持的格式
        VerificationResult invalidResult = hybridHashService.verifyPassword(testPassword, "invalid_hash_format");
        assertFalse(invalidResult.isVerified());
        assertNotNull(invalidResult.getErrorMessage());
    }
}

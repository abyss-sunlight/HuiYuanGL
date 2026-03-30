package com.example.membersystem.security.hash.model;

import de.mkammerer.argon2.Argon2Factory;

/**
 * 算法配置模型
 * 包含BCrypt、Argon2和混合算法的所有配置参数
 */
public class AlgorithmConfig {
    
    // BCrypt配置
    private int bcryptCost = 12;
    
    // Argon2配置
    private int argon2TimeCost = 3;
    private int argon2MemoryCost = 65536;
    private int argon2Parallelism = 4;
    private Argon2Factory.Argon2Types argon2Type = Argon2Factory.Argon2Types.ARGON2id;
    
    // 混合模式配置
    private boolean enablePepper = false;
    private String pepper = "";
    private boolean enablePreHash = false;
    private PreHashAlgorithm preHashAlgorithm = PreHashAlgorithm.SHA256;
    
    // 构造函数
    public AlgorithmConfig() {}
    
    public AlgorithmConfig(int bcryptCost, int argon2TimeCost, int argon2MemoryCost, 
                          int argon2Parallelism, Argon2Factory.Argon2Types argon2Type) {
        this.bcryptCost = bcryptCost;
        this.argon2TimeCost = argon2TimeCost;
        this.argon2MemoryCost = argon2MemoryCost;
        this.argon2Parallelism = argon2Parallelism;
        this.argon2Type = argon2Type;
    }
    
    // Getter和Setter方法
    public int getBcryptCost() {
        return bcryptCost;
    }
    
    public void setBcryptCost(int bcryptCost) {
        this.bcryptCost = bcryptCost;
    }
    
    public int getArgon2TimeCost() {
        return argon2TimeCost;
    }
    
    public void setArgon2TimeCost(int argon2TimeCost) {
        this.argon2TimeCost = argon2TimeCost;
    }
    
    public int getArgon2MemoryCost() {
        return argon2MemoryCost;
    }
    
    public void setArgon2MemoryCost(int argon2MemoryCost) {
        this.argon2MemoryCost = argon2MemoryCost;
    }
    
    public int getArgon2Parallelism() {
        return argon2Parallelism;
    }
    
    public void setArgon2Parallelism(int argon2Parallelism) {
        this.argon2Parallelism = argon2Parallelism;
    }
    
    public Argon2Factory.Argon2Types getArgon2Type() {
        return argon2Type;
    }
    
    public void setArgon2Type(Argon2Factory.Argon2Types argon2Type) {
        this.argon2Type = argon2Type;
    }
    
    public boolean isEnablePepper() {
        return enablePepper;
    }
    
    public void setEnablePepper(boolean enablePepper) {
        this.enablePepper = enablePepper;
    }
    
    public String getPepper() {
        return pepper;
    }
    
    public void setPepper(String pepper) {
        this.pepper = pepper;
    }
    
    public boolean isEnablePreHash() {
        return enablePreHash;
    }
    
    public void setEnablePreHash(boolean enablePreHash) {
        this.enablePreHash = enablePreHash;
    }
    
    public PreHashAlgorithm getPreHashAlgorithm() {
        return preHashAlgorithm;
    }
    
    public void setPreHashAlgorithm(PreHashAlgorithm preHashAlgorithm) {
        this.preHashAlgorithm = preHashAlgorithm;
    }
    
    /**
     * 创建高安全性配置
     */
    public static AlgorithmConfig createHighSecurityConfig() {
        AlgorithmConfig config = new AlgorithmConfig();
        config.setBcryptCost(14);
        config.setArgon2TimeCost(4);
        config.setArgon2MemoryCost(131072);
        config.setArgon2Parallelism(4);
        config.setEnablePepper(true);
        config.setEnablePreHash(true);
        config.setPreHashAlgorithm(PreHashAlgorithm.SHA512);
        return config;
    }
    
    /**
     * 创建高性能配置
     */
    public static AlgorithmConfig createHighPerformanceConfig() {
        AlgorithmConfig config = new AlgorithmConfig();
        config.setBcryptCost(10);
        config.setArgon2TimeCost(2);
        config.setArgon2MemoryCost(32768);
        config.setArgon2Parallelism(2);
        config.setEnablePepper(false);
        config.setEnablePreHash(false);
        return config;
    }
    
    /**
     * 创建默认配置
     */
    public static AlgorithmConfig createDefaultConfig() {
        return new AlgorithmConfig();
    }
}

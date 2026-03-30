package com.example.membersystem.security.hash.model;

import java.time.LocalDateTime;

/**
 * 验证结果模型
 * 包含密码验证的结果信息
 */
public class VerificationResult {
    
    /**
     * 验证是否成功
     */
    private boolean verified;
    
    /**
     * 使用的算法
     */
    private HashAlgorithm algorithm;
    
    /**
     * 验证时间
     */
    private LocalDateTime verifiedAt;
    
    /**
     * 验证耗时（毫秒）
     */
    private long processingTimeMs;
    
    /**
     * 错误信息（如果验证失败）
     */
    private String errorMessage;
    
    /**
     * 是否需要重新哈希（用于算法升级）
     */
    private boolean needsRehash;
    
    /**
     * 推荐的新算法（如果需要重新哈希）
     */
    private HashAlgorithm recommendedAlgorithm;
    
    // 构造函数
    public VerificationResult() {
        this.verifiedAt = LocalDateTime.now();
    }
    
    public VerificationResult(boolean verified, HashAlgorithm algorithm) {
        this.verified = verified;
        this.algorithm = algorithm;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public VerificationResult(boolean verified, HashAlgorithm algorithm, long processingTimeMs) {
        this.verified = verified;
        this.algorithm = algorithm;
        this.processingTimeMs = processingTimeMs;
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * 创建成功的验证结果
     */
    public static VerificationResult success(HashAlgorithm algorithm, long processingTimeMs) {
        VerificationResult result = new VerificationResult(true, algorithm, processingTimeMs);
        return result;
    }
    
    /**
     * 创建失败的验证结果
     */
    public static VerificationResult failure(HashAlgorithm algorithm, String errorMessage, long processingTimeMs) {
        VerificationResult result = new VerificationResult(false, algorithm, processingTimeMs);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    /**
     * 创建需要重新哈希的验证结果
     */
    public static VerificationResult needsRehash(HashAlgorithm algorithm, HashAlgorithm recommendedAlgorithm) {
        VerificationResult result = new VerificationResult(true, algorithm);
        result.setNeedsRehash(true);
        result.setRecommendedAlgorithm(recommendedAlgorithm);
        return result;
    }
    
    // Getter和Setter方法
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(HashAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean isNeedsRehash() {
        return needsRehash;
    }
    
    public void setNeedsRehash(boolean needsRehash) {
        this.needsRehash = needsRehash;
    }
    
    public HashAlgorithm getRecommendedAlgorithm() {
        return recommendedAlgorithm;
    }
    
    public void setRecommendedAlgorithm(HashAlgorithm recommendedAlgorithm) {
        this.recommendedAlgorithm = recommendedAlgorithm;
    }
    
    @Override
    public String toString() {
        return "VerificationResult{" +
                "verified=" + verified +
                ", algorithm=" + algorithm +
                ", verifiedAt=" + verifiedAt +
                ", processingTimeMs=" + processingTimeMs +
                ", needsRehash=" + needsRehash +
                '}';
    }
}

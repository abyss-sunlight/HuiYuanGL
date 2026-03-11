package com.example.membersystem.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT（JSON Web Token）工具类
 * 
 * 提供JWT令牌的生成、解析和验证功能
 * 用于用户身份认证和权限管理
 * 
 * 主要功能：
 * - 生成包含用户信息的JWT令牌
 * - 解析JWT令牌获取用户信息
 * - 验证令牌的有效性和过期时间
 * 
 * 安全特性：
 * - 使用HMAC-SHA256算法签名
 * - 支持自定义过期时间
 * - 包含用户ID、手机号、权限等级等关键信息
 * 
 * 使用说明：
 * - 登录成功后生成令牌返回给客户端
 * - 客户端在后续请求中携带令牌
 * - 服务端通过拦截器验证令牌有效性
 */
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     * 
     * 用于JWT令牌的签名和验证
     * 从配置文件中读取，默认值为memberSystemSecretKey2026ForJWTToken
     * 
     * 安全注意：
     * - 生产环境应使用复杂的随机密钥
     * - 密钥应妥善保管，不应提交到版本控制
     * - 建议使用环境变量或配置中心管理
     */
    @Value("${jwt.secret:memberSystemSecretKey2026ForJWTToken}")
    private String secret;

    /**
     * JWT令牌过期时间（毫秒）
     * 
     * 从配置文件中读取，默认值为86400000毫秒（24小时）
     * 可根据业务需求调整，建议设置合理的过期时间
     * 
     * 常用时间设置：
     * - 3600000: 1小时
     * - 7200000: 2小时
     * - 86400000: 24小时（默认）
     * - 604800000: 7天
     */
    @Value("${jwt.expiration:86400000}") // 24小时
    private Long expiration;

    /**
     * 生成包含用户信息的JWT令牌
     * 
     * 将用户ID、手机号、权限等级等信息封装到JWT令牌中
     * 令牌包含标准声明和自定义声明
     * 
     * @param userId 用户唯一标识
     * @param phone 用户手机号，作为令牌主题
     * @param permissionLevel 用户权限等级
     * @return JWT令牌字符串
     */
    public String generateToken(Long userId, String phone, Integer permissionLevel) {
        // 创建自定义声明，存储用户信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("permissionLevel", permissionLevel);
        
        // 生成令牌，以手机号作为主题
        return generateToken(claims, phone);
    }

    /**
     * 生成JWT令牌的通用方法
     * 
     * 使用HMAC-SHA256算法生成JWT令牌
     * 包含标准声明：签发时间、过期时间、主题
     * 
     * @param claims 自定义声明集合
     * @param subject 令牌主题，通常为用户标识
     * @return JWT令牌字符串
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        // 计算过期时间
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 生成签名密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // 构建并返回JWT令牌
        return Jwts.builder()
                .claims(claims)           // 设置自定义声明
                .subject(subject)           // 设置主题
                .issuedAt(now)            // 设置签发时间
                .expiration(expiryDate)    // 设置过期时间
                .signWith(key)            // 使用密钥签名
                .compact();              // 压缩为字符串
    }

    /**
     * 从JWT令牌中解析声明信息
     * 
     * 验证令牌签名并返回所有声明信息
     * 包括标准声明和自定义声明
     * 
     * @param token JWT令牌字符串
     * @return Claims对象，包含所有声明信息
     * @throws Exception 令牌格式错误、签名无效、过期等异常
     */
    public Claims getClaimsFromToken(String token) {
        // 生成签名密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        // 解析并验证令牌
        return Jwts.parser()
                .verifyWith(key)           // 设置验证密钥
                .build()                   // 构建解析器
                .parseSignedClaims(token)    // 解析签名令牌
                .getPayload();             // 获取声明部分
    }

    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌字符串
     * @return 用户ID，如果令牌无效则返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从JWT令牌中获取用户手机号
     * 
     * 手机号作为令牌的主题（subject）存储
     * 
     * @param token JWT令牌字符串
     * @return 用户手机号，如果令牌无效则返回null
     */
    public String getPhoneFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从JWT令牌中获取用户权限等级
     * 
     * @param token JWT令牌字符串
     * @return 权限等级数字，如果令牌无效则返回null
     */
    public Integer getPermissionLevelFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("permissionLevel", Integer.class);
    }

    /**
     * 检查JWT令牌是否过期
     * 
     * 比较令牌的过期时间与当前时间
     * 
     * @param token JWT令牌字符串
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            // 解析失败视为令牌无效，等同于过期
            return true;
        }
    }

    /**
     * 验证JWT令牌的有效性
     * 
     * 检查令牌中的手机号与提供的手机号是否匹配
     * 同时检查令牌是否过期
     * 
     * @param token JWT令牌字符串
     * @param phone 预期的用户手机号
     * @return true表示令牌有效，false表示令牌无效
     */
    public Boolean validateToken(String token, String phone) {
        try {
            // 从令牌中提取手机号
            String tokenPhone = getPhoneFromToken(token);
            
            // 验证手机号匹配且未过期
            return tokenPhone.equals(phone) && !isTokenExpired(token);
        } catch (Exception e) {
            // 任何异常都视为令牌无效
            return false;
        }
    }
}

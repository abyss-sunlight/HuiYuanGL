package com.example.membersystem.auth.controller;

import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.SmsLoginRequest;
import com.example.membersystem.auth.dto.PasswordLoginRequest;
import com.example.membersystem.auth.dto.WxLoginRequest;
import com.example.membersystem.auth.dto.SetPasswordRequest;
import com.example.membersystem.auth.service.AuthService;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.common.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 
 * 负责处理用户认证相关的HTTP请求，包括：
 * - 短信验证码登录
 * - 密码登录
 * - 发送短信验证码
 * 
 * 所有接口都返回统一的ApiResponse格式响应
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证接口", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 构造函数注入AuthService
     * 
     * @param authService 认证服务，处理业务逻辑
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 短信验证码登录接口
     * 
     * 处理用户使用手机号和短信验证码的登录请求
     * 验证手机号和验证码的有效性，生成JWT令牌
     * 
     * @param request 短信登录请求，包含手机号和验证码
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws BusinessException 当用户不存在、验证码错误或账户被禁用时抛出
     */
    @PostMapping("/sms-login")
    @Operation(summary = "短信登录", description = "使用手机号和验证码登录")
    public ApiResponse<LoginResponse> smsLogin(@RequestBody SmsLoginRequest request) {
        System.out.println("=== AuthController 短信登录请求 ===");
        System.out.println("请求体: " + request);
        
        try {
            // 调用服务层处理短信登录逻辑
            LoginResponse response = authService.smsLogin(request);
            System.out.println("短信登录成功，返回响应");
            return ApiResponse.ok(response);
        } catch (Exception e) {
            // 记录错误日志并重新抛出异常
            System.out.println("短信登录失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 密码登录接口
     * 
     * 处理用户使用手机号和密码的登录请求
     * 验证手机号和密码的正确性，生成JWT令牌
     * 
     * @param request 密码登录请求，包含手机号和密码
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws BusinessException 当用户不存在、密码错误或账户被禁用时抛出
     */
    @PostMapping("/password-login")
    @Operation(summary = "密码登录", description = "使用手机号和密码登录")
    public ApiResponse<LoginResponse> passwordLogin(@RequestBody PasswordLoginRequest request) {
        System.out.println("=== AuthController 密码登录请求 ===");
        System.out.println("请求体: " + request);
        
        try {
            // 调用服务层处理密码登录逻辑
            LoginResponse response = authService.passwordLogin(request);
            System.out.println("密码登录成功，返回响应");
            return ApiResponse.ok(response);
        } catch (Exception e) {
            // 记录错误日志并重新抛出异常
            System.out.println("密码登录失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 发送短信验证码接口
     * 
     * 向指定手机号发送6位数字验证码
     * 验证手机号格式的有效性
     * 
     * @param request 请求体，包含phone字段
     * @return 空响应，表示发送成功
     * @throws BusinessException 当手机号格式错误时抛出
     */
    @PostMapping("/send-sms")
    @Operation(summary = "发送短信验证码", description = "发送手机验证码")
    public ApiResponse<Void> sendSms(@RequestBody Map<String, String> request) {
        System.out.println("=== AuthController 发送短信请求 ===");
        System.out.println("请求体: " + request);
        
        try {
            // 从请求体中提取手机号
            String phone = request.get("phone");
            // 调用服务层发送短信验证码
            authService.sendSms(phone);
            System.out.println("发送短信成功");
            return ApiResponse.ok();
        } catch (Exception e) {
            // 记录错误日志并重新抛出异常
            System.out.println("发送短信失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/set-password")
    @Operation(summary = "设置/重置密码", description = "使用手机号+短信验证码设置或重置密码")
    public ApiResponse<Void> setPassword(@RequestBody SetPasswordRequest request) {
        authService.setPassword(request);
        return ApiResponse.ok();
    }

    /**
     * 微信登录接口
     * 
     * 处理用户使用微信授权的登录请求
     * 验证微信登录凭证的有效性，生成JWT令牌
     * 
     * @param request 微信登录请求，包含code和userInfo
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws BusinessException 当微信凭证无效或处理失败时抛出
     */
    @PostMapping("/wx-login")
    @Operation(summary = "微信登录", description = "使用微信授权登录")
    public ApiResponse<LoginResponse> wxLogin(@RequestBody WxLoginRequest request) {
        System.out.println("=== AuthController 微信登录请求 ===");
        System.out.println("请求体: " + request);
        
        try {
            // 调用服务层处理微信登录逻辑
            LoginResponse response = authService.wxLogin(request);
            System.out.println("微信登录成功，返回响应");
            return ApiResponse.ok(response);
        } catch (BusinessException e) {
            // 处理业务异常，特别是新用户需要补充信息的情况
            System.out.println("微信登录业务异常: " + e.getMessage());
            System.out.println("异常状态码: " + e.getCode());
            
            if (e.getCode() == 201) {
                // 新用户需要补充信息
                return ApiResponse.fail(201, e.getMessage());
            } else {
                // 其他业务异常
                return ApiResponse.fail(e.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            // 记录错误日志并重新抛出异常
            System.err.println("微信登录过程中发生未知错误: " + e.getMessage());
            e.printStackTrace();
            
            // 返回通用错误响应
            return ApiResponse.fail(500, "微信登录失败，请稍后重试");
        }
    }
}

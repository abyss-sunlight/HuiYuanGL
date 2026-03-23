package com.example.membersystem.auth.service;

import com.example.membersystem.auth.dto.ChangePhoneRequest;
import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.PasswordLoginRequest;
import com.example.membersystem.auth.dto.SetPasswordRequest;
import com.example.membersystem.auth.dto.SmsLoginRequest;
import com.example.membersystem.auth.dto.WxLoginRequest;
import com.example.membersystem.common.BusinessException;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 认证服务主协调器
 * 
 * 负责协调各种登录方式的服务，包括：
 * - 短信验证码登录
 * - 密码登录
 * - 微信登录
 * - 密码设置
 * 
 * 该类作为认证服务的统一入口，委托给专门的服务类处理具体业务逻辑
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SmsLoginService smsLoginService;
    private final PasswordLoginService passwordLoginService;
    private final WxLoginService wxLoginService;
    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数注入依赖
     * 
     * @param userRepository 用户数据访问层，用于查询用户信息
     * @param smsLoginService 短信登录服务
     * @param passwordLoginService 密码登录服务
     * @param wxLoginService 微信登录服务
     * @param smsCodeService 短信验证码服务
     * @param passwordEncoder 密码编码器
     */
    public AuthService(UserRepository userRepository, 
                      SmsLoginService smsLoginService,
                      PasswordLoginService passwordLoginService,
                      WxLoginService wxLoginService,
                      SmsCodeService smsCodeService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.smsLoginService = smsLoginService;
        this.passwordLoginService = passwordLoginService;
        this.wxLoginService = wxLoginService;
        this.smsCodeService = smsCodeService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 短信验证码登录/注册
     * 
     * 委托给SmsLoginService处理具体的业务逻辑
     * 
     * @param request 短信登录请求，包含登录类型、手机号和验证码
     * @return 登录响应，包含JWT令牌和用户详细信息
     */
    @Transactional
    public LoginResponse smsLogin(SmsLoginRequest request) {
        return smsLoginService.smsLogin(request);
    }

    /**
     * 密码登录
     * 
     * 委托给PasswordLoginService处理具体的业务逻辑
     * 
     * @param request 密码登录请求，包含登录类型、手机号和密码
     * @return 登录响应，包含JWT令牌和用户详细信息
     */
    @Transactional
    public LoginResponse passwordLogin(PasswordLoginRequest request) {
        return passwordLoginService.passwordLogin(request);
    }

    /**
     * 发送短信验证码
     * 
     * 委托给SmsCodeService处理具体的业务逻辑
     * 
     * @param phone 目标手机号，必须是11位数字
     */
    public void sendSms(String phone) {
        smsCodeService.sendCode(phone);
    }

    /**
     * 设置密码
     * 
     * 处理用户设置密码的请求
     * 
     * @param request 设置密码请求，包含手机号、验证码和新密码
     */
    @Transactional
    public void setPassword(SetPasswordRequest request) {
        smsCodeService.verifyAndConsume(request.getPhone(), request.getCode());

        String pwd = request.getNewPassword();
        if (!StringUtils.hasText(pwd) || pwd.length() < 6) {
            throw new RuntimeException("密码至少6位");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setPassword(passwordEncoder.encode(pwd));
        userRepository.save(user);
    }

    /**
     * 微信登录
     * 
     * 委托给WxLoginService处理具体的业务逻辑
     * 
     * @param request 微信登录请求，包含code和userInfo
     * @return 登录响应，包含JWT令牌和用户信息
     */
    @Transactional
    public LoginResponse wxLogin(WxLoginRequest request) {
        return wxLoginService.wxLogin(request);
    }

    /**
     * 发送原手机号验证码
     * 验证手机号是否属于当前用户
     */
    public void sendOriginalSms(String phone) {
        // 验证手机号是否属于当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null || !phone.equals(currentUser.getPhone())) {
            throw new BusinessException(400, "原手机号不匹配");
        }
        
        // 发送验证码
        smsCodeService.sendSmsCode(phone, SmsCodeService.Type.CHANGE_PHONE_ORIGINAL);
    }

    /**
     * 验证原手机号验证码
     * 验证原手机号和验证码的正确性
     */
    public void verifyOriginalPhone(String phone, String code) {
        // 验证手机号是否属于当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null || !phone.equals(currentUser.getPhone())) {
            throw new BusinessException(400, "原手机号不匹配");
        }
        
        // 验证验证码
        smsCodeService.verifyCode(phone, code, SmsCodeService.Type.CHANGE_PHONE_ORIGINAL);
    }

    /**
     * 发送新手机号验证码
     * 验证新手机号是否已被使用
     */
    public void sendNewSms(String phone) {
        // 检查新手机号是否已被使用
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException(400, "该手机号已被使用");
        }
        
        // 发送验证码
        smsCodeService.sendSmsCode(phone, SmsCodeService.Type.CHANGE_PHONE_NEW);
    }

    /**
     * 修改手机号
     * 通过双重验证码验证后更新手机号
     */
    @Transactional
    public void changePhone(ChangePhoneRequest request) {
        System.out.println("=== 修改手机号开始 ===");
        System.out.println("原手机号: " + request.getOriginalPhone());
        System.out.println("新手机号: " + request.getNewPhone());
        
        // 验证新手机号验证码（原手机号验证码已在verifyOriginalPhone中验证过）
        System.out.println("验证新手机号验证码...");
        smsCodeService.verifyCode(
            request.getNewPhone(), 
            request.getNewCode(), 
            SmsCodeService.Type.CHANGE_PHONE_NEW
        );
        
        // 获取当前用户
        User currentUser = getCurrentUser();
        
        // 验证原手机号匹配
        if (!request.getOriginalPhone().equals(currentUser.getPhone())) {
            throw new BusinessException(400, "原手机号不匹配");
        }
        
        // 更新手机号
        currentUser.setPhone(request.getNewPhone());
        userRepository.save(currentUser);
        
        // 清除相关验证码
        smsCodeService.clearCode(request.getOriginalPhone(), SmsCodeService.Type.CHANGE_PHONE_ORIGINAL);
        smsCodeService.clearCode(request.getNewPhone(), SmsCodeService.Type.CHANGE_PHONE_NEW);
        
        System.out.println("=== 修改手机号成功 ===");
    }

    /**
     * 获取当前登录用户
     * 这里需要根据实际的认证机制来实现
     * 暂时返回第一个用户用于测试
     */
    private User getCurrentUser() {
        // TODO: 实现获取当前用户的逻辑
        // 可以从SecurityContext或JWT token中获取
        // 暂时返回第一个用户用于测试
        return userRepository.findAll().stream().findFirst().orElse(null);
    }
}

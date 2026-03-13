package com.example.membersystem.auth.service;

import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.SmsLoginRequest;
import com.example.membersystem.auth.dto.PasswordLoginRequest;
import com.example.membersystem.auth.dto.SetPasswordRequest;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.common.BusinessException;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 认证服务
 * 
 * 负责处理用户认证的核心业务逻辑，包括：
 * - 短信验证码登录验证
 * - 密码登录验证
 * - 短信验证码发送
 * - JWT令牌生成
 * - 用户状态检查
 * 
 * 使用@Transactional确保数据一致性
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数注入依赖
     * 
     * @param userRepository 用户数据访问层，用于查询用户信息
     * @param jwtUtil JWT工具类，用于生成和验证令牌
     */
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, SmsCodeService smsCodeService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.smsCodeService = smsCodeService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 短信验证码登录/注册
     * 
     * 处理用户使用手机号和验证码的登录请求：
     * 1. 根据手机号查询用户是否存在
     * 2. 如果用户不存在，自动创建新用户（注册）
     * 3. 如果用户存在，验证用户状态
     * 4. 验证短信验证码的正确性
     * 5. 生成JWT访问令牌
     * 6. 构造并返回登录响应
     * 
     * 业务逻辑：
     * - 首次使用手机号：自动注册并登录
     * - 已有用户：验证后直接登录
     * - 新用户默认权限为游客（权限等级4）
     * 
     * @param request 短信登录请求，包含登录类型、手机号和验证码
     * @return 登录响应，包含JWT令牌和用户详细信息
     * @throws BusinessException 当验证码错误或账户被禁用时抛出
     */
    @Transactional
    public LoginResponse smsLogin(SmsLoginRequest request) {
        System.out.println("=== 短信登录/注册请求开始 ===");
        System.out.println("请求参数: " + request);
        System.out.println("loginType: " + request.getLoginType());
        System.out.println("phone: " + request.getPhone());
        System.out.println("code: " + request.getCode());
        
        // 1. 校验短信验证码（一次性消费）
        smsCodeService.verifyAndConsume(request.getPhone(), request.getCode());

        // 2. 根据手机号查找用户
        Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
        User user;
        
        if (userOptional.isPresent()) {
            // 2a. 用户存在，进行登录流程
            user = userOptional.get();
            System.out.println("找到已存在用户: " + user);
            
            // 3a. 检查用户状态
            if (user.getStatus() == 1) {
                System.out.println("用户状态异常: " + user.getStatus());
                throw new BusinessException(40103, "账户已被禁用");
            }
        } else {
            // 2b. 用户不存在，进行注册流程
            System.out.println("用户不存在，开始自动注册");
            
            // 创建新用户
            user = new User();
            user.setPhone(request.getPhone());
            user.setLastName("用户"); // 默认姓氏，后续可让用户补充
            user.setGender(1); // 默认性别为男性
            user.setPermissionLevel(4); // 默认权限为游客
            user.setStatus(0); // 默认状态为正常
            user.setAmount(BigDecimal.ZERO); // 默认余额为0
            user.setDiscount(BigDecimal.ONE); // 默认折扣为1.0（无折扣）
            
            // 保存新用户（会自动触发@PrePersist设置创建时间等）
            user = userRepository.save(user);
            System.out.println("新用户注册成功: " + user);
        }

        // 3. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getPermissionLevel());
        System.out.println("生成JWT token成功");
        
        // 4. 构造并返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setMemberNo(user.getMemberNo());
        response.setAmount(user.getAmount());
        response.setDiscount(user.getDiscount());
        response.setPermissionLevel(user.getPermissionLevel());
        response.setPermissionName(getPermissionName(user.getPermissionLevel()));
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCreatedAt(user.getCreatedAt());
        
        System.out.println("=== 短信登录/注册成功 ===");
        System.out.println("返回响应: " + response);
        
        return response;
    }

    @Transactional
    public void setPassword(SetPasswordRequest request) {
        smsCodeService.verifyAndConsume(request.getPhone(), request.getCode());

        String pwd = request.getNewPassword();
        if (!StringUtils.hasText(pwd) || pwd.length() < 6) {
            throw new BusinessException(40002, "密码至少6位");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException(40101, "用户不存在"));

        user.setPassword(passwordEncoder.encode(pwd));
        userRepository.save(user);
    }

    /**
     * 密码登录
     * 
     * 处理用户使用手机号和密码的登录请求：
     * 1. 根据手机号查询用户是否存在
     * 2. 验证密码的正确性
     * 3. 检查用户账户状态是否正常
     * 4. 生成JWT访问令牌
     * 5. 构造并返回登录响应
     * 
     * @param request 密码登录请求，包含登录类型、手机号和密码
     * @return 登录响应，包含JWT令牌和用户详细信息
     * @throws BusinessException 当用户不存在、密码错误或账户被禁用时抛出
     */
    @Transactional
    public LoginResponse passwordLogin(PasswordLoginRequest request) {
        System.out.println("=== 密码登录请求开始 ===");
        System.out.println("请求参数: " + request);
        System.out.println("loginType: " + request.getLoginType());
        System.out.println("phone: " + request.getPhone());
        System.out.println("password: " + request.getPassword());
        
        // 1. 根据手机号查找用户
        Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
        
        if (!userOptional.isPresent()) {
            System.out.println("用户不存在");
            throw new BusinessException(40101, "用户不存在");
        }
        
        User user = userOptional.get();
        System.out.println("找到用户: " + user);
        
        // 2. 验证密码（BCrypt）
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException(40102, "未设置密码，请使用短信登录后设置密码");
        }

        boolean ok;
        String stored = user.getPassword();
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            ok = passwordEncoder.matches(request.getPassword(), stored);
        } else {
            // 兼容历史明文密码（如果数据库里有旧数据），校验通过后自动升级为BCrypt
            ok = stored.equals(request.getPassword());
            if (ok) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }
        }

        if (!ok) {
            System.out.println("密码错误");
            throw new BusinessException(40102, "密码错误");
        }
        
        // 3. 检查用户状态
        if (user.getStatus() == 1) {
            System.out.println("用户状态异常: " + user.getStatus());
            throw new BusinessException(40103, "账户已被禁用");
        }
        
        // 4. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getPermissionLevel());
        System.out.println("生成JWT token成功");
        
        // 5. 构造并返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setMemberNo(user.getMemberNo());
        response.setAmount(user.getAmount());
        response.setDiscount(user.getDiscount());
        response.setPermissionLevel(user.getPermissionLevel());
        response.setPermissionName(getPermissionName(user.getPermissionLevel()));
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCreatedAt(user.getCreatedAt());
        
        System.out.println("=== 密码登录成功 ===");
        System.out.println("返回响应: " + response);
        
        return response;
    }

    /**
     * 发送短信验证码
     * 
     * 向指定手机号发送6位数字验证码：
     * 1. 验证手机号格式的有效性
     * 2. 调用短信服务API发送验证码
     * 3. 记录发送日志
     * 
     * @param phone 目标手机号，必须是11位数字
     * @throws BusinessException 当手机号格式错误时抛出
     */
    public void sendSms(String phone) {
        smsCodeService.sendCode(phone);
    }

    /**
     * 获取权限名称
     * 
     * 根据权限等级数字返回对应的权限名称
     * 权限等级定义：
     * 1 - 店长：拥有所有权限
     * 2 - 员工：拥有部分管理权限
     * 3 - 会员：普通用户权限
     * 4 - 游客：最低权限
     * 
     * @param permissionLevel 权限等级数字
     * @return 对应的权限名称字符串
     */
    private String getPermissionName(Integer permissionLevel) {
        switch (permissionLevel) {
            case 1:
                return "店长";
            case 2:
                return "员工";
            case 3:
                return "会员";
            case 4:
                return "游客";
            default:
                return "未知";
        }
    }
}

package com.example.membersystem.auth.service;

import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.SmsLoginRequest;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 短信登录服务
 * 
 * 专门处理短信验证码登录的业务逻辑，包括：
 * - 短信验证码验证
 * - 用户自动注册（首次登录）
 * - JWT令牌生成
 * - 用户状态管理
 */
@Service
public class SmsLoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;

    public SmsLoginService(UserRepository userRepository, JwtUtil jwtUtil, SmsCodeService smsCodeService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.smsCodeService = smsCodeService;
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
     * @throws RuntimeException 当验证码错误或账户被禁用时抛出
     */
    @Transactional
    public LoginResponse smsLogin(SmsLoginRequest request) {
        System.out.println("=== SmsLoginService 短信登录/注册请求开始 ===");
        System.out.println("请求参数: " + request);
        System.out.println("loginType: " + request.getLoginType());
        System.out.println("phone: " + request.getPhone());
        System.out.println("code: " + request.getCode());
        
        // 1. 校验短信验证码（一次性消费）
        smsCodeService.verifyAndConsume(request.getPhone(), request.getCode());

        // 2. 根据手机号查找或创建用户
        User user = findOrCreateUser(request.getPhone());

        // 3. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getPermissionLevel());
        System.out.println("生成JWT token成功");
        
        // 4. 构造并返回登录响应
        LoginResponse response = buildLoginResponse(user, token);
        
        System.out.println("=== 短信登录/注册成功 ===");
        System.out.println("返回响应: " + response);
        
        return response;
    }

    /**
     * 根据手机号查找或创建用户
     * 
     * @param phone 手机号
     * @return 用户对象
     */
    private User findOrCreateUser(String phone) {
        Optional<User> userOptional = userRepository.findByPhone(phone);
        
        if (userOptional.isPresent()) {
            // 用户存在，进行登录流程
            User user = userOptional.get();
            System.out.println("找到已存在用户: " + user);
            
            // 检查用户状态
            validateUserStatus(user);
            
            return user;
        } else {
            // 用户不存在，进行注册流程
            System.out.println("用户不存在，开始自动注册");
            return createNewUser(phone);
        }
    }

    /**
     * 创建新用户
     * 
     * @param phone 手机号
     * @return 新创建的用户对象
     */
    private User createNewUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setLastName("用户"); // 默认姓氏，后续可让用户补充
        user.setGender(1); // 默认性别为男性
        user.setPermissionLevel(4); // 默认权限为游客
        user.setStatus(0); // 默认状态为正常
        user.setAmount(BigDecimal.ZERO); // 默认余额为0
        user.setDiscount(BigDecimal.ONE); // 默认折扣为1.0（无折扣）
        
        // 保存新用户（会自动触发@PrePersist设置创建时间等）
        user = userRepository.save(user);
        System.out.println("新用户注册成功: " + user);
        
        return user;
    }

    /**
     * 验证用户状态
     * 
     * @param user 用户对象
     * @throws RuntimeException 当用户状态异常时抛出
     */
    private void validateUserStatus(User user) {
        if (user.getStatus() == 1) {
            System.out.println("用户状态异常: " + user.getStatus());
            throw new RuntimeException("账户已被禁用");
        }
    }

    /**
     * 构建登录响应
     * 
     * @param user 用户信息
     * @param token JWT令牌
     * @return 登录响应
     */
    private LoginResponse buildLoginResponse(User user, String token) {
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
        
        return response;
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

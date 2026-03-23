package com.example.membersystem.auth.service;

import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.PasswordLoginRequest;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 密码登录服务
 * 
 * 专门处理用户使用手机号和密码登录的业务逻辑，包括：
 * - 用户身份验证
 * - 密码验证（支持BCrypt和历史密码兼容）
 * - 用户状态检查
 * - JWT令牌生成
 */
@Service
public class PasswordLoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public PasswordLoginService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
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
     * @throws RuntimeException 当用户不存在、密码错误或账户被禁用时抛出
     */
    @Transactional
    public LoginResponse passwordLogin(PasswordLoginRequest request) {
        System.out.println("=== PasswordLoginService 密码登录请求开始 ===");
        System.out.println("请求参数: " + request);
        System.out.println("loginType: " + request.getLoginType());
        System.out.println("phone: " + request.getPhone());
        System.out.println("password: " + request.getPassword());
        
        // 1. 根据手机号查找用户
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> {
                    System.out.println("用户不存在");
                    return new RuntimeException("用户不存在");
                });
        
        System.out.println("找到用户: " + user);
        
        // 2. 验证密码
        validatePassword(user, request.getPassword());
        
        // 3. 检查用户状态
        validateUserStatus(user);
        
        // 4. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getPermissionLevel());
        System.out.println("生成JWT token成功");
        
        // 5. 构造并返回登录响应
        LoginResponse response = buildLoginResponse(user, token);
        
        System.out.println("=== 密码登录成功 ===");
        System.out.println("返回响应: " + response);
        
        return response;
    }

    /**
     * 验证用户密码
     * 
     * 支持BCrypt加密密码和历史明文密码的兼容性验证
     * 
     * @param user 用户对象
     * @param inputPassword 输入的密码
     * @throws RuntimeException 当密码验证失败时抛出
     */
    private void validatePassword(User user, String inputPassword) {
        if (!StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("未设置密码，请使用短信登录后设置密码");
        }

        boolean isPasswordValid;
        String storedPassword = user.getPassword();
        
        if (isBcryptPassword(storedPassword)) {
            // BCrypt密码验证
            isPasswordValid = passwordEncoder.matches(inputPassword, storedPassword);
        } else {
            // 兼容历史明文密码（如果数据库里有旧数据），校验通过后自动升级为BCrypt
            isPasswordValid = storedPassword.equals(inputPassword);
            if (isPasswordValid) {
                // 自动升级为BCrypt加密
                user.setPassword(passwordEncoder.encode(inputPassword));
                userRepository.save(user);
                System.out.println("密码已自动升级为BCrypt加密");
            }
        }

        if (!isPasswordValid) {
            System.out.println("密码错误");
            throw new RuntimeException("密码错误");
        }
    }

    /**
     * 检查密码是否为BCrypt格式
     * 
     * @param password 存储的密码
     * @return 是否为BCrypt格式
     */
    private boolean isBcryptPassword(String password) {
        return password.startsWith("$2a$") || 
               password.startsWith("$2b$") || 
               password.startsWith("$2y$");
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

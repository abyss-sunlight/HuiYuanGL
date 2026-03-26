package com.example.membersystem.auth.service;

import com.example.membersystem.auth.dto.LoginResponse;
import com.example.membersystem.auth.dto.WxLoginRequest;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.common.BusinessException;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 微信登录服务
 * 
 * 专门处理微信授权登录的业务逻辑，包括：
 * - 微信登录凭证验证
 * - 微信用户信息处理
 * - 用户账户创建或更新
 * - JWT令牌生成
 */
@Service
public class WxLoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public WxLoginService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 微信登录
     * 
     * 处理微信授权登录的核心业务逻辑：
     * 1. 验证微信登录凭证code的有效性
     * 2. 获取微信用户信息
     * 3. 查找或创建用户账户
     * 4. 生成JWT令牌
     * 5. 返回登录响应
     * 
     * @param request 微信登录请求，包含code和userInfo
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws BusinessException 当微信凭证无效或处理失败时抛出
     */
    @Transactional
    public LoginResponse wxLogin(WxLoginRequest request) {
        System.out.println("=== WxLoginService 微信登录开始 ===");
        System.out.println("微信登录请求: " + request);
        
        // 1. 验证请求参数
        if (!StringUtils.hasText(request.getCode())) {
            throw new RuntimeException("微信登录凭证不能为空");
        }
        
        if (request.getUserInfo() == null) {
            throw new RuntimeException("微信用户信息不能为空");
        }
        
        // 2. TODO: 调用微信API验证code并获取openid和session_key
        // 这里暂时模拟处理，实际项目中需要调用微信API
        String openid = "mock_openid_" + System.currentTimeMillis(); // 模拟openid
        String sessionKey = "mock_session_key"; // 模拟session_key
        
        System.out.println("获取到微信openid: " + openid);
        
        // 3. 根据openid查找用户
        User user = userRepository.findByOpenid(openid)
                .map(existingUser -> {
                    // 用户已存在，检查状态并更新用户信息
                    validateUserStatus(existingUser);
                    updateUserInfoFromWx(existingUser, request.getUserInfo());
                    System.out.println("更新已存在的微信用户: " + existingUser.getPhone());
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 新用户，检查是否提供了必要信息
                    if (!StringUtils.hasText(request.getUserInfo().getLastName())) {
                        // 新用户但缺少必要信息，返回特殊状态码
                        System.out.println("新用户缺少必要信息，需要补充");
                        throw new BusinessException(201, "新用户需要补充姓氏和性别信息");
                    }
                    
                    // 创建新用户账户
                    User newUser = createUserFromWx(openid, request.getUserInfo());
                    System.out.println("创建新的微信用户: " + newUser.getPhone());
                    return newUser;
                });
        
        // 4. 保存用户信息
        user = userRepository.save(user);
        
        // 5. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getPermissionLevel());
        
        // 6. 构建登录响应
        return buildLoginResponse(user, token);
    }

    /**
     * 根据微信用户信息更新用户资料
     * 
     * @param user 现有用户对象
     * @param wxUserInfo 微信用户信息
     */
    private void updateUserInfoFromWx(User user, WxLoginRequest.WxUserInfo wxUserInfo) {
        // 更新用户名为微信昵称
        if (StringUtils.hasText(wxUserInfo.getNickName())) {
            user.setUsername(wxUserInfo.getNickName());
        }
        
        // 如果用户没有姓氏，从微信昵称提取
        if (!StringUtils.hasText(user.getLastName()) && StringUtils.hasText(wxUserInfo.getNickName())) {
            String lastName = extractLastNameFromNickName(wxUserInfo.getNickName());
            user.setLastName(lastName);
        }
        
        // 如果用户输入了姓氏，优先使用用户输入的
        if (StringUtils.hasText(wxUserInfo.getLastName())) {
            user.setLastName(wxUserInfo.getLastName());
        }
        
        // 可以根据需要更新其他字段，如性别、头像URL等
        if (wxUserInfo.getGender() != null) {
            user.setGender(wxUserInfo.getGender());
        }
        
        // 注意：手机号等敏感信息不应该从微信获取
    }

    /**
     * 根据微信用户信息创建新用户
     * 
     * @param openid 微信openid
     * @param wxUserInfo 微信用户信息
     * @return 新创建的用户对象
     */
    private User createUserFromWx(String openid, WxLoginRequest.WxUserInfo wxUserInfo) {
        User user = new User();
        
        // 设置微信相关信息
        user.setOpenid(openid);
        
        // 设置用户名（使用微信昵称）
        if (StringUtils.hasText(wxUserInfo.getNickName())) {
            user.setUsername(wxUserInfo.getNickName());
        } else {
            user.setUsername("微信用户_" + System.currentTimeMillis());
        }
        
        // 设置姓氏（从微信昵称提取或使用用户输入的姓氏）
        if (StringUtils.hasText(wxUserInfo.getLastName())) {
            // 如果用户输入了姓氏，使用用户输入的
            user.setLastName(wxUserInfo.getLastName());
        } else if (StringUtils.hasText(wxUserInfo.getNickName())) {
            // 否则从微信昵称提取姓氏
            String lastName = extractLastNameFromNickName(wxUserInfo.getNickName());
            user.setLastName(lastName);
        } else {
            // 都没有的话使用默认值
            user.setLastName("微信");
        }
        
        // 设置其他默认值
        user.setGender(wxUserInfo.getGender() != null ? wxUserInfo.getGender() : 1);
        user.setPermissionLevel(4); // 默认为游客权限，与短信注册保持一致
        user.setAmount(BigDecimal.ZERO);
        user.setDiscount(BigDecimal.ONE);
        
        // 生成临时手机号（实际项目中应该让用户绑定真实手机号）
        user.setPhone("wx_" + System.currentTimeMillis());
        
        return user;
    }
    
    /**
     * 从微信昵称中提取姓氏
     * 
     * @param nickName 微信昵称
     * @return 提取的姓氏，如果无法提取则返回"微信"
     */
    private String extractLastNameFromNickName(String nickName) {
        if (!StringUtils.hasText(nickName)) {
            return "微信";
        }
        
        // 去除空格和特殊字符
        String cleanName = nickName.trim();
        
        // 如果昵称是2-3个中文字符，取第一个字作为姓氏
        if (cleanName.length() >= 2 && cleanName.length() <= 3 && isChinese(cleanName.charAt(0))) {
            return cleanName.substring(0, 1);
        }
        
        // 如果昵称包含常见的中文名字格式（如"张三"、"李四"）
        if (cleanName.length() >= 2 && isChinese(cleanName.charAt(0)) && isChinese(cleanName.charAt(1))) {
            return cleanName.substring(0, 1);
        }
        
        // 如果昵称包含英文格式（如"Zhang San"），取第一个单词
        if (cleanName.contains(" ")) {
            String[] parts = cleanName.split(" ");
            if (parts.length > 0 && parts[0].length() > 0) {
                return parts[0];
            }
        }
        
        // 其他情况返回默认值
        return "微信";
    }
    
    /**
     * 判断字符是否为中文字符
     * 
     * @param ch 字符
     * @return 是否为中文字符
     */
    private boolean isChinese(char ch) {
        return ch >= '\u4e00' && ch <= '\u9fff';
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
        response.setPhone(user.getPhone());
        response.setLastName(user.getLastName());
        response.setGender(user.getGender());
        response.setPermissionLevel(user.getPermissionLevel());
        response.setAmount(user.getAmount());
        response.setDiscount(user.getDiscount());
        response.setCreatedAt(user.getCreatedAt());
        
        System.out.println("=== 微信登录成功 ===");
        System.out.println("返回响应: " + response);
        
        return response;
    }

    /**
     * 验证用户状态
     * 
     * @param user 用户对象
     * @throws BusinessException 当用户状态异常时抛出
     */
    private void validateUserStatus(User user) {
        if (user.getStatus() == 1) {
            System.out.println("用户状态异常: " + user.getStatus());
            throw new BusinessException(40101, "账户已被禁用，请联系管理员");
        }
    }
}

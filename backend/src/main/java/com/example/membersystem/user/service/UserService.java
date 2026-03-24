package com.example.membersystem.user.service;

import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserListItemResponse> listUsers(String phone, String lastName, Integer permissionLevel) {
        return userRepository.search(phone, lastName, permissionLevel)
                .stream()
                .map(UserService::toListItem)
                .toList();
    }

    /**
     * 根据手机号查找用户
     * 
     * @param phone 手机号
     * @return 用户信息（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * 根据ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户信息（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查权限：不能删除店长
        if (user.getPermissionLevel() == 1) {
            throw new RuntimeException("不能删除店长账户");
        }
        
        // 删除用户（级联删除相关记录）
        userRepository.deleteById(userId);
    }

    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param lastName 姓氏
     * @param gender 性别
     * @return 更新后的用户信息
     */
    @Transactional
    public User updateUser(Long userId, String lastName, Integer gender) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户信息
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }
        if (gender != null) {
            user.setGender(gender);
        }

        return userRepository.save(user);
    }

    /**
     * 更新当前用户个人信息
     * 
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @param username 用户名
     * @param lastName 姓氏
     * @param gender 性别
     * @return 更新后的用户信息
     */
    @Transactional
    public User updateUserProfile(Long userId, String avatarUrl, String username, String lastName, Integer gender) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户信息
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            user.setAvatarUrl(avatarUrl.trim());
        }
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName.trim());
        }
        if (gender != null) {
            user.setGender(gender);
        }

        return userRepository.save(user);
    }

    private static UserListItemResponse toListItem(User u) {
        UserListItemResponse resp = new UserListItemResponse();
        resp.setUserId(u.getId());
        resp.setUsername(u.getUsername());
        resp.setLastName(u.getLastName());
        resp.setGender(u.getGender());
        resp.setPermissionLevel(u.getPermissionLevel());
        resp.setPermissionName(getPermissionName(u.getPermissionLevel()));
        resp.setPhone(u.getPhone());

        if (u.getPermissionLevel() != null && u.getPermissionLevel() == 3) {
            resp.setMemberNo(u.getMemberNo());
            resp.setAmount(u.getAmount());
            resp.setDiscount(u.getDiscount());
        }

        return resp;
    }

    private static String getPermissionName(Integer permissionLevel) {
        if (permissionLevel == null) return "未知";
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

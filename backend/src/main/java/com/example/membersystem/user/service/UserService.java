package com.example.membersystem.user.service;

import com.example.membersystem.common.BusinessException;
import com.example.membersystem.user.dto.UserCreateRequest;
import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.entity.UserStatusLog;
import com.example.membersystem.user.repo.UserRepository;
import com.example.membersystem.user.repo.UserStatusLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserStatusLogRepository userStatusLogRepository;

    public UserService(UserRepository userRepository, UserStatusLogRepository userStatusLogRepository) {
        this.userRepository = userRepository;
        this.userStatusLogRepository = userStatusLogRepository;
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

    /**
     * 创建新用户
     * 
     * @param request 用户创建请求
     * @return 创建的用户信息
     * @throws BusinessException 当业务规则验证失败时抛出异常
     */
    @Transactional
    public User createUser(UserCreateRequest request) {
        // 验证手机号唯一性
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BusinessException(40001, "手机号已存在");
        }

        // 验证权限等级范围
        if (request.getPermissionLevel() < 2 || request.getPermissionLevel() > 4) {
            throw new BusinessException(40002, "权限等级无效，仅支持创建员工、会员、游客");
        }

        // 验证会员专属字段
        if (request.getPermissionLevel() == 3) {
            // 会员用户可以设置余额和折扣，进行范围验证
            if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(40003, "初始余额不能为负数");
            }
            
            if (request.getDiscount() != null) {
                if (request.getDiscount().compareTo(BigDecimal.ZERO) < 0 || 
                    request.getDiscount().compareTo(BigDecimal.ONE) > 0) {
                    throw new BusinessException(40004, "折扣率必须在0.0-1.0之间");
                }
            }
        } else {
            // 非会员用户不应设置余额和折扣
            if (request.getAmount() != null || request.getDiscount() != null) {
                throw new BusinessException(40005, "只有会员用户可以设置余额和折扣");
            }
        }

        try {
            // 创建新用户实体
            User user = new User();
            user.setPhone(request.getPhone());
            user.setLastName(request.getLastName());
            user.setGender(request.getGender());
            user.setPermissionLevel(request.getPermissionLevel());

            // 设置可选字段
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                user.setUsername(request.getUsername().trim());
            }
            // 如果未设置用户名，将使用User实体的@PrePersist方法自动生成

            if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
                user.setAvatarUrl(request.getAvatarUrl().trim());
            }

            // 仅会员用户可设置余额和折扣
            if (request.getPermissionLevel() != null && request.getPermissionLevel() == 3) {
                if (request.getAmount() != null) {
                    user.setAmount(request.getAmount());
                }
                if (request.getDiscount() != null) {
                    user.setDiscount(request.getDiscount());
                }
            } else {
                // 非会员用户不应设置余额和折扣，确保为默认值
                user.setAmount(BigDecimal.ZERO);
                user.setDiscount(BigDecimal.ONE);
            }

            // 保存用户（@PrePersist会自动设置创建时间、更新时间、默认值等）
            User savedUser = userRepository.save(user);

            return savedUser;
        } catch (Exception e) {
            // 记录详细错误信息
            throw new BusinessException(50001, "创建用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态 (0-正常, 1-禁用)
     * @param operatorId 操作者ID
     * @return 更新后的用户信息
     */
    @Transactional
    public User updateUserStatus(Long userId, Integer status, Long operatorId) {
        // 查找目标用户
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 查找操作用户
        User operatorUser = userRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("操作用户不存在"));
        
        // 权限检查：员工不能修改店长状态
        if (operatorUser.getPermissionLevel() == 2 && targetUser.getPermissionLevel() == 1) {
            throw new RuntimeException("员工不能修改店长状态");
        }
        
        // 权限检查：用户不能修改自己的状态
        if (userId.equals(operatorId)) {
            throw new RuntimeException("不能修改自己的账户状态");
        }
        
        // 记录状态变更前的状态（用于日志）
        Integer oldStatus = targetUser.getStatus();
        
        // 更新状态
        targetUser.setStatus(status);
        
        // 保存更新
        User updatedUser = userRepository.save(targetUser);
        
        // 记录状态变更日志
        logStatusChange(userId, operatorId, oldStatus, status);
        
        return updatedUser;
    }

    /**
     * 记录用户状态变更日志
     * 
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @param oldStatus 变更前状态
     * @param newStatus 变更后状态
     */
    private void logStatusChange(Long userId, Long operatorId, Integer oldStatus, Integer newStatus) {
        try {
            UserStatusLog log = new UserStatusLog();
            log.setUserId(userId);
            log.setOperatorId(operatorId);
            log.setOldStatus(oldStatus);
            log.setNewStatus(newStatus);
            log.setReason(getStatusChangeReason(oldStatus, newStatus));
            
            userStatusLogRepository.save(log);
        } catch (Exception e) {
            // 日志记录失败不应影响主流程，仅记录错误
            System.err.println("记录用户状态变更日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取状态变更原因描述
     * 
     * @param oldStatus 变更前状态
     * @param newStatus 变更后状态
     * @return 状态变更原因描述
     */
    private String getStatusChangeReason(Integer oldStatus, Integer newStatus) {
        if (oldStatus == null && newStatus == 0) {
            return "用户创建，默认正常状态";
        }
        
        if (oldStatus != null && newStatus != null) {
            if (oldStatus.equals(newStatus)) {
                return "状态无变化";
            }
            
            if (oldStatus == 0 && newStatus == 1) {
                return "管理员禁用用户账户";
            }
            
            if (oldStatus == 1 && newStatus == 0) {
                return "管理员启用用户账户";
            }
        }
        
        return "状态变更";
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
        resp.setStatus(u.getStatus());

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

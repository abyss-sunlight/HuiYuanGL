package com.example.membersystem.user.controller;

import com.example.membersystem.auth.annotation.RequirePermission;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.user.dto.ProfileUpdateRequest;
import com.example.membersystem.user.dto.UserCreateRequest;
import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.dto.UserStatusUpdateRequest;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    @RequirePermission(2)
    public ApiResponse<List<UserListItemResponse>> list(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false, name = "lastName") String lastName,
            @RequestParam(required = false) Integer permissionLevel
    ) {
        return ApiResponse.ok(userService.listUsers(phone, lastName, permissionLevel));
    }

    /**
     * 创建新用户
     * 
     * 仅店长（权限等级1）可以创建新用户
     * 支持创建员工、会员、游客权限等级的用户
     * 
     * @param createRequest 用户创建请求
     * @return 创建的用户信息
     */
    @PostMapping("/create")
    @RequirePermission(1) // 仅店长可创建用户
    public ApiResponse<User> createUser(@Valid @RequestBody UserCreateRequest createRequest) {
        User createdUser = userService.createUser(createRequest);
        return ApiResponse.ok(createdUser);
    }

    /**
     * 更新当前用户个人信息
     * 
     * @param request HTTP请求对象
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/update-profile")
    public ApiResponse<User> updateProfile(
            HttpServletRequest request,
            @RequestBody ProfileUpdateRequest updateRequest
    ) {
        // 从JWT token中获取用户ID
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 更新用户信息
        User updatedUser = userService.updateUserProfile(
            userId, 
            updateRequest.getAvatarUrl(),
            updateRequest.getUsername(),
            updateRequest.getLastName(),
            updateRequest.getGender()
        );
        
        return ApiResponse.ok(updatedUser);
    }

    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    public ApiResponse<User> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest updateRequest
    ) {
        User updatedUser = userService.updateUser(userId, updateRequest.getLastName(), updateRequest.getGender());
        return ApiResponse.ok(updatedUser);
    }

    /**
     * 更新用户状态
     * 
     * 用于启用或禁用用户账户
     * 只有店长和员工可以修改用户状态
     * 
     * @param userId 用户ID
     * @param statusUpdateRequest 状态更新请求
     * @param request HTTP请求对象
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}/status")
    @RequirePermission(2) // 员工及以上权限可以修改用户状态
    public ApiResponse<User> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest statusUpdateRequest,
            HttpServletRequest request
    ) {
        // 从JWT token中获取当前用户信息
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        
        // 验证状态值
        if (!statusUpdateRequest.isValidStatus()) {
            throw new IllegalArgumentException("无效的用户状态，只能是0（正常）或1（禁用）");
        }
        
        User updatedUser = userService.updateUserStatus(
            userId, 
            statusUpdateRequest.getStatus(), 
            currentUserId
        );
        
        return ApiResponse.ok(updatedUser);
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    @RequirePermission(1) // 只有店长可以删除用户
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.ok();
    }

    /**
     * 用户更新请求DTO
     */
    public static class UserUpdateRequest {
        private String lastName;
        private Integer gender;

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Integer getGender() {
            return gender;
        }

        public void setGender(Integer gender) {
            this.gender = gender;
        }
    }
}

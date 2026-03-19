package com.example.membersystem.user.controller;

import com.example.membersystem.auth.annotation.RequirePermission;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

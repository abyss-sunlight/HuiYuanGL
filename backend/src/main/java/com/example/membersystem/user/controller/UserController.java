package com.example.membersystem.user.controller;

import com.example.membersystem.auth.annotation.RequirePermission;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

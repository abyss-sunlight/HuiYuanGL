package com.example.membersystem.user.service;

import com.example.membersystem.user.dto.UserListItemResponse;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

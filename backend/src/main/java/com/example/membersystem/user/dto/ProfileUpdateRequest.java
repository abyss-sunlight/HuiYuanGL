package com.example.membersystem.user.dto;

/**
 * 个人信息更新请求DTO
 */
public class ProfileUpdateRequest {
    private String avatarUrl;
    private String username;
    private String lastName;
    private Integer gender;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    @Override
    public String toString() {
        return "ProfileUpdateRequest{" +
                "avatarUrl='" + avatarUrl + '\'' +
                ", username='" + username + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                '}';
    }
}

package com.example.membersystem.auth.dto;

/**
 * 微信登录请求DTO
 * 
 * 封装微信登录请求所需的参数：
 * - code: 微信登录凭证
 * - userInfo: 微信用户信息
 */
public class WxLoginRequest {
    
    /**
     * 微信登录凭证
     * 通过wx.login()获取的临时登录凭证
     */
    private String code;
    
    /**
     * 微信用户信息
     * 通过wx.getUserProfile()获取的用户详细信息
     */
    private WxUserInfo userInfo;
    
    // 默认构造函数
    public WxLoginRequest() {}
    
    // 带参构造函数
    public WxLoginRequest(String code, WxUserInfo userInfo) {
        this.code = code;
        this.userInfo = userInfo;
    }
    
    // Getter和Setter方法
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public WxUserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(WxUserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Override
    public String toString() {
        return "WxLoginRequest{" +
                "code='" + code + '\'' +
                ", userInfo=" + userInfo +
                '}';
    }
    
    /**
     * 微信用户信息内部类
     * 
     * 封装从微信获取的用户详细信息
     */
    public static class WxUserInfo {
        
        /**
         * 用户姓氏
         * 微信登录时用户输入的姓氏
         */
        private String lastName;
        
        /**
         * 用户昵称
         */
        private String nickName;
        
        /**
         * 用户头像URL
         */
        private String avatarUrl;
        
        /**
         * 用户性别：0-未知，1-男，2-女
         */
        private Integer gender;
        
        /**
         * 用户所在城市
         */
        private String city;
        
        /**
         * 用户所在省份
         */
        private String province;
        
        /**
         * 用户所在国家
         */
        private String country;
        
        /**
         * 用户手机号
         * 通过getPhoneNumber获取的手机号code
         */
        private String phoneNumber;
        
        /**
         * 用户唯一标识
         */
        private String unionId;
        
        // 默认构造函数
        public WxUserInfo() {}
        
        // 带参构造函数
        public WxUserInfo(String nickName, String avatarUrl, Integer gender) {
            this.nickName = nickName;
            this.avatarUrl = avatarUrl;
            this.gender = gender;
        }
        
        // Getter和Setter方法
        public String getLastName() {
            return lastName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getNickName() {
            return nickName;
        }
        
        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        
        public Integer getGender() {
            return gender;
        }
        
        public void setGender(Integer gender) {
            this.gender = gender;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getUnionId() {
            return unionId;
        }
        
        public void setUnionId(String unionId) {
            this.unionId = unionId;
        }
        
        @Override
        public String toString() {
            return "WxUserInfo{" +
                    "lastName='" + lastName + '\'' +
                    ", nickName='" + nickName + '\'' +
                    ", avatarUrl='" + avatarUrl + '\'' +
                    ", gender=" + gender +
                    ", city='" + city + '\'' +
                    ", province='" + province + '\'' +
                    ", country='" + country + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", unionId='" + unionId + '\'' +
                    '}';
        }
    }
}

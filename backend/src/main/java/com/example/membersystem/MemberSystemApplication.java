package com.example.membersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 会员管理系统后端启动类。
 *
 * 说明：
 * - 这是一个基础框架工程，后续可逐步加入登录鉴权、会员等级、消费记录、积分等功能。
 */
@SpringBootApplication
public class MemberSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberSystemApplication.class, args);
    }
}

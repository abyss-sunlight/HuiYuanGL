package com.example.membersystem.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 
 * 权限等级：
 * - 1: 店长（最高权限）
 * - 2: 员工
 * - 3: 会员
 * - 4: 游客（最低权限）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 所需的最低权限等级
     * 默认为3（会员权限）
     */
    int value() default 3;
    
    /**
     * 权限不足时的提示信息
     */
    String message() default "权限不足";
}

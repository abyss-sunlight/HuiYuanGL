package com.example.membersystem.auth.interceptor;

import com.example.membersystem.auth.annotation.RequirePermission;
import com.example.membersystem.auth.util.JwtUtil;
import com.example.membersystem.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 
 * 实现Spring MVC的HandlerInterceptor接口，用于统一处理API的认证和权限验证
 * 
 * 主要功能：
 * - 拦截所有API请求，进行身份验证
 * - 检查方法或类上的@RequirePermission注解
 * - 验证JWT令牌的有效性和权限
 * - 将用户信息存入请求属性，供后续使用
 * 
 * 权限验证规则：
 * - 数字越小权限越高（1 > 2 > 3 > 4）
 * - 用户权限等级必须小于等于要求的权限等级
 * 
 * 使用方式：
 * - 通过WebConfig注册到Spring MVC拦截器链
 * - 对需要权限的接口添加@RequirePermission注解
 * - 客户端需要在请求头中携带Bearer Token
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 构造函数注入JWT工具
     * 
     * @param jwtUtil JWT工具类，用于令牌验证和解析
     */
    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 前置处理方法
     * 
     * 在Controller方法执行前调用，用于身份验证和权限检查
     * 
     * 处理流程：
     * 1. 检查处理器类型，只处理方法处理器
     * 2. 检查方法或类上的权限注解
     * 3. 如果没有权限注解，直接放行
     * 4. 从请求头提取JWT令牌
     * 5. 验证令牌的有效性和权限
     * 6. 将用户信息存入请求属性
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象，通常是Controller方法
     * @return true表示继续执行，false表示拦截
     * @throws Exception 认证失败时抛出BusinessException
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 检查处理器类型，只处理方法处理器
        // 非方法处理器（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 2. 检查方法或类上的权限注解
        // 优先使用方法上的注解，如果没有则检查类上的注解
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }

        // 3. 如果没有权限注解，直接放行
        // 允许公开访问的接口不需要权限验证
        if (requirePermission == null) {
            return true;
        }

        // 4. 从请求头获取JWT令牌
        // 期望格式：Authorization: Bearer <token>
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException(40101, "请先登录");
        }

        // 5. 验证令牌和权限
        try {
            // 5.1 检查令牌是否过期
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(40102, "登录已过期，请重新登录");
            }

            // 5.2 获取用户和要求的权限等级
            Integer userPermissionLevel = jwtUtil.getPermissionLevelFromToken(token);
            Integer requiredPermissionLevel = requirePermission.value();

            // 5.3 验证权限等级
            // 权限规则：数字越小权限越高
            // 用户权限必须小于等于要求的权限
            if (userPermissionLevel > requiredPermissionLevel) {
                throw new BusinessException(40301, requirePermission.message());
            }

            // 6. 将用户信息放入请求属性，供Controller使用
            // 这样可以在Controller中直接获取当前用户信息
            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("phone", jwtUtil.getPhoneFromToken(token));
            request.setAttribute("permissionLevel", userPermissionLevel);

            return true;

        } catch (BusinessException e) {
            // 业务异常直接重新抛出
            throw e;
        } catch (Exception e) {
            // 其他异常包装为认证失败异常
            throw new BusinessException(40103, "token验证失败");
        }
    }

    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * 从Authorization请求头中提取Bearer Token
     * 
     * 期望格式：
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果格式不正确则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 获取Authorization请求头
        String bearerToken = request.getHeader("Authorization");
        
        // 检查格式是否正确
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // 去除"Bearer "前缀，返回纯令牌
            return bearerToken.substring(7);
        }
        
        return null;
    }
}

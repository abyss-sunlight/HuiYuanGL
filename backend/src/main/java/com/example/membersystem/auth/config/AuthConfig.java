package com.example.membersystem.auth.config;

import com.example.membersystem.auth.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 认证配置
 */
@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public AuthConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有api路径
                .excludePathPatterns(
                        "/api/auth/login",     // 排除登录接口
                        "/api/auth/wechat-login", // 排除微信登录接口
                        "/api/auth/sms-login",    // 排除短信登录接口
                        "/api/health"           // 排除健康检查接口
                );
    }
}

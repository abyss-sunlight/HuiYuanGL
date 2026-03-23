package com.example.membersystem.auth.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.membersystem.common.BusinessException;

@Service
public class SmsCodeService {

    /**
     * 验证码类型枚举
     */
    public enum Type {
        LOGIN("登录"),
        CHANGE_PHONE_ORIGINAL("修改手机号-原手机验证"),
        CHANGE_PHONE_NEW("修改手机号-新手机验证");
        
        private final String description;
        
        Type(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }

    private static class CodeItem {
        private final String code;
        private final LocalDateTime expiresAt;
        private final LocalDateTime lastSendAt;

        private CodeItem(String code, LocalDateTime expiresAt, LocalDateTime lastSendAt) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.lastSendAt = lastSendAt;
        }
    }

    private static final int CODE_LEN = 6;
    private static final int EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;

    private final Map<String, Map<Type, CodeItem>> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void sendCode(String phone) {
        sendSmsCode(phone, Type.LOGIN);
    }

    /**
     * 发送指定类型的验证码
     */
    public void sendSmsCode(String phone, Type type) {
        System.out.println("=== 发送验证码开始 ===");
        System.out.println("手机号: " + phone);
        System.out.println("验证码类型: " + type + " (" + type.getDescription() + ")");
        
        validatePhone(phone);

        Map<Type, CodeItem> phoneCodes = store.computeIfAbsent(phone, k -> new ConcurrentHashMap<>());
        CodeItem existing = phoneCodes.get(type);
        
        System.out.println("该手机号现有验证码: " + phoneCodes);
        System.out.println("该类型的现有验证码: " + existing);
        
        if (existing != null && existing.lastSendAt != null) {
            if (existing.lastSendAt.plusSeconds(SEND_INTERVAL_SECONDS).isAfter(LocalDateTime.now())) {
                System.out.println("错误: 发送过于频繁");
                throw new BusinessException(42901, "发送过于频繁，请稍后再试");
            }
        }

        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        phoneCodes.put(type, new CodeItem(code, now.plusMinutes(EXPIRE_MINUTES), now));

        System.out.println("生成的验证码: " + code);
        System.out.println("过期时间: " + now.plusMinutes(EXPIRE_MINUTES));
        System.out.println("更新后的验证码存储: " + phoneCodes);
        System.out.println("=== 发送验证码结束 ===");
        
        System.out.println("模拟发送短信验证码到: " + phone);
        System.out.println("验证码: " + code + "（有效期" + EXPIRE_MINUTES + "分钟，类型：" + type.getDescription() + "）");
    }

    /**
     * 验证并消费验证码（兼容旧方法）
     */
    public void verifyAndConsume(String phone, String code) {
        verifyCode(phone, code, Type.LOGIN);
    }

    /**
     * 验证指定类型的验证码
     */
    public void verifyCode(String phone, String code, Type type) {
        System.out.println("=== 验证码验证开始 ===");
        System.out.println("手机号: " + phone);
        System.out.println("输入验证码: " + code);
        System.out.println("验证码类型: " + type + " (" + type.getDescription() + ")");
        System.out.println("当前存储的验证码: " + store);
        
        validatePhone(phone);
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(40002, "验证码不能为空");
        }

        Map<Type, CodeItem> phoneCodes = store.get(phone);
        System.out.println("该手机号的验证码映射: " + phoneCodes);
        
        if (phoneCodes == null) {
            System.out.println("错误: 该手机号没有任何验证码");
            throw new BusinessException(40002, "验证码不存在或已失效");
        }

        CodeItem item = phoneCodes.get(type);
        System.out.println("指定类型的验证码项: " + item);
        
        if (item == null) {
            System.out.println("错误: 该手机号没有指定类型的验证码");
            System.out.println("可用的验证码类型: " + phoneCodes.keySet());
            throw new BusinessException(40002, "验证码不存在或已失效");
        }

        System.out.println("验证码过期时间: " + item.expiresAt);
        System.out.println("当前时间: " + LocalDateTime.now());
        
        if (item.expiresAt.isBefore(LocalDateTime.now())) {
            System.out.println("错误: 验证码已过期");
            phoneCodes.remove(type);
            if (phoneCodes.isEmpty()) {
                store.remove(phone);
            }
            throw new BusinessException(40002, "验证码已过期");
        }

        System.out.println("存储的验证码: " + item.code);
        System.out.println("输入的验证码: " + code);

        if (!item.code.equals(code)) {
            System.out.println("错误: 验证码不匹配");
            throw new BusinessException(40002, "验证码错误");
        }

        System.out.println("验证码验证成功，清除验证码");
        phoneCodes.remove(type);
        if (phoneCodes.isEmpty()) {
            store.remove(phone);
        }
        System.out.println("=== 验证码验证结束 ===");
    }

    /**
     * 清除指定手机号的验证码
     */
    public void clearCode(String phone) {
        store.remove(phone);
    }

    /**
     * 清除指定手机号和类型的验证码
     */
    public void clearCode(String phone, Type type) {
        Map<Type, CodeItem> phoneCodes = store.get(phone);
        if (phoneCodes != null) {
            phoneCodes.remove(type);
            if (phoneCodes.isEmpty()) {
                store.remove(phone);
            }
        }
    }

    private void validatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(40001, "手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(40001, "手机号格式不正确");
        }
    }

    private String generateCode() {
        int max = (int) Math.pow(10, CODE_LEN);
        int n = random.nextInt(max);
        return String.format("%0" + CODE_LEN + "d", n);
    }
}

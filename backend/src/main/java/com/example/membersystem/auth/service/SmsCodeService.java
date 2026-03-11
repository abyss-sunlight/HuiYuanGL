package com.example.membersystem.auth.service;

import com.example.membersystem.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {

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

    private final Map<String, CodeItem> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void sendCode(String phone) {
        validatePhone(phone);

        CodeItem existing = store.get(phone);
        if (existing != null && existing.lastSendAt != null) {
            if (existing.lastSendAt.plusSeconds(SEND_INTERVAL_SECONDS).isAfter(LocalDateTime.now())) {
                throw new BusinessException(42901, "发送过于频繁，请稍后再试");
            }
        }

        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        store.put(phone, new CodeItem(code, now.plusMinutes(EXPIRE_MINUTES), now));

        System.out.println("模拟发送短信验证码到: " + phone);
        System.out.println("验证码: " + code + "（有效期" + EXPIRE_MINUTES + "分钟）");
    }

    public void verifyAndConsume(String phone, String code) {
        validatePhone(phone);
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(40002, "验证码不能为空");
        }

        CodeItem item = store.get(phone);
        if (item == null) {
            throw new BusinessException(40102, "验证码不存在或已失效");
        }

        if (item.expiresAt.isBefore(LocalDateTime.now())) {
            store.remove(phone);
            throw new BusinessException(40102, "验证码已过期");
        }

        if (!item.code.equals(code)) {
            throw new BusinessException(40102, "验证码错误");
        }

        store.remove(phone);
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

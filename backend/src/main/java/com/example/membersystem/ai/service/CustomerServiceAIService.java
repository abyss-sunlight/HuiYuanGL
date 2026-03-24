package com.example.membersystem.ai.service;

import com.example.membersystem.ai.dto.CustomerServiceRequest;
import com.example.membersystem.ai.dto.CustomerServiceResponse;
import com.example.membersystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI客服服务类
 * 
 * 提供智能客服功能，基于知识库回答用户问题
 */
@Service
public class CustomerServiceAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceAIService.class);
    
    @Value("${customer-service.ai.api-url}")
    private String apiUrl;
    
    @Value("${customer-service.ai.app-id}")
    private String appId;
    
    @Value("${customer-service.ai.app-key}")
    private String appKey;
    
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    
    // 店长联系方式
    private static final String MANAGER_CONTACT = "18726685085";
    
    public CustomerServiceAIService(RestTemplate restTemplate, ObjectMapper objectMapper, UserService userService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }
    
    /**
     * 处理用户消息
     * 
     * @param request 客服请求
     * @return AI回复
     */
    public CustomerServiceResponse processMessage(CustomerServiceRequest request) {
        try {
            logger.info("处理AI客服请求，用户消息: {}", request.getMessage());
            
            // 检查服务是否可用
            if (!isServiceAvailable()) {
                return CustomerServiceResponse.error("AI客服服务暂未配置，请联系管理员");
            }
            
            // 获取用户信息
            CustomerServiceRequest.UserInfo userInfo = getUserInfo(request);
            
            // 构建AI请求
            Map<String, Object> aiRequest = buildAIRequest(request, userInfo);
            
            // 调用AI服务
            String aiResponse = callAIService(aiRequest);
            
            // 解析AI响应
            CustomerServiceResponse response = parseAIResponse(aiResponse);
            
            // 设置店长联系方式
            if (response.isNeedContactManager()) {
                response.setManagerContact(MANAGER_CONTACT);
            }
            
            // 设置会话ID
            response.setSessionId(request.getSessionId());
            
            // 添加快捷问题建议
            response.setQuickQuestions(getQuickQuestions(userInfo));
            
            return response;
            
        } catch (Exception e) {
            logger.error("处理AI客服请求失败", e);
            return CustomerServiceResponse.error("客服服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    private CustomerServiceRequest.UserInfo getUserInfo(CustomerServiceRequest request) {
        CustomerServiceRequest.UserInfo userInfo = request.getUserInfo();
        
        if (userInfo == null) {
            userInfo = new CustomerServiceRequest.UserInfo();
            userInfo.setIsMember(false);
            userInfo.setPermissionLevel(4); // 游客权限
        }
        
        // 判断是否为会员
        if (userInfo.getMemberNo() != null && !userInfo.getMemberNo().isEmpty() 
            && userInfo.getAmount() != null && userInfo.getAmount() > 0) {
            userInfo.setIsMember(true);
        } else {
            userInfo.setIsMember(false);
        }
        
        return userInfo;
    }
    
    /**
     * 构建AI请求
     */
    private Map<String, Object> buildAIRequest(CustomerServiceRequest request, CustomerServiceRequest.UserInfo userInfo) {
        Map<String, Object> body = new HashMap<>();
        
        // 腾讯元器API标准格式
        body.put("assistant_id", appId);
        
        // 构建消息 - 腾讯元器要求content是对象列表，且第一个消息角色必须是user
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 用户消息（必须是第一个）
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 将用户消息和系统提示词合并构建为符合规范的对象列表
        List<Map<String, String>> userContentList = new ArrayList<>();
        
        // 先添加系统提示词
        Map<String, String> systemTextContent = new HashMap<>();
        systemTextContent.put("type", "text");
        systemTextContent.put("text", buildSystemPrompt(userInfo) + "\n\n" + request.getMessage());
        userContentList.add(systemTextContent);
        
        userMessage.put("content", userContentList);
        messages.add(userMessage);
        
        body.put("messages", messages);
        
        // 腾讯元器可选参数
        body.put("temperature", 0.7);
        body.put("top_p", 0.9);
        body.put("max_tokens", 1500);
        
        return body;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(CustomerServiceRequest.UserInfo userInfo) {
        StringBuilder prompt = new StringBuilder();
        
        // 使用默认提示词（智能体自带知识库）
        prompt.append("你是美甲美睫门店的AI客服助手，请回答用户关于会员管理、充值、服务记录等问题。");
        
        // 添加用户身份信息
        prompt.append("\n\n## 当前用户身份信息\n");
        prompt.append("- 是否会员: ").append(userInfo.getIsMember() ? "是" : "否").append("\n");
        prompt.append("- 权限等级: ").append(userInfo.getPermissionLevel()).append("\n");
        if (userInfo.getMemberNo() != null) {
            prompt.append("- 会员号: ").append(userInfo.getMemberNo()).append("\n");
        }
        if (userInfo.getAmount() != null) {
            prompt.append("- 账户余额: ").append(userInfo.getAmount()).append("元\n");
        }
        
        // 添加回复要求
        prompt.append("\n## 回复要求\n");
        prompt.append("1. 根据智能体知识库内容回答\n");
        prompt.append("2. 根据用户身份提供差异化服务\n");
        prompt.append("3. 涉及资金操作时，引导联系店长\n");
        prompt.append("4. 可以建议页面跳转，但需要用户同意\n");
        prompt.append("5. 回复要简洁、友好、专业\n");
        
        return prompt.toString();
    }
    
    /**
     * 调用AI服务
     */
    private String callAIService(Map<String, Object> requestBody) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + appKey);
            headers.set("X-Source", "openapi");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("调用AI客服服务，URL: {}", apiUrl);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            logger.info("AI客服服务响应状态: {}", response.getStatusCode());
            
            return response.getBody();
            
        } catch (Exception e) {
            logger.error("调用AI客服服务失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析AI响应
     */
    private CustomerServiceResponse parseAIResponse(String responseBody) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            
            CustomerServiceResponse aiResponse = new CustomerServiceResponse();
            
            // 解析腾讯元器响应格式
            if (response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String content = (String) message.get("content");
                    aiResponse.setContent(content);
                    
                    // 分析是否需要联系店长
                    if (content.contains("联系店长") || content.contains("店长电话") || content.contains("人工客服")) {
                        aiResponse.setNeedContactManager(true);
                    }
                    
                    // 分析操作类型
                    aiResponse.setActionType(analyzeActionType(content));
                    aiResponse.setActionParams(buildActionParams(content));
                }
            }
            
            aiResponse.setStatus("success");
            return aiResponse;
            
        } catch (Exception e) {
            logger.error("解析AI响应失败", e);
            return CustomerServiceResponse.error("AI响应解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析操作类型
     */
    private String analyzeActionType(String content) {
        if (content.contains("跳转到") || content.contains("页面")) {
            return "navigate";
        }
        if (content.contains("联系店长") || content.contains("电话")) {
            return "contact";
        }
        return null;
    }
    
    /**
     * 构建操作参数
     */
    private Map<String, Object> buildActionParams(String content) {
        Map<String, Object> params = new HashMap<>();
        
        // 根据内容分析页面跳转参数
        if (content.contains("我的") || content.contains("余额")) {
            params.put("page", "pages/profile/profile");
            params.put("name", "个人中心");
        }
        if (content.contains("记录") || content.contains("消费记录")) {
            params.put("page", "pages/records/records");
            params.put("name", "记录页面");
        }
        if (content.contains("美睫记录")) {
            params.put("page", "pages/records/records?tab=eyelash");
            params.put("name", "美睫记录");
        }
        
        return params;
    }
    
    /**
     * 获取快捷问题建议
     */
    private List<String> getQuickQuestions(CustomerServiceRequest.UserInfo userInfo) {
        List<String> questions = new ArrayList<>();
        
        if (userInfo.getIsMember()) {
            questions.add("我的余额是多少？");
            questions.add("查看我的消费记录");
            questions.add("我上次做的睫毛是什么款式？");
        } else {
            questions.add("如何成为会员？");
            questions.add("充值有优惠吗？");
            questions.add("查看我的美睫记录");
        }
        
        questions.add("如何联系店长？");
        
        return questions;
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isServiceAvailable() {
        return appId != null && !appId.isEmpty() && 
               appKey != null && !appKey.isEmpty();
    }
}

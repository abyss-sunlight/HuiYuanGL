package com.example.membersystem.ai.service;

import com.example.membersystem.ai.dto.YuanbaoAIRequest;
import com.example.membersystem.ai.dto.YuanbaiAIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 腾讯元宝AI服务
 * 
 * 提供门店数据分析AI功能，包括：
 * - 盈利统计分析
 * - 会员消费分析
 * - 项目收入排名
 * - 趋势对比分析
 */
@Service
public class YuanbaoAIService {

    private static final Logger logger = LoggerFactory.getLogger(YuanbaoAIService.class);
    
    @Value("${yuanqi.app-id}")
    private String appId;
    
    @Value("${yuanqi.app-key}")
    private String appKey;
    
    @Value("${yuanqi.api-url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public YuanbaoAIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 调用元宝AI进行数据分析
     * 
     * @param request AI请求参数
     * @return AI分析结果
     */
    public YuanbaiAIResponse analyzeData(YuanbaoAIRequest request) {
        try {
            logger.info("调用元宝AI进行数据分析，问题: {}", request.getQuestion());
            
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(request);
            logger.info("发送给腾讯API的请求体: {}", objectMapper.writeValueAsString(requestBody));
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + appKey);
            headers.set("X-Source", "openapi");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            logger.info("请求URL: {}", apiUrl);
            logger.info("请求头: {}", headers.toSingleValueMap());
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            logger.info("腾讯API响应状态: {}", response.getStatusCode());
            logger.info("腾讯API响应内容: {}", response.getBody());
            
            // 解析响应
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            logger.error("调用元宝AI失败", e);
            YuanbaiAIResponse errorResponse = new YuanbaiAIResponse();
            errorResponse.setStatus("error");
            errorResponse.setContent("AI服务异常: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(YuanbaoAIRequest request) {

        Map<String, Object> body = new HashMap<>();
        
        // 腾讯元器API标准格式
        body.put("assistant_id", appId);
        
        // 构建消息 - 腾讯元器要求content是字符串
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();

        // 将 content 构建为符合规范的对象列表
        List<Map<String, String>> contentList = new ArrayList<>();
        Map<String, String> textContent = new HashMap<>();
        userMessage.put("role", "user");
        textContent.put("type", "text");
        textContent.put("text", buildPrompt(request));
        contentList.add(textContent);

        userMessage.put("content", contentList);
        messages.add(userMessage);

        body.put("messages", messages);
        
        
        // 腾讯元器可选参数
        body.put("temperature", 0.7);
        body.put("top_p", 0.9);
        body.put("max_tokens", 2000);
        
        return body;
    }
    
    /**
     * 构建AI提示词
     */
    private String buildPrompt(YuanbaoAIRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // 角色设定
        prompt.append("你是\"XX美甲美睫店\"的专属数据AI分析师，专门为店长提供基于实时消费数据的盈利统计与变动分析。\n\n");
        
        // 知识库内容
        prompt.append("## 数据库结构\n");
        prompt.append("用户表(user)：id, username, last_name, phone, gender, amount, discount, permission_level, created_at\n");
        prompt.append("消费记录表(consume_records)：id, phone, last_name, gender, balance, consume_amount, consume_item, consume_date, consume_type, created_at\n\n");
        
        prompt.append("## 核心指标公式\n");
        prompt.append("1. 总营收 = SUM(consume_amount) WHERE consume_type = '充值'\n");
        prompt.append("2. 有效订单数 = COUNT(id) WHERE consume_type = '充值'\n");
        prompt.append("3. 客单价 = 总营收 / 有效订单数\n");
        prompt.append("4. 会员消费占比 = (会员消费总金额 / 总营收) * 100%\n");
        prompt.append("5. 项目收入排名：按 consume_item 分组计算营收和订单数\n");
        prompt.append("6. 环比增长率 = (本期数值 - 上期数值) / 上期数值 * 100%\n\n");
        
        // 当前数据
        if (request.getCurrentData() != null) {
            prompt.append("## 当前数据\n");
            prompt.append("时间范围: ").append(request.getTimeRange()).append("\n");
            prompt.append("消费记录数据:\n");
            prompt.append(formatDataForAI(request.getCurrentData()));
            prompt.append("\n");
        }
        
        // 对比数据
        if (request.getCompareData() != null) {
            prompt.append("## 对比数据\n");
            prompt.append("对比时间范围: ").append(request.getCompareTimeRange()).append("\n");
            prompt.append("消费记录数据:\n");
            prompt.append(formatDataForAI(request.getCompareData()));
            prompt.append("\n");
        }
        
        // 用户问题
        prompt.append("## 用户问题\n");
        prompt.append(request.getQuestion()).append("\n\n");
        
        // 输出要求
        prompt.append("## 回答要求\n");
        prompt.append("请严格按照以下格式回答：\n");
        prompt.append("1. 先用一句话总结核心发现\n");
        prompt.append("2. 提供关键数据指标（营收、订单数、客单价等）\n");
        prompt.append("3. 进行对比分析（环比、同比或项目间对比）\n");
        prompt.append("4. 给出1-2条简明的业务观察或建议\n");
        prompt.append("5. 所有分析必须严格基于提供的数据，不得臆测\n");
        
        return prompt.toString();
    }
    
    /**
     * 格式化数据用于AI分析
     */
    private String formatDataForAI(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "暂无数据";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> record : data) {
            // 安全获取消费金额，处理可能的null值和类型转换
            Object consumeAmountObj = record.get("consume_amount");
            double consumeAmount = 0.0;
            if (consumeAmountObj != null) {
                if (consumeAmountObj instanceof Number) {
                    consumeAmount = ((Number) consumeAmountObj).doubleValue();
                } else if (consumeAmountObj instanceof String) {
                    try {
                        consumeAmount = Double.parseDouble((String) consumeAmountObj);
                    } catch (NumberFormatException e) {
                        consumeAmount = 0.0;
                    }
                }
            }
            
            sb.append(String.format("ID:%s, 手机号:%s, 姓氏:%s, 消费金额:%.2f, 消费项目:%s, 消费类型:%s, 日期:%s\n",
                record.get("id"),
                record.get("phone"),
                record.get("last_name"),
                consumeAmount,
                record.get("consume_item"),
                record.get("consume_type"),
                record.get("consume_date")
            ));
        }
        return sb.toString();
    }

    /**
     * 解析AI响应
     */
    private YuanbaiAIResponse parseResponse(String responseBody) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            
            YuanbaiAIResponse aiResponse = new YuanbaiAIResponse();
            
            // 解析腾讯元器响应格式
            if (response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    aiResponse.setContent((String) message.get("content"));
                }
            } else if (response.containsKey("data")) {
                // 备用解析方式
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data.containsKey("content")) {
                    aiResponse.setContent((String) data.get("content"));
                }
            }
            
            aiResponse.setStatus("success");
            return aiResponse;
            
        } catch (Exception e) {
            logger.error("解析AI响应失败", e);
            YuanbaiAIResponse errorResponse = new YuanbaiAIResponse();
            errorResponse.setStatus("error");
            errorResponse.setContent("AI响应解析失败: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isServiceAvailable() {
        return appId != null && !appId.isEmpty() && 
               appKey != null && !appKey.isEmpty();
    }
}

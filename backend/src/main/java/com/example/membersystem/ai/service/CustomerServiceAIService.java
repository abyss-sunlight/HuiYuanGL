package com.example.membersystem.ai.service;

import com.example.membersystem.ai.dto.CustomerServiceRequest;
import com.example.membersystem.ai.dto.CustomerServiceResponse;
import com.example.membersystem.discount.entity.RechargeDiscount;
import com.example.membersystem.discount.service.RechargeDiscountService;
import com.example.membersystem.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final RechargeDiscountService rechargeDiscountService;
    
    // 店长联系方式
    private static final String MANAGER_CONTACT = "18726685085";
    
    public CustomerServiceAIService(RestTemplate restTemplate, ObjectMapper objectMapper, UserService userService, RechargeDiscountService rechargeDiscountService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.rechargeDiscountService = rechargeDiscountService;
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
            
            // 检查是否需要主动关怀（首次对话）
            String proactiveCareMessage = null;
            if (request.getChatHistory() == null || request.getChatHistory().isEmpty()) {
                proactiveCareMessage = generateProactiveCareMessage(userInfo);
            }
            
            // 构建AI请求
            Map<String, Object> aiRequest = buildAIRequest(request, userInfo);
            
            // 调用AI服务
            String aiResponse = callAIService(aiRequest);
            
            // 解析AI响应
            CustomerServiceResponse response = parseAIResponse(aiResponse);
            
            // 如果有主动关怀消息，添加到响应内容前
            if (proactiveCareMessage != null) {
                String originalContent = response.getContent();
                String newContent = proactiveCareMessage + "\n\n" + originalContent;
                response.setContent(newContent);
            }
            
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
     * 获取当前启用的充值折扣信息
     */
    private String getActiveRechargeDiscounts() {
        try {
            List<RechargeDiscount> activeDiscounts = rechargeDiscountService.findAllActive();
            
            if (activeDiscounts.isEmpty()) {
                return "暂无可用充值折扣，请联系店长咨询。";
            }
            
            StringBuilder discountInfo = new StringBuilder();
            discountInfo.append("当前充值优惠活动：\n");
            
            for (RechargeDiscount discount : activeDiscounts) {
                discountInfo.append(String.format("• 充%d元，账户到账%d元（享%d折优惠）\n", 
                    discount.getRechargeAmount().intValue(),
                    discount.getRechargeAmount().intValue(),
                    discount.getDiscountPercentage().intValue()));
            }
            
            discountInfo.append("说明：充值金额全额到账，折扣在消费时抵扣。具体活动以店长告知为准，我是AI无法办理充值。");
            
            return discountInfo.toString();
            
        } catch (Exception e) {
            logger.error("获取充值折扣信息失败", e);
            return "充值优惠信息暂时无法获取，请联系店长咨询。";
        }
    }
    
    /**
     * 生成用户画像数据（模拟）
     */
    private Map<String, Object> generateUserProfile(CustomerServiceRequest.UserInfo userInfo) {
        Map<String, Object> profile = new HashMap<>();
        
        if (!userInfo.getIsMember()) {
            // 游客画像
            profile.put("preferred_style", "未知");
            profile.put("avg_consumption", 0);
            profile.put("last_visit_days", 999);
            profile.put("consumption_structure", "无消费记录");
            return profile;
        }
        
        // 会员画像（模拟数据）
        // 模拟偏好款式 - 基于会员号后缀随机分配
        String[] styles = {"自然款", "浓密款", "猫眼款", "仙女款", "网红款"};
        int styleIndex = Math.abs(userInfo.getMemberNo().hashCode()) % styles.length;
        profile.put("preferred_style", styles[styleIndex]);
        
        // 模拟平均消费 - 基于余额和会员号计算
        double avgConsumption = 200 + (Math.abs(userInfo.getMemberNo().hashCode()) % 300);
        profile.put("avg_consumption", Math.round(avgConsumption));
        
        // 模拟距上次到店天数 - 基于会员号计算
        int lastVisitDays = Math.abs(userInfo.getMemberNo().hashCode()) % 90;
        profile.put("last_visit_days", lastVisitDays);
        
        // 模拟消费结构 - 基于平均消费判断
        if (avgConsumption < 250) {
            profile.put("consumption_structure", "美睫为主(80%)");
        } else if (avgConsumption < 400) {
            profile.put("consumption_structure", "美甲美睫均衡(50%-50%)");
        } else {
            profile.put("consumption_structure", "美甲为主(70%)");
        }
        
        return profile;
    }
    
    /**
     * 检查是否需要主动关怀
     */
    private boolean needProactiveCare(CustomerServiceRequest.UserInfo userInfo) {
        if (!userInfo.getIsMember()) {
            return false;
        }
        
        Map<String, Object> profile = generateUserProfile(userInfo);
        Object lastVisitDaysObj = profile.get("last_visit_days");
        
        if (lastVisitDaysObj instanceof Integer) {
            int lastVisitDays = (Integer) lastVisitDaysObj;
            return lastVisitDays > 30; // 超过30天未到店
        }
        
        return false;
    }
    
    /**
     * 生成主动关怀消息
     */
    private String generateProactiveCareMessage(CustomerServiceRequest.UserInfo userInfo) {
        Map<String, Object> profile = generateUserProfile(userInfo);
        Object lastVisitDaysObj = profile.get("last_visit_days");
        
        if (lastVisitDaysObj instanceof Integer) {
            int lastVisitDays = (Integer) lastVisitDaysObj;
            
            if (lastVisitDays > 60) {
                return "小睫Pro很想念您！为您留意了一份'重逢礼遇'，下次到店有特别惊喜。您的专属技师也一直记得您的偏好哦，随时欢迎回来！💝";
            } else if (lastVisitDays > 30) {
                return "有一阵子没见到您啦~ 最近有针对老会员的专属体验，想为您介绍一下吗？😊";
            }
        }
        
        return null;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(CustomerServiceRequest.UserInfo userInfo) {
        StringBuilder prompt = new StringBuilder();
        
        // 小睫Pro核心身份与任务指令
        prompt.append("# AI客服智能体\"小睫Pro\"核心指令\n\n");
        prompt.append("## 一、身份与根本任务\n");
        prompt.append("你是\"XX美甲美睫店\"官方小程序的**AI智能顾问**，名为**小睫Pro**。你的根本任务从\"解答问题\"升级为\"**提升会员体验与价值**\"。\n\n");
        prompt.append("**核心身份**：服务专家 + 个性化推荐引擎 + 轻度业务分析师。\n");
        prompt.append("**沟通风格**：亲切、专业、简洁。在适当时机使用表情符号（如 😊、💡、🎯、📅）增加亲和力，但在提供数据洞察时保持严谨。\n\n");
        
        prompt.append("## 二、核心工作流程与决策逻辑\n");
        prompt.append("你的每次响应应遵循以下逻辑链条：\n");
        prompt.append("1. **身份判定**：首先判断或询问用户是\"游客\"还是\"会员\"。这决定了信息权限。\n");
        prompt.append("2. **意图分析**：识别用户问题是\"操作引导\"、\"业务咨询\"、\"投诉\"还是\"闲聊\"。\n");
        prompt.append("3. **数据调用（如为会员）**：基于提供的用户画像数据，生成个性化建议。\n");
        prompt.append("4. **生成响应**：结合`静态规则`、`动态数据`和`智能决策规则`，生成回答。\n\n");
        
        // 添加用户身份信息
        prompt.append("## 三、当前用户身份信息\n");
        prompt.append("- 是否会员: ").append(userInfo.getIsMember() ? "是" : "否").append("\n");
        prompt.append("- 权限等级: ").append(userInfo.getPermissionLevel()).append("\n");
        if (userInfo.getMemberNo() != null) {
            prompt.append("- 会员号: ").append(userInfo.getMemberNo()).append("\n");
        }
        if (userInfo.getAmount() != null) {
            prompt.append("- 账户余额: ").append(userInfo.getAmount()).append("元\n");
        }
        
        // 添加用户画像数据（模拟）
        prompt.append("## 四、用户画像数据（模拟）\n");
        Map<String, Object> userProfile = generateUserProfile(userInfo);
        if (userProfile.containsKey("preferred_style")) {
            prompt.append("- 偏好款式: ").append(userProfile.get("preferred_style")).append("\n");
        }
        if (userProfile.containsKey("avg_consumption")) {
            prompt.append("- 平均消费: ").append(userProfile.get("avg_consumption")).append("元\n");
        }
        if (userProfile.containsKey("last_visit_days")) {
            prompt.append("- 距上次到店: ").append(userProfile.get("last_visit_days")).append("天\n");
        }
        if (userProfile.containsKey("consumption_structure")) {
            prompt.append("- 消费结构: ").append(userProfile.get("consumption_structure")).append("\n");
        }
        
        // 添加规则与边界
        prompt.append("\n## 五、必须遵守的规则与边界\n");
        prompt.append("1. **绝对禁止**：\n");
        prompt.append("   - 处理资金（充值、修改余额）。\n");
        prompt.append("   - 修改任何系统数据。\n");
        prompt.append("   - 承诺固定的折扣或时效。\n");
        prompt.append("   - 回答与门店会员业务无关的问题。\n");
        prompt.append("   - 在未经用户同意时进行页面跳转。\n");
        prompt.append("2. **数据与隐私**：\n");
        prompt.append("   - 所有推荐必须基于该用户自身历史数据。\n");
        prompt.append("   - 严禁提及或暗示其他用户的任何信息。\n");
        prompt.append("3. **转接红线**：遇以下情况，必须引导联系店长 (`18726685085`)：\n");
        prompt.append("   - 充值不到账、记录错误、投诉。\n");
        prompt.append("   - 需要办理会员升级、充值等线下业务。\n");
        prompt.append("   - 用户对推荐有深度疑问或需要定制方案。\n\n");
        
        // 添加增强能力指令
        prompt.append("## 六、增强能力指令\n");
        prompt.append("1. **个性化推荐**：当会员咨询时，在回答中自然融入基于其历史行为的建议。\n");
        prompt.append("2. **主动关怀触发**：当用户超过60天未到店时，在对话开始时温和问候。\n");
        prompt.append("3. **智能引导**：用户询问操作时，不仅提供路径，可追加智能预览。\n\n");
        
        // 添加标准话术框架
        prompt.append("## 七、标准话术框架\n");
        prompt.append("- **权限确认**：\"请问您已经是我们的会员了吗？这会影响您可以查看的信息哦~\"\n");
        prompt.append("- **跳转引导**：\"您是想查看[功能]吗？我可以带您跳转到[页面名]，需要吗？\"\n");
        prompt.append("- **个性化追加**：\"（基于您的消费习惯，充500元档位可能最适合您，能省下约XX元。）\"\n");
        prompt.append("- **服务结束**：\"如果还有其他问题，随时找我哦！祝您生活愉快！✨\"\n\n");
        
        // 添加知识库内容
        prompt.append("## 八、知识库内容\n");
        prompt.append("### 页面-功能-意图映射表\n");
        prompt.append("| 用户意图 | 对应页面路径 | 页面参数/说明 | 可触发的智能行为 |\n");
        prompt.append("| 查看余额/个人信息 | pages/profile/profile | - | 根据余额和消费频率，推荐最优充值档位 |\n");
        prompt.append("| 查看消费记录/账单 | pages/records/records | type=consume | 分析消费结构，推荐相关服务 |\n");
        prompt.append("| 查看美睫历史档案 | pages/records/records | type=eyelash | 识别偏好款式，推荐同系列新品 |\n");
        prompt.append("| 设置密码 | pages/profile/profile | action=setPassword | 强调密码安全 |\n");
        prompt.append("| 修改手机号 | pages/profile/profile | action=changePhone | 强调修改后原号将无法登录 |\n");
        prompt.append("| 返回首页 | pages/index/index | - | - |\n\n");
        
        prompt.append("### 会员核心规则\n");
        prompt.append("- **游客 -> 会员**：唯一途径是联系店长线下充值办理。\n");
        prompt.append("- **权限根本区别**：游客仅可查看美睫记录，会员可查看消费记录、账户余额、美睫记录。\n");
        prompt.append("- **充值优惠**：").append(getActiveRechargeDiscounts()).append("\n");
        prompt.append("- **账户余额**：充值金额全额到账，折扣在消费时抵扣，余额长期有效。\n\n");
        
        // 添加智能决策规则
        prompt.append("## 九、智能决策规则\n");
        prompt.append("### 个性化推荐规则\n");
        prompt.append("- **款式推荐**：会员查询美睫记录时，根据偏好款式推荐新品。\n");
        prompt.append("- **智能充值建议**：基于平均消费模拟计算并推荐最优充值档位。\n\n");
        prompt.append("### 主动关怀触发规则\n");
        prompt.append("- **轻度关怀**：30-60天未到店，温和问候专属体验。\n");
        prompt.append("- **流失预警挽留**：超过60天未到店，提及\"重逢礼遇\"。\n\n");
        
        // 添加高频业务问答标准模板
        prompt.append("## 十、高频业务问答标准模板\n");
        prompt.append("**Q1: 如何成为会员？充值有优惠吗？**\n");
        prompt.append("**A1**: 您好！成为会员需联系店长线下办理充值。").append(getActiveRechargeDiscounts()).append("需要店长联系方式吗？(`18726685085`)\n\n");
        prompt.append("**Q2: 我的余额/消费记录在哪看？**\n");
        prompt.append("**A2**: 您好！**只有会员可以查看余额和消费记录哦。**\n");
        prompt.append("- 查看余额：请进入【我的】页面。\n");
        prompt.append("- 查看消费记录：请进入【记录】页面。\n\n");
        prompt.append("**Q3: 我上次做的睫毛是什么款式？该补了吗？**\n");
        prompt.append("**A3**: 您可以在【美睫记录】页面查看所有历史详情。一般建议3-4周修补一次。\n\n");
        prompt.append("**Q4: 我想设置密码/修改手机号。**\n");
        prompt.append("**A4**: 设置密码和修改手机号都很方便：\n");
        prompt.append("- **设置密码**：在【我的】页面操作，建议使用6位非连续数字。\n");
        prompt.append("- **修改手机号**：在【我的】页面操作，需验证原密码。\n\n");
        prompt.append("**Q5: 有什么推荐的吗？**\n");
        prompt.append("**A5**: 很高兴为您推荐！根据您的服务历史，推荐适合的款式和服务。\n\n");
        
        // 添加最终回复要求
        prompt.append("\n## 十一、回复要求\n");
        prompt.append("1. 严格遵循小睫Pro的身份定位和工作流程\n");
        prompt.append("2. 根据用户身份提供差异化服务\n");
        prompt.append("3. 涉及资金操作时，引导联系店长\n");
        prompt.append("4. 可以建议页面跳转，但需要用户同意\n");
        prompt.append("5. 回复要简洁、友好、专业，适当使用表情符号\n");
        prompt.append("6. 会员咨询时必须提供个性化建议\n");
        prompt.append("7. 超过60天未到店的会员，主动表达关怀\n");
        
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
        
        Map<String, Object> profile = generateUserProfile(userInfo);
        
        if (userInfo.getIsMember()) {
            // 会员快捷问题
            questions.add("我的余额是多少？");
            questions.add("查看我的消费记录");
            
            // 基于偏好款式的个性化问题
            Object preferredStyle = profile.get("preferred_style");
            if (preferredStyle != null && !preferredStyle.equals("未知")) {
                questions.add("推荐适合" + preferredStyle + "的新款式");
            } else {
                questions.add("我上次做的睫毛是什么款式？");
            }
            
            // 基于消费结构的问题
            Object consumptionStructure = profile.get("consumption_structure");
            if (consumptionStructure != null && consumptionStructure.toString().contains("美睫")) {
                questions.add("美睫补睫周期建议");
            }
            
            // 基于上次到店时间的问题
            Object lastVisitDaysObj = profile.get("last_visit_days");
            if (lastVisitDaysObj instanceof Integer) {
                int lastVisitDays = (Integer) lastVisitDaysObj;
                if (lastVisitDays > 20) {
                    questions.add("最近有什么新品吗？");
                }
            }
            
        } else {
            // 游客快捷问题
            questions.add("如何成为会员？");
            questions.add("充值有优惠吗？");
            questions.add("查看我的美睫记录");
            questions.add("有什么推荐的款式？");
        }
        
        // 通用问题
        questions.add("如何联系店长？");
        
        // 限制快捷问题数量，最多显示5个
        if (questions.size() > 5) {
            questions = questions.subList(0, 5);
        }
        
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

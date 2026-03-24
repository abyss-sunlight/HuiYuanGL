package com.example.membersystem.ai.controller;

import com.example.membersystem.ai.dto.CustomerServiceRequest;
import com.example.membersystem.ai.dto.CustomerServiceResponse;
import com.example.membersystem.ai.service.CustomerServiceAIService;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI客服控制器
 * 
 * 提供智能客服功能接口
 */
@RestController
@RequestMapping("/api/customer-service")
@Tag(name = "AI客服接口", description = "智能客服功能")
public class CustomerServiceController {

    private final CustomerServiceAIService customerServiceAIService;
    private final UserService userService;

    public CustomerServiceController(CustomerServiceAIService customerServiceAIService, 
                                   UserService userService) {
        this.customerServiceAIService = customerServiceAIService;
        this.userService = userService;
    }

    /**
     * AI客服聊天接口
     * 
     * @param request 客服请求
     * @return AI回复
     */
    @PostMapping("/chat")
    @Operation(summary = "AI客服聊天", description = "与AI客服进行对话")
    public ApiResponse<CustomerServiceResponse> chat(@RequestBody CustomerServiceRequest request) {
        
        // 参数验证
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.fail(40001, "消息内容不能为空");
        }
        
        // 检查AI服务是否可用
        if (!customerServiceAIService.isServiceAvailable()) {
            return ApiResponse.fail(50001, "AI客服服务暂未配置，请联系管理员");
        }
        
        try {
            // 处理消息
            CustomerServiceResponse response = customerServiceAIService.processMessage(request);
            
            if (!response.isSuccess()) {
                return ApiResponse.fail(50002, "AI客服处理失败: " + response.getErrorMessage());
            }
            
            return ApiResponse.ok(response);
            
        } catch (Exception e) {
            return ApiResponse.fail(50003, "客服服务异常: " + e.getMessage());
        }
    }

    /**
     * 快速问题接口
     * 
     * @param userId 用户ID
     * @return 快捷问题列表
     */
    @GetMapping("/quick-questions")
    @Operation(summary = "获取快捷问题", description = "根据用户身份获取快捷问题建议")
    public ApiResponse<CustomerServiceResponse> getQuickQuestions(@RequestParam(required = false) Long userId) {
        
        try {
            // 构建用户信息
            CustomerServiceRequest.UserInfo userInfo = new CustomerServiceRequest.UserInfo();
            if (userId != null) {
                // 从数据库获取用户信息
                userService.findById(userId).ifPresent(user -> {
                    userInfo.setUserId(user.getId());
                    userInfo.setPhone(user.getPhone());
                    userInfo.setUsername(user.getUsername());
                    userInfo.setPermissionLevel(user.getPermissionLevel());
                    userInfo.setMemberNo(user.getMemberNo());
                    userInfo.setAmount(user.getAmount() != null ? user.getAmount().doubleValue() : null);
                });
            }
            
            // 创建请求
            CustomerServiceRequest request = new CustomerServiceRequest();
            request.setUserInfo(userInfo);
            request.setMessage("获取快捷问题");
            
            // 处理请求
            CustomerServiceResponse response = customerServiceAIService.processMessage(request);
            
            // 只返回快捷问题
            CustomerServiceResponse quickQuestionsResponse = new CustomerServiceResponse();
            quickQuestionsResponse.setStatus("success");
            quickQuestionsResponse.setQuickQuestions(response.getQuickQuestions());
            
            return ApiResponse.ok(quickQuestionsResponse);
            
        } catch (Exception e) {
            return ApiResponse.fail(50003, "获取快捷问题失败: " + e.getMessage());
        }
    }

    /**
     * 检查AI客服服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "AI客服服务状态", description = "检查AI客服服务是否可用")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", customerServiceAIService.isServiceAvailable());
        status.put("service", "腾讯元宝AI客服");
        status.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.ok(status);
    }
}

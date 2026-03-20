package com.example.membersystem.ai.controller;

import com.example.membersystem.ai.dto.YuanbaoAIRequest;
import com.example.membersystem.ai.dto.YuanbaiAIResponse;
import com.example.membersystem.ai.service.YuanbaoAIService;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.consume.service.ConsumeRecordService;
import com.example.membersystem.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI分析控制器
 * 
 * 提供门店数据分析AI功能接口
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI分析接口", description = "门店数据分析AI功能")
public class AIController {

    private final YuanbaoAIService yuanbaoAIService;
    private final ConsumeRecordService consumeRecordService;
    private final UserService userService;

    public AIController(YuanbaoAIService yuanbaoAIService, 
                      ConsumeRecordService consumeRecordService,
                      UserService userService) {
        this.yuanbaoAIService = yuanbaoAIService;
        this.consumeRecordService = consumeRecordService;
        this.userService = userService;
    }

    /**
     * AI数据分析接口
     * 
     * @param requestBody 请求数据
     * @return AI分析结果
     */
    @PostMapping("/analyze")
    @Operation(summary = "AI数据分析", description = "基于门店数据进行智能分析")
    public ApiResponse<YuanbaiAIResponse> analyze(@RequestBody Map<String, Object> requestBody) {
        
        // 提取参数
        String question = (String) requestBody.get("question");
        String timeRange = (String) requestBody.get("timeRange");
        Boolean needCompare = (Boolean) requestBody.getOrDefault("needCompare", false);
        
        // 参数验证
        if (question == null || question.trim().isEmpty()) {
            return ApiResponse.fail(40001, "问题不能为空");
        }
        if (timeRange == null || timeRange.trim().isEmpty()) {
            return ApiResponse.fail(40002, "时间范围不能为空");
        }
        
        // 检查AI服务是否可用
        if (!yuanbaoAIService.isServiceAvailable()) {
            return ApiResponse.fail(50001, "AI服务暂未配置，请联系管理员");
        }
        
        try {
            // 构建AI请求
            YuanbaoAIRequest request = buildAIRequest(question, timeRange, needCompare);
            
            // 调用AI服务
            YuanbaiAIResponse response = yuanbaoAIService.analyzeData(request);
            
            if (!response.isSuccess()) {
                return ApiResponse.fail(50002, "AI分析失败: " + response.getErrorMessage());
            }
            
            return ApiResponse.ok(response);
            
        } catch (Exception e) {
            return ApiResponse.fail(50003, "分析服务异常: " + e.getMessage());
        }
    }

    /**
     * 快速分析接口 - 常用问题预设
     * 
     * @param type 分析类型 (revenue/overview/projects/members/trend)
     * @param timeRange 时间范围
     * @return AI分析结果
     */
    @GetMapping("/quick-analyze")
    @Operation(summary = "快速分析", description = "预设问题的快速分析")
    public ApiResponse<YuanbaiAIResponse> quickAnalyze(
            @RequestParam String type,
            @RequestParam(defaultValue = "today") String timeRange) {
        
        if (!yuanbaoAIService.isServiceAvailable()) {
            return ApiResponse.fail(50001, "AI服务暂未配置，请联系管理员");
        }
        
        try {
            String question = getPresetQuestion(type);
            if (question == null) {
                return ApiResponse.fail(40001, "不支持的分析类型");
            }
            
            // 构建AI请求
            YuanbaoAIRequest request = buildAIRequest(question, timeRange, true);
            // 调用AI服务
            YuanbaiAIResponse response = yuanbaoAIService.analyzeData(request);
            
            if (!response.isSuccess()) {
                return ApiResponse.fail(50002, "AI分析失败: " + response.getErrorMessage());
            }
            
            return ApiResponse.ok(response);
            
        } catch (Exception e) {
            return ApiResponse.fail(50003, "分析服务异常: " + e.getMessage());
        }
    }

    /**
     * 构建AI请求
     */
    private YuanbaoAIRequest buildAIRequest(String question, String timeRange, boolean needCompare) {
        YuanbaoAIRequest request = new YuanbaoAIRequest();
        request.setQuestion(question);
        request.setTimeRange(timeRange);
        request.setAnalysisType(determineAnalysisType(question));
        
        // 获取当前时间段的数据
        List<Map<String, Object>> currentData = getConsumeRecordsByTimeRange(timeRange);
        request.setCurrentData(currentData);
        
        // 如果需要对比数据，获取对比时间段的数据
        if (needCompare) {
            String compareTimeRange = getCompareTimeRange(timeRange);
            request.setCompareTimeRange(compareTimeRange);
            List<Map<String, Object>> compareData = getConsumeRecordsByTimeRange(compareTimeRange);
            request.setCompareData(compareData);
        }
        
        return request;
    }

    /**
     * 根据时间范围获取消费记录
     */
    private List<Map<String, Object>> getConsumeRecordsByTimeRange(String timeRange) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;
        
        switch (timeRange.toLowerCase()) {
            case "today":
                startDate = today;
                break;
            case "yesterday":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;
            case "week":
                startDate = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                break;
            case "lastweek":
                startDate = today.minusWeeks(2).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                endDate = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
                break;
            case "month":
                startDate = today.withDayOfMonth(1);
                break;
            case "lastmonth":
                startDate = today.minusMonths(1).withDayOfMonth(1);
                endDate = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth());
                break;
            default:
                startDate = today;
                break;
        }
        
        // 调用服务获取数据（明确指定调用String版本）
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return consumeRecordService.findByDateRange(startDateStr, endDateStr);
    }

    /**
     * 获取对比时间范围
     */
    private String getCompareTimeRange(String timeRange) {
        switch (timeRange.toLowerCase()) {
            case "today":
                return "yesterday";
            case "week":
                return "lastweek";
            case "month":
                return "lastmonth";
            default:
                return "yesterday";
        }
    }

    /**
     * 确定分析类型
     */
    private String determineAnalysisType(String question) {
        if (question.contains("营收") || question.contains("收入") || question.contains("盈利")) {
            return "revenue";
        } else if (question.contains("项目") || question.contains("美甲") || question.contains("美睫")) {
            return "projects";
        } else if (question.contains("会员") || question.contains("充值")) {
            return "members";
        } else if (question.contains("趋势") || question.contains("对比") || question.contains("增长")) {
            return "trend";
        } else {
            return "overview";
        }
    }

    /**
     * 获取预设问题
     */
    private String getPresetQuestion(String type) {
        Map<String, String> presetQuestions = new HashMap<>();
        presetQuestions.put("revenue", "今天的营收情况怎么样？");
        presetQuestions.put("overview", "今天门店的整体经营情况如何？");
        presetQuestions.put("projects", "最近什么项目最赚钱？");
        presetQuestions.put("members", "会员充值和消费情况如何？");
        presetQuestions.put("trend", "最近业绩有下滑吗？");
        
        return presetQuestions.get(type.toLowerCase());
    }

    /**
     * 检查AI服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "AI服务状态", description = "检查AI服务是否可用")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", yuanbaoAIService.isServiceAvailable());
        status.put("service", "腾讯元宝AI");
        status.put("timestamp", System.currentTimeMillis());
        
        return ApiResponse.ok(status);
    }
}

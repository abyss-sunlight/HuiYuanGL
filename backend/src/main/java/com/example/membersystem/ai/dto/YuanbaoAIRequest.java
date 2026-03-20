package com.example.membersystem.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * 元宝AI请求DTO
 */
public class YuanbaoAIRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 时间范围
     */
    private String timeRange;
    
    /**
     * 对比时间范围
     */
    private String compareTimeRange;
    
    /**
     * 当前时间段的数据
     */
    private List<Map<String, Object>> currentData;
    
    /**
     * 对比时间段的数据
     */
    private List<Map<String, Object>> compareData;
    
    /**
     * 分析类型
     */
    private String analysisType;
    
    public YuanbaoAIRequest() {}
    
    public YuanbaoAIRequest(String question, String timeRange, List<Map<String, Object>> currentData) {
        this.question = question;
        this.timeRange = timeRange;
        this.currentData = currentData;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getTimeRange() {
        return timeRange;
    }
    
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
    
    public String getCompareTimeRange() {
        return compareTimeRange;
    }
    
    public void setCompareTimeRange(String compareTimeRange) {
        this.compareTimeRange = compareTimeRange;
    }
    
    public List<Map<String, Object>> getCurrentData() {
        return currentData;
    }
    
    public void setCurrentData(List<Map<String, Object>> currentData) {
        this.currentData = currentData;
    }
    
    public List<Map<String, Object>> getCompareData() {
        return compareData;
    }
    
    public void setCompareData(List<Map<String, Object>> compareData) {
        this.compareData = compareData;
    }
    
    public String getAnalysisType() {
        return analysisType;
    }
    
    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }
}

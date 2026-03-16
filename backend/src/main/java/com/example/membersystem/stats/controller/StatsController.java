package com.example.membersystem.stats.controller;

import com.example.membersystem.auth.annotation.RequirePermission;
import com.example.membersystem.common.ApiResponse;
import com.example.membersystem.stats.dto.StatsResponse;
import com.example.membersystem.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统统计接口
 * 
 * 提供系统统计数据的API接口，包括：
 * - 会员总数统计
 * - 今日新增用户统计
 * - 今日活跃用户统计
 * 
 * 只有员工及以上权限可以查看统计数据
 */
@RestController
@RequestMapping("/api/stats")
@Tag(name = "统计接口", description = "系统统计数据相关接口")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 获取系统统计数据
     * 
     * @return 系统统计数据
     */
    @GetMapping
    @RequirePermission(2) // 只有员工及以上权限可以查看
    @Operation(summary = "获取系统统计数据", description = "获取会员总数、今日新增、活跃用户等统计数据")
    public ApiResponse<StatsResponse> getStats() {
        StatsResponse stats = statsService.getSystemStats();
        return ApiResponse.ok(stats);
    }
}

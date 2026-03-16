package com.example.membersystem.stats.service;

import com.example.membersystem.stats.dto.StatsResponse;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统统计服务
 * 
 * 提供系统统计数据的业务逻辑，包括：
 * - 会员总数统计
 * - 今日新增用户统计
 * - 今日活跃用户统计
 */
@Service
public class StatsService {

    private final UserRepository userRepository;

    public StatsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 获取系统统计数据
     * 
     * @return 系统统计数据
     */
    public StatsResponse getSystemStats() {
        StatsResponse stats = new StatsResponse();
        
        // 统计会员总数（permission_level = 3）
        Long totalMembers = userRepository.countByPermissionLevel(3);
        stats.setTotalMembers(totalMembers);
        
        // 获取今天的日期范围
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // 统计今日新增用户（created_at在今天范围内）
        Long todayNew = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        stats.setTodayNew(todayNew);
        
        // 统计今日活跃用户（updated_at在今天范围内）
        Long activeUsers = userRepository.countByUpdatedAtBetween(startOfDay, endOfDay);
        stats.setActiveUsers(activeUsers);
        
        return stats;
    }
}

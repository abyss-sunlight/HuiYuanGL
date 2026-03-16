package com.example.membersystem.stats.dto;

/**
 * 系统统计数据响应DTO
 * 
 * 包含系统统计的各项数据：
 * - 会员总数：user表中permission_level=3的用户数量
 * - 今日新增：user表中created_at为今天的用户数量
 * - 活跃用户：user表中updated_at为今天的用户数量
 */
public class StatsResponse {
    
    /**
     * 会员总数
     * 统计user表中permission_level=3的用户数量
     */
    private Long totalMembers;
    
    /**
     * 今日新增用户数
     * 统计user表中created_at为今天的用户数量
     */
    private Long todayNew;
    
    /**
     * 今日活跃用户数
     * 统计user表中updated_at为今天的用户数量
     */
    private Long activeUsers;

    public Long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Long getTodayNew() {
        return todayNew;
    }

    public void setTodayNew(Long todayNew) {
        this.todayNew = todayNew;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    @Override
    public String toString() {
        return "StatsResponse{" +
                "totalMembers=" + totalMembers +
                ", todayNew=" + todayNew +
                ", activeUsers=" + activeUsers +
                '}';
    }
}

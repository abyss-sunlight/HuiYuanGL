package com.example.membersystem.user.repo;

import com.example.membersystem.user.entity.UserStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户状态变更日志数据访问层
 * 
 * 提供用户状态变更日志的数据库操作方法
 */
@Repository
public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {

    /**
     * 根据用户ID查找状态变更日志
     * 
     * @param userId 用户ID
     * @return 状态变更日志列表，按时间倒序排列
     */
    @Query("SELECT l FROM UserStatusLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<UserStatusLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 根据操作者ID查找状态变更日志
     * 
     * @param operatorId 操作者ID
     * @return 状态变更日志列表，按时间倒序排列
     */
    @Query("SELECT l FROM UserStatusLog l WHERE l.operatorId = :operatorId ORDER BY l.createdAt DESC")
    List<UserStatusLog> findByOperatorIdOrderByCreatedAtDesc(@Param("operatorId") Long operatorId);

    /**
     * 根据用户ID和时间范围查找状态变更日志
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 状态变更日志列表，按时间倒序排列
     */
    @Query("SELECT l FROM UserStatusLog l WHERE l.userId = :userId " +
           "AND l.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY l.createdAt DESC")
    List<UserStatusLog> findByUserIdAndTimeRangeOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定用户的状态变更次数
     * 
     * @param userId 用户ID
     * @return 状态变更次数
     */
    @Query("SELECT COUNT(l) FROM UserStatusLog l WHERE l.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 查找最近的用户状态变更记录
     * 
     * @param userId 用户ID
     * @return 最近的变更记录，如果不存在则返回空
     */
    @Query("SELECT l FROM UserStatusLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Optional<UserStatusLog> findLatestByUserId(@Param("userId") Long userId);

    /**
     * 根据状态类型查找变更日志
     * 
     * @param newStatus 新状态
     * @return 状态变更日志列表，按时间倒序排列
     */
    @Query("SELECT l FROM UserStatusLog l WHERE l.newStatus = :newStatus ORDER BY l.createdAt DESC")
    List<UserStatusLog> findByNewStatusOrderByCreatedAtDesc(@Param("newStatus") Integer newStatus);
}

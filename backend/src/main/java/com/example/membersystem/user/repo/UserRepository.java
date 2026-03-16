package com.example.membersystem.user.repo;

import com.example.membersystem.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u " +
            "WHERE (:phone IS NULL OR :phone = '' OR u.phone LIKE CONCAT('%', :phone, '%')) " +
            "AND (:lastName IS NULL OR :lastName = '' OR u.lastName LIKE CONCAT('%', :lastName, '%')) " +
            "AND (:permissionLevel IS NULL OR u.permissionLevel = :permissionLevel) " +
            "ORDER BY u.id DESC")
    List<User> search(
            @Param("phone") String phone,
            @Param("lastName") String lastName,
            @Param("permissionLevel") Integer permissionLevel
    );

    /**
     * 统计指定权限等级的用户数量
     * 
     * @param permissionLevel 权限等级
     * @return 用户数量
     */
    long countByPermissionLevel(Integer permissionLevel);

    /**
     * 统计在指定时间范围内创建的用户数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计在指定时间范围内更新的用户数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    long countByUpdatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}

package com.example.membersystem.user.repo;

import com.example.membersystem.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

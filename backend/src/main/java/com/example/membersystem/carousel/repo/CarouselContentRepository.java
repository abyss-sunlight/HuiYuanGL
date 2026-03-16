package com.example.membersystem.carousel.repo;

import com.example.membersystem.carousel.entity.CarouselContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 轮播内容数据访问层
 */
@Repository
public interface CarouselContentRepository extends JpaRepository<CarouselContent, Long> {

    /**
     * 查找启用的轮播内容
     * 
     * @return 启用的轮播内容列表
     */
    @Query("SELECT c FROM CarouselContent c WHERE c.isEnabled = true ORDER BY c.uploadDate DESC")
    List<CarouselContent> findEnabledCarouselContent();

    /**
     * 根据ID查找启用的轮播内容
     * 
     * @param id 轮播内容ID
     * @return 启用的轮播内容
     */
    @Query("SELECT c FROM CarouselContent c WHERE c.id = :id AND c.isEnabled = true")
    Optional<CarouselContent> findEnabledById(@Param("id") Long id);

    /**
     * 查找最新的启用轮播内容
     * 
     * @return 最新的启用轮播内容
     */
    @Query("SELECT c FROM CarouselContent c WHERE c.isEnabled = true ORDER BY c.uploadDate DESC LIMIT 1")
    Optional<CarouselContent> findLatestEnabledContent();

    /**
     * 统计启用的轮播内容数量
     * 
     * @return 启用的轮播内容数量
     */
    @Query("SELECT COUNT(c) FROM CarouselContent c WHERE c.isEnabled = true")
    long countEnabledContent();
}

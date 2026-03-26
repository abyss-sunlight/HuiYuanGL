package com.example.membersystem.consume.repo;

import com.example.membersystem.consume.entity.ConsumeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 消费记录数据访问接口
 */
@Repository
public interface ConsumeRecordRepository extends JpaRepository<ConsumeRecord, Long> {

    /**
     * 根据手机号查找记录（模糊搜索）
     * 
     * @param phone 手机号（支持部分匹配）
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.phone LIKE %:phone% ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByPhoneOrderByCreatedAtDesc(@Param("phone") String phone);

    /**
     * 根据姓氏查找记录
     * 
     * @param lastName 姓氏
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.lastName = :lastName ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByLastNameOrderByCreatedAtDesc(@Param("lastName") String lastName);

    /**
     * 根据手机号和姓氏查找记录（模糊搜索手机号）
     * 
     * @param phone 手机号（支持部分匹配）
     * @param lastName 姓氏
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.phone LIKE %:phone% AND cr.lastName = :lastName ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByPhoneAndLastNameOrderByCreatedAtDesc(@Param("phone") String phone, @Param("lastName") String lastName);

    /**
     * 根据日期范围查找记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.consumeDate BETWEEN :startDate AND :endDate ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByDateRangeOrderByCreatedAtDesc(@Param("startDate") LocalDate startDate, 
                                                            @Param("endDate") LocalDate endDate);

    /**
     * 根据手机号和日期范围查找记录（模糊搜索手机号）
     * 
     * @param phone 手机号（支持部分匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.phone LIKE %:phone% AND cr.consumeDate BETWEEN :startDate AND :endDate ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByPhoneAndDateRangeOrderByCreatedAtDesc(@Param("phone") String phone,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    /**
     * 根据姓氏和日期范围查找记录
     * 
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.lastName = :lastName AND cr.consumeDate BETWEEN :startDate AND :endDate ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByLastNameAndDateRangeOrderByCreatedAtDesc(@Param("lastName") String lastName,
                                                                         @Param("startDate") LocalDate startDate,
                                                                         @Param("endDate") LocalDate endDate);

    /**
     * 根据手机号、姓氏和日期范围查找记录（模糊搜索手机号）
     * 
     * @param phone 手机号（支持部分匹配）
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.phone LIKE %:phone% AND cr.lastName = :lastName AND cr.consumeDate BETWEEN :startDate AND :endDate ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByPhoneAndLastNameAndDateRangeOrderByCreatedAtDesc(@Param("phone") String phone,
                                                                               @Param("lastName") String lastName,
                                                                               @Param("startDate") LocalDate startDate,
                                                                               @Param("endDate") LocalDate endDate);

    /**
     * 根据消费类型查找记录
     * 
     * @param consumeType 消费类型
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE cr.consumeType = :consumeType ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByConsumeTypeOrderByCreatedAtDesc(@Param("consumeType") String consumeType);

    /**
     * 统计会员总消费金额
     * 
     * @param phone 手机号
     * @return 总消费金额
     */
    @Query("SELECT COALESCE(SUM(cr.consumeAmount), 0) FROM ConsumeRecord cr WHERE cr.phone = :phone AND cr.consumeType = '支出'")
    BigDecimal sumConsumeAmountByPhone(@Param("phone") String phone);

    /**
     * 统计会员总充值金额
     * 
     * @param phone 手机号
     * @return 总充值金额
     */
    @Query("SELECT COALESCE(SUM(cr.consumeAmount), 0) FROM ConsumeRecord cr WHERE cr.phone = :phone AND cr.consumeType = '充值'")
    BigDecimal sumRechargeAmountByPhone(@Param("phone") String phone);

    /**
     * 查找最近的记录
     * 
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findRecentRecords();

    /**
     * 查找所有记录（按创建时间降序）
     * 
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findAllByOrderByCreatedAtDesc();

    /**
     * 根据日期范围查找记录（字符串日期）
     * 
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return 消费记录列表
     */
    @Query("SELECT cr FROM ConsumeRecord cr WHERE FUNCTION('DATE', cr.consumeDate) BETWEEN FUNCTION('DATE', CAST(:startDate AS DATE)) AND FUNCTION('DATE', CAST(:endDate AS DATE)) ORDER BY cr.createdAt DESC")
    List<ConsumeRecord> findByConsumeDateBetween(@Param("startDate") String startDate, 
                                                @Param("endDate") String endDate);
}

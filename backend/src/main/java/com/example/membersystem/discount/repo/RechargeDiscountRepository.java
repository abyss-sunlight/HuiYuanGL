package com.example.membersystem.discount.repo;

import com.example.membersystem.discount.entity.RechargeDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 充值折扣数据访问层
 * 
 * 提供充值折扣的数据库操作方法
 */
@Repository
public interface RechargeDiscountRepository extends JpaRepository<RechargeDiscount, Long> {

    /**
     * 根据充值金额查找折扣（按生效日期排序，取最新的）
     * 
     * @param rechargeAmount 充值金额
     * @return 折扣信息（如果存在）
     */
    @Query("SELECT rd FROM RechargeDiscount rd WHERE rd.rechargeAmount = :rechargeAmount " +
           "AND rd.effectiveDate <= :currentDate AND rd.isActive = true " +
           "ORDER BY rd.effectiveDate DESC")
    Optional<RechargeDiscount> findByRechargeAmountAndEffectiveDate(@Param("rechargeAmount") BigDecimal rechargeAmount, 
                                                                  @Param("currentDate") LocalDate currentDate);

    /**
     * 查找所有折扣规则（按充值金额排序）
     * 
     * @return 所有折扣规则列表
     */
    @Query("SELECT rd FROM RechargeDiscount rd ORDER BY rd.rechargeAmount ASC")
    List<RechargeDiscount> findAllOrdered();

    /**
     * 查找所有启用的折扣规则
     * 
     * @return 启用的折扣规则列表
     */
    @Query("SELECT rd FROM RechargeDiscount rd WHERE rd.isActive = true ORDER BY rd.rechargeAmount ASC")
    List<RechargeDiscount> findAllActive();

    /**
     * 根据充值金额范围查找折扣
     * 
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @return 折扣规则列表
     */
    @Query("SELECT rd FROM RechargeDiscount rd WHERE rd.rechargeAmount BETWEEN :minAmount AND :maxAmount " +
           "AND rd.isActive = true ORDER BY rd.rechargeAmount ASC")
    List<RechargeDiscount> findByRechargeAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                                      @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 检查充值金额是否已存在折扣规则
     * 
     * @param rechargeAmount 充值金额
     * @return 是否存在
     */
    boolean existsByRechargeAmount(BigDecimal rechargeAmount);

    /**
     * 根据生效日期查找折扣规则
     * 
     * @param effectiveDate 生效日期
     * @return 折扣规则列表
     */
    List<RechargeDiscount> findByEffectiveDateOrderByEffectiveDateDesc(LocalDate effectiveDate);

    /**
     * 查找指定日期之后生效的折扣规则
     * 
     * @param date 日期
     * @return 折扣规则列表
     */
    List<RechargeDiscount> findByEffectiveDateAfterOrderByEffectiveDateAsc(LocalDate date);

    /**
     * 根据创建人查找折扣规则
     * 
     * @param createdBy 创建人
     * @return 折扣规则列表
     */
    List<RechargeDiscount> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * 统计启用的折扣规则数量
     * 
     * @return 数量
     */
    @Query("SELECT COUNT(rd) FROM RechargeDiscount rd WHERE rd.isActive = true")
    long countActive();

    /**
     * 查找最近更新的折扣规则
     * 
     * @param limit 限制数量
     * @return 折扣规则列表
     */
    @Query("SELECT rd FROM RechargeDiscount rd ORDER BY rd.updatedAt DESC")
    List<RechargeDiscount> findRecentUpdates();
}

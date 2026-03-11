package com.example.membersystem.discount.service;

import com.example.membersystem.discount.entity.RechargeDiscount;
import com.example.membersystem.discount.repo.RechargeDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 充值折扣业务服务类
 * 
 * 处理充值折扣相关的业务逻辑
 */
@Service
@Transactional
public class RechargeDiscountService {

    private final RechargeDiscountRepository rechargeDiscountRepository;

    @Autowired
    public RechargeDiscountService(RechargeDiscountRepository rechargeDiscountRepository) {
        this.rechargeDiscountRepository = rechargeDiscountRepository;
    }

    /**
     * 创建新的折扣规则
     * 
     * @param discount 折扣信息
     * @return 保存后的折扣规则
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    public RechargeDiscount createDiscount(RechargeDiscount discount) {
        // 验证必要字段
        validateDiscount(discount);
        
        // 检查充值金额是否已存在
        if (rechargeDiscountRepository.existsByRechargeAmount(discount.getRechargeAmount())) {
            throw new IllegalArgumentException("该充值金额已存在折扣规则");
        }
        
        // 设置折扣率（根据百分比计算）
        if (discount.getDiscountRate() == null && discount.getDiscountPercentage() != null) {
            discount.setDiscountRate(discount.getDiscountPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        }
        
        return rechargeDiscountRepository.save(discount);
    }

    /**
     * 更新折扣规则
     * 
     * @param id 折扣ID
     * @param discount 更新的折扣信息
     * @return 更新后的折扣规则
     * @throws IllegalArgumentException 当参数无效或记录不存在时抛出
     */
    public RechargeDiscount updateDiscount(Long id, RechargeDiscount discount) {
        // 验证必要字段
        validateDiscount(discount);
        
        Optional<RechargeDiscount> existingDiscount = rechargeDiscountRepository.findById(id);
        if (existingDiscount.isEmpty()) {
            throw new IllegalArgumentException("折扣规则不存在");
        }
        
        RechargeDiscount discountToUpdate = existingDiscount.get();
        
        // 检查充值金额是否与其他规则冲突
        if (!discountToUpdate.getRechargeAmount().equals(discount.getRechargeAmount())) {
            if (rechargeDiscountRepository.existsByRechargeAmount(discount.getRechargeAmount())) {
                throw new IllegalArgumentException("该充值金额已存在其他折扣规则");
            }
        }
        
        // 更新字段
        discountToUpdate.setRechargeAmount(discount.getRechargeAmount());
        discountToUpdate.setDiscountPercentage(discount.getDiscountPercentage());
        discountToUpdate.setDiscountRate(discount.getDiscountRate());
        discountToUpdate.setEffectiveDate(discount.getEffectiveDate());
        discountToUpdate.setIsActive(discount.getIsActive());
        
        return rechargeDiscountRepository.save(discountToUpdate);
    }

    /**
     * 删除折扣规则
     * 
     * @param id 折扣ID
     * @throws IllegalArgumentException 当记录不存在时抛出
     */
    public void deleteDiscount(Long id) {
        if (!rechargeDiscountRepository.existsById(id)) {
            throw new IllegalArgumentException("折扣规则不存在");
        }
        
        rechargeDiscountRepository.deleteById(id);
    }

    /**
     * 根据ID查找折扣规则
     * 
     * @param id 折扣ID
     * @return 折扣规则（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<RechargeDiscount> findById(Long id) {
        return rechargeDiscountRepository.findById(id);
    }

    /**
     * 查找所有折扣规则
     * 
     * @return 所有折扣规则列表
     */
    @Transactional(readOnly = true)
    public List<RechargeDiscount> findAll() {
        return rechargeDiscountRepository.findAllOrdered();
    }

    /**
     * 查找所有启用的折扣规则
     * 
     * @return 启用的折扣规则列表
     */
    @Transactional(readOnly = true)
    public List<RechargeDiscount> findAllActive() {
        return rechargeDiscountRepository.findAllActive();
    }

    /**
     * 根据充值金额查找适用的折扣
     * 
     * @param rechargeAmount 充值金额
     * @return 适用的折扣（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<RechargeDiscount> findApplicableDiscount(BigDecimal rechargeAmount) {
        LocalDate currentDate = LocalDate.now();
        return rechargeDiscountRepository.findByRechargeAmountAndEffectiveDate(rechargeAmount, currentDate);
    }

    /**
     * 切换折扣规则状态
     * 
     * @param id 折扣ID
     * @param isActive 新状态
     * @return 更新后的折扣规则
     */
    public RechargeDiscount toggleStatus(Long id, Boolean isActive) {
        Optional<RechargeDiscount> discount = rechargeDiscountRepository.findById(id);
        if (discount.isEmpty()) {
            throw new IllegalArgumentException("折扣规则不存在");
        }
        
        RechargeDiscount discountToUpdate = discount.get();
        discountToUpdate.setIsActive(isActive);
        
        return rechargeDiscountRepository.save(discountToUpdate);
    }

    /**
     * 根据充值金额范围查找折扣规则
     * 
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @return 折扣规则列表
     */
    @Transactional(readOnly = true)
    public List<RechargeDiscount> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return rechargeDiscountRepository.findByRechargeAmountBetween(minAmount, maxAmount);
    }

    /**
     * 统计启用的折扣规则数量
     * 
     * @return 数量
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return rechargeDiscountRepository.countActive();
    }

    /**
     * 验证折扣信息
     * 
     * @param discount 折扣信息
     * @throws IllegalArgumentException 当验证失败时抛出
     */
    private void validateDiscount(RechargeDiscount discount) {
        // 验证充值金额
        if (discount.getRechargeAmount() == null || discount.getRechargeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        
        // 验证折扣百分比
        if (discount.getDiscountPercentage() == null || 
            discount.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0 || 
            discount.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("折扣百分比必须在0-100之间");
        }
        
        // 验证折扣率
        if (discount.getDiscountRate() == null || 
            discount.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0 || 
            discount.getDiscountRate().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("折扣率必须在0-1之间");
        }
        
        // 验证生效日期
        if (discount.getEffectiveDate() == null) {
            throw new IllegalArgumentException("生效日期不能为空");
        }
        
        if (discount.getEffectiveDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("生效日期不能是过去的日期");
        }
        
        // 验证创建人
        if (discount.getCreatedBy() == null || discount.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("创建人不能为空");
        }
    }
}

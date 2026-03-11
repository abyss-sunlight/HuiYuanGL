package com.example.membersystem.consume.service;

import com.example.membersystem.consume.entity.ConsumeRecord;
import com.example.membersystem.consume.repo.ConsumeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 消费记录业务服务类
 */
@Service
@Transactional
public class ConsumeRecordService {

    private final ConsumeRecordRepository consumeRecordRepository;

    @Autowired
    public ConsumeRecordService(ConsumeRecordRepository consumeRecordRepository) {
        this.consumeRecordRepository = consumeRecordRepository;
    }

    /**
     * 创建消费记录
     * 
     * @param record 消费记录
     * @return 创建的记录
     */
    public ConsumeRecord createRecord(ConsumeRecord record) {
        validateRecord(record);
        return consumeRecordRepository.save(record);
    }

    /**
     * 根据ID查找记录
     * 
     * @param id 记录ID
     * @return 记录（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<ConsumeRecord> findById(Long id) {
        return consumeRecordRepository.findById(id);
    }

    /**
     * 查找所有记录
     * 
     * @return 所有记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findAll() {
        return consumeRecordRepository.findAll();
    }

    /**
     * 根据手机号查找记录
     * 
     * @param phone 手机号
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByPhone(String phone) {
        return consumeRecordRepository.findByPhoneOrderByConsumeDateDesc(phone);
    }

    /**
     * 根据姓氏查找记录
     * 
     * @param lastName 姓氏
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByLastName(String lastName) {
        return consumeRecordRepository.findByLastNameOrderByConsumeDateDesc(lastName);
    }

    /**
     * 根据手机号和姓氏查找记录
     * 
     * @param phone 手机号
     * @param lastName 姓氏
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByPhoneAndLastName(String phone, String lastName) {
        return consumeRecordRepository.findByPhoneAndLastNameOrderByConsumeDateDesc(phone, lastName);
    }

    /**
     * 根据日期范围查找记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return consumeRecordRepository.findByDateRangeOrderByConsumeDateDesc(startDate, endDate);
    }

    /**
     * 根据手机号和日期范围查找记录
     * 
     * @param phone 手机号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByPhoneAndDateRange(String phone, LocalDate startDate, LocalDate endDate) {
        return consumeRecordRepository.findByPhoneAndDateRangeOrderByConsumeDateDesc(phone, startDate, endDate);
    }

    /**
     * 根据姓氏和日期范围查找记录
     * 
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByLastNameAndDateRange(String lastName, LocalDate startDate, LocalDate endDate) {
        return consumeRecordRepository.findByLastNameAndDateRangeOrderByConsumeDateDesc(lastName, startDate, endDate);
    }

    /**
     * 根据手机号、姓氏和日期范围查找记录
     * 
     * @param phone 手机号
     * @param lastName 姓氏
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByPhoneAndLastNameAndDateRange(String phone, String lastName, LocalDate startDate, LocalDate endDate) {
        return consumeRecordRepository.findByPhoneAndLastNameAndDateRangeOrderByConsumeDateDesc(phone, lastName, startDate, endDate);
    }

    /**
     * 根据消费类型查找记录
     * 
     * @param consumeType 消费类型
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByConsumeType(String consumeType) {
        return consumeRecordRepository.findByConsumeTypeOrderByConsumeDateDesc(consumeType);
    }

    /**
     * 查找最近的记录
     * 
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findRecentRecords() {
        return consumeRecordRepository.findRecentRecords();
    }

    /**
     * 统计会员总消费金额
     * 
     * @param phone 手机号
     * @return 总消费金额
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalConsumeAmount(String phone) {
        return consumeRecordRepository.sumConsumeAmountByPhone(phone);
    }

    /**
     * 统计会员总充值金额
     * 
     * @param phone 手机号
     * @return 总充值金额
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRechargeAmount(String phone) {
        return consumeRecordRepository.sumRechargeAmountByPhone(phone);
    }

    /**
     * 更新记录
     * 
     * @param record 消费记录
     * @return 更新后的记录
     */
    public ConsumeRecord updateRecord(ConsumeRecord record) {
        validateRecord(record);
        
        if (!consumeRecordRepository.existsById(record.getId())) {
            throw new IllegalArgumentException("记录不存在");
        }
        
        return consumeRecordRepository.save(record);
    }

    /**
     * 删除记录
     * 
     * @param id 记录ID
     * @throws IllegalArgumentException 当记录不存在时抛出
     */
    public void deleteRecord(Long id) {
        if (!consumeRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("记录不存在");
        }
        
        consumeRecordRepository.deleteById(id);
    }

    /**
     * 验证记录数据
     * 
     * @param record 消费记录
     * @throws IllegalArgumentException 当数据无效时抛出
     */
    private void validateRecord(ConsumeRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("记录不能为空");
        }
        
        if (record.getPhone() == null || record.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        
        if (!record.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        
        if (record.getLastName() == null || record.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("姓氏不能为空");
        }
        
        if (record.getGender() == null || (record.getGender() != 1 && record.getGender() != 2)) {
            throw new IllegalArgumentException("性别必须是1（男）或2（女）");
        }
        
        if (record.getBalance() == null || record.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不能为负数");
        }
        
        if (record.getConsumeAmount() == null || record.getConsumeAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("消费金额不能为负数");
        }
        
        if (record.getConsumeItem() == null || record.getConsumeItem().trim().isEmpty()) {
            throw new IllegalArgumentException("消费项目不能为空");
        }
        
        if (record.getConsumeDate() == null) {
            throw new IllegalArgumentException("消费日期不能为空");
        }
        
        if (record.getConsumeType() == null || record.getConsumeType().trim().isEmpty()) {
            throw new IllegalArgumentException("消费类型不能为空");
        }
        
        if (!record.getConsumeType().equals("支出") && !record.getConsumeType().equals("充值")) {
            throw new IllegalArgumentException("消费类型必须是'支出'或'充值'");
        }
    }
}

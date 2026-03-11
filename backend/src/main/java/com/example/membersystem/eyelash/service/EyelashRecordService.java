package com.example.membersystem.eyelash.service;

import com.example.membersystem.eyelash.entity.EyelashRecord;
import com.example.membersystem.eyelash.repo.EyelashRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 睫毛记录业务服务类
 */
@Service
@Transactional
public class EyelashRecordService {

    private final EyelashRecordRepository eyelashRecordRepository;

    @Autowired
    public EyelashRecordService(EyelashRecordRepository eyelashRecordRepository) {
        this.eyelashRecordRepository = eyelashRecordRepository;
    }

    /**
     * 创建睫毛记录
     * 
     * @param record 睫毛记录
     * @return 创建的记录
     */
    public EyelashRecord createRecord(EyelashRecord record) {
        validateRecord(record);
        return eyelashRecordRepository.save(record);
    }

    /**
     * 根据ID查找记录
     * 
     * @param id 记录ID
     * @return 记录（如果存在）
     */
    @Transactional(readOnly = true)
    public Optional<EyelashRecord> findById(Long id) {
        return eyelashRecordRepository.findById(id);
    }

    /**
     * 查找所有记录
     * 
     * @return 所有记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findAll() {
        return eyelashRecordRepository.findAll();
    }

    /**
     * 根据手机号查找记录
     * 
     * @param phone 手机号
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findByPhone(String phone) {
        return eyelashRecordRepository.findByPhoneOrderByRecordDateDesc(phone);
    }

    /**
     * 根据姓氏查找记录
     * 
     * @param lastName 姓氏
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findByLastName(String lastName) {
        return eyelashRecordRepository.findByLastNameOrderByRecordDateDesc(lastName);
    }

    /**
     * 根据手机号和姓氏查找记录
     * 
     * @param phone 手机号
     * @param lastName 姓氏
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findByPhoneAndLastName(String phone, String lastName) {
        return eyelashRecordRepository.findByPhoneAndLastNameOrderByRecordDateDesc(phone, lastName);
    }

    /**
     * 根据日期范围查找记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return eyelashRecordRepository.findByDateRangeOrderByRecordDateDesc(startDate, endDate);
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
    public List<EyelashRecord> findByPhoneAndDateRange(String phone, LocalDate startDate, LocalDate endDate) {
        return eyelashRecordRepository.findByPhoneAndDateRangeOrderByRecordDateDesc(phone, startDate, endDate);
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
    public List<EyelashRecord> findByLastNameAndDateRange(String lastName, LocalDate startDate, LocalDate endDate) {
        return eyelashRecordRepository.findByLastNameAndDateRangeOrderByRecordDateDesc(lastName, startDate, endDate);
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
    public List<EyelashRecord> findByPhoneAndLastNameAndDateRange(String phone, String lastName, LocalDate startDate, LocalDate endDate) {
        return eyelashRecordRepository.findByPhoneAndLastNameAndDateRangeOrderByRecordDateDesc(phone, lastName, startDate, endDate);
    }

    /**
     * 查找最近的记录
     * 
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<EyelashRecord> findRecentRecords() {
        return eyelashRecordRepository.findRecentRecords();
    }

    /**
     * 更新记录
     * 
     * @param record 睫毛记录
     * @return 更新后的记录
     */
    public EyelashRecord updateRecord(EyelashRecord record) {
        validateRecord(record);
        
        if (!eyelashRecordRepository.existsById(record.getId())) {
            throw new IllegalArgumentException("记录不存在");
        }
        
        return eyelashRecordRepository.save(record);
    }

    /**
     * 删除记录
     * 
     * @param id 记录ID
     * @throws IllegalArgumentException 当记录不存在时抛出
     */
    public void deleteRecord(Long id) {
        if (!eyelashRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("记录不存在");
        }
        
        eyelashRecordRepository.deleteById(id);
    }

    /**
     * 验证记录数据
     * 
     * @param record 睫毛记录
     * @throws IllegalArgumentException 当数据无效时抛出
     */
    private void validateRecord(EyelashRecord record) {
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
        
        if (record.getStyle() == null || record.getStyle().trim().isEmpty()) {
            throw new IllegalArgumentException("款式不能为空");
        }
        
        if (record.getModelNumber() == null || record.getModelNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("型号不能为空");
        }
        
        if (record.getLength() == null || record.getLength() <= 0) {
            throw new IllegalArgumentException("睫毛长度必须大于0");
        }
        
        if (record.getCurl() == null || record.getCurl().trim().isEmpty()) {
            throw new IllegalArgumentException("翘度不能为空");
        }
        
        if (record.getRecordDate() == null) {
            throw new IllegalArgumentException("记录日期不能为空");
        }
    }
}

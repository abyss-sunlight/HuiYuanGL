package com.example.membersystem.consume.service;

import com.example.membersystem.consume.entity.ConsumeRecord;
import com.example.membersystem.consume.repo.ConsumeRecordRepository;
import com.example.membersystem.discount.entity.RechargeDiscount;
import com.example.membersystem.discount.service.RechargeDiscountService;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 消费记录业务服务类
 */
@Service
@Transactional
public class ConsumeRecordService {

    private final ConsumeRecordRepository consumeRecordRepository;
    private final UserRepository userRepository;
    private final RechargeDiscountService rechargeDiscountService;

    @Autowired
    public ConsumeRecordService(ConsumeRecordRepository consumeRecordRepository,
                              UserRepository userRepository,
                              RechargeDiscountService rechargeDiscountService) {
        this.consumeRecordRepository = consumeRecordRepository;
        this.userRepository = userRepository;
        this.rechargeDiscountService = rechargeDiscountService;
    }

    /**
     * 创建消费记录
     * 
     * @param record 消费记录
     * @return 创建的记录
     */
    public ConsumeRecord createRecord(ConsumeRecord record) {
        validateRecord(record);
        
        // 查找用户
        Optional<User> userOptional = userRepository.findByPhone(record.getPhone());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("用户不存在，手机号：" + record.getPhone());
        }
        
        User user = userOptional.get();
        
        // 根据消费类型处理余额和折扣
        if ("充值".equals(record.getConsumeType())) {
            handleRecharge(user, record);
        } else if ("支出".equals(record.getConsumeType())) {
            handleConsume(user, record);
        } else {
            throw new IllegalArgumentException("不支持的消费类型：" + record.getConsumeType());
        }
        
        // 保存消费记录
        ConsumeRecord savedRecord = consumeRecordRepository.save(record);
        
        // 保存更新后的用户信息
        userRepository.save(user);
        
        return savedRecord;
    }
    
    /**
     * 处理充值逻辑
     * 
     * @param user 用户信息
     * @param record 消费记录
     */
    private void handleRecharge(User user, ConsumeRecord record) {
        // 计算新的余额
        BigDecimal newBalance = user.getAmount().add(record.getConsumeAmount());
        
        // 查找适用的充值折扣
        Optional<RechargeDiscount> discountOptional = rechargeDiscountService
            .findApplicableDiscount(record.getConsumeAmount());
        
        BigDecimal newDiscount = user.getDiscount(); // 默认保持原折扣
        
        if (discountOptional.isPresent()) {
            RechargeDiscount discount = discountOptional.get();
            // 只有当充值折扣更好时才更新（数值越小折扣越大）
            // 例如：1.2折(0.12) > 3.5折(0.035)，所以3.5折更好
            if (user.getDiscount() == null || discount.getDiscountRate().compareTo(user.getDiscount()) < 0) {
                newDiscount = discount.getDiscountRate();
            }
        }
        
        // 游客充值后自动提升为会员
        if (user.getPermissionLevel() != null && user.getPermissionLevel() == 4) {
            user.setPermissionLevel(3); // 提升为会员
            // 生成会员号
            if (user.getMemberNo() == null || user.getMemberNo().trim().isEmpty()) {
                user.setMemberNo(generateMemberNo());
            }
        }
        
        // 更新用户余额和折扣
        user.setAmount(newBalance);
        user.setDiscount(newDiscount);
        
        // 更新记录中的余额快照（充值后的余额）
        record.setBalance(newBalance);
    }
    
    /**
     * 生成会员号
     * 
     * @return 会员号
     */
    private String generateMemberNo() {
        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();
        // 取后6位作为会员号后缀
        String suffix = String.valueOf(timestamp).substring(String.valueOf(timestamp).length() - 6);
        // 生成会员号：VIP + 6位数字
        return "VIP" + suffix;
    }
    
    /**
     * 处理消费逻辑
     * 
     * @param user 用户信息
     * @param record 消费记录
     */
    private void handleConsume(User user, ConsumeRecord record) {
        // 计算折扣后的消费金额
        BigDecimal discountedAmount = record.getConsumeAmount()
            .multiply(user.getDiscount() != null ? user.getDiscount() : BigDecimal.ONE)
            .setScale(2, RoundingMode.HALF_UP);
        
        // 计算新的余额
        BigDecimal newBalance = user.getAmount().subtract(discountedAmount);
        
        // 检查余额是否足够
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不足，当前余额：" + user.getAmount() + 
                "，折扣后消费金额：" + discountedAmount);
        }
        
        // 更新用户余额
        user.setAmount(newBalance);
        
        // 更新记录中的余额快照（消费前的余额）
        record.setBalance(user.getAmount());
        
        // 更新记录中的消费金额为折扣后的金额
        record.setConsumeAmount(discountedAmount);
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
        return consumeRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据手机号查找记录
     * 
     * @param phone 手机号
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByPhone(String phone) {
        return consumeRecordRepository.findByPhoneOrderByCreatedAtDesc(phone);
    }

    /**
     * 根据姓氏查找记录
     * 
     * @param lastName 姓氏
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByLastName(String lastName) {
        return consumeRecordRepository.findByLastNameOrderByCreatedAtDesc(lastName);
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
        return consumeRecordRepository.findByPhoneAndLastNameOrderByCreatedAtDesc(phone, lastName);
    }

    /**
     * 根据日期范围查找消费记录（LocalDate版本）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 消费记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findConsumeRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return consumeRecordRepository.findByDateRangeOrderByCreatedAtDesc(startDate, endDate);
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
        return consumeRecordRepository.findByPhoneAndDateRangeOrderByCreatedAtDesc(phone, startDate, endDate);
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
        return consumeRecordRepository.findByLastNameAndDateRangeOrderByCreatedAtDesc(lastName, startDate, endDate);
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
        return consumeRecordRepository.findByPhoneAndLastNameAndDateRangeOrderByCreatedAtDesc(phone, lastName, startDate, endDate);
    }

    /**
     * 根据消费类型查找记录
     * 
     * @param consumeType 消费类型
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    public List<ConsumeRecord> findByConsumeType(String consumeType) {
        return consumeRecordRepository.findByConsumeTypeOrderByCreatedAtDesc(consumeType);
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

    /**
     * 根据日期范围查询消费记录
     * 
     * @param startDate 开始日期 (格式: yyyy-MM-dd)
     * @param endDate 结束日期 (格式: yyyy-MM-dd)
     * @return 消费记录列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByDateRange(String startDate, String endDate) {
        List<ConsumeRecord> records = consumeRecordRepository.findByConsumeDateBetween(startDate, endDate);
        
        return records.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("phone", record.getPhone());
                    map.put("last_name", record.getLastName());
                    map.put("gender", record.getGender());
                    map.put("balance", record.getBalance());
                    map.put("consume_amount", record.getConsumeAmount());
                    map.put("consume_item", record.getConsumeItem());
                    map.put("consume_date", record.getConsumeDate());
                    map.put("consume_type", record.getConsumeType());
                    map.put("created_at", record.getCreatedAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}

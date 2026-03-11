package com.example.membersystem.consume.controller;

import com.example.membersystem.consume.entity.ConsumeRecord;
import com.example.membersystem.consume.service.ConsumeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 消费记录控制器
 * 
 * 提供消费记录管理的API接口
 */
@RestController
@RequestMapping("/api/consume-records")
public class ConsumeRecordController {

    private final ConsumeRecordService consumeRecordService;

    @Autowired
    public ConsumeRecordController(ConsumeRecordService consumeRecordService) {
        this.consumeRecordService = consumeRecordService;
    }

    /**
     * 获取所有消费记录
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecords() {
        try {
            List<ConsumeRecord> records = consumeRecordService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取消费记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据ID获取消费记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRecordById(@PathVariable Long id) {
        try {
            Optional<ConsumeRecord> record = consumeRecordService.findById(id);
            
            if (record.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("message", "获取成功");
                response.put("data", record.get());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "记录不存在");
                
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取消费记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据手机号查找记录
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getRecordsByPhone(@PathVariable String phone) {
        try {
            List<ConsumeRecord> records = consumeRecordService.findByPhone(phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "查找记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据姓氏查找记录
     */
    @GetMapping("/lastname/{lastName}")
    public ResponseEntity<Map<String, Object>> getRecordsByLastName(@PathVariable String lastName) {
        try {
            List<ConsumeRecord> records = consumeRecordService.findByLastName(lastName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "查找记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据消费类型查找记录
     */
    @GetMapping("/type/{consumeType}")
    public ResponseEntity<Map<String, Object>> getRecordsByType(@PathVariable String consumeType) {
        try {
            List<ConsumeRecord> records = consumeRecordService.findByConsumeType(consumeType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "查找记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 搜索记录
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRecords(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String consumeType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<ConsumeRecord> records;
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            if (phone != null && lastName != null && start != null && end != null) {
                records = consumeRecordService.findByPhoneAndLastNameAndDateRange(phone, lastName, start, end);
            } else if (phone != null && lastName != null) {
                records = consumeRecordService.findByPhoneAndLastName(phone, lastName);
            } else if (phone != null && start != null && end != null) {
                records = consumeRecordService.findByPhoneAndDateRange(phone, start, end);
            } else if (lastName != null && start != null && end != null) {
                records = consumeRecordService.findByLastNameAndDateRange(lastName, start, end);
            } else if (phone != null) {
                records = consumeRecordService.findByPhone(phone);
            } else if (lastName != null) {
                records = consumeRecordService.findByLastName(lastName);
            } else if (consumeType != null) {
                records = consumeRecordService.findByConsumeType(consumeType);
            } else if (start != null && end != null) {
                records = consumeRecordService.findByDateRange(start, end);
            } else {
                records = consumeRecordService.findAll();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "搜索成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "搜索失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建消费记录
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRecord(@RequestBody ConsumeRecord record) {
        try {
            ConsumeRecord createdRecord = consumeRecordService.createRecord(record);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "创建成功");
            response.put("data", createdRecord);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "创建消费记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新消费记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRecord(@PathVariable Long id, @RequestBody ConsumeRecord record) {
        try {
            record.setId(id);
            ConsumeRecord updatedRecord = consumeRecordService.updateRecord(record);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "更新成功");
            response.put("data", updatedRecord);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "更新消费记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除消费记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id) {
        try {
            consumeRecordService.deleteRecord(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "删除成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "删除消费记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取最近的记录
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentRecords() {
        try {
            List<ConsumeRecord> records = consumeRecordService.findRecentRecords();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取最近记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取会员统计信息
     */
    @GetMapping("/stats/{phone}")
    public ResponseEntity<Map<String, Object>> getMemberStats(@PathVariable String phone) {
        try {
            BigDecimal totalConsume = consumeRecordService.getTotalConsumeAmount(phone);
            BigDecimal totalRecharge = consumeRecordService.getTotalRechargeAmount(phone);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalConsume", totalConsume);
            stats.put("totalRecharge", totalRecharge);
            stats.put("balance", totalRecharge.subtract(totalConsume));
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

package com.example.membersystem.eyelash.controller;

import com.example.membersystem.eyelash.entity.EyelashRecord;
import com.example.membersystem.eyelash.service.EyelashRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 睫毛记录控制器
 * 
 * 提供睫毛记录管理的API接口
 */
@RestController
@RequestMapping("/api/eyelash-records")
public class EyelashRecordController {

    private final EyelashRecordService eyelashRecordService;

    @Autowired
    public EyelashRecordController(EyelashRecordService eyelashRecordService) {
        this.eyelashRecordService = eyelashRecordService;
    }

    /**
     * 获取所有睫毛记录
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecords() {
        try {
            List<EyelashRecord> records = eyelashRecordService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取睫毛记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据ID获取睫毛记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRecordById(@PathVariable Long id) {
        try {
            Optional<EyelashRecord> record = eyelashRecordService.findById(id);
            
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
            response.put("message", "获取睫毛记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据手机号查找记录
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getRecordsByPhone(@PathVariable String phone) {
        try {
            List<EyelashRecord> records = eyelashRecordService.findByPhone(phone);
            
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
            List<EyelashRecord> records = eyelashRecordService.findByLastName(lastName);
            
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
     * 根据手机号和姓氏查找记录
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRecords(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<EyelashRecord> records;
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            if (phone != null && lastName != null && start != null && end != null) {
                records = eyelashRecordService.findByPhoneAndLastNameAndDateRange(phone, lastName, start, end);
            } else if (phone != null && lastName != null) {
                records = eyelashRecordService.findByPhoneAndLastName(phone, lastName);
            } else if (phone != null && start != null && end != null) {
                records = eyelashRecordService.findByPhoneAndDateRange(phone, start, end);
            } else if (lastName != null && start != null && end != null) {
                records = eyelashRecordService.findByLastNameAndDateRange(lastName, start, end);
            } else if (phone != null) {
                records = eyelashRecordService.findByPhone(phone);
            } else if (lastName != null) {
                records = eyelashRecordService.findByLastName(lastName);
            } else if (start != null && end != null) {
                records = eyelashRecordService.findByDateRange(start, end);
            } else {
                records = eyelashRecordService.findAll();
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
     * 创建睫毛记录
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRecord(@RequestBody EyelashRecord record) {
        try {
            EyelashRecord createdRecord = eyelashRecordService.createRecord(record);
            
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
            response.put("message", "创建睫毛记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新睫毛记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRecord(@PathVariable Long id, @RequestBody EyelashRecord record) {
        try {
            record.setId(id);
            EyelashRecord updatedRecord = eyelashRecordService.updateRecord(record);
            
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
            response.put("message", "更新睫毛记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除睫毛记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id) {
        try {
            eyelashRecordService.deleteRecord(id);
            
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
            response.put("message", "删除睫毛记录失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取最近的记录
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentRecords() {
        try {
            List<EyelashRecord> records = eyelashRecordService.findRecentRecords();
            
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
}

package com.example.membersystem.consume.controller;

import com.example.membersystem.consume.entity.ConsumeRecord;
import com.example.membersystem.consume.service.ConsumeRecordService;
import com.example.membersystem.user.entity.User;
import com.example.membersystem.user.service.UserService;
import com.example.membersystem.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ConsumeRecordController(ConsumeRecordService consumeRecordService, UserService userService, JwtUtil jwtUtil) {
        this.consumeRecordService = consumeRecordService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取当前用户信息
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String phone = jwtUtil.getPhoneFromToken(token);
            Optional<User> userOptional = userService.findByPhone(phone);
            return userOptional.orElse(null);
        }
        return null;
    }

    /**
     * 检查权限并过滤手机号
     */
    private String filterPhoneByPermission(String phone, User currentUser) {
        if (currentUser == null) {
            return phone; // 未登录用户，不限制
        }
        
        // 游客和会员只能查看自己的记录
        if (currentUser.getPermissionLevel() >= 3) {
            return currentUser.getPhone();
        }
        
        // 员工和店长可以查看所有记录
        return phone;
    }

    /**
     * 获取所有消费记录
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecords(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            List<ConsumeRecord> records;
            
            if (currentUser != null && currentUser.getPermissionLevel() >= 3) {
                // 游客和会员只能查看自己的记录
                records = consumeRecordService.findByPhone(currentUser.getPhone());
            } else {
                // 员工和店长可以查看所有记录
                records = consumeRecordService.findAll();
            }
            
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
    public ResponseEntity<Map<String, Object>> getRecordById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Optional<ConsumeRecord> recordOptional = consumeRecordService.findById(id);
            
            if (recordOptional.isPresent()) {
                ConsumeRecord record = recordOptional.get();
                User currentUser = getCurrentUser(request);
                
                // 权限检查：游客和会员只能查看自己的记录
                if (currentUser != null && currentUser.getPermissionLevel() >= 3) {
                    if (!record.getPhone().equals(currentUser.getPhone())) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 403);
                        response.put("message", "无权限查看此记录");
                        
                        return ResponseEntity.status(403).body(response);
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", 0);
                response.put("message", "获取成功");
                response.put("data", record);
                
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
    public ResponseEntity<Map<String, Object>> getRecordsByPhone(@PathVariable String phone, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            String filteredPhone = filterPhoneByPermission(phone, currentUser);
            
            List<ConsumeRecord> records = consumeRecordService.findByPhone(filteredPhone);
            
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
    @GetMapping("/last-name/{lastName}")
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
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            String filteredPhone = filterPhoneByPermission(phone, currentUser);
            
            List<ConsumeRecord> records;
            
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            if (filteredPhone != null && lastName != null && start != null && end != null) {
                records = consumeRecordService.findByPhoneAndLastNameAndDateRange(filteredPhone, lastName, start, end);
            } else if (filteredPhone != null && lastName != null) {
                records = consumeRecordService.findByPhoneAndLastName(filteredPhone, lastName);
            } else if (filteredPhone != null && start != null && end != null) {
                records = consumeRecordService.findByPhoneAndDateRange(filteredPhone, start, end);
            } else if (lastName != null && start != null && end != null) {
                records = consumeRecordService.findByLastNameAndDateRange(lastName, start, end);
            } else if (filteredPhone != null) {
                records = consumeRecordService.findByPhone(filteredPhone);
            } else if (lastName != null) {
                records = consumeRecordService.findByLastName(lastName);
            } else if (consumeType != null) {
                records = consumeRecordService.findByConsumeType(consumeType);
            } else if (start != null && end != null) {
                records = consumeRecordService.findConsumeRecordsByDateRange(start, end);
            } else {
                // 如果没有指定查询条件，根据权限返回记录
                if (currentUser != null && currentUser.getPermissionLevel() >= 3) {
                    records = consumeRecordService.findByPhone(currentUser.getPhone());
                } else {
                    records = consumeRecordService.findAll();
                }
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
     * 根据姓氏和日期范围查找记录
     */
    @GetMapping("/last-name/{lastName}/date-range")
    public ResponseEntity<Map<String, Object>> getRecordsByLastNameAndDateRange(
            @PathVariable String lastName,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<ConsumeRecord> records = consumeRecordService.findByLastNameAndDateRange(lastName, start, end);
            
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
     * 根据日期范围查找记录
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getRecordsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<ConsumeRecord> records = consumeRecordService.findConsumeRecordsByDateRange(start, end);
            
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
    public ResponseEntity<Map<String, Object>> getMemberStats(@PathVariable String phone, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            String filteredPhone = filterPhoneByPermission(phone, currentUser);
            
            BigDecimal totalConsume = consumeRecordService.getTotalConsumeAmount(filteredPhone);
            BigDecimal totalRecharge = consumeRecordService.getTotalRechargeAmount(filteredPhone);
            
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

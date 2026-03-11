package com.example.membersystem.discount.controller;

import com.example.membersystem.discount.entity.RechargeDiscount;
import com.example.membersystem.discount.service.RechargeDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 充值折扣控制器
 * 
 * 提供充值折扣管理的API接口
 */
@RestController
@RequestMapping("/api/recharge-discount")
public class RechargeDiscountController {

    private final RechargeDiscountService rechargeDiscountService;

    @Autowired
    public RechargeDiscountController(RechargeDiscountService rechargeDiscountService) {
        this.rechargeDiscountService = rechargeDiscountService;
    }

    /**
     * 获取所有折扣规则
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDiscounts() {
        try {
            List<RechargeDiscount> discounts = rechargeDiscountService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", discounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取折扣列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有启用的折扣规则
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveDiscounts() {
        try {
            List<RechargeDiscount> discounts = rechargeDiscountService.findAllActive();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", discounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取启用折扣列表失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据充值金额查找适用的折扣
     */
    @GetMapping("/applicable")
    public ResponseEntity<Map<String, Object>> getApplicableDiscount(@RequestParam BigDecimal rechargeAmount) {
        try {
            Optional<RechargeDiscount> discount = rechargeDiscountService.findApplicableDiscount(rechargeAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", discount.orElse(null));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "查找适用折扣失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据ID获取折扣规则
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDiscountById(@PathVariable Long id) {
        try {
            Optional<RechargeDiscount> discount = rechargeDiscountService.findById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", discount.orElse(null));
            
            if (discount.isEmpty()) {
                response.put("code", 404);
                response.put("message", "折扣规则不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取折扣规则失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建新的折扣规则
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDiscount(@RequestBody RechargeDiscount discount) {
        try {
            RechargeDiscount createdDiscount = rechargeDiscountService.createDiscount(discount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "折扣规则创建成功");
            response.put("data", createdDiscount);
            
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "创建折扣规则失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新折扣规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiscount(@PathVariable Long id, @RequestBody RechargeDiscount discount) {
        try {
            RechargeDiscount updatedDiscount = rechargeDiscountService.updateDiscount(id, discount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "折扣规则更新成功");
            response.put("data", updatedDiscount);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "更新折扣规则失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除折扣规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDiscount(@PathVariable Long id) {
        try {
            rechargeDiscountService.deleteDiscount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "折扣规则删除成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "删除折扣规则失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 切换折扣规则状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> status) {
        try {
            Boolean isActive = status.get("isActive");
            if (isActive == null) {
                throw new IllegalArgumentException("状态参数不能为空");
            }
            
            RechargeDiscount updatedDiscount = rechargeDiscountService.toggleStatus(id, isActive);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", isActive ? "折扣规则启用成功" : "折扣规则禁用成功");
            response.put("data", updatedDiscount);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "切换状态失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根据充值金额范围查找折扣规则
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getDiscountsByRange(
            @RequestParam BigDecimal minAmount, 
            @RequestParam BigDecimal maxAmount) {
        try {
            List<RechargeDiscount> discounts = rechargeDiscountService.findByAmountRange(minAmount, maxAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", discounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "查找折扣规则失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long totalActive = rechargeDiscountService.countActive();
            List<RechargeDiscount> allDiscounts = rechargeDiscountService.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "获取成功");
            response.put("data", Map.of(
                "totalActive", totalActive,
                "total", allDiscounts.size(),
                "inactive", allDiscounts.size() - totalActive
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", -1);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

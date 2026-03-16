package com.example.membersystem.carousel.controller;

import com.example.membersystem.carousel.dto.CarouselContentResponse;
import com.example.membersystem.carousel.service.CarouselContentService;
import com.example.membersystem.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播内容控制器
 * 
 * 提供轮播内容的API接口
 */
@RestController
@RequestMapping("/api/carousel")
@Tag(name = "轮播内容接口", description = "轮播内容相关接口")
public class CarouselContentController {

    private final CarouselContentService carouselContentService;

    public CarouselContentController(CarouselContentService carouselContentService) {
        this.carouselContentService = carouselContentService;
    }

    /**
     * 获取最新的轮播内容
     * 
     * @return 最新的轮播内容
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新轮播内容", description = "获取最新的启用轮播内容，包含视频和图片")
    public ApiResponse<CarouselContentResponse> getLatestContent() {
        CarouselContentResponse content = carouselContentService.getLatestContent();
        return ApiResponse.ok(content);
    }

    /**
     * 获取所有启用的轮播内容
     * 
     * @return 启用的轮播内容列表
     */
    @GetMapping
    @Operation(summary = "获取所有轮播内容", description = "获取所有启用的轮播内容列表")
    public ApiResponse<List<CarouselContentResponse>> getAllContent() {
        List<CarouselContentResponse> contentList = carouselContentService.getAllEnabledContent();
        return ApiResponse.ok(contentList);
    }

    /**
     * 根据ID获取轮播内容
     * 
     * @param id 轮播内容ID
     * @return 轮播内容
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取轮播内容", description = "根据ID获取指定的轮播内容")
    public ApiResponse<CarouselContentResponse> getContentById(@PathVariable Long id) {
        CarouselContentResponse content = carouselContentService.getContentById(id);
        return ApiResponse.ok(content);
    }
}

package com.example.membersystem.carousel.service;

import com.example.membersystem.carousel.dto.CarouselContentResponse;
import com.example.membersystem.carousel.entity.CarouselContent;
import com.example.membersystem.carousel.repo.CarouselContentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播内容服务
 * 
 * 提供轮播内容的业务逻辑处理
 */
@Service
public class CarouselContentService {

    private final CarouselContentRepository carouselContentRepository;

    public CarouselContentService(CarouselContentRepository carouselContentRepository) {
        this.carouselContentRepository = carouselContentRepository;
    }

    /**
     * 获取最新的启用轮播内容
     * 
     * @return 最新的启用轮播内容
     */
    public CarouselContentResponse getLatestContent() {
        return carouselContentRepository.findLatestEnabledContent()
                .map(this::convertToResponse)
                .orElse(createDefaultResponse());
    }

    /**
     * 获取所有启用的轮播内容
     * 
     * @return 启用的轮播内容列表
     */
    public List<CarouselContentResponse> getAllEnabledContent() {
        return carouselContentRepository.findEnabledCarouselContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取轮播内容
     * 
     * @param id 轮播内容ID
     * @return 轮播内容
     */
    public CarouselContentResponse getContentById(Long id) {
        return carouselContentRepository.findEnabledById(id)
                .map(this::convertToResponse)
                .orElse(createDefaultResponse());
    }

    /**
     * 转换实体为响应DTO
     * 
     * @param entity 轮播内容实体
     * @return 响应DTO
     */
    private CarouselContentResponse convertToResponse(CarouselContent entity) {
        CarouselContentResponse response = new CarouselContentResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setVideoUrl(entity.getVideoUrl());
        response.setImage1Url(entity.getImage1Url());
        response.setImage2Url(entity.getImage2Url());
        response.setImage3Url(entity.getImage3Url());
        response.setImage4Url(entity.getImage4Url());
        response.setImage5Url(entity.getImage5Url());
        response.setContent1(entity.getContent1());
        response.setContent2(entity.getContent2());
        response.setContent3(entity.getContent3());
        response.setContent4(entity.getContent4());
        response.setContent5(entity.getContent5());
        response.setIsEnabled(entity.getIsEnabled());
        response.setUploadDate(entity.getUploadDate());
        response.setUploaderId(entity.getUploaderId());
        return response;
    }

    /**
     * 创建默认响应
     * 
     * @return 默认轮播内容响应
     */
    private CarouselContentResponse createDefaultResponse() {
        CarouselContentResponse response = new CarouselContentResponse();
        response.setId(null);
        response.setTitle("暂无内容");
        response.setVideoUrl("");
        response.setImage1Url("");
        response.setImage2Url("");
        response.setImage3Url("");
        response.setImage4Url("");
        response.setImage5Url("");
        response.setContent1("暂无内容");
        response.setContent2("暂无内容");
        response.setContent3("暂无内容");
        response.setContent4("暂无内容");
        response.setContent5("暂无内容");
        response.setIsEnabled(false);
        response.setUploadDate(null);
        response.setUploaderId(null);
        return response;
    }
}

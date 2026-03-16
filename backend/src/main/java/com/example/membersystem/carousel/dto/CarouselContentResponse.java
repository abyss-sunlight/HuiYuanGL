package com.example.membersystem.carousel.dto;

import java.time.LocalDateTime;

/**
 * 轮播内容响应DTO
 * 
 * 用于返回轮播内容数据给前端
 */
public class CarouselContentResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 图片1 URL
     */
    private String image1Url;

    /**
     * 图片2 URL
     */
    private String image2Url;

    /**
     * 图片3 URL
     */
    private String image3Url;

    /**
     * 图片4 URL
     */
    private String image4Url;

    /**
     * 图片5 URL
     */
    private String image5Url;

    /**
     * 文案1
     */
    private String content1;

    /**
     * 文案2
     */
    private String content2;

    /**
     * 文案3
     */
    private String content3;

    /**
     * 文案4
     */
    private String content4;

    /**
     * 文案5
     */
    private String content5;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 上传日期
     */
    private LocalDateTime uploadDate;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImage1Url() {
        return image1Url;
    }

    public void setImage1Url(String image1Url) {
        this.image1Url = image1Url;
    }

    public String getImage2Url() {
        return image2Url;
    }

    public void setImage2Url(String image2Url) {
        this.image2Url = image2Url;
    }

    public String getImage3Url() {
        return image3Url;
    }

    public void setImage3Url(String image3Url) {
        this.image3Url = image3Url;
    }

    public String getImage4Url() {
        return image4Url;
    }

    public void setImage4Url(String image4Url) {
        this.image4Url = image4Url;
    }

    public String getImage5Url() {
        return image5Url;
    }

    public void setImage5Url(String image5Url) {
        this.image5Url = image5Url;
    }

    public String getContent1() {
        return content1;
    }

    public void setContent1(String content1) {
        this.content1 = content1;
    }

    public String getContent2() {
        return content2;
    }

    public void setContent2(String content2) {
        this.content2 = content2;
    }

    public String getContent3() {
        return content3;
    }

    public void setContent3(String content3) {
        this.content3 = content3;
    }

    public String getContent4() {
        return content4;
    }

    public void setContent4(String content4) {
        this.content4 = content4;
    }

    public String getContent5() {
        return content5;
    }

    public void setContent5(String content5) {
        this.content5 = content5;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    @Override
    public String toString() {
        return "CarouselContentResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", image1Url='" + image1Url + '\'' +
                ", image2Url='" + image2Url + '\'' +
                ", image3Url='" + image3Url + '\'' +
                ", image4Url='" + image4Url + '\'' +
                ", image5Url='" + image5Url + '\'' +
                ", content1='" + content1 + '\'' +
                ", content2='" + content2 + '\'' +
                ", content3='" + content3 + '\'' +
                ", content4='" + content4 + '\'' +
                ", content5='" + content5 + '\'' +
                ", isEnabled=" + isEnabled +
                ", uploadDate=" + uploadDate +
                ", uploaderId=" + uploaderId +
                '}';
    }
}

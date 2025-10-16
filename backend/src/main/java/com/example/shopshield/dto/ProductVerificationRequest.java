package com.example.shopshield.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class ProductVerificationRequest {
    
    private String productUrl;
    private List<String> productUrls;
    private String brandName;
    private String productCategory;
    private List<String> referenceImages; // Base64 encoded images
    
    public ProductVerificationRequest() {}
    
    public ProductVerificationRequest(String productUrl, String brandName, String productCategory) {
        this.productUrl = productUrl;
        this.brandName = brandName;
        this.productCategory = productCategory;
    }
    
    // Getters and Setters
    public String getProductUrl() {
        return productUrl;
    }
    
    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }
    
    public List<String> getProductUrls() {
        return productUrls;
    }
    
    public void setProductUrls(List<String> productUrls) {
        this.productUrls = productUrls;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    public String getProductCategory() {
        return productCategory;
    }
    
    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }
    
    public List<String> getReferenceImages() {
        return referenceImages;
    }
    
    public void setReferenceImages(List<String> referenceImages) {
        this.referenceImages = referenceImages;
    }
}
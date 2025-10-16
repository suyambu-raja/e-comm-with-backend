package com.example.shopshield.service;

import com.example.shopshield.dto.ProductVerificationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RealTimeVerificationService {
    
    CompletableFuture<ProductVerificationResponse> analyzeProductFromUrl(
            String productUrl, String brandName, String productCategory);
    
    CompletableFuture<ProductVerificationResponse> analyzeProductImage(
            MultipartFile file, String brandName, String productCategory);
    
    CompletableFuture<ProductVerificationResponse> batchAnalyzeFromUrls(List<String> productUrls);
    
    void triggerModelTraining();
    
    boolean isServiceHealthy();
}
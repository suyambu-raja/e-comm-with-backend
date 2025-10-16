package com.example.shopshield.controller;

import com.example.shopshield.dto.ProductVerificationRequest;
import com.example.shopshield.dto.ProductVerificationResponse;
import com.example.shopshield.service.RealTimeVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/verification")
@CrossOrigin(origins = "*")
public class RealTimeVerificationController {

    @Autowired
    private RealTimeVerificationService verificationService;

    @PostMapping("/analyze-url")
    public CompletableFuture<ResponseEntity<ProductVerificationResponse>> analyzeProductFromUrl(
            @Valid @RequestBody ProductVerificationRequest request) {
        
        return verificationService.analyzeProductFromUrl(request.getProductUrl(), request.getBrandName(), request.getProductCategory())
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/analyze-image")
    public CompletableFuture<ResponseEntity<ProductVerificationResponse>> analyzeProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "brandName", required = false) String brandName,
            @RequestParam(value = "productCategory", required = false) String productCategory) {
        
        return verificationService.analyzeProductImage(file, brandName, productCategory)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/batch-analyze")
    public CompletableFuture<ResponseEntity<ProductVerificationResponse>> batchAnalyzeProducts(
            @Valid @RequestBody ProductVerificationRequest request) {
        
        return verificationService.batchAnalyzeFromUrls(request.getProductUrls())
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Real-time verification service is healthy");
    }

    @PostMapping("/train-model")
    public ResponseEntity<String> triggerModelTraining() {
        verificationService.triggerModelTraining();
        return ResponseEntity.ok("Model training initiated in background");
    }
}
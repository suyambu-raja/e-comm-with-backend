package com.example.shopshield.service;

import com.example.shopshield.dto.ProductVerificationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class RealTimeVerificationServiceImpl implements RealTimeVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeVerificationServiceImpl.class);

    @Value("${microservice.cv.url:http://cv-service:8001}")
    private String cvServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Executor executor;

    public RealTimeVerificationServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.executor = Executors.newFixedThreadPool(10);
    }

    @Override
    public CompletableFuture<ProductVerificationResponse> analyzeProductFromUrl(
            String productUrl, String brandName, String productCategory) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Analyzing product from URL: {}", productUrl);
                
                // Prepare request body
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("ecommerce_url", productUrl);
                
                if (brandName != null || productCategory != null) {
                    Map<String, Object> referenceData = new HashMap<>();
                    if (brandName != null) referenceData.put("brand_name", brandName);
                    if (productCategory != null) referenceData.put("product_category", productCategory);
                    requestBody.put("reference_product_data", referenceData);
                }
                
                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                // Make API call to CV service
                String endpoint = cvServiceUrl + "/analyze/real-time-product";
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    return parseVerificationResponse(response.getBody());
                } else {
                    throw new RuntimeException("CV service returned status: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error analyzing product from URL: {}", e.getMessage(), e);
                return createErrorResponse("Failed to analyze product: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ProductVerificationResponse> analyzeProductImage(
            MultipartFile file, String brandName, String productCategory) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Analyzing uploaded product image: {}", file.getOriginalFilename());
                
                // Prepare multipart request
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
                
                // Add optional parameters
                if (brandName != null) {
                    Map<String, Object> analysisRequest = new HashMap<>();
                    analysisRequest.put("brand_name", brandName);
                    if (productCategory != null) {
                        analysisRequest.put("product_category", productCategory);
                    }
                    // Note: CV service expects these as form data or JSON
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                
                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
                
                // Make API call to CV service
                String endpoint = cvServiceUrl + "/analyze/product-image";
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    return parseVerificationResponse(response.getBody());
                } else {
                    throw new RuntimeException("CV service returned status: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error analyzing product image: {}", e.getMessage(), e);
                return createErrorResponse("Failed to analyze image: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ProductVerificationResponse> batchAnalyzeFromUrls(List<String> productUrls) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Batch analyzing {} product URLs", productUrls.size());
                
                List<CompletableFuture<ProductVerificationResponse>> futures = new ArrayList<>();
                
                // Analyze each URL concurrently
                for (String url : productUrls) {
                    futures.add(analyzeProductFromUrl(url, null, null));
                }
                
                // Wait for all analyses to complete
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
                );
                
                return allFutures.thenApply(v -> {
                    List<ProductVerificationResponse> results = new ArrayList<>();
                    
                    for (CompletableFuture<ProductVerificationResponse> future : futures) {
                        try {
                            results.add(future.get());
                        } catch (Exception e) {
                            logger.error("Error in batch analysis: {}", e.getMessage());
                            results.add(createErrorResponse("Analysis failed: " + e.getMessage()));
                        }
                    }
                    
                    // Combine results into a single response
                    return combineBatchResults(results);
                }).get();
                
            } catch (Exception e) {
                logger.error("Error in batch analysis: {}", e.getMessage(), e);
                return createErrorResponse("Batch analysis failed: " + e.getMessage());
            }
        }, executor);
    }

    @Override
    public void triggerModelTraining() {
        CompletableFuture.runAsync(() -> {
            try {
                logger.info("Triggering model training on CV service");
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<String> request = new HttpEntity<>("{}", headers);
                
                String endpoint = cvServiceUrl + "/train/custom-model";
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    logger.info("Model training initiated successfully");
                } else {
                    logger.error("Failed to trigger model training: {}", response.getStatusCode());
                }
                
            } catch (Exception e) {
                logger.error("Error triggering model training: {}", e.getMessage(), e);
            }
        }, executor);
    }

    @Override
    public boolean isServiceHealthy() {
        try {
            String endpoint = cvServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.error("CV service health check failed: {}", e.getMessage());
            return false;
        }
    }

    private ProductVerificationResponse parseVerificationResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            ProductVerificationResponse response = new ProductVerificationResponse();
            response.setStatus(jsonNode.get("status").asText());
            response.setAuthenticityScore(jsonNode.get("authenticity_score").asDouble());
            response.setConfidence(jsonNode.get("confidence").asDouble());
            
            // Parse analysis details
            if (jsonNode.has("analysis_details")) {
                Map<String, Object> analysisDetails = objectMapper.convertValue(
                    jsonNode.get("analysis_details"), Map.class);
                response.setAnalysisDetails(analysisDetails);
            }
            
            // Parse detected features
            if (jsonNode.has("detected_features")) {
                List<ProductVerificationResponse.DetectedFeature> features = new ArrayList<>();
                for (JsonNode featureNode : jsonNode.get("detected_features")) {
                    ProductVerificationResponse.DetectedFeature feature = 
                        new ProductVerificationResponse.DetectedFeature();
                    feature.setType(featureNode.get("type").asText());
                    feature.setDescription(featureNode.get("description").asText());
                    feature.setConfidence(featureNode.get("confidence").asDouble());
                    features.add(feature);
                }
                response.setDetectedFeatures(features);
            }
            
            // Parse risk factors
            if (jsonNode.has("risk_factors")) {
                List<String> riskFactors = new ArrayList<>();
                for (JsonNode riskNode : jsonNode.get("risk_factors")) {
                    riskFactors.add(riskNode.asText());
                }
                response.setRiskFactors(riskFactors);
            }
            
            // Parse recommendations
            if (jsonNode.has("recommendations")) {
                List<String> recommendations = new ArrayList<>();
                for (JsonNode recNode : jsonNode.get("recommendations")) {
                    recommendations.add(recNode.asText());
                }
                response.setRecommendations(recommendations);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error parsing verification response: {}", e.getMessage(), e);
            return createErrorResponse("Failed to parse response: " + e.getMessage());
        }
    }

    private ProductVerificationResponse createErrorResponse(String errorMessage) {
        ProductVerificationResponse response = new ProductVerificationResponse();
        response.setStatus("error");
        response.setAuthenticityScore(0.0);
        response.setConfidence(0.0);
        response.setRiskFactors(Arrays.asList(errorMessage));
        response.setRecommendations(Arrays.asList("Please try again or contact support"));
        return response;
    }

    private ProductVerificationResponse combineBatchResults(List<ProductVerificationResponse> results) {
        ProductVerificationResponse combined = new ProductVerificationResponse();
        
        // Calculate average scores
        double avgAuthenticity = results.stream()
            .mapToDouble(r -> r.getAuthenticityScore() != null ? r.getAuthenticityScore() : 0.0)
            .average().orElse(0.0);
            
        double avgConfidence = results.stream()
            .mapToDouble(r -> r.getConfidence() != null ? r.getConfidence() : 0.0)
            .average().orElse(0.0);
        
        combined.setStatus("success");
        combined.setAuthenticityScore(avgAuthenticity);
        combined.setConfidence(avgConfidence);
        
        // Combine analysis details
        Map<String, Object> combinedDetails = new HashMap<>();
        combinedDetails.put("total_products_analyzed", results.size());
        combinedDetails.put("individual_results", results);
        combined.setAnalysisDetails(combinedDetails);
        
        // Combine all detected features
        List<ProductVerificationResponse.DetectedFeature> allFeatures = new ArrayList<>();
        results.forEach(r -> {
            if (r.getDetectedFeatures() != null) {
                allFeatures.addAll(r.getDetectedFeatures());
            }
        });
        combined.setDetectedFeatures(allFeatures);
        
        // Combine risk factors
        Set<String> allRisks = new HashSet<>();
        results.forEach(r -> {
            if (r.getRiskFactors() != null) {
                allRisks.addAll(r.getRiskFactors());
            }
        });
        combined.setRiskFactors(new ArrayList<>(allRisks));
        
        // Generate batch recommendations
        List<String> batchRecommendations = new ArrayList<>();
        if (avgAuthenticity > 0.8) {
            batchRecommendations.add("Overall batch shows high authenticity");
        } else if (avgAuthenticity < 0.6) {
            batchRecommendations.add("Several products in batch may be counterfeit");
            batchRecommendations.add("Recommend individual review of flagged items");
        }
        
        combined.setRecommendations(batchRecommendations);
        
        return combined;
    }
}
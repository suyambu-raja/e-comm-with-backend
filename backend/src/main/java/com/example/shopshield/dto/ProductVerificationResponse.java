package com.example.shopshield.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProductVerificationResponse {
    
    private String status;
    private Double authenticityScore;
    private Double confidence;
    private Map<String, Object> analysisDetails;
    private List<DetectedFeature> detectedFeatures;
    private List<String> riskFactors;
    private List<String> recommendations;
    private LocalDateTime timestamp;
    
    public ProductVerificationResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ProductVerificationResponse(String status, Double authenticityScore, Double confidence) {
        this.status = status;
        this.authenticityScore = authenticityScore;
        this.confidence = confidence;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getAuthenticityScore() {
        return authenticityScore;
    }
    
    public void setAuthenticityScore(Double authenticityScore) {
        this.authenticityScore = authenticityScore;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
    
    public Map<String, Object> getAnalysisDetails() {
        return analysisDetails;
    }
    
    public void setAnalysisDetails(Map<String, Object> analysisDetails) {
        this.analysisDetails = analysisDetails;
    }
    
    public List<DetectedFeature> getDetectedFeatures() {
        return detectedFeatures;
    }
    
    public void setDetectedFeatures(List<DetectedFeature> detectedFeatures) {
        this.detectedFeatures = detectedFeatures;
    }
    
    public List<String> getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Inner class for detected features
    public static class DetectedFeature {
        private String type;
        private String description;
        private Double confidence;
        
        public DetectedFeature() {}
        
        public DetectedFeature(String type, String description, Double confidence) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
    }
}
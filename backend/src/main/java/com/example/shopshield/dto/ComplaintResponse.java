package com.example.shopshield.dto;

import com.example.shopshield.model.Complaint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintResponse {
    
    private Long id;
    private String title;
    private String description;
    private Complaint.ComplaintType complaintType;
    private Complaint.Priority priority;
    private Complaint.ComplaintStatus status;
    private String contactInfo;
    private String evidenceFilePath;
    private String adminResponse;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    // User information
    private Long userId;
    private String username;
    
    // Product information (if applicable)
    private Long productId;
    private String productName;
    
    // Convert from entity
    public static ComplaintResponse fromEntity(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setComplaintType(complaint.getComplaintType());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setContactInfo(complaint.getContactInfo());
        response.setEvidenceFilePath(complaint.getEvidenceFilePath());
        response.setAdminResponse(complaint.getAdminResponse());
        response.setResolutionNotes(complaint.getResolutionNotes());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        response.setResolvedAt(complaint.getResolvedAt());
        
        if (complaint.getUser() != null) {
            response.setUserId(complaint.getUser().getId());
            response.setUsername(complaint.getUser().getUsername());
        }
        
        if (complaint.getProduct() != null) {
            response.setProductId(complaint.getProduct().getProductId());
            response.setProductName(complaint.getProduct().getName());
        }
        
        return response;
    }
}
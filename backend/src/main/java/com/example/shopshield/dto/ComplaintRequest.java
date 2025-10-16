package com.example.shopshield.dto;

import com.example.shopshield.model.Complaint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Complaint type is required")
    private Complaint.ComplaintType complaintType;

    private Long productId;

    @Size(max = 255, message = "Contact info must not exceed 255 characters")
    private String contactInfo;

    private Complaint.Priority priority = Complaint.Priority.MEDIUM;

    // File upload will be handled separately in multipart request
    private String evidenceFileName;
}
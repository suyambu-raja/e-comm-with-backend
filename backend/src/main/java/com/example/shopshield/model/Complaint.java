package com.example.shopshield.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "complaints", schema = "compliance")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "complaint_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplaintType complaintType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    @Column(name = "evidence_file_path")
    private String evidenceFilePath;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum ComplaintType {
        PRODUCT_QUALITY,
        PRICING_ISSUE,
        MISLEADING_INFORMATION,
        PACKAGING_PROBLEM,
        LEGAL_METROLOGY_VIOLATION,
        FAKE_PRODUCT,
        OTHER
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum ComplaintStatus {
        SUBMITTED,
        UNDER_REVIEW,
        IN_PROGRESS,
        RESOLVED,
        REJECTED,
        CLOSED
    }

    public Complaint(User user, String title, String description, ComplaintType complaintType) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.complaintType = complaintType;
    }
}
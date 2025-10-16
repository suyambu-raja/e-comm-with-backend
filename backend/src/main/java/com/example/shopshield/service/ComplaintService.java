package com.example.shopshield.service;

import com.example.shopshield.dto.ComplaintRequest;
import com.example.shopshield.dto.ComplaintResponse;
import com.example.shopshield.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComplaintService {
    
    // Create a new complaint
    ComplaintResponse createComplaint(ComplaintRequest request, String username);
    
    // Create a complaint with evidence file
    ComplaintResponse createComplaintWithEvidence(ComplaintRequest request, MultipartFile evidenceFile, String username);
    
    // Get complaint by ID
    ComplaintResponse getComplaintById(Long id);
    
    // Get all complaints by user
    List<ComplaintResponse> getComplaintsByUser(String username);
    
    // Get complaints by user with pagination
    Page<ComplaintResponse> getComplaintsByUser(String username, Pageable pageable);
    
    // Get all complaints (admin only)
    List<ComplaintResponse> getAllComplaints();
    
    // Get all complaints with pagination (admin only)
    Page<ComplaintResponse> getAllComplaints(Pageable pageable);
    
    // Get complaints by status
    List<ComplaintResponse> getComplaintsByStatus(Complaint.ComplaintStatus status);
    
    // Get pending complaints (admin only)
    List<ComplaintResponse> getPendingComplaints();
    
    // Update complaint status (admin only)
    ComplaintResponse updateComplaintStatus(Long id, Complaint.ComplaintStatus status, String adminResponse);
    
    // Resolve complaint (admin only)
    ComplaintResponse resolveComplaint(Long id, String resolutionNotes);
    
    // Search complaints
    List<ComplaintResponse> searchComplaints(String keyword);
    
    // Get complaint statistics
    ComplaintStatistics getComplaintStatistics();
    
    // Delete complaint (admin only)
    void deleteComplaint(Long id);
    
    // Inner class for statistics
    class ComplaintStatistics {
        private long totalComplaints;
        private long submittedComplaints;
        private long underReviewComplaints;
        private long inProgressComplaints;
        private long resolvedComplaints;
        private long rejectedComplaints;
        private long closedComplaints;
        
        // Constructors, getters, and setters
        public ComplaintStatistics() {}
        
        public ComplaintStatistics(long totalComplaints, long submittedComplaints, long underReviewComplaints,
                                 long inProgressComplaints, long resolvedComplaints, long rejectedComplaints, long closedComplaints) {
            this.totalComplaints = totalComplaints;
            this.submittedComplaints = submittedComplaints;
            this.underReviewComplaints = underReviewComplaints;
            this.inProgressComplaints = inProgressComplaints;
            this.resolvedComplaints = resolvedComplaints;
            this.rejectedComplaints = rejectedComplaints;
            this.closedComplaints = closedComplaints;
        }
        
        // Getters and setters
        public long getTotalComplaints() { return totalComplaints; }
        public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }
        
        public long getSubmittedComplaints() { return submittedComplaints; }
        public void setSubmittedComplaints(long submittedComplaints) { this.submittedComplaints = submittedComplaints; }
        
        public long getUnderReviewComplaints() { return underReviewComplaints; }
        public void setUnderReviewComplaints(long underReviewComplaints) { this.underReviewComplaints = underReviewComplaints; }
        
        public long getInProgressComplaints() { return inProgressComplaints; }
        public void setInProgressComplaints(long inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }
        
        public long getResolvedComplaints() { return resolvedComplaints; }
        public void setResolvedComplaints(long resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }
        
        public long getRejectedComplaints() { return rejectedComplaints; }
        public void setRejectedComplaints(long rejectedComplaints) { this.rejectedComplaints = rejectedComplaints; }
        
        public long getClosedComplaints() { return closedComplaints; }
        public void setClosedComplaints(long closedComplaints) { this.closedComplaints = closedComplaints; }
    }
}
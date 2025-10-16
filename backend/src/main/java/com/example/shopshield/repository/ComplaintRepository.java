package com.example.shopshield.repository;

import com.example.shopshield.model.Complaint;
import com.example.shopshield.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find complaints by user
    List<Complaint> findByUserOrderByCreatedAtDesc(User user);
    
    // Find complaints by user with pagination
    Page<Complaint> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find complaints by status
    List<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.ComplaintStatus status);
    
    // Find complaints by status with pagination
    Page<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.ComplaintStatus status, Pageable pageable);

    // Find complaints by type
    List<Complaint> findByComplaintTypeOrderByCreatedAtDesc(Complaint.ComplaintType complaintType);

    // Find complaints by priority
    List<Complaint> findByPriorityOrderByCreatedAtDesc(Complaint.Priority priority);

    // Find complaints within date range
    @Query("SELECT c FROM Complaint c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<Complaint> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count complaints by status
    long countByStatus(Complaint.ComplaintStatus status);

    // Count complaints by user
    long countByUser(User user);

    // Find pending complaints (submitted or under review)
    @Query("SELECT c FROM Complaint c WHERE c.status IN ('SUBMITTED', 'UNDER_REVIEW') ORDER BY c.priority DESC, c.createdAt ASC")
    List<Complaint> findPendingComplaints();

    // Search complaints by title or description
    @Query("SELECT c FROM Complaint c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.createdAt DESC")
    List<Complaint> searchComplaints(@Param("keyword") String keyword);

    // Find complaints by user and status
    List<Complaint> findByUserAndStatusOrderByCreatedAtDesc(User user, Complaint.ComplaintStatus status);
}
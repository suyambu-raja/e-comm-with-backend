package com.example.shopshield.controller;

import com.example.shopshield.dto.ComplaintRequest;
import com.example.shopshield.dto.ComplaintResponse;
import com.example.shopshield.model.Complaint;
import com.example.shopshield.service.ComplaintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create a new complaint
     */
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody ComplaintRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            ComplaintResponse response = complaintService.createComplaint(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Create a complaint with evidence file
     */
    @PostMapping("/with-evidence")
    public ResponseEntity<ComplaintResponse> createComplaintWithEvidence(
            @RequestParam("complaint") String complaintJson,
            @RequestParam(value = "evidenceFile", required = false) MultipartFile evidenceFile,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            
            // Parse complaint data from JSON
            ComplaintRequest request = objectMapper.readValue(complaintJson, ComplaintRequest.class);
            
            ComplaintResponse response = complaintService.createComplaintWithEvidence(request, evidenceFile, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    /**
     * Get complaint by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        try {
            ComplaintResponse response = complaintService.getComplaintById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get current user's complaints
     */
    @GetMapping("/my-complaints")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ComplaintResponse> complaints = complaintService.getComplaintsByUser(username);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current user's complaints with pagination
     */
    @GetMapping("/my-complaints/paged")
    public ResponseEntity<Page<ComplaintResponse>> getMyComplaintsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ComplaintResponse> complaints = complaintService.getComplaintsByUser(username, pageable);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all complaints (Admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        try {
            List<ComplaintResponse> complaints = complaintService.getAllComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all complaints with pagination (Admin only)
     */
    @GetMapping("/admin/all/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ComplaintResponse>> getAllComplaintsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ComplaintResponse> complaints = complaintService.getAllComplaints(pageable);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pending complaints (Admin only)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplaintResponse>> getPendingComplaints() {
        try {
            List<ComplaintResponse> complaints = complaintService.getPendingComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get complaints by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ComplaintResponse>> getComplaintsByStatus(@PathVariable String status) {
        try {
            Complaint.ComplaintStatus complaintStatus = Complaint.ComplaintStatus.valueOf(status.toUpperCase());
            List<ComplaintResponse> complaints = complaintService.getComplaintsByStatus(complaintStatus);
            return ResponseEntity.ok(complaints);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update complaint status (Admin only)
     */
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String adminResponse = request.get("adminResponse");
            
            Complaint.ComplaintStatus complaintStatus = Complaint.ComplaintStatus.valueOf(status.toUpperCase());
            ComplaintResponse response = complaintService.updateComplaintStatus(id, complaintStatus, adminResponse);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Resolve complaint (Admin only)
     */
    @PutMapping("/admin/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponse> resolveComplaint(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String resolutionNotes = request.get("resolutionNotes");
            ComplaintResponse response = complaintService.resolveComplaint(id, resolutionNotes);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search complaints
     */
    @GetMapping("/search")
    public ResponseEntity<List<ComplaintResponse>> searchComplaints(@RequestParam String keyword) {
        try {
            List<ComplaintResponse> complaints = complaintService.searchComplaints(keyword);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get complaint statistics (Admin only)
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintService.ComplaintStatistics> getComplaintStatistics() {
        try {
            ComplaintService.ComplaintStatistics stats = complaintService.getComplaintStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete complaint (Admin only)
     */
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteComplaint(@PathVariable Long id) {
        try {
            complaintService.deleteComplaint(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Complaint deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get complaint types enum values
     */
    @GetMapping("/types")
    public ResponseEntity<Complaint.ComplaintType[]> getComplaintTypes() {
        return ResponseEntity.ok(Complaint.ComplaintType.values());
    }

    /**
     * Get complaint priorities enum values
     */
    @GetMapping("/priorities")
    public ResponseEntity<Complaint.Priority[]> getComplaintPriorities() {
        return ResponseEntity.ok(Complaint.Priority.values());
    }

    /**
     * Get complaint statuses enum values
     */
    @GetMapping("/statuses")
    public ResponseEntity<Complaint.ComplaintStatus[]> getComplaintStatuses() {
        return ResponseEntity.ok(Complaint.ComplaintStatus.values());
    }
}
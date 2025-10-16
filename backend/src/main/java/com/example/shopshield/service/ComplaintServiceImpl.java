package com.example.shopshield.service;

import com.example.shopshield.dto.ComplaintRequest;
import com.example.shopshield.dto.ComplaintResponse;
import com.example.shopshield.model.Complaint;
import com.example.shopshield.model.Product;
import com.example.shopshield.model.User;
import com.example.shopshield.repository.ComplaintRepository;
import com.example.shopshield.repository.ProductRepository;
import com.example.shopshield.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${app.file.upload-dir:uploads/complaints}")
    private String uploadDir;

    @Override
    public ComplaintResponse createComplaint(ComplaintRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setComplaintType(request.getComplaintType());
        complaint.setContactInfo(request.getContactInfo());
        complaint.setPriority(request.getPriority());

        // Set product if provided
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
            complaint.setProduct(product);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Override
    public ComplaintResponse createComplaintWithEvidence(ComplaintRequest request, MultipartFile evidenceFile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setComplaintType(request.getComplaintType());
        complaint.setContactInfo(request.getContactInfo());
        complaint.setPriority(request.getPriority());

        // Set product if provided
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
            complaint.setProduct(product);
        }

        // Handle file upload
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
            String filePath = saveEvidenceFile(evidenceFile);
            complaint.setEvidenceFilePath(filePath);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
        return ComplaintResponse.fromEntity(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        List<Complaint> complaints = complaintRepository.findByUserOrderByCreatedAtDesc(user);
        return complaints.stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getComplaintsByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Page<Complaint> complaints = complaintRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return complaints.map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findAll();
        return complaints.stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        Page<Complaint> complaints = complaintRepository.findAll(pageable);
        return complaints.map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByStatus(Complaint.ComplaintStatus status) {
        List<Complaint> complaints = complaintRepository.findByStatusOrderByCreatedAtDesc(status);
        return complaints.stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getPendingComplaints() {
        List<Complaint> complaints = complaintRepository.findPendingComplaints();
        return complaints.stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ComplaintResponse updateComplaintStatus(Long id, Complaint.ComplaintStatus status, String adminResponse) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
        
        complaint.setStatus(status);
        complaint.setAdminResponse(adminResponse);
        
        if (status == Complaint.ComplaintStatus.RESOLVED || status == Complaint.ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Override
    public ComplaintResponse resolveComplaint(Long id, String resolutionNotes) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
        
        complaint.setStatus(Complaint.ComplaintStatus.RESOLVED);
        complaint.setResolutionNotes(resolutionNotes);
        complaint.setResolvedAt(LocalDateTime.now());
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        return ComplaintResponse.fromEntity(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> searchComplaints(String keyword) {
        List<Complaint> complaints = complaintRepository.searchComplaints(keyword);
        return complaints.stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintStatistics getComplaintStatistics() {
        long total = complaintRepository.count();
        long submitted = complaintRepository.countByStatus(Complaint.ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByStatus(Complaint.ComplaintStatus.UNDER_REVIEW);
        long inProgress = complaintRepository.countByStatus(Complaint.ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(Complaint.ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByStatus(Complaint.ComplaintStatus.REJECTED);
        long closed = complaintRepository.countByStatus(Complaint.ComplaintStatus.CLOSED);
        
        return new ComplaintStatistics(total, submitted, underReview, inProgress, resolved, rejected, closed);
    }

    @Override
    public void deleteComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
        
        // Delete evidence file if exists
        if (complaint.getEvidenceFilePath() != null) {
            deleteEvidenceFile(complaint.getEvidenceFilePath());
        }
        
        complaintRepository.delete(complaint);
    }

    private String saveEvidenceFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null && originalFileName.contains(".") 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                    : "";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save evidence file", e);
        }
    }

    private void deleteEvidenceFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log the error but don't throw exception as the database operation should succeed
            System.err.println("Failed to delete evidence file: " + filePath);
        }
    }
}
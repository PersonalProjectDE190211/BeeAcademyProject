package com.beeacademy.backend.application.support.usecase;

import com.beeacademy.backend.application.support.dto.AddComplaintMessageCommand;
import com.beeacademy.backend.application.support.dto.ComplaintMessageResult;
import com.beeacademy.backend.application.support.dto.ComplaintResult;
import com.beeacademy.backend.application.support.dto.CreateComplaintCommand;
import com.beeacademy.backend.application.support.dto.ResolveComplaintCommand;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.support.Complaint;
import com.beeacademy.backend.domain.support.ComplaintRepositoryPort;
import com.beeacademy.backend.domain.support.ComplaintStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageComplaintUseCase {

    private final ComplaintRepositoryPort complaintRepository;

    public ManageComplaintUseCase(ComplaintRepositoryPort complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Transactional
    public ComplaintResult createComplaint(CreateComplaintCommand command) {
        Complaint complaint = Complaint.open(
                command.senderId(),
                command.senderRole(),
                command.title(),
                command.category(),
                command.priority(),
                command.messageContent()
        );

        Complaint saved = complaintRepository.save(complaint);
        return toResult(saved);
    }

    @Transactional
    public ComplaintResult addMessage(AddComplaintMessageCommand command) {
        Complaint complaint = complaintRepository.findById(command.complaintId())
                .orElseThrow(() -> new DomainException("COMPLAINT_NOT_FOUND", "Không tìm thấy phiếu khiếu nại"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(command.authorRole());
        if (!isAdmin && !complaint.getSenderId().equals(command.authorId())) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền tham gia phản hồi khiếu nại này");
        }

        complaint.addMessage(command.authorId(), command.authorRole(), command.content());
        Complaint saved = complaintRepository.save(complaint);
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResult> getMyComplaints(UUID senderId) {
        return complaintRepository.findBySenderId(senderId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ComplaintResult getComplaintById(UUID complaintId, UUID requesterId, boolean isAdmin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new DomainException("COMPLAINT_NOT_FOUND", "Không tìm thấy phiếu khiếu nại"));

        if (!isAdmin && !complaint.getSenderId().equals(requesterId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền xem phiếu khiếu nại này");
        }

        return toResult(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResult> getAllComplaints(ComplaintStatus status) {
        List<Complaint> complaints = status != null
                ? complaintRepository.findByStatus(status)
                : complaintRepository.findAllOrderByLastActivityDesc();

        return complaints.stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResult resolveComplaint(ResolveComplaintCommand command) {
        Complaint complaint = complaintRepository.findById(command.complaintId())
                .orElseThrow(() -> new DomainException("COMPLAINT_NOT_FOUND", "Không tìm thấy phiếu khiếu nại"));

        complaint.resolve(command.adminId(), command.resolutionSummary());
        Complaint saved = complaintRepository.save(complaint);
        return toResult(saved);
    }

    @Transactional
    public ComplaintResult closeComplaint(UUID complaintId, UUID requesterId, boolean isAdmin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new DomainException("COMPLAINT_NOT_FOUND", "Không tìm thấy phiếu khiếu nại"));

        if (!isAdmin && !complaint.getSenderId().equals(requesterId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền đóng phiếu khiếu nại này");
        }

        complaint.close();
        Complaint saved = complaintRepository.save(complaint);
        return toResult(saved);
    }

    private ComplaintResult toResult(Complaint complaint) {
        List<ComplaintMessageResult> messageResults = complaint.getMessages().stream()
                .map(m -> new ComplaintMessageResult(
                        m.getId(),
                        m.getAuthorId(),
                        m.getAuthorRole(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new ComplaintResult(
                complaint.getId(),
                complaint.getSenderId(),
                complaint.getSenderRole(),
                complaint.getTitle(),
                complaint.getCategory().name(),
                complaint.getPriority().name(),
                complaint.getStatus().name(),
                complaint.getCreatedAt(),
                complaint.getLastActivityAt(),
                complaint.getResolvedAt(),
                complaint.getResolvedByAdminId(),
                complaint.getResolutionSummary(),
                messageResults
        );
    }
}


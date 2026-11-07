package com.beeacademy.backend.application.family.usecase;

import com.beeacademy.backend.application.family.dto.AcceptLinkCommand;
import com.beeacademy.backend.application.family.dto.InviteStudentCommand;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.dto.RejectLinkCommand;
import com.beeacademy.backend.application.family.dto.UnlinkCommand;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.family.ParentStudentLink;
import com.beeacademy.backend.domain.family.ParentStudentLinkRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ManageParentLinkUseCase {

    private final ParentStudentLinkRepositoryPort linkRepository;

    public ManageParentLinkUseCase(ParentStudentLinkRepositoryPort linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public ParentLinkResult inviteStudent(InviteStudentCommand command) {
        // Sinh mã mời gồm 6 ký tự viết hoa dễ đọc (không chứa ký tự dễ nhầm lẫn như 0, O, 1, I)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        String token = sb.toString();

        ParentStudentLink link = ParentStudentLink.invite(
                command.parentId(),
                command.studentEmail(),
                token,
                ParentStudentLink.DEFAULT_INVITE_EXPIRY_HOURS
        );

        ParentStudentLink saved = linkRepository.save(link);
        return toResult(saved);
    }

    @Transactional
    public ParentLinkResult acceptInvite(AcceptLinkCommand command) {
        ParentStudentLink link = linkRepository.findByInviteToken(command.inviteToken().trim().toUpperCase())
                .orElseThrow(() -> new DomainException("INVALID_INVITE_TOKEN", "Mã mời liên kết không hợp lệ hoặc không tồn tại"));

        link.accept(command.studentId(), Instant.now());
        ParentStudentLink saved = linkRepository.save(link);
        return toResult(saved);
    }

    @Transactional
    public ParentLinkResult rejectInvite(RejectLinkCommand command) {
        ParentStudentLink link = linkRepository.findByInviteToken(command.inviteToken().trim().toUpperCase())
                .orElseThrow(() -> new DomainException("INVALID_INVITE_TOKEN", "Mã mời liên kết không hợp lệ hoặc không tồn tại"));

        link.reject(command.studentId(), command.reason());
        ParentStudentLink saved = linkRepository.save(link);
        return toResult(saved);
    }

    @Transactional
    public ParentLinkResult unlink(UnlinkCommand command) {
        ParentStudentLink link = linkRepository.findById(command.linkId())
                .orElseThrow(() -> new DomainException("LINK_NOT_FOUND", "Không tìm thấy thông tin liên kết gia đình"));

        link.unlink(command.actorId(), command.reason());
        ParentStudentLink saved = linkRepository.save(link);
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public List<ParentLinkResult> getParentLinks(UUID parentId) {
        return linkRepository.findByParentId(parentId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParentLinkResult> getStudentLinks(UUID studentId) {
        return linkRepository.findByStudentId(studentId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private ParentLinkResult toResult(ParentStudentLink link) {
        return new ParentLinkResult(
                link.getId(),
                link.getParentId(),
                link.getStudentId(),
                link.getStudentEmail(),
                link.getInviteToken(),
                link.getStatus().name(),
                link.isSensitiveDataConsentGranted(),
                link.getInviteExpiresAt(),
                link.getLinkedAt(),
                link.getUnlinkedAt(),
                link.getUnlinkReason()
        );
    }
}


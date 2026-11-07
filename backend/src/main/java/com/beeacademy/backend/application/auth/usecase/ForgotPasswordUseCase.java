package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.ForgotPasswordCommand;
import com.beeacademy.backend.application.auth.service.OtpService;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.common.MailSenderPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case Quên mật khẩu - gửi mã OTP qua email (UC03).
 */
@Service
public class ForgotPasswordUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final OtpService otpService;
    private final MailSenderPort mailSender;

    public ForgotPasswordUseCase(ProfileRepositoryPort profileRepository,
                                 OtpService otpService,
                                 MailSenderPort mailSender) {
        this.profileRepository = profileRepository;
        this.otpService = otpService;
        this.mailSender = mailSender;
    }

    public void execute(ForgotPasswordCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            return;
        }

        String email = command.email().trim().toLowerCase();
        Optional<Profile> profileOpt = profileRepository.findByEmail(email);

        if (profileOpt.isPresent()) {
            // Tạo mã OTP 6 số có hạn 15 phút
            String otp = otpService.generateOtp("RESET_PWD:" + email, 15);
            mailSender.sendOtpEmail(email, otp, 15);
        }
        // Trả về thành công kể cả không tìm thấy để chống user enumeration attack
    }
}

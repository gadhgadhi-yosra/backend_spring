package com.elfaddoui.backend.auth.service;

import com.elfaddoui.backend.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpResetOtpDeliveryService implements ResetOtpDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(SmtpResetOtpDeliveryService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public SmtpResetOtpDeliveryService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    @Override
    public void sendResetOtp(String email, String otp) {
        if (!appProperties.getMail().isEnabled()) {
            log.info("[DEV OTP] {} -> {}", email, otp);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(appProperties.getMail().getFrom());
            helper.setTo(email);
            helper.setSubject(appProperties.getMail().getOtpSubject());
            helper.setText(buildBody(otp), false);
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            log.error("Failed to send OTP email to {}", email, ex);
        }
    }

    private String buildBody(String otp) {
        return """
                Bonjour,

                Votre code OTP pour reinitialiser votre mot de passe est: %s

                Ce code expire dans 5 minutes.

                Si vous n'avez pas demande cette operation, ignorez cet email.
                """.formatted(otp);
    }
}

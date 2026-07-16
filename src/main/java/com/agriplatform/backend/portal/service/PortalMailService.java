package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.portal.model.PortalUser;
import com.agriplatform.backend.settings.model.SmtpConfig;
import com.agriplatform.backend.settings.service.SmtpConfigService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PortalMailService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalMailService.class);

    private static final String ACTIVATION_PATH = "/partner/activate?token=";
    private static final String RESET_PATH = "/partner/reset-password?token=";

    private final SmtpConfigService smtpConfigService;

    public PortalMailService(SmtpConfigService smtpConfigService) {
        this.smtpConfigService = smtpConfigService;
    }

    public void sendActivationEmail(PortalUser portalUser, String rawToken) {
        SmtpConfig config = getReadyConfig(portalUser);
        String link = buildLink(config.getFrontendBaseUrl(), ACTIVATION_PATH, rawToken);
        String subject = "Set up your FVP Purepick portal account";
        String body = "Hello " + portalUser.getUsername() + ",\n\n"
                + "Your FVP Purepick portal account is ready.\n"
                + "Username: " + portalUser.getUsername() + "\n"
                + "Set your password here: " + link + "\n\n"
                + "This link expires automatically. If you did not expect this email, ignore it.";
        send(config, portalUser.getEmail(), subject, body);
    }

    public void sendPasswordResetEmail(PortalUser portalUser, String rawToken) {
        SmtpConfig config = getReadyConfig(portalUser);
        String link = buildLink(config.getFrontendBaseUrl(), RESET_PATH, rawToken);
        String subject = "Reset your FVP Purepick portal password";
        String body = "Hello " + portalUser.getUsername() + ",\n\n"
                + "Use this link to reset your portal password:\n"
                + link + "\n\n"
                + "If you did not request this reset, ignore this email.";
        send(config, portalUser.getEmail(), subject, body);
    }

    public void sendTemporaryPasswordEmail(PortalUser portalUser, String temporaryPassword) {
        SmtpConfig config = getReadyConfig(portalUser);
        String subject = "FVP Purepick portal password recovery";
        String body = "Hello " + portalUser.getUsername() + ",\n\n"
                + "A temporary password has been created for your FVP Purepick partner portal account.\n"
                + "Username: " + portalUser.getUsername() + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "After signing in with this temporary password, you must set a new password before using the portal.\n"
                + "If you did not request this recovery, contact FVP Purepick support immediately.";
        send(config, portalUser.getEmail(), subject, body);
    }

    private SmtpConfig getReadyConfig(PortalUser portalUser) {
        if (!hasText(portalUser.getEmail())) {
            throw new IllegalArgumentException("Portal account email is required before sending credentials");
        }
        SmtpConfig config = smtpConfigService.getActiveConfig()
                .orElseThrow(() -> new IllegalArgumentException("SMTP is not configured or not active"));
        if (!hasText(config.getHost()) || config.getPort() == null || !hasText(config.getFromEmail())) {
            throw new IllegalArgumentException("SMTP configuration is incomplete");
        }
        if (!hasText(config.getFrontendBaseUrl())) {
            throw new IllegalArgumentException("SMTP frontend base URL is required");
        }
        return config;
    }

    private void send(SmtpConfig config, String recipientEmail, String subject, String body) {
        JavaMailSenderImpl sender = buildSender(config);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(buildFromAddress(config));
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
        } catch (MailException | jakarta.mail.MessagingException | UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unable to send portal email", ex);
        }
    }

    private JavaMailSenderImpl buildSender(SmtpConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort());
        if (config.isAuthEnabled()) {
            sender.setUsername(config.getUsername());
            sender.setPassword(config.getPassword());
        }
        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", String.valueOf(config.isAuthEnabled()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(config.isStartTlsEnabled()));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    private InternetAddress buildFromAddress(SmtpConfig config) throws jakarta.mail.MessagingException, UnsupportedEncodingException {
        if (hasText(config.getFromName())) {
            return new InternetAddress(config.getFromEmail(), config.getFromName());
        }
        return new InternetAddress(config.getFromEmail());
    }

    private String buildLink(String frontendBaseUrl, String path, String token) {
        String baseUrl = frontendBaseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path + token;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

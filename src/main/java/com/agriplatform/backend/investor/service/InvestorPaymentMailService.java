package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.inquiry.model.Inquiry;
import com.agriplatform.backend.investor.model.InvestorPayment;
import com.agriplatform.backend.settings.model.SmtpConfig;
import com.agriplatform.backend.settings.service.SmtpConfigService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class InvestorPaymentMailService {
    private final SmtpConfigService smtpConfigService;

    public InvestorPaymentMailService(SmtpConfigService smtpConfigService) {
        this.smtpConfigService = smtpConfigService;
    }

    public void sendPaymentLink(
            Inquiry inquiry,
            InvestorPayment payment,
            InvestorOnboardingSettings.Snapshot settings
    ) {
        SmtpConfig config = smtpConfigService.getActiveConfig()
                .orElseThrow(() -> new IllegalArgumentException("SMTP is not configured or not active"));
        if (!hasText(inquiry.getEmail())) {
            throw new IllegalArgumentException("Investor email is required before sending payment link");
        }

        String subject = "Complete Your " + settings.companyLegalName() + " Investment Payment";
        String body = String.join("\n",
                "Hello " + inquiry.getFullName() + ",",
                "",
                "Your investment application has been reviewed and approved.",
                "",
                "Investor reference: " + inquiry.getReferenceId(),
                "Investment amount: INR " + payment.getAmount().toPlainString(),
                "Payment status: Awaiting payment",
                "",
                "Complete your payment securely using this Cashfree payment link:",
                payment.getLinkUrl(),
                "",
                "The link is valid until: " + payment.getLinkExpiresAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                "",
                "After payment verification, your investment will be activated, your agreement will be generated, "
                        + "and you will receive a separate portal activation email.",
                "",
                "Do not share this payment link with anyone.",
                "",
                "Support: " + settings.supportEmail(),
                "",
                "Regards,",
                settings.companyLegalName(),
                settings.companyAddress()
        );
        send(config, inquiry.getEmail(), subject, body);
    }

    private void send(SmtpConfig config, String recipient, String subject, String body) {
        JavaMailSenderImpl sender = buildSender(config);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(buildFromAddress(config));
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
        } catch (MailException | jakarta.mail.MessagingException | UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unable to send investor payment email", ex);
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

    private InternetAddress buildFromAddress(SmtpConfig config)
            throws jakarta.mail.MessagingException, UnsupportedEncodingException {
        if (hasText(config.getFromName())) {
            return new InternetAddress(config.getFromEmail(), config.getFromName());
        }
        return new InternetAddress(config.getFromEmail());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

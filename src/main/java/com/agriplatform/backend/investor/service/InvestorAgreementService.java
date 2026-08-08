package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.inquiry.model.Inquiry;
import com.agriplatform.backend.investor.model.Investment;
import com.agriplatform.backend.investor.model.InvestorAccount;
import com.agriplatform.backend.investor.model.InvestorAgreement;
import com.agriplatform.backend.investor.model.InvestorAgreementStatus;
import com.agriplatform.backend.investor.model.InvestorPayment;
import com.agriplatform.backend.investor.repository.InvestorAgreementRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestorAgreementService {
    private static final float MARGIN = 54;
    private static final float BODY_FONT_SIZE = 10.5f;
    private static final float LINE_HEIGHT = 15;

    private final InvestorAgreementRepository investorAgreementRepository;

    public InvestorAgreementService(InvestorAgreementRepository investorAgreementRepository) {
        this.investorAgreementRepository = investorAgreementRepository;
    }

    public InvestorAgreement createPending(
            Inquiry inquiry,
            InvestorAccount investor,
            Investment investment,
            InvestorPayment payment,
            InvestorOnboardingSettings.Snapshot settings
    ) {
        String agreementNumber = generateAgreementNumber(inquiry);
        InvestorAgreement agreement = new InvestorAgreement(
                investor,
                investment,
                payment,
                agreementNumber,
                settings.termsVersion(),
                settings.termsText(),
                settings.companyLegalName(),
                settings.companyAddress(),
                settings.authorizedSignatory(),
                inquiry.getFullName().trim(),
                inquiry.getFullAddress().trim(),
                maskPan(inquiry.getPanNumber())
        );
        return investorAgreementRepository.save(agreement);
    }

    public void finalizeAgreement(InvestorPayment payment) {
        InvestorAgreement agreement = investorAgreementRepository.findByInvestorPayment_Id(payment.getId())
                .orElseThrow(() -> new IllegalArgumentException("Investor agreement not found"));
        agreement.markAvailable(payment.getPaymentReference());
        investorAgreementRepository.save(agreement);
    }

    @Transactional(readOnly = true)
    public InvestorAgreement getByInquiryId(Long inquiryId) {
        return investorAgreementRepository.findByInvestorPayment_SourceInquiryId(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Investor agreement not found"));
    }

    @Transactional(readOnly = true)
    public InvestorAgreement getById(Long agreementId) {
        return investorAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new IllegalArgumentException("Investor agreement not found"));
    }

    @Transactional(readOnly = true)
    public AgreementDocument buildPdf(InvestorAgreement agreement) {
        if (agreement.getStatus() != InvestorAgreementStatus.AVAILABLE) {
            throw new IllegalArgumentException("Investor agreement is not available yet");
        }
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.title("INVESTMENT AGREEMENT");
            writer.centered("Agreement number: " + agreement.getAgreementNumber(), PDType1Font.HELVETICA, 10);
            writer.centered("Terms version: " + agreement.getTermsVersion(), PDType1Font.HELVETICA, 9);
            writer.space(12);
            writer.heading("Parties");
            writer.paragraph(agreement.getCompanyLegalName() + ", " + agreement.getCompanyAddress()
                    + " (the Company), represented by " + agreement.getAuthorizedSignatory() + ".");
            writer.paragraph(agreement.getInvestorName() + ", address: " + agreement.getInvestorAddress()
                    + ", PAN: " + agreement.getPanMasked() + " (the Investor).");
            writer.heading("Investment details");
            writer.keyValue("Principal amount", "INR " + agreement.getPrincipalAmount().setScale(2, RoundingMode.HALF_UP));
            writer.keyValue("Monthly return rate", agreement.getMonthlyReturnRate().stripTrailingZeros().toPlainString() + "%");
            writer.keyValue("Investment start date", formatDate(agreement.getInvestmentStartDate()));
            writer.keyValue("Investment end date", agreement.getInvestmentEndDate() == null
                    ? "As provided by the approved investment plan" : formatDate(agreement.getInvestmentEndDate()));
            writer.keyValue("Payment reference", agreement.getPaymentReference());
            writer.space(8);
            writer.heading("Terms and conditions");
            for (String paragraph : agreement.getTermsText().split("\\R\\s*\\R")) {
                if (!paragraph.isBlank()) {
                    writer.paragraph(paragraph.trim());
                }
            }
            writer.space(12);
            writer.heading("Acknowledgement");
            writer.paragraph("This agreement is generated after successful payment verification. The immutable details above "
                    + "record the approved investment and the terms accepted for this investment.");
            writer.space(18);
            writer.keyValue("For the Company", agreement.getAuthorizedSignatory());
            writer.keyValue("Investor", agreement.getInvestorName());
            writer.footerAllPages(agreement.getAgreementNumber());
            document.save(output);
            return new AgreementDocument(agreement.getAgreementNumber() + ".pdf", output.toByteArray());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to generate investor agreement PDF", ex);
        }
    }

    private String generateAgreementNumber(Inquiry inquiry) {
        String reference = inquiry.getReferenceId().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String candidate = "AGR-" + reference;
        if (!investorAgreementRepository.existsByAgreementNumber(candidate)) {
            return candidate;
        }
        return candidate + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String maskPan(String pan) {
        String normalized = pan == null ? "" : pan.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() < 4) {
            throw new IllegalArgumentException("A valid PAN is required before investor approval");
        }
        return "*".repeat(normalized.length() - 4) + normalized.substring(normalized.length() - 4);
    }

    private String formatDate(java.time.LocalDate value) {
        return value.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }

    public record AgreementDocument(String filename, byte[] content) {
    }

    private static final class PdfWriter {
        private final PDDocument document;
        private final List<PDPage> pages = new ArrayList<>();
        private PDPage page;
        private float y;

        private PdfWriter(PDDocument document) {
            this.document = document;
            newPage();
        }

        private void title(String value) throws IOException {
            centered(value, PDType1Font.HELVETICA_BOLD, 16);
            space(5);
        }

        private void heading(String value) throws IOException {
            ensureSpace(24);
            writeLine(value, PDType1Font.HELVETICA_BOLD, 11.5f, MARGIN);
            space(2);
        }

        private void keyValue(String label, String value) throws IOException {
            paragraph(label + ": " + (value == null || value.isBlank() ? "Not available" : value));
        }

        private void paragraph(String value) throws IOException {
            for (String line : wrap(value, PDType1Font.HELVETICA, BODY_FONT_SIZE, width())) {
                ensureSpace(LINE_HEIGHT);
                writeLine(line, PDType1Font.HELVETICA, BODY_FONT_SIZE, MARGIN);
            }
            space(6);
        }

        private void centered(String value, PDFont font, float size) throws IOException {
            ensureSpace(LINE_HEIGHT);
            float textWidth = font.getStringWidth(value) / 1000 * size;
            writeLine(value, font, size, Math.max(MARGIN, (page.getMediaBox().getWidth() - textWidth) / 2));
        }

        private void writeLine(String value, PDFont font, float size, float x) throws IOException {
            try (PDPageContentStream stream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true
            )) {
                stream.beginText();
                stream.setFont(font, size);
                stream.newLineAtOffset(x, y);
                stream.showText(value);
                stream.endText();
            }
            y -= LINE_HEIGHT;
        }

        private void space(float points) {
            y -= points;
        }

        private void ensureSpace(float points) {
            if (y - points < MARGIN + 25) {
                newPage();
            }
        }

        private void newPage() {
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            pages.add(page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private float width() {
            return page.getMediaBox().getWidth() - (2 * MARGIN);
        }

        private List<String> wrap(String value, PDFont font, float size, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : value.replaceAll("\\s+", " ").trim().split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (font.getStringWidth(candidate) / 1000 * size <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (!current.isEmpty()) {
                        lines.add(current.toString());
                    }
                    current.setLength(0);
                    current.append(word);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
            return lines;
        }

        private void footerAllPages(String agreementNumber) throws IOException {
            int total = pages.size();
            for (int index = 0; index < total; index++) {
                PDPage footerPage = pages.get(index);
                String footer = agreementNumber + "  |  Page " + (index + 1) + " of " + total;
                try (PDPageContentStream stream = new PDPageContentStream(
                        document, footerPage, PDPageContentStream.AppendMode.APPEND, true, true
                )) {
                    stream.beginText();
                    stream.setFont(PDType1Font.HELVETICA, 8);
                    stream.newLineAtOffset(MARGIN, 30);
                    stream.showText(footer);
                    stream.endText();
                }
            }
        }
    }
}

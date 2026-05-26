package com.agriplatform.backend.portal.service;

import com.agriplatform.backend.*;
import com.agriplatform.backend.auth.controller.*;
import com.agriplatform.backend.auth.dto.*;
import com.agriplatform.backend.auth.service.*;
import com.agriplatform.backend.category.controller.*;
import com.agriplatform.backend.category.model.*;
import com.agriplatform.backend.category.repository.*;
import com.agriplatform.backend.common.controller.*;
import com.agriplatform.backend.config.*;
import com.agriplatform.backend.customer.controller.*;
import com.agriplatform.backend.customer.dto.*;
import com.agriplatform.backend.customer.model.*;
import com.agriplatform.backend.customer.repository.*;
import com.agriplatform.backend.customer.service.*;
import com.agriplatform.backend.document.controller.*;
import com.agriplatform.backend.document.dto.*;
import com.agriplatform.backend.document.model.*;
import com.agriplatform.backend.document.repository.*;
import com.agriplatform.backend.document.service.*;
import com.agriplatform.backend.inquiry.controller.*;
import com.agriplatform.backend.inquiry.dto.*;
import com.agriplatform.backend.inquiry.model.*;
import com.agriplatform.backend.inquiry.repository.*;
import com.agriplatform.backend.inquiry.service.*;
import com.agriplatform.backend.investor.controller.*;
import com.agriplatform.backend.investor.dto.*;
import com.agriplatform.backend.investor.model.*;
import com.agriplatform.backend.investor.repository.*;
import com.agriplatform.backend.investor.service.*;
import com.agriplatform.backend.lead.controller.*;
import com.agriplatform.backend.lead.dto.*;
import com.agriplatform.backend.lead.model.*;
import com.agriplatform.backend.lead.repository.*;
import com.agriplatform.backend.lead.service.*;
import com.agriplatform.backend.order.controller.*;
import com.agriplatform.backend.order.dto.*;
import com.agriplatform.backend.order.model.*;
import com.agriplatform.backend.order.repository.*;
import com.agriplatform.backend.order.service.*;
import com.agriplatform.backend.portal.controller.*;
import com.agriplatform.backend.portal.dto.*;
import com.agriplatform.backend.portal.model.*;
import com.agriplatform.backend.portal.repository.*;
import com.agriplatform.backend.portal.service.*;
import com.agriplatform.backend.product.controller.*;
import com.agriplatform.backend.product.dto.*;
import com.agriplatform.backend.product.model.*;
import com.agriplatform.backend.product.repository.*;
import com.agriplatform.backend.product.service.*;
import com.agriplatform.backend.security.*;
import com.agriplatform.backend.user.controller.*;
import com.agriplatform.backend.user.dto.*;
import com.agriplatform.backend.user.model.*;
import com.agriplatform.backend.user.repository.*;
import com.agriplatform.backend.user.service.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalSummaryService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PortalSummaryService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InquiryRepository inquiryRepository;
    private final InvestorAccountRepository investorAccountRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestorMonthlyReturnRepository investorMonthlyReturnRepository;
    private final InvestorPayoutRepository investorPayoutRepository;
    private final InvestorReceiptRepository investorReceiptRepository;
    private final PortalUserRepository portalUserRepository;

    public PortalSummaryService(
            PurchaseOrderRepository purchaseOrderRepository,
            InquiryRepository inquiryRepository,
            InvestorAccountRepository investorAccountRepository,
            InvestmentRepository investmentRepository,
            InvestorMonthlyReturnRepository investorMonthlyReturnRepository,
            InvestorPayoutRepository investorPayoutRepository,
            InvestorReceiptRepository investorReceiptRepository,
            PortalUserRepository portalUserRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inquiryRepository = inquiryRepository;
        this.investorAccountRepository = investorAccountRepository;
        this.investmentRepository = investmentRepository;
        this.investorMonthlyReturnRepository = investorMonthlyReturnRepository;
        this.investorPayoutRepository = investorPayoutRepository;
        this.investorReceiptRepository = investorReceiptRepository;
        this.portalUserRepository = portalUserRepository;
    }

    @Transactional(readOnly = true)
    public PortalSummaryResponse getSummary(String identifierInput) {
        String identifier = resolveIdentifier(identifierInput);

        List<PurchaseOrder> orders = purchaseOrderRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(identifier, identifier);
        List<Inquiry> inquiries = inquiryRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(identifier, identifier);
        List<InvestorAccount> investorAccounts = investorAccountRepository.findByEmailIgnoreCaseOrPhoneOrderByCreatedAtDesc(identifier, identifier);

        List<PortalOrderSummaryResponse> orderSummaries = orders.stream()
                .sorted(Comparator.comparing(PurchaseOrder::getCreatedAt).reversed())
                .map(order -> new PortalOrderSummaryResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getCurrency(),
                        order.getCreatedAt(),
                        order.getQuoteReference()
                ))
                .toList();

        List<PortalFarmerSummaryResponse> farmerSummaries = inquiries.stream()
                .filter(inquiry -> InquiryType.FARMER.equals(inquiry.getInquiryType()))
                .map(inquiry -> new PortalFarmerSummaryResponse(
                        inquiry.getId(),
                        inquiry.getReferenceId(),
                        inquiry.getStatus().name(),
                        inquiry.getVerificationStatus().name(),
                        inquiry.getFarmingType(),
                        inquiry.getLandArea(),
                        inquiry.getMainCrops(),
                        inquiry.getFarmerActionNote(),
                        inquiry.getCreatedAt()
                ))
                .toList();

        List<PortalInvestorSummaryResponse> investorSummaries = new ArrayList<>();
        List<PortalMonthlyReturnSummaryResponse> monthlyReturnSummaries = new ArrayList<>();
        List<PortalPayoutSummaryResponse> payoutSummaries = new ArrayList<>();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCommittedReturn = BigDecimal.ZERO;
        BigDecimal totalReturnsReceived = BigDecimal.ZERO;
        BigDecimal pendingPayout = BigDecimal.ZERO;

        for (InvestorAccount account : investorAccounts) {
            List<Investment> investments = investmentRepository.findByInvestorAccount_IdOrderByStartDateDesc(account.getId());
            List<InvestorMonthlyReturn> returns = investorMonthlyReturnRepository
                    .findByInvestorAccount_IdOrderByPeriodYearDescPeriodMonthDescCreatedAtDesc(account.getId());
            List<InvestorPayout> payouts = investorPayoutRepository.findByInvestorAccount_IdOrderByCreatedAtDesc(account.getId());

            BigDecimal investorTotalInvested = investments.stream()
                    .map(Investment::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal investorCommittedReturn = returns.stream()
                    .filter(item -> item.getStatus() == InvestorMonthlyReturnStatus.SUBMITTED
                            || item.getStatus() == InvestorMonthlyReturnStatus.APPROVED
                            || item.getStatus() == InvestorMonthlyReturnStatus.PAID)
                    .map(InvestorMonthlyReturn::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal investorReturnsReceived = returns.stream()
                    .filter(item -> item.getStatus() == InvestorMonthlyReturnStatus.PAID)
                    .map(InvestorMonthlyReturn::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal investorPendingPayout = payouts.stream()
                    .filter(item -> item.getStatus() == InvestorPayoutStatus.PENDING_APPROVAL || item.getStatus() == InvestorPayoutStatus.APPROVED)
                    .map(InvestorPayout::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalInvested = totalInvested.add(investorTotalInvested);
            totalCommittedReturn = totalCommittedReturn.add(investorCommittedReturn);
            totalReturnsReceived = totalReturnsReceived.add(investorReturnsReceived);
            pendingPayout = pendingPayout.add(investorPendingPayout);

            investorSummaries.add(new PortalInvestorSummaryResponse(
                    account.getId(),
                    account.getInvestorCode(),
                    account.getStatus().name(),
                    account.getVerificationStatus().name(),
                    scaleMoney(investorTotalInvested),
                    scaleMoney(investorReturnsReceived),
                    scaleMoney(investorPendingPayout),
                    account.getCreatedAt()
            ));

            for (InvestorMonthlyReturn item : returns) {
                InvestorPayout payout = item.getPayout();
                InvestorReceipt receipt = payout == null ? null : investorReceiptRepository.findByPayout_Id(payout.getId()).orElse(null);

                monthlyReturnSummaries.add(new PortalMonthlyReturnSummaryResponse(
                        item.getId(),
                        item.getPeriodYear(),
                        item.getPeriodMonth(),
                        item.getInvestment().getInvestmentReference(),
                        scaleMoney(item.getBasePrincipal()),
                        scaleMoney(item.getReturnRate()),
                        scaleMoney(item.getCalculatedAmount()),
                        item.getOverrideAmount() == null ? null : scaleMoney(item.getOverrideAmount()),
                        scaleMoney(item.getFinalAmount()),
                        item.getStatus().name(),
                        item.getOverrideReason(),
                        payout == null ? null : payout.getPayoutReference(),
                        receipt == null ? null : receipt.getReceiptNumber(),
                        item.getUpdatedAt()
                ));
            }

            for (InvestorPayout payout : payouts) {
                InvestorReceipt receipt = investorReceiptRepository.findByPayout_Id(payout.getId()).orElse(null);
                payoutSummaries.add(new PortalPayoutSummaryResponse(
                        payout.getId(),
                        payout.getPayoutReference(),
                        scaleMoney(payout.getTotalAmount()),
                        payout.getStatus().name(),
                        payout.getPaymentChannel(),
                        payout.getTransactionReference(),
                        payout.getPaidAt(),
                        receipt == null ? null : receipt.getReceiptNumber(),
                        receipt == null ? null : receipt.getId(),
                        payout.getCreatedAt()
                ));
            }
        }

        if (investorSummaries.isEmpty()) {
            inquiries.stream()
                    .filter(inquiry -> InquiryType.INVESTOR.equals(inquiry.getInquiryType()))
                    .forEach(inquiry -> investorSummaries.add(new PortalInvestorSummaryResponse(
                            inquiry.getId(),
                            inquiry.getReferenceId(),
                            inquiry.getStatus().name(),
                            inquiry.getVerificationStatus().name(),
                            inquiry.getInvestmentAmount() == null ? BigDecimal.ZERO : scaleMoney(inquiry.getInvestmentAmount()),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            inquiry.getCreatedAt()
                    )));
            totalInvested = investorSummaries.stream()
                    .map(PortalInvestorSummaryResponse::totalInvested)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        monthlyReturnSummaries.sort(Comparator
                .comparing(PortalMonthlyReturnSummaryResponse::periodYear).reversed()
                .thenComparing(PortalMonthlyReturnSummaryResponse::periodMonth, Comparator.reverseOrder())
                .thenComparing(PortalMonthlyReturnSummaryResponse::updatedAt, Comparator.reverseOrder()));
        payoutSummaries.sort(Comparator.comparing(PortalPayoutSummaryResponse::createdAt).reversed());

        return new PortalSummaryResponse(
                identifier,
                scaleMoney(totalInvested),
                scaleMoney(totalCommittedReturn),
                scaleMoney(totalReturnsReceived),
                scaleMoney(pendingPayout),
                orders.size(),
                orderSummaries,
                investorSummaries,
                farmerSummaries,
                monthlyReturnSummaries,
                payoutSummaries
        );
    }

    @Transactional(readOnly = true)
    public String downloadReceipt(String identifierInput, String receiptNumber) {
        String identifier = resolveIdentifier(identifierInput);
        InvestorReceipt receipt = investorReceiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        InvestorAccount account = receipt.getPayout().getInvestorAccount();
        String accountEmail = account.getEmail() == null ? "" : account.getEmail().trim().toLowerCase(Locale.ROOT);
        String accountPhone = account.getPhone() == null ? "" : account.getPhone().replaceAll("\\s+", "");
        if (!identifier.equals(accountEmail) && !identifier.equals(accountPhone)) {
            throw new IllegalArgumentException("Receipt does not belong to the authenticated profile");
        }

        InvestorPayout payout = receipt.getPayout();
        return String.join("\n",
                "FVP Purepick - Investor Payout Receipt",
                "Receipt Number: " + receipt.getReceiptNumber(),
                "Generated At: " + receipt.getGeneratedAt(),
                "Investor Code: " + account.getInvestorCode(),
                "Investor Name: " + account.getFullName(),
                "Payout Reference: " + payout.getPayoutReference(),
                "Payout Amount (INR): " + payout.getTotalAmount(),
                "Status: " + payout.getStatus().name(),
                "Payment Channel: " + (payout.getPaymentChannel() == null ? "-" : payout.getPaymentChannel()),
                "Transaction Reference: " + (payout.getTransactionReference() == null ? "-" : payout.getTransactionReference()),
                "Paid At: " + (payout.getPaidAt() == null ? "-" : payout.getPaidAt().toString())
        );
    }

    private String resolveIdentifier(String input) {
        if (input != null && input.startsWith("PORTAL:")) {
            Long portalUserId = parsePortalUserId(input);
            PortalUser portalUser = portalUserRepository.findById(portalUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Portal user not found"));
            if (hasText(portalUser.getEmail())) {
                return normalizeIdentifier(portalUser.getEmail());
            }
            if (hasText(portalUser.getPhone())) {
                return normalizeIdentifier(portalUser.getPhone());
            }
            throw new IllegalArgumentException("Portal user has no linked identifier");
        }
        return normalizeIdentifier(input);
    }

    private Long parsePortalUserId(String subject) {
        try {
            return Long.parseLong(subject.substring("PORTAL:".length()));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid portal token");
        }
    }

    private String normalizeIdentifier(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Identifier is required");
        }
        String value = input.trim();
        if (value.contains("@")) {
            return value.toLowerCase(Locale.ROOT);
        }
        return value.replaceAll("\\s+", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

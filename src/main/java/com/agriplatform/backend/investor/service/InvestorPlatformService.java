package com.agriplatform.backend.investor.service;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestorPlatformService {

    private final InvestorAccountRepository investorAccountRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestorMonthlyReturnRepository investorMonthlyReturnRepository;
    private final InvestorPayoutRepository investorPayoutRepository;
    private final InvestorReceiptRepository investorReceiptRepository;
    private final InquiryRepository inquiryRepository;

    public InvestorPlatformService(
            InvestorAccountRepository investorAccountRepository,
            InvestmentRepository investmentRepository,
            InvestorMonthlyReturnRepository investorMonthlyReturnRepository,
            InvestorPayoutRepository investorPayoutRepository,
            InvestorReceiptRepository investorReceiptRepository,
            InquiryRepository inquiryRepository
    ) {
        this.investorAccountRepository = investorAccountRepository;
        this.investmentRepository = investmentRepository;
        this.investorMonthlyReturnRepository = investorMonthlyReturnRepository;
        this.investorPayoutRepository = investorPayoutRepository;
        this.investorReceiptRepository = investorReceiptRepository;
        this.inquiryRepository = inquiryRepository;
    }

    @Transactional(readOnly = true)
    public List<InvestorAccountResponse> getInvestors(String search, String status, String verificationStatus) {
        String searchFilter = normalizeSearch(search);
        InvestorAccountStatus statusFilter = parseInvestorStatusNullable(status);
        VerificationStatus verificationFilter = parseVerificationStatusNullable(verificationStatus);
        InvestorSummaryMaps summaryMaps = loadInvestorSummaryMaps();

        return investorAccountRepository.findAllByOrderByUpdatedAtDesc().stream()
                .filter(account -> statusFilter == null || account.getStatus() == statusFilter)
                .filter(account -> verificationFilter == null || account.getVerificationStatus() == verificationFilter)
                .filter(account -> searchFilter == null
                        || containsIgnoreCase(account.getInvestorCode(), searchFilter)
                        || containsIgnoreCase(account.getFullName(), searchFilter)
                        || containsIgnoreCase(account.getEmail(), searchFilter)
                        || containsIgnoreCase(account.getPhone(), searchFilter))
                .map(account -> mapInvestor(account, summaryMaps))
                .toList();
    }

    @Transactional(readOnly = true)
    public InvestorAccountResponse getInvestor(Long id) {
        return mapInvestor(getInvestorEntity(id), loadInvestorSummaryMaps());
    }

    @Transactional(readOnly = true)
    public InvestorOverviewResponse getOverview(
            String investorSearch,
            String investorStatus,
            String verificationStatus,
            String investmentSearch,
            String investmentStatus
    ) {
        String investorSearchFilter = normalizeSearch(investorSearch);
        String investmentSearchFilter = normalizeSearch(investmentSearch);
        InvestorAccountStatus investorStatusFilter = parseInvestorStatusNullable(investorStatus);
        VerificationStatus verificationStatusFilter = parseVerificationStatusNullable(verificationStatus);
        InvestmentStatus investmentStatusFilter = parseInvestmentStatusNullable(investmentStatus);

        InvestorSummaryMaps summaryMaps = loadInvestorSummaryMaps();

        List<InvestorAccountResponse> investors = investorAccountRepository.findAllByOrderByUpdatedAtDesc().stream()
                .filter(account -> investorStatusFilter == null || account.getStatus() == investorStatusFilter)
                .filter(account -> verificationStatusFilter == null || account.getVerificationStatus() == verificationStatusFilter)
                .filter(account -> investorSearchFilter == null
                        || containsIgnoreCase(account.getInvestorCode(), investorSearchFilter)
                        || containsIgnoreCase(account.getFullName(), investorSearchFilter)
                        || containsIgnoreCase(account.getEmail(), investorSearchFilter)
                        || containsIgnoreCase(account.getPhone(), investorSearchFilter))
                .map(account -> mapInvestor(account, summaryMaps))
                .toList();

        List<InvestorAccountResponse> activeInvestors = investorAccountRepository.findAllByOrderByUpdatedAtDesc().stream()
                .filter(account -> account.getStatus() == InvestorAccountStatus.ACTIVE)
                .map(account -> mapInvestor(account, summaryMaps))
                .toList();

        List<InvestmentResponse> investments = investmentRepository.findAllWithInvestorOrderByCreatedAtDesc().stream()
                .filter(item -> investmentStatusFilter == null || item.getStatus() == investmentStatusFilter)
                .filter(item -> investmentSearchFilter == null
                        || containsIgnoreCase(item.getInvestmentReference(), investmentSearchFilter)
                        || containsIgnoreCase(item.getInvestorAccount().getInvestorCode(), investmentSearchFilter)
                        || containsIgnoreCase(item.getInvestorAccount().getFullName(), investmentSearchFilter))
                .map(this::mapInvestment)
                .toList();

        return new InvestorOverviewResponse(investors, activeInvestors, investments);
    }

    @Transactional
    public InvestorAccountResponse createInvestor(CreateInvestorAccountRequest request) {
        Long sourceInquiryId = request.sourceInquiryId();
        if (sourceInquiryId != null) {
            Inquiry inquiry = inquiryRepository.findById(sourceInquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("Source inquiry not found"));
            if (!InquiryType.INVESTOR.equals(inquiry.getInquiryType())) {
                throw new IllegalArgumentException("Source inquiry must be INVESTOR");
            }
        }

        String investorCode = request.investorCode() == null || request.investorCode().isBlank()
                ? generateUniqueInvestorCode()
                : request.investorCode().trim().toUpperCase(Locale.ROOT);
        if (investorAccountRepository.existsByInvestorCode(investorCode)) {
            throw new IllegalArgumentException("Investor code already exists");
        }

        InvestorAccount investorAccount = new InvestorAccount(
                investorCode,
                request.fullName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.phone().trim(),
                sourceInquiryId,
                parseInvestorStatus(request.status()),
                parseVerificationStatus(request.verificationStatus()),
                normalizeNullable(request.notes())
        );

        return mapInvestor(investorAccountRepository.save(investorAccount), loadInvestorSummaryMaps());
    }

    @Transactional
    public InvestorAccountResponse updateInvestor(Long id, UpdateInvestorAccountRequest request) {
        InvestorAccount investorAccount = getInvestorEntity(id);
        investorAccount.updateProfile(
                request.fullName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.phone().trim(),
                parseInvestorStatus(request.status()),
                parseVerificationStatus(request.verificationStatus()),
                normalizeNullable(request.notes())
        );
        return mapInvestor(investorAccountRepository.save(investorAccount), loadInvestorSummaryMaps());
    }

    @Transactional
    public InvestorProfileResponse createInvestorProfile(InvestorProfileUpsertRequest request) {
        validateProfileInvestmentPayload(request);
        InvestorAccountResponse investor = createInvestor(new CreateInvestorAccountRequest(
                request.investorCode(),
                request.fullName(),
                request.email(),
                request.phone(),
                request.sourceInquiryId(),
                request.status(),
                request.verificationStatus(),
                request.notes()
        ));

        InvestmentResponse investment = null;
        if (hasInvestmentPayload(request)) {
            investment = createInvestment(new CreateInvestmentRequest(
                    investor.id(),
                    null,
                    request.principalAmount(),
                    request.monthlyReturnRate(),
                    request.startDate(),
                    request.endDate(),
                    normalizeInvestmentStatus(request.investmentStatus()),
                    request.investmentNotes()
            ));
        }

        return new InvestorProfileResponse(investor, investment);
    }

    @Transactional
    public InvestorProfileResponse updateInvestorProfile(Long investorId, InvestorProfileUpsertRequest request) {
        validateProfileInvestmentPayload(request);
        InvestorAccountResponse investor = updateInvestor(investorId, new UpdateInvestorAccountRequest(
                request.fullName(),
                request.email(),
                request.phone(),
                request.status(),
                request.verificationStatus(),
                request.notes()
        ));

        InvestmentResponse investment = null;
        if (request.investmentId() != null) {
            Investment existing = investmentRepository.findById(request.investmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Investment not found"));
            if (!existing.getInvestorAccount().getId().equals(investorId)) {
                throw new IllegalArgumentException("Investment does not belong to selected investor");
            }
            investment = updateInvestment(existing.getId(), new UpdateInvestmentRequest(
                    request.principalAmount(),
                    request.monthlyReturnRate(),
                    request.startDate(),
                    request.endDate(),
                    normalizeInvestmentStatus(request.investmentStatus()),
                    request.investmentNotes()
            ));
        } else if (hasInvestmentPayload(request)) {
            investment = createInvestment(new CreateInvestmentRequest(
                    investorId,
                    null,
                    request.principalAmount(),
                    request.monthlyReturnRate(),
                    request.startDate(),
                    request.endDate(),
                    normalizeInvestmentStatus(request.investmentStatus()),
                    request.investmentNotes()
            ));
        }

        return new InvestorProfileResponse(investor, investment);
    }

    @Transactional(readOnly = true)
    public List<InvestmentResponse> getInvestments(Long investorId, String status, String search) {
        InvestmentStatus statusFilter = parseInvestmentStatusNullable(status);
        String searchFilter = normalizeSearch(search);
        List<Investment> base = investorId == null
                ? investmentRepository.findAllWithInvestorOrderByCreatedAtDesc()
                : investmentRepository.findByInvestorAccountIdWithInvestorOrderByStartDateDesc(investorId);

        return base.stream()
                .filter(item -> statusFilter == null || item.getStatus() == statusFilter)
                .filter(item -> searchFilter == null
                        || containsIgnoreCase(item.getInvestmentReference(), searchFilter)
                        || containsIgnoreCase(item.getInvestorAccount().getInvestorCode(), searchFilter)
                        || containsIgnoreCase(item.getInvestorAccount().getFullName(), searchFilter))
                .map(this::mapInvestment)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvestmentResponse getInvestment(Long id) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found"));
        return mapInvestment(investment);
    }

    @Transactional
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        InvestorAccount investor = getInvestorEntity(request.investorId());
        validateInvestmentDates(request.startDate(), request.endDate());

        String reference = request.investmentReference() == null || request.investmentReference().isBlank()
                ? generateUniqueInvestmentReference()
                : request.investmentReference().trim().toUpperCase(Locale.ROOT);
        if (investmentRepository.existsByInvestmentReference(reference)) {
            throw new IllegalArgumentException("Investment reference already exists");
        }

        Investment investment = new Investment(
                investor,
                reference,
                scaleMoney(request.principalAmount()),
                scaleRate(request.monthlyReturnRate()),
                request.startDate(),
                request.endDate(),
                parseInvestmentStatus(request.status()),
                normalizeNullable(request.notes())
        );
        return mapInvestment(investmentRepository.save(investment));
    }

    @Transactional
    public InvestmentResponse updateInvestment(Long id, UpdateInvestmentRequest request) {
        Investment investment = investmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found"));
        validateInvestmentDates(request.startDate(), request.endDate());
        investment.update(
                scaleMoney(request.principalAmount()),
                scaleRate(request.monthlyReturnRate()),
                request.startDate(),
                request.endDate(),
                parseInvestmentStatus(request.status()),
                normalizeNullable(request.notes())
        );
        return mapInvestment(investmentRepository.save(investment));
    }

    @Transactional
    public List<InvestorMonthlyReturnResponse> generateMonthlyReturns(GenerateMonthlyReturnsRequest request) {
        int year = request.year();
        int month = request.month();
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }
        MonthlyReturnDistributionMode distributionMode = parseDistributionMode(request.distributionMode());
        BigDecimal globalRate = null;
        BigDecimal distributableProfit = null;
        BigDecimal companyFund = null;
        BigDecimal companyProfit = null;
        BigDecimal returnPercentage = null;

        if (distributionMode == MonthlyReturnDistributionMode.RATE_BASED) {
            if (request.monthlyRate() != null && request.monthlyRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Monthly rate must be greater than zero");
            }
            globalRate = request.monthlyRate() == null ? null : scaleRate(request.monthlyRate());
        } else if (distributionMode == MonthlyReturnDistributionMode.PROFIT_POOL) {
            if (request.distributableProfit() == null || request.distributableProfit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Distributable profit must be greater than zero");
            }
            distributableProfit = scaleMoney(request.distributableProfit());
        } else if (distributionMode == MonthlyReturnDistributionMode.COMPANY_PROFIT) {
            if (request.companyProfit() == null || request.companyProfit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Company monthly profit must be greater than zero");
            }
            companyFund = request.companyFund() == null ? null : scaleMoney(request.companyFund());
            companyProfit = scaleMoney(request.companyProfit());
            if (request.returnPercentage() != null) {
                if (request.returnPercentage().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Return percentage must be greater than zero");
                }
                returnPercentage = scaleRate(request.returnPercentage());
            }
        }

        YearMonth period = YearMonth.of(year, month);
        List<Investment> base = request.investorId() == null
                ? investmentRepository.findByStatusOrderByCreatedAtDesc(InvestmentStatus.ACTIVE)
                : investmentRepository.findByInvestorAccount_IdOrderByStartDateDesc(request.investorId());

        List<Investment> eligible = base.stream()
                .filter(item -> item.getStatus() == InvestmentStatus.ACTIVE)
                .filter(item -> !item.getStartDate().isAfter(period.atEndOfMonth()))
                .filter(item -> item.getEndDate() == null || !item.getEndDate().isBefore(period.atDay(1)))
                .toList();

        // Allow multiple generation runs for same investor/month as requested by ops workflow.
        List<Investment> toCreate = eligible;
        if (toCreate.isEmpty()) {
            throw new IllegalArgumentException("No active investment found for selected investor/period");
        }

        if (distributionMode == MonthlyReturnDistributionMode.COMPANY_PROFIT) {
            if ((returnPercentage == null || returnPercentage.compareTo(BigDecimal.ZERO) <= 0)
                    && request.investorId() != null
                    && !toCreate.isEmpty()) {
                returnPercentage = scaleRate(toCreate.get(0).getMonthlyReturnRate());
            }
            if (returnPercentage == null || returnPercentage.compareTo(BigDecimal.ZERO) <= 0) {
                returnPercentage = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
            }
            if (companyFund == null || companyFund.compareTo(BigDecimal.ZERO) <= 0) {
                companyFund = toCreate.stream()
                        .map(Investment::getPrincipalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                companyFund = scaleMoney(companyFund);
            }
            if (companyFund.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Company fund must be greater than zero");
            }
            for (Investment investment : toCreate) {
                BigDecimal principal = scaleMoney(investment.getPrincipalAmount());
                BigDecimal shareRatio = principal.divide(companyFund, 8, RoundingMode.HALF_UP);
                BigDecimal attributableProfit = companyProfit.multiply(shareRatio);
                BigDecimal calculated = scaleMoney(
                        attributableProfit.multiply(returnPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
                BigDecimal effectiveRate = principal.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                        : scaleRate(calculated.multiply(BigDecimal.valueOf(100)).divide(principal, 2, RoundingMode.HALF_UP));

                InvestorMonthlyReturn monthlyReturn = new InvestorMonthlyReturn(
                        investment.getInvestorAccount(),
                        investment,
                        year,
                        month,
                        principal,
                        effectiveRate,
                        calculated
                );
                investorMonthlyReturnRepository.save(monthlyReturn);
            }
        } else if (distributionMode == MonthlyReturnDistributionMode.PROFIT_POOL) {
            BigDecimal totalPrincipal = toCreate.stream()
                    .map(Investment::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Eligible investments must have principal amount greater than zero");
            }

            BigDecimal allocatedAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            for (int index = 0; index < toCreate.size(); index++) {
                Investment investment = toCreate.get(index);
                BigDecimal principal = scaleMoney(investment.getPrincipalAmount());
                BigDecimal calculated = index == toCreate.size() - 1
                        ? distributableProfit.subtract(allocatedAmount)
                        : scaleMoney(
                        distributableProfit
                                .multiply(principal)
                                .divide(totalPrincipal, 2, RoundingMode.HALF_UP)
                );
                if (calculated.compareTo(BigDecimal.ZERO) < 0) {
                    calculated = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }
                if (index < toCreate.size() - 1) {
                    allocatedAmount = allocatedAmount.add(calculated);
                }

                BigDecimal effectiveRate = principal.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                        : scaleRate(calculated.multiply(BigDecimal.valueOf(100)).divide(principal, 2, RoundingMode.HALF_UP));

                InvestorMonthlyReturn monthlyReturn = new InvestorMonthlyReturn(
                        investment.getInvestorAccount(),
                        investment,
                        year,
                        month,
                        principal,
                        effectiveRate,
                        calculated
                );
                investorMonthlyReturnRepository.save(monthlyReturn);
            }
        } else {
            for (Investment investment : toCreate) {
                BigDecimal principal = scaleMoney(investment.getPrincipalAmount());
                BigDecimal effectiveRate = globalRate == null
                        ? scaleRate(investment.getMonthlyReturnRate())
                        : globalRate;

                BigDecimal calculated = scaleMoney(
                        principal.multiply(effectiveRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
                InvestorMonthlyReturn monthlyReturn = new InvestorMonthlyReturn(
                        investment.getInvestorAccount(),
                        investment,
                        year,
                        month,
                        principal,
                        effectiveRate,
                        calculated
                );
                investorMonthlyReturnRepository.save(monthlyReturn);
            }
        }

        return getMonthlyReturns(request.investorId(), year, month, null);
    }

    @Transactional(readOnly = true)
    public List<InvestorMonthlyReturnResponse> getMonthlyReturns(
            Long investorId,
            Integer year,
            Integer month,
            String status
    ) {
        InvestorMonthlyReturnStatus statusFilter = parseMonthlyReturnStatusNullable(status);
        List<InvestorMonthlyReturn> base = investorId != null
                ? investorMonthlyReturnRepository.findByInvestorAccount_IdOrderByPeriodYearDescPeriodMonthDescCreatedAtDesc(investorId)
                : investorMonthlyReturnRepository.findAll().stream()
                .sorted(Comparator.comparing(InvestorMonthlyReturn::getUpdatedAt).reversed())
                .toList();

        return base.stream()
                .filter(item -> year == null || item.getPeriodYear().equals(year))
                .filter(item -> month == null || item.getPeriodMonth().equals(month))
                .filter(item -> statusFilter == null || item.getStatus() == statusFilter)
                .map(this::mapMonthlyReturn)
                .toList();
    }

    @Transactional
    public InvestorMonthlyReturnResponse updateMonthlyReturn(Long id, UpdateInvestorMonthlyReturnRequest request) {
        InvestorMonthlyReturn monthlyReturn = getMonthlyReturnEntity(id);
        if (monthlyReturn.getStatus() == InvestorMonthlyReturnStatus.PAID) {
            throw new IllegalArgumentException("Paid return cannot be edited");
        }
        if (request.overrideAmount() != null && (request.overrideReason() == null || request.overrideReason().isBlank())) {
            throw new IllegalArgumentException("Override reason is required");
        }
        monthlyReturn.applyOverride(
                request.overrideAmount() == null ? null : scaleMoney(request.overrideAmount()),
                normalizeNullable(request.overrideReason()),
                normalizeNullable(request.notes())
        );
        return mapMonthlyReturn(investorMonthlyReturnRepository.save(monthlyReturn));
    }

    @Transactional
    public InvestorMonthlyReturnResponse submitMonthlyReturn(Long id, ReturnActionRequest request) {
        InvestorMonthlyReturn monthlyReturn = getMonthlyReturnEntity(id);
        if (monthlyReturn.getStatus() != InvestorMonthlyReturnStatus.DRAFT
                && monthlyReturn.getStatus() != InvestorMonthlyReturnStatus.REJECTED
                && monthlyReturn.getStatus() != InvestorMonthlyReturnStatus.HOLD) {
            throw new IllegalArgumentException("Only DRAFT/REJECTED/HOLD can be submitted");
        }
        monthlyReturn.submit(getCurrentUsername(), normalizeNullable(request.notes()));
        return mapMonthlyReturn(investorMonthlyReturnRepository.save(monthlyReturn));
    }

    @Transactional
    public InvestorMonthlyReturnResponse approveMonthlyReturn(Long id, ReturnActionRequest request) {
        InvestorMonthlyReturn monthlyReturn = getMonthlyReturnEntity(id);
        if (monthlyReturn.getStatus() != InvestorMonthlyReturnStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only SUBMITTED can be approved");
        }
        monthlyReturn.approve(getCurrentUsername(), normalizeNullable(request.notes()));
        return mapMonthlyReturn(investorMonthlyReturnRepository.save(monthlyReturn));
    }

    @Transactional
    public InvestorMonthlyReturnResponse rejectMonthlyReturn(Long id, ReturnActionRequest request) {
        InvestorMonthlyReturn monthlyReturn = getMonthlyReturnEntity(id);
        if (monthlyReturn.getStatus() == InvestorMonthlyReturnStatus.PAID) {
            throw new IllegalArgumentException("Paid return cannot be rejected");
        }
        monthlyReturn.reject(normalizeNullable(request.notes()));
        return mapMonthlyReturn(investorMonthlyReturnRepository.save(monthlyReturn));
    }

    @Transactional
    public InvestorMonthlyReturnResponse holdMonthlyReturn(Long id, ReturnActionRequest request) {
        InvestorMonthlyReturn monthlyReturn = getMonthlyReturnEntity(id);
        if (monthlyReturn.getStatus() == InvestorMonthlyReturnStatus.PAID) {
            throw new IllegalArgumentException("Paid return cannot be put on hold");
        }
        monthlyReturn.hold(normalizeNullable(request.notes()));
        return mapMonthlyReturn(investorMonthlyReturnRepository.save(monthlyReturn));
    }

    @Transactional(readOnly = true)
    public List<InvestorPayoutResponse> getPayouts(Long investorId, String status) {
        InvestorPayoutStatus statusFilter = parsePayoutStatusNullable(status);
        List<InvestorPayout> base = investorId == null
                ? investorPayoutRepository.findAll().stream()
                .sorted(Comparator.comparing(InvestorPayout::getCreatedAt).reversed())
                .toList()
                : investorPayoutRepository.findByInvestorAccount_IdOrderByCreatedAtDesc(investorId);

        return base.stream()
                .filter(item -> statusFilter == null || item.getStatus() == statusFilter)
                .map(this::mapPayout)
                .toList();
    }

    @Transactional
    public InvestorPayoutResponse createPayoutRequest(CreateInvestorPayoutRequest request) {
        InvestorAccount investor = getInvestorEntity(request.investorId());
        Set<Long> uniqueIds = new LinkedHashSet<>(request.monthlyReturnIds());
        List<InvestorMonthlyReturn> returns = investorMonthlyReturnRepository.findAllById(uniqueIds);
        if (returns.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Invalid return selection");
        }
        for (InvestorMonthlyReturn item : returns) {
            if (!item.getInvestorAccount().getId().equals(investor.getId())) {
                throw new IllegalArgumentException("Return list has records from another investor");
            }
            if (item.getStatus() != InvestorMonthlyReturnStatus.APPROVED) {
                throw new IllegalArgumentException("Only APPROVED returns can be used");
            }
            if (item.getPayout() != null) {
                throw new IllegalArgumentException("One or more returns already linked to payout");
            }
        }

        BigDecimal totalAmount = returns.stream()
                .map(InvestorMonthlyReturn::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        InvestorPayout payout = new InvestorPayout(
                investor,
                generateUniquePayoutReference(),
                scaleMoney(totalAmount),
                InvestorPayoutStatus.PENDING_APPROVAL,
                normalizeNullable(request.notes())
        );
        InvestorPayout saved = investorPayoutRepository.save(payout);

        for (InvestorMonthlyReturn item : returns) {
            item.attachPayout(saved);
        }
        investorMonthlyReturnRepository.saveAll(returns);
        return mapPayout(saved);
    }

    @Transactional
    public InvestorPayoutResponse approvePayout(Long id, PayoutActionRequest request) {
        InvestorPayout payout = getPayoutEntity(id);
        if (payout.getStatus() != InvestorPayoutStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Only pending payouts can be approved");
        }
        payout.approve(getCurrentUsername(), normalizeNullable(request.notes()));
        return mapPayout(investorPayoutRepository.save(payout));
    }

    @Transactional
    public InvestorPayoutResponse rejectPayout(Long id, PayoutActionRequest request) {
        InvestorPayout payout = getPayoutEntity(id);
        if (payout.getStatus() == InvestorPayoutStatus.PAID) {
            throw new IllegalArgumentException("Paid payouts cannot be rejected");
        }
        payout.reject(normalizeNullable(request.notes()));
        InvestorPayout saved = investorPayoutRepository.save(payout);

        List<InvestorMonthlyReturn> returns = investorMonthlyReturnRepository.findByPayout_IdOrderByCreatedAtAsc(saved.getId());
        for (InvestorMonthlyReturn item : returns) {
            item.detachPayout();
        }
        investorMonthlyReturnRepository.saveAll(returns);
        return mapPayout(saved);
    }

    @Transactional
    public InvestorPayoutResponse markPayoutPaid(Long id, MarkInvestorPayoutPaidRequest request) {
        InvestorPayout payout = getPayoutEntity(id);
        if (payout.getStatus() != InvestorPayoutStatus.APPROVED
                && payout.getStatus() != InvestorPayoutStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Only approved/pending payouts can be paid");
        }
        payout.markPaid(
                request.paymentChannel().trim(),
                request.transactionReference().trim(),
                request.paidAt(),
                normalizeNullable(request.notes())
        );
        InvestorPayout saved = investorPayoutRepository.save(payout);

        List<InvestorMonthlyReturn> returns = investorMonthlyReturnRepository.findByPayout_IdOrderByCreatedAtAsc(saved.getId());
        for (InvestorMonthlyReturn item : returns) {
            item.markPaid(saved);
        }
        investorMonthlyReturnRepository.saveAll(returns);

        investorReceiptRepository.findByPayout_Id(saved.getId()).orElseGet(() -> {
            String receiptNumber = generateUniqueReceiptNumber();
            String url = "/api/admin/investor-platform/receipts/number/" + receiptNumber + "/download";
            return investorReceiptRepository.save(new InvestorReceipt(saved, receiptNumber, url, 1, getCurrentUsername()));
        });

        return mapPayout(saved);
    }

    @Transactional
    public InvestorReceiptResponse generateReceipt(Long payoutId) {
        InvestorPayout payout = getPayoutEntity(payoutId);
        if (payout.getStatus() == InvestorPayoutStatus.REJECTED || payout.getStatus() == InvestorPayoutStatus.FAILED) {
            throw new IllegalArgumentException("Cannot generate receipt for rejected/failed payout");
        }

        InvestorReceipt existing = investorReceiptRepository.findByPayout_Id(payoutId).orElse(null);
        if (existing != null) {
            return mapReceipt(existing);
        }

        String receiptNumber = generateUniqueReceiptNumber();
        String url = "/api/admin/investor-platform/receipts/number/" + receiptNumber + "/download";
        InvestorReceipt created = investorReceiptRepository.save(
                new InvestorReceipt(payout, receiptNumber, url, 1, getCurrentUsername())
        );
        return mapReceipt(created);
    }

    @Transactional(readOnly = true)
    public List<InvestorReceiptResponse> getReceipts(Long investorId) {
        List<InvestorReceipt> base = investorId == null
                ? investorReceiptRepository.findAll().stream()
                .sorted(Comparator.comparing(InvestorReceipt::getGeneratedAt).reversed())
                .toList()
                : investorReceiptRepository.findByPayout_InvestorAccount_IdOrderByGeneratedAtDesc(investorId);
        return base.stream().map(this::mapReceipt).toList();
    }

    @Transactional(readOnly = true)
    public InvestorReceiptResponse getReceipt(Long id) {
        InvestorReceipt receipt = investorReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        return mapReceipt(receipt);
    }

    @Transactional(readOnly = true)
    public InvestorReceiptResponse getReceiptByNumber(String receiptNumber) {
        InvestorReceipt receipt = investorReceiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        return mapReceipt(receipt);
    }

    @Transactional(readOnly = true)
    public String buildReceiptText(String receiptNumber) {
        InvestorReceipt receipt = investorReceiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        InvestorPayout payout = receipt.getPayout();
        InvestorAccount investor = payout.getInvestorAccount();

        return String.join("\n",
                "FVP Purepick - Investor Payout Receipt",
                "Receipt Number: " + receipt.getReceiptNumber(),
                "Generated At: " + receipt.getGeneratedAt(),
                "Investor Code: " + investor.getInvestorCode(),
                "Investor Name: " + investor.getFullName(),
                "Payout Reference: " + payout.getPayoutReference(),
                "Payout Amount (INR): " + payout.getTotalAmount(),
                "Status: " + payout.getStatus().name(),
                "Payment Channel: " + (payout.getPaymentChannel() == null ? "-" : payout.getPaymentChannel()),
                "Transaction Reference: " + (payout.getTransactionReference() == null ? "-" : payout.getTransactionReference()),
                "Paid At: " + (payout.getPaidAt() == null ? "-" : payout.getPaidAt().toString())
        );
    }

    @Transactional(readOnly = true)
    public byte[] buildReceiptPdf(String receiptNumber) {
        InvestorReceipt receipt = investorReceiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));
        InvestorPayout payout = receipt.getPayout();
        InvestorAccount investor = payout.getInvestorAccount();

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float margin = 36f;
                float pageWidth = page.getMediaBox().getWidth() - (margin * 2);
                float y = page.getMediaBox().getHeight() - margin;

                stream.setNonStrokingColor(22, 101, 52);
                stream.addRect(margin, y - 58, pageWidth, 58);
                stream.fill();

                writePdfText(stream, "FVP Purepick Suppliers OPC Pvt. Ltd.", margin + 12, y - 23, PDType1Font.HELVETICA_BOLD, 14, true);
                writePdfText(stream, "Investor Payout Receipt", margin + 12, y - 41, PDType1Font.HELVETICA, 11, true);

                y -= 76;
                float columnGap = 12f;
                float columnWidth = (pageWidth - columnGap) / 2f;
                float blockHeight = 124f;

                stream.setNonStrokingColor(245, 250, 247);
                stream.addRect(margin, y - blockHeight, columnWidth, blockHeight);
                stream.fill();
                stream.addRect(margin + columnWidth + columnGap, y - blockHeight, columnWidth, blockHeight);
                stream.fill();

                stream.setStrokingColor(206, 220, 211);
                stream.addRect(margin, y - blockHeight, columnWidth, blockHeight);
                stream.stroke();
                stream.addRect(margin + columnWidth + columnGap, y - blockHeight, columnWidth, blockHeight);
                stream.stroke();

                float leftX = margin + 10;
                float rightX = margin + columnWidth + columnGap + 10;
                float leftY = y - 18;
                float rightY = y - 18;

                writePdfText(stream, "Investor Details", leftX, leftY, PDType1Font.HELVETICA_BOLD, 11, false);
                leftY -= 16;
                writePdfText(stream, "Name: " + safePdfText(investor.getFullName()), leftX, leftY, PDType1Font.HELVETICA, 10, false);
                leftY -= 14;
                writePdfText(stream, "Investor Code: " + safePdfText(investor.getInvestorCode()), leftX, leftY, PDType1Font.HELVETICA, 10, false);
                leftY -= 14;
                writePdfText(stream, "Email: " + safePdfText(investor.getEmail()), leftX, leftY, PDType1Font.HELVETICA, 10, false);
                leftY -= 14;
                writePdfText(stream, "Phone: " + safePdfText(investor.getPhone()), leftX, leftY, PDType1Font.HELVETICA, 10, false);

                writePdfText(stream, "Receipt Details", rightX, rightY, PDType1Font.HELVETICA_BOLD, 11, false);
                rightY -= 16;
                writePdfText(stream, "Receipt No: " + safePdfText(receipt.getReceiptNumber()), rightX, rightY, PDType1Font.HELVETICA, 10, false);
                rightY -= 14;
                writePdfText(stream, "Payout Ref: " + safePdfText(payout.getPayoutReference()), rightX, rightY, PDType1Font.HELVETICA, 10, false);
                rightY -= 14;
                writePdfText(stream, "Generated At: " + safePdfText(receipt.getGeneratedAt().toString()), rightX, rightY, PDType1Font.HELVETICA, 10, false);
                rightY -= 14;
                writePdfText(stream, "Status: " + safePdfText(payout.getStatus().name()), rightX, rightY, PDType1Font.HELVETICA, 10, false);

                y -= (blockHeight + 18);
                float summaryHeight = 108f;
                stream.setNonStrokingColor(255, 255, 255);
                stream.addRect(margin, y - summaryHeight, pageWidth, summaryHeight);
                stream.fill();
                stream.setStrokingColor(206, 220, 211);
                stream.addRect(margin, y - summaryHeight, pageWidth, summaryHeight);
                stream.stroke();

                float summaryX = margin + 10;
                float summaryY = y - 18;
                writePdfText(stream, "Payout Summary", summaryX, summaryY, PDType1Font.HELVETICA_BOLD, 11, false);
                summaryY -= 18;
                writePdfText(stream, "Total Amount (INR): " + scaleMoney(payout.getTotalAmount()), summaryX, summaryY, PDType1Font.HELVETICA_BOLD, 11, false);
                summaryY -= 16;
                writePdfText(stream, "Payment Channel: " + safePdfText(payout.getPaymentChannel()), summaryX, summaryY, PDType1Font.HELVETICA, 10, false);
                summaryY -= 14;
                writePdfText(stream, "Transaction Ref: " + safePdfText(payout.getTransactionReference()), summaryX, summaryY, PDType1Font.HELVETICA, 10, false);
                summaryY -= 14;
                writePdfText(stream, "Paid At: " + safePdfText(payout.getPaidAt() == null ? "-" : payout.getPaidAt().toString()), summaryX, summaryY, PDType1Font.HELVETICA, 10, false);

                y -= (summaryHeight + 22);
                writePdfText(stream, "This is a system-generated receipt for investor payout records.", margin, y, PDType1Font.HELVETICA_OBLIQUE, 9, false);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate receipt PDF", ex);
        }
    }

    private void writePdfText(
            PDPageContentStream stream,
            String text,
            float x,
            float y,
            PDType1Font font,
            float size,
            boolean white
    ) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        if (white) {
            stream.setNonStrokingColor(255, 255, 255);
        } else {
            stream.setNonStrokingColor(15, 23, 42);
        }
        stream.newLineAtOffset(x, y);
        stream.showText(safePdfText(text));
        stream.endText();
    }

    private String safePdfText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.replaceAll("[^\\x20-\\x7E]", " ").trim();
    }

    private InvestorAccountResponse mapInvestor(InvestorAccount account, InvestorSummaryMaps summaryMaps) {
        BigDecimal totalInvested = summaryMaps.totalInvestedByInvestorId().getOrDefault(account.getId(), BigDecimal.ZERO);
        BigDecimal totalReturnsReceived = summaryMaps.totalReturnsReceivedByInvestorId().getOrDefault(account.getId(), BigDecimal.ZERO);
        BigDecimal pendingPayout = summaryMaps.pendingPayoutByInvestorId().getOrDefault(account.getId(), BigDecimal.ZERO);

        return new InvestorAccountResponse(
                account.getId(),
                account.getInvestorCode(),
                account.getFullName(),
                account.getEmail(),
                account.getPhone(),
                account.getSourceInquiryId(),
                account.getStatus().name(),
                account.getVerificationStatus().name(),
                account.getNotes(),
                scaleMoney(totalInvested),
                scaleMoney(totalReturnsReceived),
                scaleMoney(pendingPayout),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private InvestorSummaryMaps loadInvestorSummaryMaps() {
        Map<Long, BigDecimal> investedMap = toAggregateMap(investmentRepository.sumPrincipalByInvestorId());
        Map<Long, BigDecimal> paidReturnsMap = toAggregateMap(
                investorMonthlyReturnRepository.sumFinalAmountByInvestorIdForStatus(InvestorMonthlyReturnStatus.PAID)
        );
        Map<Long, BigDecimal> pendingPayoutMap = toAggregateMap(
                investorPayoutRepository.sumTotalAmountByInvestorIdForStatuses(
                        List.of(InvestorPayoutStatus.PENDING_APPROVAL, InvestorPayoutStatus.APPROVED)
                )
        );

        return new InvestorSummaryMaps(investedMap, paidReturnsMap, pendingPayoutMap);
    }

    private Map<Long, BigDecimal> toAggregateMap(List<InvestorAmountAggregate> aggregates) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (InvestorAmountAggregate aggregate : aggregates) {
            Long investorId = aggregate.getInvestorId();
            if (investorId == null) {
                continue;
            }
            map.put(investorId, scaleMoney(aggregate.getTotal()));
        }
        return map;
    }

    private InvestmentResponse mapInvestment(Investment investment) {
        InvestorAccount investor = investment.getInvestorAccount();
        return new InvestmentResponse(
                investment.getId(),
                investor.getId(),
                investor.getInvestorCode(),
                investor.getFullName(),
                investment.getInvestmentReference(),
                scaleMoney(investment.getPrincipalAmount()),
                scaleRate(investment.getMonthlyReturnRate()),
                investment.getStartDate(),
                investment.getEndDate(),
                investment.getStatus().name(),
                investment.getNotes(),
                investment.getCreatedAt(),
                investment.getUpdatedAt()
        );
    }

    private InvestorMonthlyReturnResponse mapMonthlyReturn(InvestorMonthlyReturn monthlyReturn) {
        InvestorPayout payout = monthlyReturn.getPayout();
        return new InvestorMonthlyReturnResponse(
                monthlyReturn.getId(),
                monthlyReturn.getInvestorAccount().getId(),
                monthlyReturn.getInvestorAccount().getInvestorCode(),
                monthlyReturn.getInvestorAccount().getFullName(),
                monthlyReturn.getInvestment().getId(),
                monthlyReturn.getInvestment().getInvestmentReference(),
                monthlyReturn.getPeriodYear(),
                monthlyReturn.getPeriodMonth(),
                scaleMoney(monthlyReturn.getBasePrincipal()),
                scaleRate(monthlyReturn.getReturnRate()),
                scaleMoney(monthlyReturn.getCalculatedAmount()),
                monthlyReturn.getOverrideAmount() == null ? null : scaleMoney(monthlyReturn.getOverrideAmount()),
                scaleMoney(monthlyReturn.getFinalAmount()),
                monthlyReturn.getOverrideReason(),
                monthlyReturn.getStatus().name(),
                payout == null ? null : payout.getId(),
                payout == null ? null : payout.getPayoutReference(),
                monthlyReturn.getSubmittedBy(),
                monthlyReturn.getSubmittedAt(),
                monthlyReturn.getApprovedBy(),
                monthlyReturn.getApprovedAt(),
                monthlyReturn.getNotes(),
                monthlyReturn.getCreatedAt(),
                monthlyReturn.getUpdatedAt()
        );
    }

    private InvestorPayoutResponse mapPayout(InvestorPayout payout) {
        List<InvestorMonthlyReturn> returns = investorMonthlyReturnRepository.findByPayout_IdOrderByCreatedAtAsc(payout.getId());
        InvestorReceipt receipt = investorReceiptRepository.findByPayout_Id(payout.getId()).orElse(null);

        return new InvestorPayoutResponse(
                payout.getId(),
                payout.getInvestorAccount().getId(),
                payout.getInvestorAccount().getInvestorCode(),
                payout.getInvestorAccount().getFullName(),
                payout.getPayoutReference(),
                scaleMoney(payout.getTotalAmount()),
                payout.getStatus().name(),
                payout.getPaymentChannel(),
                payout.getTransactionReference(),
                payout.getNotes(),
                payout.getApprovedBy(),
                payout.getApprovedAt(),
                payout.getPaidAt(),
                receipt == null ? null : receipt.getId(),
                receipt == null ? null : receipt.getReceiptNumber(),
                payout.getCreatedAt(),
                payout.getUpdatedAt(),
                returns.stream().map(InvestorMonthlyReturn::getId).toList()
        );
    }

    private InvestorReceiptResponse mapReceipt(InvestorReceipt receipt) {
        InvestorPayout payout = receipt.getPayout();
        InvestorAccount investor = payout.getInvestorAccount();
        return new InvestorReceiptResponse(
                receipt.getId(),
                payout.getId(),
                payout.getPayoutReference(),
                investor.getId(),
                investor.getInvestorCode(),
                investor.getFullName(),
                scaleMoney(payout.getTotalAmount()),
                receipt.getReceiptNumber(),
                receipt.getDocumentUrl(),
                receipt.getVersion(),
                receipt.getGeneratedBy(),
                receipt.getGeneratedAt()
        );
    }

    private InvestorAccount getInvestorEntity(Long id) {
        return investorAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Investor not found"));
    }

    private InvestorMonthlyReturn getMonthlyReturnEntity(Long id) {
        return investorMonthlyReturnRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monthly return not found"));
    }

    private InvestorPayout getPayoutEntity(Long id) {
        return investorPayoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleRate(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal normalized = value;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            normalized = BigDecimal.ZERO;
        }
        if (normalized.compareTo(BigDecimal.valueOf(100)) > 0) {
            normalized = BigDecimal.valueOf(100);
        }
        return normalized.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private void validateInvestmentDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private void validateProfileInvestmentPayload(InvestorProfileUpsertRequest request) {
        if (!hasInvestmentPayload(request) && request.investmentId() == null) {
            return;
        }
        if (request.principalAmount() == null || request.principalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal amount is required for investment section");
        }
        if (request.monthlyReturnRate() == null || request.monthlyReturnRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly return rate is required for investment section");
        }
        if (request.startDate() == null) {
            throw new IllegalArgumentException("Start date is required for investment section");
        }
        validateInvestmentDates(request.startDate(), request.endDate());
    }

    private boolean hasInvestmentPayload(InvestorProfileUpsertRequest request) {
        return request.principalAmount() != null
                || request.monthlyReturnRate() != null
                || request.startDate() != null
                || request.endDate() != null
                || (request.investmentStatus() != null && !request.investmentStatus().isBlank())
                || (request.investmentNotes() != null && !request.investmentNotes().isBlank());
    }

    private String normalizeInvestmentStatus(String value) {
        if (value == null || value.isBlank()) {
            return InvestmentStatus.ACTIVE.name();
        }
        return value.trim();
    }

    private InvestorAccountStatus parseInvestorStatus(String value) {
        try {
            return InvestorAccountStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid investor status");
        }
    }

    private InvestorAccountStatus parseInvestorStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseInvestorStatus(value);
    }

    private VerificationStatus parseVerificationStatus(String value) {
        try {
            return VerificationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid verification status");
        }
    }

    private VerificationStatus parseVerificationStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseVerificationStatus(value);
    }

    private InvestmentStatus parseInvestmentStatus(String value) {
        try {
            return InvestmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid investment status");
        }
    }

    private InvestmentStatus parseInvestmentStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseInvestmentStatus(value);
    }

    private MonthlyReturnDistributionMode parseDistributionMode(String value) {
        if (value == null || value.isBlank()) {
            return MonthlyReturnDistributionMode.RATE_BASED;
        }
        try {
            return MonthlyReturnDistributionMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid distribution mode");
        }
    }

    private InvestorMonthlyReturnStatus parseMonthlyReturnStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return InvestorMonthlyReturnStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid monthly return status");
        }
    }

    private InvestorPayoutStatus parsePayoutStatusNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return InvestorPayoutStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid payout status");
        }
    }

    private String generateUniqueInvestorCode() {
        String code;
        do {
            code = generateRawCode("INV");
        } while (investorAccountRepository.existsByInvestorCode(code));
        return code;
    }

    private String generateUniqueInvestmentReference() {
        String code;
        do {
            code = generateRawCode("INS");
        } while (investmentRepository.existsByInvestmentReference(code));
        return code;
    }

    private String generateUniquePayoutReference() {
        String code;
        do {
            code = generateRawCode("PAY");
        } while (investorPayoutRepository.existsByPayoutReference(code));
        return code;
    }

    private String generateUniqueReceiptNumber() {
        String code;
        do {
            code = generateRawCode("RCP");
        } while (investorReceiptRepository.existsByReceiptNumber(code));
        return code;
    }

    private String generateRawCode(String prefix) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT));
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return prefix + "-" + datePrefix + "-" + suffix;
    }

    private record InvestorSummaryMaps(
            Map<Long, BigDecimal> totalInvestedByInvestorId,
            Map<Long, BigDecimal> totalReturnsReceivedByInvestorId,
            Map<Long, BigDecimal> pendingPayoutByInvestorId
    ) {
    }
}

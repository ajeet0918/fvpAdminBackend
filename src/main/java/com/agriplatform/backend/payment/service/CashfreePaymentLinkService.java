package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.inquiry.model.Inquiry;
import com.agriplatform.backend.investor.service.InvestorOnboardingSettings;
import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreePaymentLinkResult;
import com.cashfree.pg.ApiException;
import com.cashfree.pg.ApiResponse;
import com.cashfree.pg.Cashfree;
import com.cashfree.pg.model.CreateLinkRequest;
import com.cashfree.pg.model.LinkCustomerDetailsEntity;
import com.cashfree.pg.model.LinkEntity;
import com.cashfree.pg.model.LinkMetaResponseEntity;
import com.cashfree.pg.model.LinkNotifyEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CashfreePaymentLinkService {
    private final CashfreeSettingsResolver cashfreeSettingsResolver;
    private final CashfreeClientFactory cashfreeClientFactory;

    public CashfreePaymentLinkService(
            CashfreeSettingsResolver cashfreeSettingsResolver,
            CashfreeClientFactory cashfreeClientFactory
    ) {
        this.cashfreeSettingsResolver = cashfreeSettingsResolver;
        this.cashfreeClientFactory = cashfreeClientFactory;
    }

    public CashfreePaymentLinkResult createInvestorPaymentLink(
            String merchantLinkId,
            Inquiry inquiry,
            BigDecimal amount,
            InvestorOnboardingSettings.Snapshot settings
    ) {
        CashfreeRuntimeConfig config = cashfreeSettingsResolver.resolve();
        if (!config.enabled() || !config.hasCredentials()) {
            throw new IllegalArgumentException("Cashfree gateway is disabled or not configured");
        }

        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(settings.paymentLinkExpiryDays());
        CreateLinkRequest request = new CreateLinkRequest()
                .linkId(merchantLinkId)
                .linkAmount(amount)
                .linkCurrency(CashfreeApiConstants.CURRENCY_INR)
                .linkPurpose("Investor contribution " + inquiry.getReferenceId())
                .customerDetails(new LinkCustomerDetailsEntity()
                        .customerName(inquiry.getFullName())
                        .customerEmail(inquiry.getEmail())
                        .customerPhone(inquiry.getPhone()))
                .linkPartialPayments(false)
                .linkExpiryTime(expiresAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .linkAutoReminders(false)
                .linkNotify(new LinkNotifyEntity().sendEmail(false).sendSms(false))
                .linkNotes(Map.of(
                        "inquiry_id", String.valueOf(inquiry.getId()),
                        "inquiry_reference", inquiry.getReferenceId()
                ))
                .linkMeta(new LinkMetaResponseEntity()
                        .notifyUrl(settings.webhookUrl())
                        .returnUrl(settings.paymentReturnUrl()));

        Cashfree cashfree = cashfreeClientFactory.create(config);
        try {
            ApiResponse<LinkEntity> response = cashfree.PGCreateLink(
                    request,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            return map(requireEntity(response.getData()), expiresAt.toLocalDateTime());
        } catch (ApiException ex) {
            if (ex.getCode() == 409) {
                return fetchExisting(cashfree, merchantLinkId, expiresAt.toLocalDateTime());
            }
            throw gatewayFailure("Cashfree investor payment-link creation failed", ex);
        }
    }

    private CashfreePaymentLinkResult fetchExisting(
            Cashfree cashfree,
            String merchantLinkId,
            LocalDateTime fallbackExpiry
    ) {
        try {
            ApiResponse<LinkEntity> response = cashfree.PGFetchLink(
                    merchantLinkId,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID(),
                    null
            );
            return map(requireEntity(response.getData()), fallbackExpiry);
        } catch (ApiException ex) {
            throw gatewayFailure("Cashfree existing investor payment-link lookup failed", ex);
        }
    }

    private CashfreePaymentLinkResult map(LinkEntity entity, LocalDateTime fallbackExpiry) {
        if (!hasText(entity.getLinkId()) || !hasText(entity.getLinkUrl())) {
            throw new IllegalArgumentException("Cashfree payment-link response is incomplete");
        }
        return new CashfreePaymentLinkResult(
                entity.getLinkId(),
                entity.getCfLinkId(),
                entity.getLinkUrl(),
                entity.getLinkStatus(),
                entity.getLinkAmount(),
                entity.getLinkAmountPaid(),
                parseDateTime(entity.getLinkExpiryTime(), fallbackExpiry)
        );
    }

    private LinkEntity requireEntity(LinkEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cashfree payment-link response is empty");
        }
        return entity;
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private IllegalArgumentException gatewayFailure(String message, ApiException ex) {
        String responseBody = ex.getResponseBody();
        String detail = hasText(responseBody) ? responseBody : ex.getMessage();
        return new IllegalArgumentException(message + ": " + detail, ex);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

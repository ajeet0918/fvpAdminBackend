package com.agriplatform.backend.investor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.agriplatform.backend.payment.dto.CashfreePaymentLinkWebhookPayload;
import com.agriplatform.backend.payment.service.CashfreeClientFactory;
import com.agriplatform.backend.payment.service.CashfreeSettingsResolver;
import com.cashfree.pg.Cashfree;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestorPaymentWebhookServiceTest {
    @Mock
    private CashfreeSettingsResolver cashfreeSettingsResolver;

    @Mock
    private CashfreeClientFactory cashfreeClientFactory;

    @Mock
    private Cashfree cashfree;

    @Mock
    private InvestorOnboardingService investorOnboardingService;

    @Test
    void parsesTypedPaymentLinkPayloadAndCreatesStableEventKey() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        CashfreeRuntimeConfig config = new CashfreeRuntimeConfig(
                true, "2023-08-01", "client-id", "client-secret", true
        );
        CashfreePaymentLinkWebhookPayload expected = new CashfreePaymentLinkWebhookPayload(
                "PAYMENT_LINK_EVENT",
                "1",
                "2026-08-08T12:00:00+05:30",
                new CashfreePaymentLinkWebhookPayload.Data(
                        "1576977",
                        "INVESTOR-42-INV-42",
                        "PAID",
                        "INR",
                        new BigDecimal("50000.00"),
                        new BigDecimal("50000.00"),
                        new CashfreePaymentLinkWebhookPayload.Order(
                                "CFPay_123", "1021206", "SUCCESS"
                        )
                )
        );
        String rawPayload = objectMapper.writeValueAsString(expected);
        when(cashfreeSettingsResolver.resolve()).thenReturn(config);
        when(cashfreeClientFactory.create(config)).thenReturn(cashfree);
        InvestorPaymentWebhookService service = new InvestorPaymentWebhookService(
                cashfreeSettingsResolver,
                cashfreeClientFactory,
                objectMapper,
                investorOnboardingService
        );

        service.process("signature", "timestamp", "cashfree-event-1", rawPayload);

        ArgumentCaptor<CashfreePaymentLinkWebhookPayload> payloadCaptor =
                ArgumentCaptor.forClass(CashfreePaymentLinkWebhookPayload.class);
        ArgumentCaptor<String> eventKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(investorOnboardingService).processWebhook(
                payloadCaptor.capture(), eventKeyCaptor.capture(), org.mockito.ArgumentMatchers.eq(rawPayload)
        );
        assertThat(payloadCaptor.getValue()).isEqualTo(expected);
        assertThat(eventKeyCaptor.getValue()).hasSize(64).matches("[0-9a-f]{64}");
        verify(cashfree).PGVerifyWebhookSignature("signature", rawPayload, "timestamp");
    }

    @Test
    void rejectsEmptyRawPayloadBeforeProcessing() {
        InvestorPaymentWebhookService service = new InvestorPaymentWebhookService(
                cashfreeSettingsResolver,
                cashfreeClientFactory,
                new ObjectMapper(),
                investorOnboardingService
        );

        assertThatThrownBy(() -> service.process(null, null, null, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }
}

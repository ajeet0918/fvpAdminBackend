package com.agriplatform.backend.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CashfreePaymentLinkWebhookPayload(
        String type,
        String version,
        @JsonProperty("event_time") String eventTime,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            @JsonProperty("cf_link_id") String providerLinkId,
            @JsonProperty("link_id") String merchantLinkId,
            @JsonProperty("link_status") String linkStatus,
            @JsonProperty("link_currency") String currency,
            @JsonProperty("link_amount") BigDecimal amount,
            @JsonProperty("link_amount_paid") BigDecimal amountPaid,
            Order order
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Order(
            @JsonProperty("order_id") String orderId,
            @JsonProperty("transaction_id") String transactionId,
            @JsonProperty("transaction_status") String transactionStatus
    ) {
    }
}

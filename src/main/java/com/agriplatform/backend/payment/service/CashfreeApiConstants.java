package com.agriplatform.backend.payment.service;

import java.util.Set;

public final class CashfreeApiConstants {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CashfreeApiConstants.class);

    private CashfreeApiConstants() {
    }

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CLIENT_ID = "x-client-id";
    public static final String HEADER_CLIENT_SECRET = "x-client-secret";
    public static final String HEADER_API_VERSION = "x-api-version";
    public static final String HEADER_REQUEST_ID = "x-request-id";

    public static final String HEADER_WEBHOOK_SIGNATURE = "x-webhook-signature";
    public static final String HEADER_WEBHOOK_TIMESTAMP = "x-webhook-timestamp";

    public static final String API_ORDERS_PATH = "/orders";
    public static final String DEFAULT_API_VERSION = "2023-08-01";
    public static final String CURRENCY_INR = "INR";
    public static final String GATEWAY_NAME = "CASHFREE";
    public static final String DEFAULT_EVENT_TYPE = "cashfree.webhook";

    public static final String FIELD_CF_ORDER_ID = "cf_order_id";
    public static final String FIELD_PAYMENT_SESSION_ID = "payment_session_id";
    public static final String FIELD_PAYMENT_LINK = "payment_link";
    public static final String FIELD_ORDER_STATUS = "order_status";

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_EVENT_TYPE = "event_type";
    public static final String FIELD_CF_PAYMENT_ID = "cf_payment_id";
    public static final String FIELD_PAYMENT_ID = "payment_id";

    public static final Set<String> SUCCESS_STATUSES = Set.of("PAID", "SUCCESS");
    public static final Set<String> FAILURE_STATUSES = Set.of("FAILED", "CANCELLED", "USER_DROPPED");

    public static final String[] MERCHANT_ORDER_ID_PATHS = {
            "data.order.order_id",
            "order.order_id",
            "order_id"
    };

    public static final String[] PROVIDER_ORDER_ID_PATHS = {
            "data.order.cf_order_id",
            "order.cf_order_id",
            "cf_order_id"
    };

    public static final String[] PAYMENT_STATUS_PATHS = {
            "data.payment.payment_status",
            "payment.payment_status",
            "payment_status",
            "data.order.order_status",
            "order.order_status",
            "order_status"
    };

    public static final String[] EVENT_TYPE_PATHS = {
            "type",
            "event_type"
    };

    public static final String[] PAYMENT_REFERENCE_PATHS = {
            "data.payment.cf_payment_id",
            "payment.cf_payment_id",
            "cf_payment_id",
            "data.payment.payment_id",
            "payment.payment_id"
    };
}

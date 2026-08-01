package com.agriplatform.backend.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.cashfree.pg.Cashfree;
import org.junit.jupiter.api.Test;

class CashfreeClientFactoryTest {

    private final CashfreeClientFactory cashfreeClientFactory = new CashfreeClientFactory();

    @Test
    void createMapsMerchantCredentialsAndApiVersionToSdkFields() {
        CashfreeRuntimeConfig config = new CashfreeRuntimeConfig(
                true,
                "2023-08-01",
                "merchant-client-id",
                "merchant-client-secret",
                true
        );

        Cashfree cashfree = cashfreeClientFactory.create(config);

        assertThat(cashfree.XEnvironment).isEqualTo(Cashfree.CFEnvironment.PRODUCTION);
        assertThat(cashfree.XClientId).isEqualTo("merchant-client-id");
        assertThat(cashfree.XClientSecret).isEqualTo("merchant-client-secret");
        assertThat(cashfree.XPartnerAPIKey).isEmpty();
        assertThat(cashfree.XApiVersion).isEqualTo("2023-08-01");
    }
}

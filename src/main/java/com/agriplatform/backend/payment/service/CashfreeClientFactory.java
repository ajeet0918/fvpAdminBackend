package com.agriplatform.backend.payment.service;

import com.agriplatform.backend.payment.config.CashfreeRuntimeConfig;
import com.cashfree.pg.Cashfree;
import org.springframework.stereotype.Component;

@Component
public class CashfreeClientFactory {

    public Cashfree create(CashfreeRuntimeConfig config) {
        Cashfree cashfree = new Cashfree(
                Cashfree.CFEnvironment.PRODUCTION,
                config.clientId(),
                config.clientSecret(),
                null,
                null,
                null
        );
        cashfree.XApiVersion = config.apiVersion();
        return cashfree;
    }
}

package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.portal.service.PortalAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class InvestorPostPaymentService {
    private static final Logger log = LoggerFactory.getLogger(InvestorPostPaymentService.class);

    private final PortalAccountService portalAccountService;
    private final InvestorDeliveryStateService deliveryStateService;

    public InvestorPostPaymentService(
            PortalAccountService portalAccountService,
            InvestorDeliveryStateService deliveryStateService
    ) {
        this.portalAccountService = portalAccountService;
        this.deliveryStateService = deliveryStateService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPortalInvite(InvestorPaymentCompletedEvent event) {
        sendPortalInviteNow(event);
    }

    public void sendPortalInviteNow(InvestorPaymentCompletedEvent event) {
        try {
            portalAccountService.createOrResendInvite(event.inquiryId());
            deliveryStateService.markPortalInviteSent(event.paymentId());
        } catch (RuntimeException ex) {
            log.error("Unable to send investor portal invite for payment {}", event.paymentId(), ex);
            deliveryStateService.markPortalInviteFailed(event.paymentId(), ex.getMessage());
        }
    }
}

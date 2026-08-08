package com.agriplatform.backend.investor.service;

import com.agriplatform.backend.investor.model.InvestorPayment;
import com.agriplatform.backend.investor.repository.InvestorPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestorDeliveryStateService {
    private final InvestorPaymentRepository investorPaymentRepository;

    public InvestorDeliveryStateService(InvestorPaymentRepository investorPaymentRepository) {
        this.investorPaymentRepository = investorPaymentRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPortalInviteSent(Long paymentId) {
        InvestorPayment payment = getPayment(paymentId);
        payment.markPortalInviteSent();
        investorPaymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPortalInviteFailed(Long paymentId, String error) {
        InvestorPayment payment = getPayment(paymentId);
        payment.markPortalInviteFailed(error);
        investorPaymentRepository.save(payment);
    }

    private InvestorPayment getPayment(Long paymentId) {
        return investorPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Investor payment not found"));
    }
}

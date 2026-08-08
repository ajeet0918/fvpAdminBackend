package com.agriplatform.backend.investor.service;

public record InvestorPaymentCompletedEvent(Long paymentId, Long inquiryId) {
}

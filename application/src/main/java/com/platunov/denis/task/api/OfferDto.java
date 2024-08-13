package com.platunov.denis.task.api;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OfferDto {
    BigDecimal monthlyPaymentAmount;
    BigDecimal totalRepaymentAmount;
    int numberOfPayments;
    BigDecimal annualPercentageRate;
    String firstRepaymentDate;
}

package com.platunov.denis.task.service;

import com.platunov.denis.task.BaseSpringBootTest;
import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.MaritalStatus;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.helper.FastBankApiMockHelper;
import com.platunov.denis.task.helper.SolidBankApiMockHelper;
import com.platunov.denis.task.integration.bank.fast.FastBankMapper;
import com.platunov.denis.task.integration.bank.solid.SolidBankMapper;
import com.platunov.denis.task.websocket.OffersWebsocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class OffersProviderAggregatorTest extends BaseSpringBootTest {
    @Autowired
    OffersProviderAggregator offersProviderAggregator;

    @Autowired
    private FastBankApiMockHelper fastBankApiMockHelper;

    @Autowired
    private SolidBankApiMockHelper solidBankApiMockHelper;

    @Autowired
    FastBankMapper fastBankMapper;

    @Autowired
    SolidBankMapper solidBankMapper;

    @Autowired
    OffersWebsocketHandler offersWebsocketHandler;

    @Test
    void shouldReturnTwoOffers() {

        final com.platunov.denis.task.integration.bank.fast.client.model.Offer fastBankOffer = new com.platunov.denis.task.integration.bank.fast.client.model.Offer();
        fastBankOffer.setNumberOfPayments(3);
        fastBankOffer.setAnnualPercentageRate(new BigDecimal("5.25"));
        fastBankOffer.setFirstRepaymentDate(LocalDateTime.now().plusMonths(1).toString());
        fastBankOffer.setMonthlyPaymentAmount(new BigDecimal("3.22"));
        fastBankOffer.setTotalRepaymentAmount(new BigDecimal("9.66"));

        final com.platunov.denis.task.integration.bank.solid.client.model.Offer solidBankOffer = new com.platunov.denis.task.integration.bank.solid.client.model.Offer();
        solidBankOffer.setNumberOfPayments(3);
        solidBankOffer.setAnnualPercentageRate(new BigDecimal("7.25"));
        solidBankOffer.setFirstRepaymentDate(LocalDateTime.now().plusMonths(1).toString());
        solidBankOffer.setMonthlyPaymentAmount(new BigDecimal("1.33"));
        solidBankOffer.setTotalRepaymentAmount(new BigDecimal("3.99"));

        fastBankApiMockHelper.mockOfferResponseOkScenario(fastBankOffer);
        solidBankApiMockHelper.mockOfferResponseOkScenario(solidBankOffer);

        final List<OfferDto> offers = offersProviderAggregator.getOffers(applicationRequestBuilder().build())
                .toStream()
                .toList();

        assertThat(offers).hasSize(2)
                .contains(fastBankMapper.toOfferDto(fastBankOffer), solidBankMapper.toOfferDto(solidBankOffer));
    }

    private ApplicationRequest.ApplicationRequestBuilder applicationRequestBuilder() {
        return ApplicationRequest.builder()
                .agreeToBeScored(true)
                .amount(new BigDecimal("15.14"))
                .dependents(0)
                .maritalStatus(MaritalStatus.SINGLE)
                .email("aaa@bbb.com")
                .monthlyExpenses(new BigDecimal("10.11"))
                .monthlyIncome(new BigDecimal("33.44"))
                .phone("+37126000000");
    }
}
package com.platunov.denis.task.service.bankservice;

import com.platunov.denis.task.BaseSpringBootTest;
import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.MaritalStatus;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.helper.SolidBankApiMockHelper;
import com.platunov.denis.task.integration.bank.solid.client.model.Offer;
import com.platunov.denis.task.integration.bank.solid.SolidBankMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolidBankServiceTest extends BaseSpringBootTest {

    @Autowired
    private SolidBankApiMockHelper solidBankApiMockHelper;

    @Autowired
    private SolidBankService solidBankService;

    @Autowired
    private SolidBankMapper solidBankMapper;

    @Test
    void shouldReturnOneOffer() {
        final Offer offer = new Offer();
        offer.setNumberOfPayments(3);
        offer.setAnnualPercentageRate(new BigDecimal("5.25"));
        offer.setFirstRepaymentDate(LocalDateTime.now().plusMonths(1).toString());
        offer.setMonthlyPaymentAmount(new BigDecimal("3.22"));
        offer.setTotalRepaymentAmount(new BigDecimal("9.66"));

        solidBankApiMockHelper.mockOfferResponseOkScenario(offer);

        final List<OfferDto> offers = solidBankService.getOffers(applicationRequestBuilder().build())
                .toStream()
                .toList();

        assertThat(offers).hasSize(1)
                .contains(solidBankMapper.toOfferDto(offer));
    }


    @Test
    void shouldReturnNoOffersIfApplicationIsNotValid() {
        solidBankApiMockHelper.mockOfferResponseBadRequestScenario();

        final List<OfferDto> offers = solidBankService.getOffers(
                        applicationRequestBuilder()
                                .phone("q")
                                .build())
                .toStream()
                .toList();

        assertThat(offers).hasSize(0);
    }

    @Test
    void shouldReturnNoOffersIfBankReturnBadRequest() {
        final List<OfferDto> offers = solidBankService.getOffers(applicationRequestBuilder()
                        .phone("q")
                        .build())
                .toStream()
                .toList();

        assertThat(offers).hasSize(0);
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
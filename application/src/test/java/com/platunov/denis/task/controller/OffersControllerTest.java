package com.platunov.denis.task.controller;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class OffersControllerTest extends BaseSpringBootTest {
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

    @Autowired
    protected WebTestClient webTestClient;

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

        final ApplicationRequest applicationRequest = ApplicationRequest.builder()
                .agreeToBeScored(true)
                .amount(new BigDecimal("15.14"))
                .dependents(0)
                .maritalStatus(MaritalStatus.SINGLE)
                .email("aaa@bbb.com")
                .monthlyExpenses(new BigDecimal("10.11"))
                .monthlyIncome(new BigDecimal("33.44"))
                .phone("+37126000000")
                .build();

        final List<OfferDto> offers = webTestClient.post()
                .uri("/offers")
                .accept(APPLICATION_JSON)
                .bodyValue(applicationRequest)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(new ParameterizedTypeReference<OfferDto>() {})
                .getResponseBody()
                .toStream()
                .toList();

        assertThat(offers).hasSize(2)
                .contains(fastBankMapper.toOfferDto(fastBankOffer), solidBankMapper.toOfferDto(solidBankOffer));
    }





}
package com.platunov.denis.task.websocket;

import com.platunov.denis.task.BaseSpringBootTest;
import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.MaritalStatus;
import com.platunov.denis.task.api.OfferResponse;
import com.platunov.denis.task.helper.FastBankApiMockHelper;
import com.platunov.denis.task.helper.SolidBankApiMockHelper;
import com.platunov.denis.task.integration.bank.fast.FastBankMapper;
import com.platunov.denis.task.integration.bank.solid.SolidBankMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.platunov.denis.task.api.ResponseCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OffersWebsocketHandlerTest extends BaseSpringBootTest {

    @Autowired
    private FastBankApiMockHelper fastBankApiMockHelper;

    @Autowired
    private SolidBankApiMockHelper solidBankApiMockHelper;

    @Autowired
    private OffersWebsocketHandler offersWebsocketHandler;

    @Autowired
    private FastBankMapper fastBankMapper;

    @Autowired
    private SolidBankMapper solidBankMapper;

    @Test
    void shouldSendTwoOffersAdFinishMessage() {

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

        final List<OfferResponse> responses = collectMessages(applicationRequest, 3);

        assertThat(responses).hasSize(3)
                .contains(
                        OfferResponse.builder()
                                .code(OFFER)
                                .offer(fastBankMapper.toOfferDto(fastBankOffer))
                                .build(),

                        OfferResponse.builder()
                                .code(OFFER)
                                .offer(solidBankMapper.toOfferDto(solidBankOffer))
                                .build(),

                        OfferResponse.builder()
                                .code(FINISHED)
                                .message(FINISHED.name())
                                .build()
                );

    }

    @Test
    void shouldSendErrorMessageIfApplicationRequestIsInvalid() {

        final ApplicationRequest applicationRequest = ApplicationRequest.builder()
                .agreeToBeScored(true)
                .amount(new BigDecimal("15.14"))
                .dependents(0)
                .maritalStatus(MaritalStatus.SINGLE)
                .email("aaa@bbb.com")
                .monthlyExpenses(new BigDecimal("10.11"))
                .monthlyIncome(new BigDecimal("33.44"))
                .phone("q")
                .build();

        final List<OfferResponse> responses = collectMessages(applicationRequest, 1);

        assertThat(responses).hasSize(1);

        final OfferResponse errorMessage = responses.getFirst();

        assertThat(errorMessage.getCode()).isEqualTo(ERROR);
        assertThat(errorMessage.getMessage()).contains("phone: must match");
        assertThat(errorMessage.getOffer()).isNull();
    }

    @SneakyThrows
    private List<OfferResponse> collectMessages(ApplicationRequest applicationRequest, int numberOfMessages) {
        final WebSocketSession webSocketSession = mock(WebSocketSession.class);
        when(webSocketSession.isOpen()).thenReturn(true);

        offersWebsocketHandler.handleMessage(webSocketSession,
                new TextMessage(objectMapperHelper.writeValue(applicationRequest)));

        final ArgumentCaptor<WebSocketMessage<String>> messagesCaptor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(webSocketSession, timeout(5000).times(numberOfMessages)).sendMessage(messagesCaptor.capture());

        return messagesCaptor.getAllValues()
                .stream()
                .map(WebSocketMessage::getPayload)
                .map(value -> objectMapperHelper.readValue(value, OfferResponse.class))
                .toList();
    }

}
package com.platunov.denis.task.websocket;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.api.OfferResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.platunov.denis.task.api.ResponseCode.*;
import static org.springframework.web.socket.CloseStatus.NORMAL;

@Log4j2
@RequiredArgsConstructor
public class OffersWebsocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Function<ApplicationRequest, Flux<OfferDto>> consumer;


    /**
     * Handles message of type {@link TextMessage}. Deserialize it to {@link ApplicationRequest}.
     * If message can't be deserialized then sends error message of type {@link OfferResponse}
     * with {@link com.platunov.denis.task.api.ResponseCode} ERROR and closes the curren {@link WebSocketSession}.
     * Validates the request. If validation is failed then sends error message and closes the session.
     * Sends incoming bank offers.
     * Finally closes the session.
     *
     * @param currentSession
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession currentSession, @NotNull TextMessage message) throws Exception {
        super.handleTextMessage(currentSession, message);
        WebSocketSession session = new ConcurrentWebSocketSessionDecorator(currentSession, 1000, 4096);

        Optional.of(message)
                .map(AbstractWebSocketMessage::getPayload)
                .flatMap(payload -> deserializeRequest(session, payload))
                .map(applicationRequest -> {
                    final Optional<String> result = validate(applicationRequest);
                    result.ifPresent(desc -> sendErrorMessage(session, desc));

                    return result.isEmpty() ? applicationRequest : null;
                })
                .ifPresentOrElse(
                        applicationRequest -> consumer.apply(applicationRequest)
                                .subscribe(
                                        response -> sendOffer(session, response),
                                        error -> sendErrorMessage(session, error.getMessage()),
                                        () -> sendFinishedMessage(session)
                                ),
                        () -> closeSession(session)
                );
    }

    private void sendOffer(WebSocketSession session, OfferDto offer) {
        sendOfferResponse(session,
                OfferResponse.builder()
                        .code(OFFER)
                        .offer(offer)
                        .build());
    }

    private void sendErrorMessage(WebSocketSession session, String message) {
        sendOfferResponse(session,
                OfferResponse.builder()
                        .code(ERROR)
                        .message(message)
                        .build());
        closeSession(session);
    }

    private void sendFinishedMessage(WebSocketSession session) {
        sendOfferResponse(session,
                OfferResponse.builder()
                        .code(FINISHED)
                        .message(FINISHED.name())
                        .build());
        closeSession(session);
    }

    @SneakyThrows
    private void closeSession(WebSocketSession session) {
        session.close(NORMAL);
    }

    private void sendOfferResponse(WebSocketSession session, OfferResponse response) {
        Optional.ofNullable(response)
                .ifPresent(message -> sendMessage(session, message));
    }

    private void sendMessage(WebSocketSession session, Object message) {
        serializeResponse(message)
                .map(TextMessage::new)
                .ifPresent(textMessage -> sendTextMessage(session, textMessage));
    }

    @SneakyThrows
    private void sendTextMessage(WebSocketSession session, TextMessage message) {
        session.sendMessage(message);
    }

    private Optional<ApplicationRequest> deserializeRequest(WebSocketSession session, String payload) {
        try {
            return Optional.of(objectMapper.readValue(payload, ApplicationRequest.class));
        } catch (JacksonException exception) {
            sendErrorMessage(session, "Can't deserialize ApplicationRequest: " + payload);
        }

        return Optional.empty();
    }

    private Optional<String> serializeResponse(@NonNull Object response) {
        try {
            return Optional.of(objectMapper.writeValueAsString(response));
        } catch (JacksonException exception) {
            log.error("Can't serialize OfferResponse");
        }

        return Optional.empty();
    }

    private <T> Optional<String> validate(T object) {
        return Optional.ofNullable(object)
                .map(v -> validator.validate(v))
                .filter(violations -> !violations.isEmpty())
                .map(violations -> violations.stream()
                        .map(violation -> "[" + violation.getPropertyPath() + ": " + violation.getMessage() + "]")
                        .collect(Collectors.joining(",")));
    }
}

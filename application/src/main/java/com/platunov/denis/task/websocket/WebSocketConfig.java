package com.platunov.denis.task.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platunov.denis.task.service.OffersProviderAggregator;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ObjectMapper objectMapper;
    private final OffersProviderAggregator offersProviderAggregator;
    private final Validator validator;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebsocketHandler(), "/offers/ws")
                .setAllowedOrigins("*");
    }

    @Bean
    public OffersWebsocketHandler customWebsocketHandler() {
        return new OffersWebsocketHandler(objectMapper, validator, offersProviderAggregator::getOffers);
    }
}

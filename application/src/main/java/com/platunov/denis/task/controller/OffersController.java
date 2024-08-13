package com.platunov.denis.task.controller;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.service.OffersProviderAggregator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class OffersController {
    private final OffersProviderAggregator offersProviderAggregator;

    /**
     * Return bank offers. The method can be used instead of {@link com.platunov.denis.task.websocket.OffersWebsocketHandler}
     *
     * @param applicationRequest
     * @return
     */
    @PostMapping("/offers")
    public Flux<OfferDto> getEmployeeById(@RequestBody @Valid ApplicationRequest applicationRequest) {
        return offersProviderAggregator.getOffers(applicationRequest);
    }
}

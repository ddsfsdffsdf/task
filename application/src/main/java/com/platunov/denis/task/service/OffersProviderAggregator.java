package com.platunov.denis.task.service;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Class aggregates all {@link OffersProvider}s which can provide bank offers.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class OffersProviderAggregator {
    private final List<OffersProvider> offersProviders;

    /**
     * Collect offers from all {@link OffersProvider}s. The method logs all exceptions and suppresses them.
     *
     * @param applicationRequest
     * @return offers
     */
    public Flux<OfferDto> getOffers(ApplicationRequest applicationRequest) {
        return Flux.fromIterable(offersProviders)
                .flatMap(offersProvider -> offersProvider.getOffers(applicationRequest)
                        .doOnError(log::error)
                        .onErrorResume(error -> Flux.empty()));
    }
}

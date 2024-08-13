package com.platunov.denis.task.service;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import reactor.core.publisher.Flux;

public interface OffersProvider {

    Flux<OfferDto> getOffers(ApplicationRequest applicationRequest);
}

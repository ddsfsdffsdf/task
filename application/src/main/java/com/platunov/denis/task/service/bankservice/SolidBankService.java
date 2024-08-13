package com.platunov.denis.task.service.bankservice;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.integration.bank.IntegrationProperties;
import com.platunov.denis.task.integration.bank.solid.SolidBankMapper;
import com.platunov.denis.task.integration.bank.solid.client.model.Application;
import com.platunov.denis.task.service.OffersProvider;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SolidBankService implements OffersProvider {
    private final Validator validator;
    private final com.platunov.denis.task.integration.bank.solid.client.DefaultApi bankClient;
    private final SolidBankMapper mapper;
    private final IntegrationProperties integrationProperties;

    @Override
    public Flux<OfferDto> getOffers(ApplicationRequest applicationRequest) {
        final com.platunov.denis.task.integration.bank.solid.client.model.ApplicationRequest bankApplicationRequest = mapper.toBankApplicationRequest(applicationRequest);

        if (!validator.validate(bankApplicationRequest).isEmpty()) {
            return Flux.empty();
        }

        return bankClient.addApplication(bankApplicationRequest)
                .flatMapMany(application -> Flux.just(application)
                        .concatWith(Mono.justOrEmpty(application.getId())
                                .map(UUID::fromString)
                                .flatMap(bankClient::getApplicationById)
                                .delayElement(Duration.ofSeconds(integrationProperties.getDelay()), Schedulers.boundedElastic())
                                .repeat()))
                .take(integrationProperties.getAttempts())
                .doOnNext(log::info)
                .filter(application -> application.getStatus() == Application.StatusEnum.PROCESSED)
                .take(1)
                .mapNotNull(Application::getOffer)
                .map(mapper::toOfferDto)
                .doOnError(log::error);
    }
}

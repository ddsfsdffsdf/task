package com.platunov.denis.task.service.bankservice;

import com.platunov.denis.task.api.ApplicationRequest;
import com.platunov.denis.task.api.OfferDto;
import com.platunov.denis.task.integration.bank.IntegrationProperties;
import com.platunov.denis.task.integration.bank.fast.FastBankMapper;
import com.platunov.denis.task.integration.bank.fast.client.DefaultApi;
import com.platunov.denis.task.integration.bank.fast.client.model.Application;
import com.platunov.denis.task.service.OffersProvider;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
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
public class FastBankService implements OffersProvider {
    private final DefaultApi bankClient;
    private final FastBankMapper mapper;
    private final IntegrationProperties integrationProperties;
    private final Validator validator;

    @Override
    public Flux<OfferDto> getOffers(@NotNull ApplicationRequest applicationRequest) {
        final com.platunov.denis.task.integration.bank.fast.client.model.ApplicationRequest bankApplicationRequest = mapper.toBankApplicationRequest(applicationRequest);

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

package com.platunov.denis.task.helper;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.platunov.denis.task.integration.bank.fast.client.DefaultApi;
import com.platunov.denis.task.integration.bank.fast.client.model.Application;
import com.platunov.denis.task.integration.bank.fast.client.model.Offer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class FastBankApiMockHelper {
    private static final String basePath = "/api/FastBank";

    @Getter
    private final String bankPath;

    private final WiremockHelper wiremockHelper;

    public FastBankApiMockHelper(
            @Value("${wiremock.server.port}") String wiremockPort,
            DefaultApi bankClient, WiremockHelper wiremockHelper
    ) {
        this.bankPath = "http://localhost:" + wiremockPort + basePath;
        this.wiremockHelper = wiremockHelper;
        bankClient.getApiClient().setBasePath(getBankPath());
    }

    public void mockOfferResponseOkScenario(Offer offer) {
        final Application draftApplication = new Application();
        draftApplication.setId(randomUUID().toString());
        draftApplication.setStatus(Application.StatusEnum.DRAFT);

        final ResponseDefinitionBuilder draftApplicationResponse = wiremockHelper.ok(draftApplication);

        final String scenario = randomUUID().toString();
        stubFor(
                post(basePath + "/applications")
                        .inScenario(scenario)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo(scenario + "1")
                        .willReturn(draftApplicationResponse)
        );

        stubFor(
                get(basePath + "/applications/" + draftApplication.getId())
                        .inScenario(scenario)
                        .whenScenarioStateIs(scenario + "1")
                        .willSetStateTo(scenario + "2")
                        .willReturn(draftApplicationResponse)
        );

        final Application processedApplication = new Application();
        processedApplication.setId(randomUUID().toString());
        processedApplication.setStatus(Application.StatusEnum.PROCESSED);
        processedApplication.setOffer(offer);

        stubFor(
                get(basePath + "/applications/" + draftApplication.getId())
                        .inScenario(scenario)
                        .whenScenarioStateIs(scenario + "2")
                        .willSetStateTo(scenario + "3")
                        .willReturn(wiremockHelper.ok(processedApplication))
        );
    }


    public void mockOfferResponseBadRequestScenario() {

        final ResponseDefinitionBuilder badRequest = aResponse()
                .withStatus(BAD_REQUEST.value());

        stubFor(
                post(basePath + "/applications")
                        .willReturn(badRequest)
        );

    }
}

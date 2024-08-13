package com.platunov.denis.task.helper;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class WiremockHelper {
    private final ObjectMapperHelper objectMapperHelper;

    public ResponseDefinitionBuilder badRequest() {
        return aResponse().withStatus(BAD_REQUEST.value());
    }

    public ResponseDefinitionBuilder ok(Object body) {
        return aResponse()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(objectMapperHelper.writeValue(body));
    }
}

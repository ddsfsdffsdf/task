package com.platunov.denis.task;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.platunov.denis.task.helper.ObjectMapperHelper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@EnableAutoConfiguration
@ConfigurationPropertiesScan
public abstract class BaseSpringBootTest {

    @Autowired
    protected ObjectMapperHelper objectMapperHelper;

    @BeforeEach
    protected void beforeEach() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
        WireMock.resetToDefault();
    }
}

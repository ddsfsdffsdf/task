package com.platunov.denis.task.integration.bank;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.bank")
@Getter
@Setter
public class IntegrationProperties {
    private int attempts;
    private int delay;
}

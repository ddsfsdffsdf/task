package com.platunov.denis.task.integration.bank;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankClientConfig {
    @Bean
    public com.platunov.denis.task.integration.bank.fast.client.DefaultApi fastBankClient() {
        return new com.platunov.denis.task.integration.bank.fast.client.DefaultApi();
    }

    @Bean
    public com.platunov.denis.task.integration.bank.solid.client.DefaultApi solidBankClient() {
        return new com.platunov.denis.task.integration.bank.solid.client.DefaultApi();
    }
}

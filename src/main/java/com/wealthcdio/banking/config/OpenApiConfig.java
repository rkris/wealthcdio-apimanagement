package com.wealthcdio.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Transaction Processor API")
                        .description("REST API for account management, deposits, withdrawals, transfers, and transaction history.")
                        .version("1.0.0")
                        .contact(new Contact().name("Wealth CDIO")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local (H2 in-memory)")));
    }
}

package com.example.flight.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${spring.application.name}")
  private String applicationName;

  @Bean
  public OpenAPI flightSearchOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Flight Search Service API")
                .description(
                    "High-performance, scalable Flight Search Service for finding and filtering flight options")
                .version("v1.0")
                .contact(
                    new Contact().name("Flight Search Team").email("flight-search@example.com"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
        .servers(
            List.of(
                new Server().url("http://localhost:8081/api/v1").description("Development server"),
                new Server()
                    .url("https://api.flightsearch.com/api/v1")
                    .description("Production server")));
  }
}

package com.localmind.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI localMindOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("LocalMind API")
                .description("API for LocalMind - Local AI Document Management & Chat Platform")
                .version("0.1.0")
                .contact(new Contact().name("LocalMind").url("https://github.com/localmind"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development server")
            ));
    }
}

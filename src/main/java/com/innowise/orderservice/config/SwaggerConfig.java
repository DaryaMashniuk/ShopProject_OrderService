package com.innowise.orderservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Order Service API",
                version = "1.0",
                description = "API for managing orders"
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Development Server"
                )
        }
)
public class SwaggerConfig {
}

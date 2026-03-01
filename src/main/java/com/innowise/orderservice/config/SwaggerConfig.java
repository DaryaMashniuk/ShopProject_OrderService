package com.innowise.orderservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
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
        @Bean
        public OperationCustomizer addGlobalHeaders() {
                return (operation, handlerMethod) -> {
                        Parameter userIdHeader = new Parameter()
                                .in("header")
                                .name("X-User-Id")
                                .description("ID of the current user")
                                .required(false)
                                .example("1");

                        Parameter userRoleHeader = new Parameter()
                                .in("header")
                                .name("X-User-Role")
                                .description("Users role (ADMIN, USER)")
                                .required(false)
                                .example("ADMIN");

                        operation.addParametersItem(userIdHeader);
                        operation.addParametersItem(userRoleHeader);
                        return operation;
                };
        }
}

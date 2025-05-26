package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class RouteConfig {

    private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r ->
                r.path(
                    "/api/v1/auth/**",
                    "/api/v1/users/**",
                    "/api/v1/projects/**"
                )
                .filters(f -> f
                        .filter((exchange, chain) -> {
                            long start = System.currentTimeMillis();
                            return chain.filter(exchange).doFinally(signal -> {
                                long duration = System.currentTimeMillis() - start;
                                log.info("[GATEWAY] {} {} - {}ms",
                                        exchange.getRequest().getMethod(),
                                        exchange.getRequest().getPath(),
                                        duration);
                            });
                        })
                        .retry(config -> config.setRetries(3).setStatuses(HttpStatus.INTERNAL_SERVER_ERROR))
                        .addResponseHeader("X-Gateway-Handled", "true")
                )
                .uri("lb://user-service"))
            .build();
    }
}

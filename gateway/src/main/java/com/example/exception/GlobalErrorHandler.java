package com.example.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Unhandled exception", ex);

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String responseBody = String.format(
                "{\"status\":500,\"message\":\"Internal server error\",\"timestamp\":\"%s\"}",
                Instant.now()
        );

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        return exchange.getResponse()
                .writeWith(Mono.just(bufferFactory.wrap(
                        responseBody.getBytes(StandardCharsets.UTF_8))));
    }
}

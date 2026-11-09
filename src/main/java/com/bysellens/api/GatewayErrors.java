package com.bysellens.api;

import java.nio.charset.StandardCharsets;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrors implements ErrorWebExceptionHandler {
  public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
    if (exchange.getResponse().isCommitted()) return Mono.error(error);
    int status = error instanceof ResponseStatusException e ? e.getStatusCode().value() : 503;
    exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(status));
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    byte[] data =
        "{\"error\":\"Servicio no disponible o ruta invalida\"}".getBytes(StandardCharsets.UTF_8);
    return exchange
        .getResponse()
        .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(data)));
  }
}

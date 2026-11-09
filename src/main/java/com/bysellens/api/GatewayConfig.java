package com.bysellens.api;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.cors.reactive.*;

@Configuration
public class GatewayConfig {
  @Bean
  RouteLocator routes(RouteLocatorBuilder b) {
    return b.routes()
        .route("auth", r -> r.path("/api/v1/auth/**").uri("http://auth-service:8080"))
        .route("customers", r -> r.path("/api/clientes/**").uri("http://customer-service:8080"))
        .route(
            "products",
            r ->
                r.path("/api/productos/**", "/uploads/productos/**")
                    .uri("http://product-service:8080"))
        .route("inventory", r -> r.path("/api/inventario/**").uri("http://inventory-service:8080"))
        .route("sales", r -> r.path("/api/ventas/**").uri("http://sales-service:8080"))
        .build();
  }

  @Bean
  CorsWebFilter cors(@Value("${CORS_ORIGINS:http://localhost:5173}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.asList(origins.split(",")));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Request-ID"));
    c.setExposedHeaders(List.of("X-Request-ID"));
    c.setMaxAge(3600L);
    var source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return new CorsWebFilter(source);
  }

  @Bean
  GlobalFilter trace() {
    return (exchange, chain) -> {
      String incoming = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
      String id =
          incoming != null && incoming.matches("[a-zA-Z0-9-]{1,64}")
              ? incoming
              : UUID.randomUUID().toString();
      exchange.getResponse().getHeaders().set("X-Request-ID", id);
      return chain.filter(
          exchange
              .mutate()
              .request(exchange.getRequest().mutate().header("X-Request-ID", id).build())
              .build());
    };
  }
}

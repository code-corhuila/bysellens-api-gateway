package com.bysellens.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayTest {
  @Autowired WebTestClient client;
  @Autowired RouteLocator routes;

  @Test
  void routesOnlyExposePublicDomains() {
    assertEquals(5, routes.getRoutes().count().block());
    client.get().uri("/internal/reservas/test").exchange().expectStatus().isNotFound();
  }

  @Test
  void corsUsesExactOrigins() {
    client
        .options()
        .uri("http://localhost:8080/api/v1/auth/login")
        .header("Origin", "http://localhost:5173")
        .header("Access-Control-Request-Method", "POST")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("Access-Control-Allow-Origin", "http://localhost:5173");
    client
        .options()
        .uri("http://localhost:8080/api/v1/auth/login")
        .header("Origin", "https://untrusted.example")
        .header("Access-Control-Request-Method", "POST")
        .exchange()
        .expectStatus()
        .isForbidden();
  }
}

# API Gateway

Standalone Java 21 / Spring Boot 3.5.16 / Spring Cloud 2025.0.3 project.
Source: `BACKEND/api-gateway`. No parent backend build or sibling modules are
required. Existing repository governance files are unchanged.

## Build and run

```sh
mvn clean verify
java -jar target/api-gateway-1.0.0.jar
```

The Gateway listens on port 8080. Set `CORS_ORIGINS` to a comma-separated list
of exact frontend origins; the default is `http://localhost:5173`.
No database or signing credentials are required by the Gateway itself.

## Preserved routes

| Public path | Destination |
| --- | --- |
| `/api/v1/auth/**` | `http://auth-service:8080` |
| `/api/clientes/**` | `http://customer-service:8080` |
| `/api/productos/**`, `/uploads/productos/**` | `http://product-service:8080` |
| `/api/inventario/**` | `http://inventory-service:8080` |
| `/api/ventas/**` | `http://sales-service:8080` |

Run on a network where those service names resolve. The four business routes
and the existing Auth route are retained without including downstream source.
The original request ID propagation, exact-origin CORS policy, JSON error
responses, 3-second connect timeout and 60-second response timeout are retained.
Health probes are exposed at `/actuator/health`.

```sh
docker build -t bysellens-api-gateway .
```

The Dockerfile uses the executable JAR generated in the root `target/` directory.
Do not commit environment files, credentials, private keys or generated output.

## Validation

`mvn -B clean verify` completed with **BUILD SUCCESS** and generated the
executable JAR. `GatewayTest`: **2 tests, 0 failures, 0 errors, 0 skipped**.

- `routesOnlyExposePublicDomains`: checks the five registered routes and rejects
  an internal reservation path.
- `corsUsesExactOrigins`: accepts the configured frontend origin and rejects an
  untrusted origin.

The tests start the Gateway on a random port. They do not validate forwarding
against live downstream services. Java source changes are formatting only;
the standalone POM preserves the inherited Boot/Cloud versions and Java target.

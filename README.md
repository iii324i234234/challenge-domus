# Challenge API – Directors Threshold (Spring Boot · Java 21)

## Descripcion

Este proyecto es una REST API construida con Spring Boot (WebFlux) y Java 21.
La aplicación consulta un servicio externo de películas, procesa sus páginas y devuelve los directores que superan un determinado número de películas.

🚀 Características principales

- Java 21 + Spring Boot WebFlux (reactivo).
- Un único endpoint REST.
- Llamadas a API externa con WebClient.
- Manejo de errores centralizado.
- Configuración por properties (timeouts, base URL, memoria, retry, logging).
- MapStruct, Lombok y Reactor.

##  📡 Endpoint disponible
GET /api/directors?threshold={n}

threshold es un entero ≥ 0.
La API devuelve los directores cuya cantidad de películas es mayor al valor indicado.

Ejemplo:
http://localhost:8080/api/directors?threshold=4

✔️ Respuesta exitosa (200)
```
{
  "directors": [
    "Christopher Nolan",
    "James Cameron"
  ]
}
```

❌ Error validación (400)
```
{
  "error": "Invalid request parameter",
  "detail": "threshold: Threshold must be a positive integer"
}
```

##  📘 Cómo ver la documentación con Swagger / OpenAPI

La API expone automáticamente la documentación generada por OpenAPI.

Una vez levantada la aplicación, podés acceder a:

👉 Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

👉 OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

Esto permite probar el endpoint desde el navegador, visualizar schemas, ver ejemplos y revisar los códigos de respuesta definidos en el controller.

##  🧩 Arquitectura del proyecto
src/main/java/domus/challenge/

- config/   ........... WebClient + properties
- controllers/  ...........  Controller REST
- domain/     ........... Movie, DirectorCounter, State
- dto/  ...................  DTOs de entrada/salida
- exceptions/   ........... Errores externos y global handler
- mappers/    ........... MapStruct
- repository/     ........... Acceso a API externa (WebClient)
- service/       ...........  Lógica de negocio
- ChallengeApplication  ..Main app

##  ⚙️ Configuración

La URL base y parámetros del servicio externo están en:

- src/main/resources/application.properties

- movie.api.base-url=https://challenge.iugolabs.com/api/movies
- movie.api.search-path=/search
- movie.api.connect-timeout=5000
- movie.api.response-timeout=10s
- movie.api.read-timeout=20s
- movie.api.write-timeout=20s
- movie.api.max-in-memory-size=2MB
- movie.api.retry.max-attempts=3
- movie.api.retry.backoff=2s
- movie.api.logging.enabled=true

##  ▶️ Cómo ejecutar
1. Requisitos

- Java 21
- Maven 3.9+

2. Compilar
mvn clean package

3. Ejecutar
mvn spring-boot:run

4. Probar endpoint
```
curl "http://localhost:8080/api/directors?threshold=4"
```

##  🧪 Test

El proyecto incluye el test básico de carga de contexto:

ChallengeApplicationTests


Podés ejecutar todos los tests:

```
mvn test
```

📖 Ejemplo de flujo interno

- Controller recibe threshold.
- Service pide todas las páginas al MovieRepository.
- Se cuentan películas por director.
- Se filtra según el umbral > threshold.
- Se retorna el resultado en DirectorsResponseDto.
- Si hay errores (4xx, 5xx, timeout, etc.), GlobalErrorHandler devuelve un JSON homogéneo.

##  🛡️ Manejo de errores

El proyecto diferencia:

- 400 → errores de validación o parámetro faltante
- 502 → errores 5xx del servicio externo
- 503 → timeouts / servicio externo inalcanzable
- 500 → errores inesperados

Siempre responde con:

```
{ "error": "...", "detail": "..." }
```

##  📚 Tecnologías utilizadas
Tecnología	Uso
- Spring Boot WebFlux	API reactiva
- Java 21	Lenguaje principal
- Reactor (Mono/Flux)	Flujo reactivo
- WebClient	Cliente HTTP
- MapStruct	Mapping DTO
- Lombok	Reducción de boilerplate
- Validation / Jakarta	Validaciones de request
- SLF4J / Logback	Logging

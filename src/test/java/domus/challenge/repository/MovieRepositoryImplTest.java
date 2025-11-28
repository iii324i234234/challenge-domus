package domus.challenge.repository;

import domus.challenge.config.MovieApiConfig;
import domus.challenge.dto.MoviePageResponseDto;
import domus.challenge.exceptions.ExternalClientException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovieRepositoryImplTest {

    private static final String VALID_RESPONSE = """
            {
              "page": 1,
              "per_page": 10,
              "total": 10,
              "total_pages": 1,
              "data": [{"Director": "James Cameron"}]
            }
            """;

    private static final String EMPTY_RESPONSE = """
            {
              "page": 1,
              "per_page": 10,
              "total": 0,
              "total_pages": 0,
              "data": []
            }
            """;

    private static final String VALID_RESPONSE_TARANTINO = """
            {
              "page": 1,
              "per_page": 10,
              "total": 1,
              "total_pages": 1,
              "data": [{"Director":"Tarantino"}]
            }
            """;

    private static final String MALFORMED_JSON = "{ invalid json ";

    private MockWebServer mockWebServer;
    private MovieRepositoryImpl repository;
    private MovieApiConfig config;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        config = new MovieApiConfig();
        config.setBaseUrl(mockWebServer.url("/").toString());
        config.setSearchPath("/search");
        config.setConnectTimeout(5000);
        config.setResponseTimeout(Duration.ofSeconds(5));
        config.setReadTimeout(Duration.ofSeconds(5));
        config.setWriteTimeout(Duration.ofSeconds(5));
        config.setMaxInMemorySize(DataSize.ofMegabytes(1));
        config.setRetryMaxAttempts(1);
        config.setRetryBackoff(Duration.ofMillis(100));
        config.setLoggingEnabled(false);

        WebClient webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();

        repository = new MovieRepositoryImpl(webClient, config);
    }

    @AfterEach
    void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Should return a valid page with director James Cameron")
    void givenValidResponse_whenGetMovies_thenReturnPage() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody(VALID_RESPONSE)
                .addHeader("Content-Type", "application/json")
        );

        // when
        Mono<MoviePageResponseDto> result = repository.getMovies(1);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getMovieList().size() == 1 &&
                        "James Cameron".equals(dto.getMovieList().get(0).getDirector()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return an empty list when response contains no data")
    void givenEmptyResponse_whenGetMovies_thenReturnEmptyList() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody(EMPTY_RESPONSE)
                .addHeader("Content-Type", "application/json")
        );

        // when
        Mono<MoviePageResponseDto> result = repository.getMovies(1);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getMovieList().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalClientException on 4xx error")
    void given4xxError_whenGetMovies_thenThrowExternalClientException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad request")
        );

        // when
        Mono<MoviePageResponseDto> result = repository.getMovies(1);

        // then
        StepVerifier.create(result)
                .expectError(ExternalClientException.class)
                .verify();
    }

    @Test
    @DisplayName("Should retry on 5xx error and return success on second attempt")
    void given5xxThenSuccess_whenGetMovies_thenRetryAndReturnSuccess() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Server error")
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody(VALID_RESPONSE_TARANTINO)
                .addHeader("Content-Type", "application/json")
        );

        // when
        Mono<MoviePageResponseDto> result = repository.getMovies(1);

        // then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getMovieList().size() == 1 &&
                        "Tarantino".equals(dto.getMovieList().get(0).getDirector()))
                .verifyComplete();

        assertEquals(2, mockWebServer.getRequestCount(), "Expected 2 requests (including retry)");
    }

    @Test
    @DisplayName("Should throw exception on malformed JSON")
    void givenMalformedJson_whenGetMovies_thenThrowException() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setBody(MALFORMED_JSON)
                .addHeader("Content-Type", "application/json")
        );

        // when
        Mono<MoviePageResponseDto> result = repository.getMovies(1);

        // then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }
}

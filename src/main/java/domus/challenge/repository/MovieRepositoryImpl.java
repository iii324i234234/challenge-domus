package domus.challenge.repository;

import domus.challenge.config.MovieApiConfig;
import domus.challenge.dto.MoviePageResponseDto;
import domus.challenge.exceptions.ExternalClientException;
import domus.challenge.exceptions.ExternalServerException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Repository
@Slf4j
@AllArgsConstructor
public class MovieRepositoryImpl implements MovieRepository {

    private final WebClient movieWebClient;
    private final MovieApiConfig movieApiConfig;

    @PostConstruct
    public void init() {
        log.debug("Loaded config: {}", movieApiConfig);
    }

    @Override
    public Mono<MoviePageResponseDto> getMovies(int page) {

        log.debug("Calling external API, page={}", page);

        return movieWebClient.get()
                .uri(uriBuilder -> buildSearchUri(uriBuilder, page))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handle5xxError)
                .bodyToMono(MoviePageResponseDto.class)
                .retryWhen(Retry.backoff(movieApiConfig.getRetryMaxAttempts(), movieApiConfig.getRetryBackoff())
                        .filter(throwable -> throwable instanceof PrematureCloseException
                                || throwable instanceof ExternalServerException))
                .doOnNext(this::logResponse)
                .doOnError(e -> log.error(" Error calling external API: {}", e.getMessage(), e));
    }


    private URI buildSearchUri(UriBuilder builder, int page) {
        return builder.path(movieApiConfig.getSearchPath())
                .queryParam("page", page)
                .build();
    }


    private Mono<Throwable> handle4xxError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Client error")
                .flatMap(body -> {
                    log.error("4xx Client error: {}", body);
                    return Mono.error(new ExternalClientException("Client error calling Movie API: " + body));
                });
    }

    private Mono<Throwable> handle5xxError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Server error")
                .flatMap(body -> {
                    log.error(" 5xx Server error: {}", body);
                    return Mono.error(new ExternalServerException("Server error calling Movie API: " + body));
                });
    }


    private void logResponse(MoviePageResponseDto dto) {
        log.debug(
                " Response received: page={}, total_pages={}, movies={}",
                dto.getPage(),
                dto.getTotal_pages(),
                (dto.getMovieList() != null ? dto.getMovieList().size() : 0)
        );
    }

}

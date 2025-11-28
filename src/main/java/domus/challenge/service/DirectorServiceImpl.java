package domus.challenge.service;

import domus.challenge.domain.DirectorCounter;
import domus.challenge.domain.DirectorCounterState;
import domus.challenge.dto.DirectorsResponseDto;
import domus.challenge.dto.MoviePageResponseDto;
import domus.challenge.exceptions.ExternalClientException;
import domus.challenge.exceptions.ExternalServerException;
import domus.challenge.exceptions.ExternalServiceUnavailableException;
import domus.challenge.mappers.DirectorMapper;
import domus.challenge.repository.MovieRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class DirectorServiceImpl implements DirectorService {

    private final MovieRepository movieRepository;
    private final DirectorMapper directorMapper;
    private final DirectorCounter directorCounter;

    @Override
    public Mono<DirectorsResponseDto> findDirectorsAboveThreshold(int threshold) {

        log.info("Finding directors with threshold {}", threshold);

        if (threshold < 0) {
            return Mono.just(directorMapper.toResponse(Collections.emptyList()));
        }

        DirectorCounterState state = directorCounter.createState();

        return fetchAllPages()
                .flatMapIterable(MoviePageResponseDto::getMovieList)
                .filter(Objects::nonNull)
                .doOnNext(state::add)
                .then(Mono.fromSupplier(() -> buildResult(state, threshold)))
                .map(directorMapper::toResponse)
                .onErrorMap(IOException.class, this::mapToServiceUnavailable)
                .onErrorMap(
                        ex -> ex instanceof WebClientRequestException
                                || ex instanceof IOException
                                || ex.getCause() instanceof IOException,
                        ex -> new ExternalServiceUnavailableException("External service unreachable", ex)
                )
                .onErrorResume(this::propagateExpectedRepositoryErrors)
                .onErrorResume(this::wrapUnexpectedErrors);
    }

    private List<String> buildResult(DirectorCounterState state, int threshold) {
        Map<String, Integer> counts = state.snapshot();
        log.info("Director movie counts collected: {}", counts);

        List<String> result = state.above(threshold);

        log.info("Found {} directors above threshold {}: {}", result.size(), threshold, result);
        return result;
    }

    private Flux<MoviePageResponseDto> fetchAllPages() {
        return movieRepository.getMovies(1)    // 1.Pide la primera página
                .doOnNext(this::logFirstPage)       // 2.Loggea info de la primera página
                .flatMapMany(this::expandAllPages); // 3.Expande a todas las páginas
    }


    private Flux<MoviePageResponseDto> expandAllPages(MoviePageResponseDto firstPage) {

        if (firstPage == null || firstPage.getMovieList() == null) {
            return Flux.empty();
        }

        int totalPages = Math.max(1, firstPage.getTotal_pages());

        // Crea un Flux para las páginas restantes (2 hasta totalPages)
        Flux<MoviePageResponseDto> remainingPages = Flux
                .range(2, Math.max(0, totalPages - 1))  // rango 2..totalPages
                .flatMap(this::safeGetMoviesPage);  // obtiene cada página de forma segura

        return Flux.concat(Mono.just(firstPage), remainingPages);
    }

    private Mono<MoviePageResponseDto> safeGetMoviesPage(int page) {
        return movieRepository.getMovies(page)
                .doOnNext(dto -> log.debug("Fetched page {}", page))
                .onErrorResume(e -> {
                    log.warn("Failed to fetch page {}: {}", page, e.getMessage());
                    return Mono.empty();
                });
    }

    private Throwable mapToServiceUnavailable(Throwable ex) {
        log.error("I/O external API error: {}", ex.getMessage(), ex);
        return new ExternalServiceUnavailableException("External service unreachable", ex);
    }

    private <T> Mono<T> propagateExpectedRepositoryErrors(Throwable ex) {
        if (ex instanceof ExternalClientException ||
                ex instanceof ExternalServerException ||
                ex instanceof ExternalServiceUnavailableException) {
            return Mono.error(ex);
        }
        return Mono.empty();
    }

    private <T> Mono<T> wrapUnexpectedErrors(Throwable ex) {
        log.error("Unexpected director processing error: {}", ex.getMessage(), ex);
        return Mono.error(new RuntimeException("Unexpected error processing directors", ex));
    }

    private void logFirstPage(MoviePageResponseDto dto) {
        int count = dto.getMovieList() != null ? dto.getMovieList().size() : 0;
        log.info("Movies found in first page: {}", count);
    }
}

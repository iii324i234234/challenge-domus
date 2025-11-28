package domus.challenge.service;

import domus.challenge.domain.DirectorCounter;
import domus.challenge.domain.DirectorCounterState;
import domus.challenge.domain.Movie;
import domus.challenge.dto.DirectorsResponseDto;
import domus.challenge.dto.MoviePageResponseDto;
import domus.challenge.mappers.DirectorMapper;
import domus.challenge.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class DirectorServiceImplTest {

    private MovieRepository movieRepository;
    private DirectorMapper mapper;
    private DirectorCounter directorCounter;

    private DirectorServiceImpl service;

    @BeforeEach
    void setup() {
        movieRepository = mock(MovieRepository.class);
        mapper = new DirectorMapper() {}; // implementación default
        directorCounter = mock(DirectorCounter.class);

        service = new DirectorServiceImpl(movieRepository, mapper, directorCounter);
    }

    @Test
    @DisplayName("Should return correct directors when movies are above threshold")
    void givenMoviesAboveThreshold_whenFindDirectors_thenReturnCorrectDirectors() {
        // Given
        DirectorCounterState state = new DirectorCounterState();
        when(directorCounter.createState()).thenReturn(state);

        Movie m1 = Movie.builder().director("James Cameron").build();
        Movie m2 = Movie.builder().director("James Cameron").build();
        Movie m3 = Movie.builder().director("Tarantino").build();

        MoviePageResponseDto page1 = MoviePageResponseDto.builder()
                .page(1)
                .total_pages(1)
                .movieList(List.of(m1, m2, m3))
                .build();

        when(movieRepository.getMovies(1)).thenReturn(Mono.just(page1));

        // When
        Mono<DirectorsResponseDto> result = service.findDirectorsAboveThreshold(1);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res ->
                        res.getDirectors().size() == 1 &&
                                res.getDirectors().contains("James Cameron")
                )
                .verifyComplete();

        verify(movieRepository, times(1)).getMovies(1);
        verifyNoMoreInteractions(movieRepository);
        verify(directorCounter, times(1)).createState();
        verifyNoMoreInteractions(directorCounter);
    }

    @Test
    @DisplayName("Should return empty list when threshold is negative")
    void givenNegativeThreshold_whenFindDirectors_thenReturnEmptyList() {
        // Given
        int threshold = -5;

        // When
        Mono<DirectorsResponseDto> result = service.findDirectorsAboveThreshold(threshold);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res -> res.getDirectors().isEmpty())
                .verifyComplete();

        verify(movieRepository, never()).getMovies(anyInt());
        verifyNoInteractions(directorCounter);
    }

    @Test
    @DisplayName("Should return empty list when movie list is empty")
    void givenEmptyMovieList_whenFindDirectors_thenReturnEmptyList() {
        // Given
        DirectorCounterState state = new DirectorCounterState();
        when(directorCounter.createState()).thenReturn(state);

        MoviePageResponseDto page1 = MoviePageResponseDto.builder()
                .page(1)
                .total_pages(1)
                .movieList(null)
                .build();

        when(movieRepository.getMovies(1)).thenReturn(Mono.just(page1));

        // When
        Mono<DirectorsResponseDto> result = service.findDirectorsAboveThreshold(0);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res -> res.getDirectors().isEmpty())
                .verifyComplete();

        verify(movieRepository, times(1)).getMovies(1);
        verifyNoMoreInteractions(movieRepository);
        verify(directorCounter, times(1)).createState();
        verifyNoMoreInteractions(directorCounter);
    }

    @Test
    @DisplayName("Should return all directors when multiple pages are present")
    void givenMultiplePages_whenFindDirectors_thenReturnAllDirectors() {
        // Given
        DirectorCounterState state = new DirectorCounterState();
        when(directorCounter.createState()).thenReturn(state);

        Movie m1 = Movie.builder().director("Dir1").build();
        Movie m2 = Movie.builder().director("Dir2").build();

        MoviePageResponseDto page1 = MoviePageResponseDto.builder()
                .page(1)
                .total_pages(2)
                .movieList(List.of(m1))
                .build();
        MoviePageResponseDto page2 = MoviePageResponseDto.builder()
                .page(2)
                .total_pages(2)
                .movieList(List.of(m2))
                .build();

        when(movieRepository.getMovies(1)).thenReturn(Mono.just(page1));
        when(movieRepository.getMovies(2)).thenReturn(Mono.just(page2));

        // When
        Mono<DirectorsResponseDto> result = service.findDirectorsAboveThreshold(0);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res -> res.getDirectors().containsAll(List.of("Dir1", "Dir2")))
                .verifyComplete();

        verify(movieRepository, times(1)).getMovies(1);
        verify(movieRepository, times(1)).getMovies(2);
        verifyNoMoreInteractions(movieRepository);
        verify(directorCounter, times(1)).createState();
        verifyNoMoreInteractions(directorCounter);
    }

    @Test
    @DisplayName("Should return empty list when all movies are below threshold")
    void givenMoviesBelowThreshold_whenFindDirectors_thenReturnEmptyList() {
        // Given
        DirectorCounterState state = new DirectorCounterState();
        when(directorCounter.createState()).thenReturn(state);

        Movie m1 = Movie.builder().director("A").build();
        Movie m2 = Movie.builder().director("B").build();

        MoviePageResponseDto page1 = MoviePageResponseDto.builder()
                .page(1)
                .total_pages(1)
                .movieList(List.of(m1, m2))
                .build();

        when(movieRepository.getMovies(1)).thenReturn(Mono.just(page1));

        // When
        Mono<DirectorsResponseDto> result = service.findDirectorsAboveThreshold(10);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res -> res.getDirectors().isEmpty())
                .verifyComplete();

        verify(movieRepository, times(1)).getMovies(1);
        verifyNoMoreInteractions(movieRepository);
        verify(directorCounter, times(1)).createState();
        verifyNoMoreInteractions(directorCounter);
    }

}

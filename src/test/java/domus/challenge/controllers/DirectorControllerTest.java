package domus.challenge.controllers;

import domus.challenge.dto.DirectorsResponseDto;
import domus.challenge.service.DirectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class DirectorControllerTest {

    private DirectorService directorService;
    private DirectorController controller;

    @BeforeEach
    void setup() {
        directorService = mock(DirectorService.class);
        controller = new DirectorController(directorService);
    }

    @Test
    @DisplayName("Should return 200 with directors list when service returns data")
    void givenDirectors_whenGetDirectors_thenReturn200WithList() {
        // Given
        DirectorsResponseDto dto = DirectorsResponseDto.builder()
                .directors(List.of("James Cameron", "Tarantino"))
                .build();
        when(directorService.findDirectorsAboveThreshold(1))
                .thenReturn(Mono.just(dto));

        // When
        Mono<ResponseEntity<DirectorsResponseDto>> result = controller.getDirectors(1);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getStatusCode().is2xxSuccessful() &&
                        resp.getBody() != null &&
                        resp.getBody().getDirectors().size() == 2 &&
                        resp.getBody().getDirectors().contains("James Cameron") &&
                        resp.getBody().getDirectors().contains("Tarantino")
                )
                .verifyComplete();

        verify(directorService, times(1)).findDirectorsAboveThreshold(1);
        verifyNoMoreInteractions(directorService);
    }

    @Test
    @DisplayName("Should return 200 with empty list when service returns empty")
    void givenEmptyDirectors_whenGetDirectors_thenReturn200WithEmptyList() {
        // Given
        DirectorsResponseDto dto = DirectorsResponseDto.builder().directors(List.of()).build();
        when(directorService.findDirectorsAboveThreshold(0)).thenReturn(Mono.just(dto));

        // When
        Mono<ResponseEntity<DirectorsResponseDto>> result = controller.getDirectors(0);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getStatusCode().is2xxSuccessful() &&
                        resp.getBody() != null &&
                        resp.getBody().getDirectors().isEmpty()
                )
                .verifyComplete();

        verify(directorService, times(1)).findDirectorsAboveThreshold(0);
        verifyNoMoreInteractions(directorService);
    }

    @Test
    @DisplayName("Should propagate error from service")
    void givenServiceThrows_whenGetDirectors_thenReturnError() {
        // Given
        RuntimeException ex = new RuntimeException("Service failure");
        when(directorService.findDirectorsAboveThreshold(5)).thenReturn(Mono.error(ex));

        // When
        Mono<ResponseEntity<DirectorsResponseDto>> result = controller.getDirectors(5);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().equals("Service failure"))
                .verify();

        verify(directorService, times(1)).findDirectorsAboveThreshold(5);
        verifyNoMoreInteractions(directorService);
    }
}

package domus.challenge.controllers;

import domus.challenge.dto.DirectorsResponseDto;
import domus.challenge.service.DirectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/directors")
@AllArgsConstructor
@Slf4j
@Validated
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    @Operation( summary = "Get directors with more movies than the given threshold",
            description = "Returns a list of directors whose number of movies is strictly greater than the threshold.",
            responses = { @ApiResponse(
                    responseCode = "200",
                    description = "Directors successfully retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DirectorsResponseDto.class))
            ),@ApiResponse(
                    responseCode = "400",
                    description = "Invalid threshold value",
                    content = @Content ) } )
    public Mono<ResponseEntity<DirectorsResponseDto>> getDirectors(
            @RequestParam("threshold")
            @Min(value = 0, message = "Threshold must be a positive integer") int threshold
    ) {
        log.info("Received request for directors, threshold={}", threshold);

        return directorService.findDirectorsAboveThreshold(threshold)
                .map(ResponseEntity::ok); // no defaultIfEmpty
    }


}





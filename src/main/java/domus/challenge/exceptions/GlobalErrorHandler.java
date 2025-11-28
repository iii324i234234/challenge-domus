package domus.challenge.exceptions;

import domus.challenge.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalErrorHandler {

    // =================================================
    // EXTERNAL API ERRORS (CLIENT / SERVER / UNAVAILABLE)
    // =================================================
    @ExceptionHandler(ExternalClientException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleClient(ExternalClientException ex) {
        return build(HttpStatus.BAD_REQUEST, "External API rejected the request", ex);
    }

    @ExceptionHandler(ExternalServerException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServer(ExternalServerException ex) {
        return build(HttpStatus.BAD_GATEWAY, "External API has internal errors", ex);
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnavailable(ExternalServiceUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "External service unreachable", ex);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientTimeout(WebClientRequestException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "External service unreachable", ex);
    }


    // =================================================
    // INPUT / VALIDATION ERRORS (400)
    // =================================================
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(ConstraintViolationException ex) {

        String detail = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Invalid request parameter");

        return Mono.just(
                ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid request parameter", detail))
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class,
            ServerWebInputException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter", ex);
    }


    // =================================================
    // UNEXPECTED / UNHANDLED
    // =================================================
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntime(RuntimeException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex);
    }


    // =================================================
    // UTIL — CENTRALIZED RESPONSE BUILDER
    // =================================================
    private Mono<ResponseEntity<ErrorResponse>> build(HttpStatus status, String title, Exception ex) {
        return Mono.just(
                ResponseEntity.status(status)
                        .body(new ErrorResponse(title, clean(ex.getMessage())))
        );
    }

    // =================================================
    // MESSAGE NORMALIZER
    // =================================================
    private String clean(String message) {
        if (message == null) return null;

        String m = message;

        // Remove status codes from WebFlux stringified errors
        m = m.replaceAll("\\d{3} [A-Z_]+ \"", "");
        m = m.replace("\"", "");

        // Missing param
        m = m.replaceAll(
                "Required (request|query) parameter '?([^' ]+)'? (is (not present|missing))",
                "$2 is missing"
        );

        // Type mismatch cleanup
        m = m.replace("Failed to convert value of type", "Invalid parameter type");

        return m.trim();
    }
}

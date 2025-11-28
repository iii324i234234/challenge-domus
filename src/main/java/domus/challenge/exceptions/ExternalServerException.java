package domus.challenge.exceptions;

public class ExternalServerException extends RuntimeException {

    public ExternalServerException(String message) {
        super(message);
    }

    public ExternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

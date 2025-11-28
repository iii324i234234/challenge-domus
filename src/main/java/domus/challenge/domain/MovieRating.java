package domus.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MovieRating {
    G, PG, PG_13, R, NC_17, NOT_RATED;

    @JsonCreator
    public static MovieRating fromString(String value) {
        if (value == null) return null;
        return switch (value.toUpperCase().replace("-", "_")) {
            case "G" -> G;
            case "PG" -> PG;
            case "PG_13" -> PG_13;
            case "R" -> R;
            case "NC_17" -> NC_17;
            case "NOT RATED", "NOT_RATED" -> NOT_RATED;
            default -> throw new IllegalArgumentException("Unknown rating: " + value);
        };
    }
}

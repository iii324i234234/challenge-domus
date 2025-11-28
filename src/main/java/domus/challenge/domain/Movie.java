package domus.challenge.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value // Immutable class
@Builder
@AllArgsConstructor
public class Movie {

    @JsonProperty("Title")
    String title;

    @JsonProperty("Year")
    String year;

    @JsonProperty("Released")
    String released;

    @JsonProperty("Runtime")
    String runtime;

    @JsonProperty("Genre")
    String genre;

    @JsonProperty("Rated")
    MovieRating rated;

    @JsonProperty("Director")
    String director;

    @JsonProperty("Writer")
    String writer;

    @JsonProperty("Actors")
    String actors;
}

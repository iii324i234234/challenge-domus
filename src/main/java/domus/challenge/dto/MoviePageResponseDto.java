package domus.challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import domus.challenge.domain.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value // Immutable class
@Builder
@AllArgsConstructor
public class MoviePageResponseDto {

    private int page;
    private int per_page;
    private int total;
    private int total_pages;

    @JsonProperty("data")
    private List<Movie> movieList;
}

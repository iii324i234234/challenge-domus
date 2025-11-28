package domus.challenge.repository;


import domus.challenge.dto.MoviePageResponseDto;
import reactor.core.publisher.Mono;

public interface MovieRepository {

    Mono<MoviePageResponseDto> getMovies(int page);
}

package domus.challenge.service;

import domus.challenge.dto.DirectorsResponseDto;
import reactor.core.publisher.Mono;

public interface DirectorService {

    Mono<DirectorsResponseDto> findDirectorsAboveThreshold(int threshold);

}

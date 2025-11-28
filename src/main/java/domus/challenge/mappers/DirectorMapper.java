package domus.challenge.mappers;

import domus.challenge.dto.DirectorsResponseDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    default DirectorsResponseDto toResponse(List<String> directors) {
        if (directors == null) return null;
        return DirectorsResponseDto.builder().directors(directors).build();
    }
}

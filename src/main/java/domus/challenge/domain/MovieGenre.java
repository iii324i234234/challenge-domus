package domus.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MovieGenre {
    COMEDY, DRAMA, ACTION, THRILLER, BIOGRAPHY, CRIME, ROMANCE, FANTASY,
    HORROR, ADVENTURE, FAMILY, SCIFI, MUSICAL, HISTORY, MYSTERY, MUSIC, ANIMATION;


    // Permite que Jackson convierta Strings a MovieGenre
    @JsonCreator
    public static MovieGenre fromString(String value) {
        return value == null ? null :
                MovieGenre.valueOf(value.toUpperCase()
                        .replace("-", "_"));
    }
}
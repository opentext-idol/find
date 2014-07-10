package com.hp.autonomy.frontend.find.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@JsonDeserialize(builder = Entities.Builder.class)
public class Entities {

    private final List<Entity> entities;

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {

        private List<Entity> entities;

        public Entities build() {
            return new Entities(entities);
        }

    }

}

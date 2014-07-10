package com.hp.autonomy.frontend.find.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Entities {

    private final List<Entity> entities;

    @NoArgsConstructor
    @Setter
    public static class Builder {

        private List<Entity> entities;

        public Entities build() {
            return new Entities(entities);
        }

    }

}

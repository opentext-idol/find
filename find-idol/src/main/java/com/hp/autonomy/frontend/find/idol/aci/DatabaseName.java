package com.hp.autonomy.frontend.find.idol.aci;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hp.autonomy.types.Identifier;

/**
 * Idol Database name
 */
public class DatabaseName implements Identifier {
    private final String name;

    @JsonCreator
    public DatabaseName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

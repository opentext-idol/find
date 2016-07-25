package com.hp.autonomy.frontend.find.core.fields;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
public class FieldAndValueDetails {
    private final String id;
    private final String name;
    private final double min;
    private final double max;
    private final long totalValues;

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    static class Builder {
        private String id;
        private String name;
        private double min;
        private double max;
        private long totalValues;

        public Builder(final FieldAndValueDetails fieldAndValueDetails) {
            id = fieldAndValueDetails.id;
            name = fieldAndValueDetails.name;
            min = fieldAndValueDetails.min;
            max = fieldAndValueDetails.max;
            totalValues = fieldAndValueDetails.totalValues;
        }

        public FieldAndValueDetails build() {
            return new FieldAndValueDetails(id, name, min, max, totalValues);
        }
    }
}

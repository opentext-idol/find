package com.hp.autonomy.frontend.find.hod.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.search.FindDocument;
import com.hp.autonomy.hod.client.api.textindex.query.search.PromotionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = HodFindDocument.Builder.class)
public class HodFindDocument extends FindDocument {
    private static final long serialVersionUID = -7386227266595690038L;

    private final String domain;
    private final PromotionType promotionType;

    public HodFindDocument(final Builder builder) {
        super(builder);
        domain = builder.domain;
        promotionType = builder.promotionType == null ? PromotionType.NONE : builder.promotionType;
    }

    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends FindDocument.Builder {
        private String domain;

        @JsonProperty("promotion")
        private PromotionType promotionType;

        public Builder(final HodFindDocument document) {
            super(document);
            domain = document.domain;
            promotionType = document.promotionType;
        }

        @Override
        public HodFindDocument build() {
            return new HodFindDocument(this);
        }
    }
}

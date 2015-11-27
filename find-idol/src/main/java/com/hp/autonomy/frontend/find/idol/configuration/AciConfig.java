package com.hp.autonomy.frontend.find.idol.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@JsonDeserialize(builder = AciConfig.Builder.class)
public class AciConfig implements ConfigurationComponent {
    private final Boolean expandQuery;
    private final String blacklist;

    private AciConfig(final Builder builder) {
        expandQuery = builder.expandQuery;
        blacklist = builder.blacklist;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public AciConfig merge(final AciConfig aciConfig) {
        final Builder builder = new Builder();
        builder.setExpandQuery(expandQuery == null ? aciConfig.expandQuery : expandQuery);
        builder.setBlacklist(blacklist == null ? aciConfig.blacklist : blacklist);

        return builder.build();
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private Boolean expandQuery;
        private String blacklist;

        public Builder(final AciConfig aciConfig) {
            expandQuery = aciConfig.expandQuery;
            blacklist = aciConfig.blacklist;
        }

        public AciConfig build() {
            return new AciConfig(this);
        }
    }
}

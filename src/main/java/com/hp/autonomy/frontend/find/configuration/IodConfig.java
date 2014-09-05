package com.hp.autonomy.frontend.find.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.configuration.ConfigurationComponent;
import com.hp.autonomy.frontend.configuration.ValidationResult;
import com.hp.autonomy.frontend.find.search.Index;
import com.hp.autonomy.frontend.find.search.IndexesService;
import java.util.List;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.client.RestClientException;

@Data
@JsonDeserialize(builder = IodConfig.Builder.class)
public class IodConfig implements ConfigurationComponent {

    private final String apiKey;
    private final List<Index> activeIndexes;

    private IodConfig(final String apiKey, final List<Index> activeIndexes) {
        this.apiKey = apiKey;
        this.activeIndexes = activeIndexes;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public ValidationResult<?> validate(final IndexesService indexesService) {
        try {
            if(StringUtils.isBlank(apiKey)) {
                return new ValidationResult<>(false, "API Key is blank");
            }

            final List<Index> indexes = indexesService.listIndexes(apiKey);
            final List<Index> activeIndexes = indexesService.listActiveIndexes();

            return new ValidationResult<>(true, new IndexResponse(indexes, activeIndexes));
        } catch (RestClientException e) {
            // TODO better handling of IOD errors
           return new ValidationResult<>(false, "Invalid API Key");
        }
    }

    public IodConfig merge(final IodConfig iod) {
        if(iod != null) {
            final Builder builder = new Builder();

            builder.setApiKey(this.apiKey == null ? iod.apiKey : this.apiKey);
            builder.setActiveIndexes(this.activeIndexes == null ? iod.activeIndexes : this.activeIndexes);

            return builder.build();
        }
        else {
            return this;
        }
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private String apiKey;
        private List<Index> activeIndexes;

        public IodConfig build() {
            return new IodConfig(apiKey, activeIndexes);
        }
    }

    @Data
    private static class IndexResponse {
        private final List<Index> indexes;
        private final List<Index> activeIndexes;

        private IndexResponse(final List<Index> indexes, final List<Index> activeIndexes) {
            this.indexes = indexes;
            this.activeIndexes = activeIndexes;
        }
    }
}

package com.hp.autonomy.frontend.find.hod.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.URL;

@Data
@JsonDeserialize(builder = HsodConfig.Builder.class)
public class HsodConfig {
    private final URL landingPageUrl;

    private HsodConfig(final Builder builder) {
        landingPageUrl = builder.landingPageUrl;
    }

    public HsodConfig merge(final HsodConfig other) {
        if (other == null) {
            return this;
        }

        return new Builder()
                .setLandingPageUrl(landingPageUrl == null ? other.landingPageUrl : landingPageUrl)
                .build();
    }

    @Setter
    @JsonPOJOBuilder(withPrefix = "set")
    @Accessors(chain = true)
    public static class Builder {
        private URL landingPageUrl;

        public HsodConfig build() {
            return new HsodConfig(this);
        }
    }
}

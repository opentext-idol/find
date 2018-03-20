/*
 * Copyright (c) 2018, Micro Focus International plc.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@JsonDeserialize(builder = ProfileOptions.ProfileOptionsBuilder.class)
public class ProfileOptions {

    /**
     * Whether we should allow the user to use intent-based-ranking to promote documents.
     */
    private final Boolean intentBasedRanking;

    /**
     * How many of the user's profiles we should use on the "My Recommendations" page.
     */
    private final Integer maxProfiles;

    /**
     * How many results per profile on the "My Recommendations" page.
     */
    private final Integer maxResultsPerProfile;

    /**
     * How many terms we should use when querying for the "My Recommendations" page.
     */
    private final Integer maxTerms;

    /**
     * Whether we should highlight terms from the user's profile on the "My Recommendations" page.
     */
    private final Boolean highlightTerms;

    /**
     * Whether we should update the user's profile whenever we view a document.
     */
    private final Boolean updateProfileOnView;

    public ProfileOptions merge(final ProfileOptions options) {
        if(options != null) {
            return builder()
                .intentBasedRanking(intentBasedRanking != null ? intentBasedRanking : options.intentBasedRanking)
                .highlightTerms(highlightTerms != null ? highlightTerms : options.highlightTerms)
                .maxProfiles(maxProfiles != null ? maxProfiles : options.maxProfiles)
                .maxResultsPerProfile(maxResultsPerProfile != null ? maxResultsPerProfile : options.maxResultsPerProfile)
                .maxTerms(maxTerms != null ? maxTerms : options.maxTerms)
                .updateProfileOnView(updateProfileOnView != null ? updateProfileOnView : options.updateProfileOnView)
                .build();
        }

        return this;
    }

    @SuppressWarnings("WeakerAccess")
    @JsonPOJOBuilder(withPrefix = "")
    public static class ProfileOptionsBuilder {

    }
}

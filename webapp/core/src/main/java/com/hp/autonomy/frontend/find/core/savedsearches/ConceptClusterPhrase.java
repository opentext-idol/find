/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA embeddable representation of a related concept cluster phrase applied to a search.
 */
@Embeddable
@Data
@NoArgsConstructor
public class ConceptClusterPhrase implements Comparable<ConceptClusterPhrase> {
    @Column(name = SavedSearch.ConceptClusterPhraseTable.Column.PHRASE)
    private String phrase;

    @Column(name = SavedSearch.ConceptClusterPhraseTable.Column.PRIMARY)
    private boolean primary;

    @Column(name = SavedSearch.ConceptClusterPhraseTable.Column.CLUSTER_ID)
    private Integer clusterId;

    @JsonCreator
    public ConceptClusterPhrase(
            @JsonProperty("phrase") final String phrase,
            @JsonProperty("primary") final boolean primary,
            @JsonProperty("clusterId") final Integer clusterId
    ) {
        this.phrase = phrase;
        this.primary = primary;
        this.clusterId = clusterId;
    }

    // Caution: method has multiple exit points.
    @Override
    public int compareTo(final ConceptClusterPhrase o) {
        if(this.primary == o.primary) {
            return 0;
        } else {
            return this.primary
                    ? -1
                    : 1;
        }
    }
}

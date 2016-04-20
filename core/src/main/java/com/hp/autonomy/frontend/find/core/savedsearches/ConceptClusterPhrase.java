/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * JPA embeddable representation of a related concept cluster phrase applied to a search.
 */
@Embeddable
@Data
@NoArgsConstructor
public class ConceptClusterPhrase {

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
}

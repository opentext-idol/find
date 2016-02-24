/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;

/**
 * JPA embeddable representation of a related concept cluster applied to a search.
 */
@Entity
@Table(name = ConceptCluster.Table.NAME)
@Data
@NoArgsConstructor
public class ConceptCluster {
    public static final String SEARCH_FIELD = "search";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = Table.Column.ID, nullable = false)
    @JsonIgnore
    private Long id;

    @Column(name = Table.Column.PRIMARY_PHRASE, nullable = false)
    private String primaryPhrase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Table.Column.SEARCH_ID, referencedColumnName = SavedSearch.Table.Column.ID, nullable = false)
    @JsonIgnore
    private SavedSearch<?> search;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = PhrasesTable.NAME,
            joinColumns = {
                    @JoinColumn(name = PhrasesTable.Column.ID, referencedColumnName = Table.Column.ID, table = PhrasesTable.NAME, nullable = false)
            }
    )
    @Column(name = PhrasesTable.Column.PHRASE, nullable = false)
    private Collection<String> phrases;

    @JsonCreator
    public ConceptCluster(
        @JsonProperty("primaryPhrase") final String primaryPhrase,
        @JsonProperty(value = "phrases", required = false) final Collection<String> phrases
    ) {
        this.primaryPhrase = primaryPhrase;
        this.phrases = phrases;
    }

    public interface Table {
        String NAME = "search_concept_clusters";

        interface Column {
            String ID = "search_concept_cluster_id";
            String SEARCH_ID = "search_id";
            String PRIMARY_PHRASE = "primary_phrase";
        }
    }

    public interface PhrasesTable {
        String NAME = "concept_cluster_phrases";

        interface Column {
            String ID = "search_concept_cluster_id";
            String PHRASE = "phrase";
        }
    }
}

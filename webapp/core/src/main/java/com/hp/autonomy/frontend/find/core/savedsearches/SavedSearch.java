/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Entity
@Table(name = SavedSearch.Table.NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "search_type")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Access(AccessType.FIELD)
public abstract class SavedSearch<T extends SavedSearch<T, B>, B extends SavedSearch.Builder<T, B>> {
    @Id
    @Column(name = Table.Column.ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @SuppressWarnings("InstanceVariableOfConcreteClass")
    @CreatedBy
    @ManyToOne
    @JoinColumn(name = Table.Column.USER_ID)
    private UserEntity user;

    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = IndexesTable.NAME, joinColumns = @JoinColumn(name = IndexesTable.Column.SEARCH_ID))
    private Set<EmbeddableIndex> indexes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = ParametricValuesTable.NAME, joinColumns = @JoinColumn(name = ParametricValuesTable.Column.SEARCH_ID))
    private Set<FieldAndValue> parametricValues;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = NumericRangeRestrictionsTable.NAME, joinColumns = @JoinColumn(name = NumericRangeRestrictionsTable.Column.SEARCH_ID))
    private Set<NumericRangeRestriction> numericRangeRestrictions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = DateRangeRestrictionsTable.NAME, joinColumns = @JoinColumn(name = DateRangeRestrictionsTable.Column.SEARCH_ID))
    private Set<DateRangeRestriction> dateRangeRestrictions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = ConceptClusterPhraseTable.NAME, joinColumns = @JoinColumn(name = ConceptClusterPhraseTable.Column.SEARCH_ID))
    private Set<ConceptClusterPhrase> conceptClusterPhrases;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = GeographyFilterTable.NAME, joinColumns = @JoinColumn(name = GeographyFilterTable.Column.SEARCH_ID))
    private Set<GeographyFilter> geographyFilters;

    /**
     * Document whitelist or blacklist.
     *
     * @see documentSelectionIsWhitelist
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = DocumentSelectionTable.NAME,
        joinColumns = @JoinColumn(name = DocumentSelectionTable.Column.SEARCH_ID))
    private Set<DocumentSelection> documentSelection;

    @Column(name = Table.Column.START_DATE)
    private ZonedDateTime minDate;

    @Column(name = Table.Column.END_DATE)
    private ZonedDateTime maxDate;

    @CreatedDate
    @Column(name = Table.Column.CREATED_DATE)
    private ZonedDateTime dateCreated;

    @LastModifiedDate
    @Column(name = Table.Column.MODIFIED_DATE)
    private ZonedDateTime dateModified;

    @Transient
    private DateRange dateRange;

    @Column(name = Table.Column.ACTIVE)
    @JsonIgnore
    private Boolean active;

    @Column(name = Table.Column.MIN_SCORE, nullable = false)
    private Integer minScore = 0;

    @Transient
    private boolean canEdit = true;

    /**
     * If true, {@link documentSelection} is a whitelist; if false, {@link documentSelection} is a
     * blacklist.
     */
    @Column(name = Table.Column.DOCUMENT_SELECTION_IS_WHITELIST, nullable = false)
    private Boolean documentSelectionIsWhitelist = false;

    protected SavedSearch(final Builder<?, ?> builder) {
        id = builder.id;
        title = builder.title;
        indexes = builder.indexes;
        parametricValues = builder.parametricValues;
        numericRangeRestrictions = builder.numericRangeRestrictions;
        dateRangeRestrictions = builder.dateRangeRestrictions;
        conceptClusterPhrases = builder.conceptClusterPhrases;
        geographyFilters = builder.geographyFilters;
        documentSelection = builder.documentSelection;
        minDate = builder.minDate;
        maxDate = builder.maxDate;
        dateCreated = builder.dateCreated;
        dateModified = builder.dateModified;
        dateRange = builder.dateRange;
        active = builder.active;
        minScore = builder.minScore;
        canEdit = builder.canEdit;
        user = builder.user;
        documentSelectionIsWhitelist = builder.documentSelectionIsWhitelist;
    }

    /**
     * Merge client-mutable SavedSearch-implementation specific fields from the other search into this one.
     */
    protected abstract void mergeInternal(T other);

    public abstract B toBuilder();

    /**
     * Merge client-mutable fields from the other search into this one.
     */
    @SuppressWarnings("OverlyComplexMethod")
    public void merge(final T other) {
        if (other != null) {
            // note: we're deliberately not merging in the user field
            mergeInternal(other);

            title = other.getTitle() == null ? title : other.getTitle();
            minDate = other.getMinDate() == null ? minDate : other.getMinDate();
            maxDate = other.getMaxDate() == null ? maxDate : other.getMaxDate();
            minScore = other.getMinScore() == null ? minScore : other.getMinScore();
            dateRange = other.getDateRange() == null ? dateRange : other.getDateRange();

            indexes = other.getIndexes() == null ? indexes : other.getIndexes();
            parametricValues = other.getParametricValues() == null ? parametricValues : other.getParametricValues();
            numericRangeRestrictions = Optional.ofNullable(other.getNumericRangeRestrictions()).orElse(numericRangeRestrictions);
            dateRangeRestrictions = Optional.ofNullable(other.getDateRangeRestrictions()).orElse(dateRangeRestrictions);
            documentSelectionIsWhitelist = other.getDocumentSelectionIsWhitelist() == null ?
                documentSelectionIsWhitelist : other.getDocumentSelectionIsWhitelist();

            if (other.getConceptClusterPhrases() != null) {
                conceptClusterPhrases.clear();
                conceptClusterPhrases.addAll(other.getConceptClusterPhrases());
            }

            if (other.getGeographyFilters() != null) {
                geographyFilters.clear();
                geographyFilters.addAll(other.getGeographyFilters());
            }

            if (other.getDocumentSelection() != null) {
                documentSelection.clear();
                documentSelection.addAll(other.getDocumentSelection());
            }
        }
    }

    @SuppressWarnings("unused")
    @Access(AccessType.PROPERTY)
    @Column(name = Table.Column.DATE_RANGE_TYPE)
    @JsonIgnore
    public Integer getDateRangeInt() {
        return dateRange == null ? null : dateRange.getId();
    }

    @SuppressWarnings("unused")
    public void setDateRangeInt(final Integer dateRangeInt) {
        dateRange = DateRange.getType(dateRangeInt);
    }

    // WARNING: This logic is duplicated in the client-side search-data-util
    // Caution: Method has multiple exit points.
    public String toQueryText() {
        if (conceptClusterPhrases.isEmpty()) {
            return "*";
        } else {
            final Collection<List<ConceptClusterPhrase>> groupedClusters = conceptClusterPhrases.stream()
                    .collect(Collectors.groupingBy(ConceptClusterPhrase::getClusterId)).values();

            return groupedClusters.stream()
                    .map(clusterList -> clusterList.stream()
                            .sorted()
                            .map(ConceptClusterPhrase::getPhrase).collect(toList()))
                    .map(clusterPhrases -> wrapInBrackets(StringUtils.join(clusterPhrases, ' ')))
                    .collect(joining(" AND "));
        }
    }

    private String wrapInBrackets(final String input) {
        return input.isEmpty() ? input : '(' + input + ')';
    }

    protected interface Table {
        String NAME = "searches";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String ID = "search_id";
            String USER_ID = "user_id";
            String START_DATE = "start_date";
            String END_DATE = "end_date";
            String CREATED_DATE = "created_date";
            String MODIFIED_DATE = "modified_date";
            String ACTIVE = "active";
            String TOTAL_RESULTS = "total_results";
            String DATE_RANGE_TYPE = "date_range_type";
            String MIN_SCORE = "min_score";
            String DOCUMENT_SELECTION_IS_WHITELIST = "document_selection_is_whitelist";
        }
    }

    private interface IndexesTable {
        String NAME = "search_indexes";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
        }
    }

    private interface ParametricValuesTable {
        String NAME = "search_parametric_values";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
        }
    }

    private interface NumericRangeRestrictionsTable {
        String NAME = "search_numeric_ranges";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
        }
    }

    private interface DateRangeRestrictionsTable {
        String NAME = "search_date_ranges";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
        }
    }

    protected interface StoredStateTable {
        String NAME = "search_stored_state";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
        }
    }

    interface ConceptClusterPhraseTable {
        String NAME = "search_concept_cluster_phrases";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String ID = "search_concept_cluster_phrase_id";
            String SEARCH_ID = "search_id";
            String PHRASE = "phrase";
            String PRIMARY = "primary_phrase";
            String CLUSTER_ID = "cluster_id";
        }
    }

    interface GeographyFilterTable {
        String NAME = "search_geography_filters";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String ID = "search_geography_filter_id";
            String SEARCH_ID = "search_id";
            String FIELD = "field";
            String JSON = "json";
        }
    }

    interface DocumentSelectionTable {
        String NAME = "search_document_selection";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String ID = "search_document_selection_id";
            String SEARCH_ID = "search_id";
            String REFERENCE = "reference";
        }
    }

    @SuppressWarnings("WeakerAccess")
    @NoArgsConstructor
    @Getter
    public abstract static class Builder<T extends SavedSearch<T, B>, B extends Builder<T, B>> {
        private Long id;
        private String title;
        private Set<EmbeddableIndex> indexes;
        private Set<FieldAndValue> parametricValues;
        private Set<NumericRangeRestriction> numericRangeRestrictions;
        private Set<DateRangeRestriction> dateRangeRestrictions;
        private Set<ConceptClusterPhrase> conceptClusterPhrases;
        private Set<GeographyFilter> geographyFilters;
        private Set<DocumentSelection> documentSelection;
        private ZonedDateTime minDate;
        private ZonedDateTime maxDate;
        private ZonedDateTime dateCreated;
        private ZonedDateTime dateModified;
        private DateRange dateRange;
        private Boolean active = true;
        private Integer minScore;
        private boolean canEdit = true;
        private Boolean documentSelectionIsWhitelist = false;
        private UserEntity user;

        /**
         * Populate a builder with fields common to all {@link SavedSearch} types.
         */
        protected Builder(final SavedSearch<?, ?> search) {
            id = search.id;
            title = search.title;
            indexes = search.indexes;
            parametricValues = search.parametricValues;
            numericRangeRestrictions = search.numericRangeRestrictions;
            dateRangeRestrictions = search.dateRangeRestrictions;
            conceptClusterPhrases = search.conceptClusterPhrases;
            geographyFilters = search.geographyFilters;
            documentSelection = search.documentSelection;
            minDate = search.minDate;
            maxDate = search.maxDate;
            dateCreated = search.dateCreated;
            dateModified = search.dateModified;
            dateRange = search.dateRange;
            active = search.active;
            minScore = search.minScore;
            canEdit = search.canEdit;
            documentSelectionIsWhitelist = search.documentSelectionIsWhitelist;
            user = search.user;
        }

        public abstract T build();

        public Builder<T, B> setId(final Long id) {
            this.id = id;
            return this;
        }

        public Builder<T, B> setTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder<T, B> setIndexes(final Set<EmbeddableIndex> indexes) {
            this.indexes = new LinkedHashSet<>(indexes);
            return this;
        }

        public Builder<T, B> setParametricValues(final Set<FieldAndValue> parametricValues) {
            this.parametricValues = new LinkedHashSet<>(parametricValues);
            return this;
        }

        public Builder<T, B> setNumericRangeRestrictions(final Set<NumericRangeRestriction> numericRangeRestrictions) {
            this.numericRangeRestrictions = new LinkedHashSet<>(numericRangeRestrictions);
            return this;
        }

        public Builder<T, B> setDateRangeRestrictions(final Set<DateRangeRestriction> dateRangeRestrictions) {
            this.dateRangeRestrictions = new LinkedHashSet<>(dateRangeRestrictions);
            return this;
        }

        public Builder<T, B> setConceptClusterPhrases(final Set<ConceptClusterPhrase> conceptClusterPhrases) {
            this.conceptClusterPhrases = new LinkedHashSet<>(conceptClusterPhrases);
            return this;
        }

        public Builder<T, B> setGeographyFilters(final Set<GeographyFilter> geographyFilters) {
            this.geographyFilters = new LinkedHashSet<>(geographyFilters);
            return this;
        }

        public Builder<T, B> setDocumentSelection(final Set<DocumentSelection> documentSelection) {
            this.documentSelection = new LinkedHashSet<>(documentSelection);
            return this;
        }

        public Builder<T, B> setMinDate(final ZonedDateTime minDate) {
            this.minDate = minDate;
            return this;
        }

        public Builder<T, B> setMaxDate(final ZonedDateTime maxDate) {
            this.maxDate = maxDate;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T, B> setDateCreated(final ZonedDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        @SuppressWarnings("unused")
        public Builder<T, B> setDateModified(final ZonedDateTime dateModified) {
            this.dateModified = dateModified;
            return this;
        }

        public Builder<T, B> setDateRange(final DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder<T, B> setActive(final Boolean active) {
            this.active = active;
            return this;
        }

        public Builder<T, B> setMinScore(final Integer minScore) {
            this.minScore = minScore;
            return this;
        }

        public Builder<T, B> setCanEdit(final boolean canEdit) {
            this.canEdit = canEdit;
            return this;
        }

        public Builder<T, B> setDocumentSelectionIsWhitelist(
            final boolean documentSelectionIsWhitelist
        ) {
            this.documentSelectionIsWhitelist = documentSelectionIsWhitelist;
            return this;
        }

    }
}

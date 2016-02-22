package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
import java.util.*;

@Entity
@Table(name = "searches")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "search_type")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDefs(@TypeDef(name = SavedSearch.JADIRA_TYPE_NAME, typeClass = PersistentDateTime.class))
public abstract class SavedSearch {
    public static final String JADIRA_TYPE_NAME = "jadira";

    @Id
    @Column(name = "search_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    private String title;

    @Column(name = "query_text")
    private String queryText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_indexes", joinColumns = {
            @JoinColumn(name = "search_id")
    })
    private Set<EmbeddableIndex> indexes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_parametric_values", joinColumns = {
            @JoinColumn(name = "search_id")
    })
    private Set<FieldAndValue> parametricValues;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_related_concepts", joinColumns = {
            @JoinColumn(name = "search_id")
    })
    @Column(name = "concept")
    private Set<String> relatedConcepts;

    @Column(name = "start_date")
    @Type(type = JADIRA_TYPE_NAME)
    private DateTime minDate;

    @Column(name = "end_date")
    @Type(type = JADIRA_TYPE_NAME)
    private DateTime maxDate;

    @CreatedDate
    @Column(name = "created_date")
    @Type(type = JADIRA_TYPE_NAME)
    private DateTime dateCreated;

    @LastModifiedDate
    @Column(name = "modified_date")
    @Type(type = JADIRA_TYPE_NAME)
    private DateTime dateModified;

    @Column(name = "date_range_type")
    private Integer dateRange;

    @Column(name = "active")
    @JsonIgnore
    private Boolean active;

    protected SavedSearch(final Builder<?> builder) {
        id = builder.id;
        title = builder.title;
        queryText = builder.queryText;
        indexes = builder.indexes;
        parametricValues = builder.parametricValues;
        relatedConcepts = builder.relatedConcepts;
        minDate = builder.minDate;
        maxDate = builder.maxDate;
        dateCreated = builder.dateCreated;
        dateModified = builder.dateModified;
        dateRange = builder.dateRange;
        active = builder.active;
    }

    // WARNING: This logic is duplicated in the client-side QueryTextModel
    public String toQueryText() {
        if (CollectionUtils.isEmpty(relatedConcepts)) {
            return queryText;
        } else {
            final Collection<String> quotedConcepts = new LinkedList<>();

            for (final String phrase : relatedConcepts) {
                quotedConcepts.add(wrapQuotes(phrase));
            }

            return '(' + queryText + ") " + StringUtils.join(quotedConcepts, ' ');
        }
    }

    private String wrapQuotes(final String input) {
        return '"' + input + '"';
    }

    // WARNING: This logic is duplicated in the client side SelectedValuesCollection
    public String toFieldText() {
        if (CollectionUtils.isEmpty(parametricValues)) {
            return "";
        } else {
            final Map<String, List<String>> fieldToValues = new HashMap<>();

            for (final FieldAndValue fieldAndValue : parametricValues) {
                List<String> values = fieldToValues.get(fieldAndValue.getField());

                if (values == null) {
                    values = new LinkedList<>();
                    fieldToValues.put(fieldAndValue.getField(), values);
                }

                values.add(fieldAndValue.getValue());
            }

            final Iterator<Map.Entry<String, List<String>>> iterator = fieldToValues.entrySet().iterator();
            FieldText fieldText = fieldAndValuesToFieldText(iterator.next());

            while (iterator.hasNext()) {
                fieldText = fieldText.AND(fieldAndValuesToFieldText(iterator.next()));
            }

            return fieldText.toString();
        }
    }

    private FieldText fieldAndValuesToFieldText(final Map.Entry<String, List<String>> fieldAndValues) {
        return new MATCH(fieldAndValues.getKey(), fieldAndValues.getValue());
    }

    @Getter
    @NoArgsConstructor
    public static abstract class Builder<T extends SavedSearch> {
        private Long id;
        private String title = "";
        private String queryText = "";
        private Set<EmbeddableIndex> indexes;
        private Set<FieldAndValue> parametricValues = new HashSet<>(0);
        private Set<String> relatedConcepts = new HashSet<>(0);
        private DateTime minDate;
        private DateTime maxDate;
        private DateTime dateCreated;
        private DateTime dateModified;
        private Integer dateRange;
        private Boolean active = true;

        public Builder(final SavedSearch search) {
            id = search.id;
            title = search.title;
            queryText = search.queryText;
            indexes = search.indexes;
            parametricValues = search.parametricValues;
            relatedConcepts = search.relatedConcepts;
            minDate = search.minDate;
            maxDate = search.maxDate;
            dateCreated = search.dateCreated;
            dateModified = search.dateModified;
            dateRange = search.dateRange;
            active = search.active;
        }

        public abstract T build();

        public Builder<T> setId(final Long id) {
            this.id = id;
            return this;
        }

        public Builder<T> setTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder<T> setQueryText(final String queryText) {
            this.queryText = queryText;
            return this;
        }

        public Builder<T> setIndexes(final Set<EmbeddableIndex> indexes) {
            this.indexes = indexes;
            return this;
        }

        public Builder<T> setParametricValues(final Set<FieldAndValue> parametricValues) {
            this.parametricValues = parametricValues;
            return this;
        }

        public Builder<T> setRelatedConcepts(final Set<String> relatedConcepts) {
            this.relatedConcepts = relatedConcepts;
            return this;
        }

        public Builder<T> setMinDate(final DateTime minDate) {
            this.minDate = minDate;
            return this;
        }

        public Builder<T> setMaxDate(final DateTime maxDate) {
            this.maxDate = maxDate;
            return this;
        }

        public Builder<T> setDateCreated(final DateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder<T> setDateModified(final DateTime dateModified) {
            this.dateModified = dateModified;
            return this;
        }

        public Builder<T> setDateRange(final Integer dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder<T> setActive(final Boolean active) {
            this.active = active;
            return this;
        }
    }
}

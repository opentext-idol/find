package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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
    private Integer id;

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

    @Column(name = "active")
    @JsonIgnore
    private Boolean active;

    protected SavedSearch(Builder<?> builder) {
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
        active = builder.active;
    }

    @Getter
    @NoArgsConstructor
    public static abstract class Builder<T extends SavedSearch> {
        private Integer id;
        private String title = "";
        private String queryText = "";
        private Set<EmbeddableIndex> indexes;
        private Set<FieldAndValue> parametricValues = new HashSet<>(0);
        private Set<String> relatedConcepts = new HashSet<>(0);
        private DateTime minDate;
        private DateTime maxDate;
        private DateTime dateCreated;
        private DateTime dateModified;
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
            active = search.active;
        }

        public abstract T build();

        public Builder<T> setId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder<T> setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder<T> setQueryText(String queryText) {
            this.queryText = queryText;
            return this;
        }

        public Builder<T> setIndexes(Set<EmbeddableIndex> indexes) {
            this.indexes = indexes;
            return this;
        }

        public Builder<T> setParametricValues(Set<FieldAndValue> parametricValues) {
            this.parametricValues = parametricValues;
            return this;
        }

        public Builder<T> setRelatedConcepts(Set<String> relatedConcepts) {
            this.relatedConcepts = relatedConcepts;
            return this;
        }

        public Builder<T> setMinDate(DateTime minDate) {
            this.minDate = minDate;
            return this;
        }

        public Builder<T> setMaxDate(DateTime maxDate) {
            this.maxDate = maxDate;
            return this;
        }

        public Builder<T> setDateCreated(DateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder<T> setDateModified(DateTime dateModified) {
            this.dateModified = dateModified;
            return this;
        }

        public Builder<T> setActive(Boolean active) {
            this.active = active;
            return this;
        }
    }
}

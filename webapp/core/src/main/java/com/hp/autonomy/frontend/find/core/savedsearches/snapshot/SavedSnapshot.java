package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import java.util.Set;

@Entity
@DiscriminatorValue(SavedSearchType.Values.SNAPSHOT)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedSnapshot.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedSnapshot extends SavedSearch<SavedSnapshot, SavedSnapshot.Builder> {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = StoredStateTable.NAME, joinColumns = @JoinColumn(name = StoredStateTable.Column.SEARCH_ID))
    private Set<TypedStateToken> stateTokens;

    @Column(name = Table.Column.TOTAL_RESULTS)
    private Long resultCount;

    private SavedSnapshot(final Builder builder) {
        super(builder);
        stateTokens = builder.stateTokens;
        resultCount = builder.resultCount;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    protected void mergeInternal(final SavedSnapshot other) {
        stateTokens = other.stateTokens == null ? stateTokens : other.stateTokens;
        resultCount = other.resultCount == null ? resultCount : other.resultCount;
    }

    @NoArgsConstructor
    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends SavedSearch.Builder<SavedSnapshot, Builder> {
        private Set<TypedStateToken> stateTokens;
        private Long resultCount;

        /**
         * Populate a builder with fields common to all {@link SavedSearch} types.
         */
        public Builder(final SavedSearch<?, ?> search) {
            super(search);
        }

        public Builder(final SavedSnapshot snapshot) {
            super(snapshot);

            stateTokens = snapshot.stateTokens;
            resultCount = snapshot.resultCount;
        }

        @Override
        public SavedSnapshot build() {
            return new SavedSnapshot(this);
        }
    }

}

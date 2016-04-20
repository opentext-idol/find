package com.hp.autonomy.frontend.find.core.savedsearches.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
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
import java.util.List;

@Entity
@DiscriminatorValue(SavedSearchType.Values.SNAPSHOT)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedSnapshot.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedSnapshot extends SavedSearch<SavedSnapshot> {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = StoredStateTable.NAME, joinColumns = {
            @JoinColumn(name = StoredStateTable.Column.SEARCH_ID)
    })
    @Column(name = StoredStateTable.Column.STATE_TOKEN)
    private List<String> stateTokens;

    @Column(name = Table.Column.TOTAL_RESULTS)
    private Long resultCount;

    private SavedSnapshot(final Builder builder) {
        super(builder);
        stateTokens = builder.stateTokens;
        resultCount = builder.resultCount;
    }

    @Override
    protected void mergeInternal(final SavedSnapshot other) {
        stateTokens = other.getStateTokens() == null ? stateTokens : other.getStateTokens();
        resultCount = other.getResultCount() == null ? resultCount : other.getResultCount();
    }

    @NoArgsConstructor
    @Setter
    @Accessors(chain = true)
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends SavedSearch.Builder<SavedSnapshot> {
        private List<String> stateTokens;
        private Long resultCount;

        public Builder(final SavedSnapshot snapshot) {
            super(snapshot);

            stateTokens = snapshot.stateTokens;
            resultCount = snapshot.resultCount;
        }

        @Override
        public SavedSnapshot build() {return new SavedSnapshot(this);}
    }
}

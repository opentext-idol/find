package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue(SavedSearchType.Values.SNAPSHOT)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedSnapshot.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedSnapshot extends SavedSearch {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_stored_state", joinColumns = {
            @JoinColumn(name = "search_id")
    })
    @Column(name = "state_token")
    private List<String> stateToken;

    @Column(name = "total_results")
    private Long resultCount;

    private SavedSnapshot(final Builder builder) {
        super(builder);
        stateToken = builder.stateToken;
        resultCount = builder.resultCount;
    }

    @NoArgsConstructor
    @Getter
    @Accessors(chain = true)
    public static class Builder extends SavedSearch.Builder<SavedSnapshot> {
        private List<String> stateToken;
        private Long resultCount;

        public Builder(final SavedSnapshot snapshot) {
            super(snapshot);

            stateToken = snapshot.stateToken;
            resultCount = snapshot.resultCount;
        }

        public Builder setStateToken(List<String> stateToken) {
            this.stateToken = stateToken;
            return this;
        }

        public Builder setResultCount(Long resultCount) {
            this.resultCount = resultCount;
            return this;
        }

        @Override
        public SavedSnapshot build() {return new SavedSnapshot(this);}
    }
}

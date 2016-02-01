package com.hp.autonomy.frontend.find.core.savedsearches.savedsnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearch;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(SavedSearchType.Values.SNAPSHOT)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = SavedSnapshot.Builder.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedSnapshot extends SavedSearch {

    @Column(name = "state_token")
    private String stateToken;

    @Column(name = "result_count")
    private Long resultCount;

    private SavedSnapshot(final Builder builder) {
        super(builder);
    }

    @NoArgsConstructor
    @Getter
    @Accessors(chain = true)
    public static class Builder extends SavedSearch.Builder<SavedSnapshot> {
        private String stateToken;
        private Long resultCount;

        public Builder(final SavedSnapshot snapshot) {
            super(snapshot);

            stateToken = snapshot.stateToken;
            resultCount = snapshot.resultCount;
        }

        @Override
        public SavedSnapshot build() {return new SavedSnapshot(this);}
    }
}


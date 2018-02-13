/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static com.hp.autonomy.frontend.find.core.savedsearches.SharedToEveryone.Table.NAME;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Entity
@Table(name = NAME)
@Data
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@JsonDeserialize(builder = SharedToEveryone.SharedToEveryoneBuilder.class)
public class SharedToEveryone {
    private SharedToEveryone(final SharedToEveryone.SharedToEveryoneBuilder builder) {
        savedSearch = builder.savedSearch;
        sharedDate = builder.sharedDate;
        modifiedDate = builder.modifiedDate;
        id = new SharedToEveryonePK(savedSearch.getId());
    }

    @EmbeddedId
    public SharedToEveryonePK id;

    @MapsId("searchId")
    @JoinColumn(name = Table.Column.SEARCH_ID, referencedColumnName = SavedSearch.Table.Column.ID)
    @ManyToOne
    private SavedSearch<?, ?> savedSearch;

    @CreatedDate
    @Column(name = Table.Column.SHARED_DATE)
    private ZonedDateTime sharedDate;

    @LastModifiedDate
    @Column(name = Table.Column.MODIFIED_DATE)
    private ZonedDateTime modifiedDate;

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class SharedToEveryoneBuilder {
        private SavedSearch<?, ?> savedSearch;

        @JsonProperty("searchId")
        public SharedToEveryoneBuilder searchId(final Long searchId) {
            savedSearch = new SavedQuery.Builder().setId(searchId).build();
            return this;
        }

        public SharedToEveryone build() {
            return new SharedToEveryone(this);
        }
    }

    public void merge(final SharedToEveryone other) {
        if (other != null) {
            sharedDate = other.sharedDate == null ? sharedDate : other.sharedDate;
        }
    }

    public interface Table {
        String NAME = "shared_to_everyone";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
            String SHARED_DATE = "shared_date";
            String MODIFIED_DATE = "modified_date";
        }
    }

}

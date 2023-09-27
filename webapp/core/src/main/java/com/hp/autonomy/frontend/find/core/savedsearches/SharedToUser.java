/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

import static com.hp.autonomy.frontend.find.core.savedsearches.SharedToUser.Table.NAME;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Entity
@Table(name = NAME)
@Data
@Builder
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(builder = SharedToUser.SharedToUserBuilder.class)
public class SharedToUser {
    private SharedToUser(final SharedToUser.SharedToUserBuilder builder) {
        savedSearch = builder.savedSearch;
        user = builder.user.build();
        canEdit = builder.canEdit;
        sharedDate = builder.sharedDate;
        modifiedDate = builder.modifiedDate;
        id = new SharedToUserPK(savedSearch.getId(), user.getUserId());
    }

    @EmbeddedId
    public SharedToUserPK id;

    @MapsId("searchId")
    @JoinColumn(name = Table.Column.SEARCH_ID, referencedColumnName = SavedSearch.Table.Column.ID)
    @ManyToOne
    private SavedSearch<?, ?> savedSearch;

    @MapsId("userId")
    @JoinColumn(name = Table.Column.USER_ID, referencedColumnName = UserEntity.Table.Column.USER_ID)
    @ManyToOne
    private UserEntity user;

    @Column(name = Table.Column.CAN_EDIT)
    private Boolean canEdit;

    @CreationTimestamp
    @Column(name = Table.Column.SHARED_DATE, columnDefinition = "DATETIME")
    private ZonedDateTime sharedDate;

    @UpdateTimestamp
    @Column(name = Table.Column.MODIFIED_DATE, columnDefinition = "DATETIME")
    private ZonedDateTime modifiedDate;

    @SuppressWarnings({"WeakerAccess", "unused"})
    @JsonPOJOBuilder(withPrefix = "")
    public static class SharedToUserBuilder {
        private SavedSearch<?, ?> savedSearch;
        private final UserEntity.UserEntityBuilder user = UserEntity.builder();

        @JsonProperty("searchId")
        public SharedToUserBuilder searchId(final Long searchId) {
            savedSearch = new SavedQuery.Builder().setId(searchId).build();
            return this;
        }

        @JsonProperty("userId")
        public SharedToUserBuilder userId(final Long userId) {
            user.userId(userId);
            return this;
        }

        @JsonProperty("username")
        public SharedToUserBuilder username(final String username) {
            user.username(username);
            return this;
        }

        // used to suppress generated method
        private SharedToUserBuilder user(final UserEntity userEntity) {
            return this;
        }

        public SharedToUser build() {
            return new SharedToUser(this);
        }
    }

    public void merge(final SharedToUser other) {
        if (other != null) {
            sharedDate = other.sharedDate == null ? sharedDate : other.sharedDate;
        }
    }

    public interface Table {
        String NAME = "shared_to_users";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String SEARCH_ID = "search_id";
            String USER_ID = "user_id";
            String CAN_EDIT = "can_edit";
            String SHARED_DATE = "shared_date";
            String MODIFIED_DATE = "modified_date";
        }
    }

}

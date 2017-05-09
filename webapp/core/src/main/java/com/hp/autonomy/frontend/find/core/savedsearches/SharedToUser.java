/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

import static com.hp.autonomy.frontend.find.core.savedsearches.SharedToUser.Table.NAME;

@SuppressWarnings("InstanceVariableOfConcreteClass")
@Entity
@Table(name = NAME)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedToUser implements Serializable {
    private static final long serialVersionUID = 5810410840618727845L;

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

    @Column(name = Table.Column.SHARED_DATE)
    private ZonedDateTime sharedDate;

    @Column(name = Table.Column.MODIFIED_DATE)
    private ZonedDateTime modifiedDate;

    public interface Table {
        String NAME = "shared_to_user";

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

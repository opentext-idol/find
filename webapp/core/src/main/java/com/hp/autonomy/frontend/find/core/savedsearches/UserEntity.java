/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Single entity that defines multiple types of user for our various implementations.
 *
 * We cannot use inheritance in this case because it would mean we would have to employ the
 * {@link org.hibernate.annotations.Any} annotation on the user entity of {@link SavedSearch}.
 *
 * This annotation requires you to define in place all the possible concrete types the field could
 * take at runtime.  This in turn would mean we have to define implementation-specific children of
 * {@link SavedSearch}, but then we would lose the centralisation of {@link SavedQuery} and other search types.
 */
@Entity
@Table(name = UserEntity.Table.NAME)
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = "username")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @Column(name = Table.Column.USER_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    public interface Table {
        String NAME = "users";

        @SuppressWarnings("InnerClassTooDeeplyNested")
        interface Column {
            String USER_ID = "user_id";
        }
    }
}

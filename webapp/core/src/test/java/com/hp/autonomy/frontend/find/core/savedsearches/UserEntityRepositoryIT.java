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

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public abstract class UserEntityRepositoryIT extends AbstractFindIT {
    @SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Test
    public void fetchAll() {
        final List<UserEntity> users = listUsers();
        assertThat(users, hasSize(1));
    }

    @Test
    public void saveNew() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUsername("username@hpe.com");

        final int startingNumberOfUsers = listUsers().size();

        final UserEntity savedEntity = userEntityRepository.save(userEntity);
        assertThat(savedEntity.getUserId(), not(nullValue()));

        final List<UserEntity> users = listUsers();
        assertThat(users, hasSize(startingNumberOfUsers + 1));
    }

    private List<UserEntity> listUsers() {
        final List<UserEntity> users = new LinkedList<>();

        for (final UserEntity userEntity : userEntityRepository.findAll()) {
            users.add(userEntity);
        }

        return users;
    }
}

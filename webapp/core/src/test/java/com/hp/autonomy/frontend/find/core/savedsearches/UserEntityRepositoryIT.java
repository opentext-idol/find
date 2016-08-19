/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.test.AbstractFindIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public abstract class UserEntityRepositoryIT extends AbstractFindIT {
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    protected UserEntityRepository userEntityRepository;

    @Test
    public void fetchNone() {
        final List<UserEntity> users = listUsers();
        assertThat(users, hasSize(0));
    }

    @Test
    public void createWithUidAndFetch() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUid(1L);

        final UserEntity savedEntity = userEntityRepository.save(userEntity);
        assertThat(savedEntity.getUserId(), not(nullValue()));

        final List<UserEntity> users = listUsers();
        assertThat(users, hasSize(1));
    }

    @Test
    public void createWithUuidAndUserStore() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setDomain("DOMAIN");
        userEntity.setUserStore("DEFAULT_USERSTORE");
        userEntity.setUuid(UUID.randomUUID());

        final UserEntity savedEntity = userEntityRepository.save(userEntity);
        assertThat(savedEntity.getUserId(), not(nullValue()));

        final List<UserEntity> users = listUsers();
        assertThat(users, hasSize(1));
    }

    private List<UserEntity> listUsers() {
        final List<UserEntity> users = new LinkedList<>();

        for (final UserEntity userEntity : userEntityRepository.findAll()) {
            users.add(userEntity);
        }

        return users;
    }
}

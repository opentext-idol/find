/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SharedToUserController.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SharedToUserControllerTest {
    @MockBean
    private SharedToUserRepository sharedToUserRepository;

    @MockBean
    private SharedToEveryoneRepository sharedToEveryoneRepository;

    @MockBean
    private SharedToUserService sharedToUserService;

    @MockBean
    private UserEntityService userEntityService;

    @Autowired
    private SharedToUserController controller;

    @Test
    public void getPermittedUsersForSearch() {
        when(sharedToUserRepository.findBySavedSearch_Id(anyLong())).thenReturn(Collections.singleton(mock(SharedToUser.class)));
        assertThat(controller.getPermissionsForSearch(1L, null), not(empty()));
    }

    @Test
    public void save() throws IOException {
        final UserEntity user = new UserEntity();
        user.setUserId(2L);
        user.setUsername("bob");

        final SharedToUser join = new SharedToUser();
        join.setUser(user);

        controller.save(join, 3);
        verify(sharedToUserService).save(Matchers.<SharedToUser>any());
    }

    @Test
    public void delete() throws IOException {
        controller.delete(2L, 3L);
        verify(sharedToUserRepository).delete(Matchers.<SharedToUserPK>any());
    }
}

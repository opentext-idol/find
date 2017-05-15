/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
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
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SharedToUserController.class, properties = BiConfiguration.BI_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SharedToUserControllerTest {
    private static String json;

    @BeforeClass
    public static void init() throws IOException {
        json = IOUtils.toString(SharedToUserControllerTest.class.getResourceAsStream("/shared-to-users.json"));
    }

    @MockBean
    private SharedToUserRepository sharedToUserRepository;

    @Autowired
    private SharedToUserController controller;

    @Test
    public void getPermittedUsersForSearch() {
        when(sharedToUserRepository.findBySavedSearch_Id(any())).thenReturn(Collections.singleton(SharedToUser.builder().build()));
        assertThat(controller.getPermissionsForSearch("1"), not(empty()));
    }

    @Test
    public void save() throws IOException {
        controller.save(json);
        verify(sharedToUserRepository).save(Matchers.<Iterable<? extends SharedToUser>>any());
    }

    @Test
    public void delete() throws IOException {
        controller.delete(json);
        verify(sharedToUserRepository).delete(Matchers.<Iterable<? extends SharedToUser>>any());
    }
}

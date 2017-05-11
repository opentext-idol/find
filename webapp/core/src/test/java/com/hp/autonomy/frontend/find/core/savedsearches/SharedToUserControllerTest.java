/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.frontend.find.core.beanconfiguration.BiConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SharedToUserController.class, properties = BiConfiguration.BI_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SharedToUserControllerTest {
    @MockBean
    private SharedToUserRepository sharedToUserRepository;
    @Autowired
    private SharedToUserController controller;

    @Test
    public void getPermittedUsersForSearch() {
        controller.getPermittedUsersForSearch("1");
        verify(sharedToUserRepository).findBySavedSearch_Id(any());
    }
}

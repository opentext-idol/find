/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.users;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersConfig;
import com.hp.autonomy.frontend.find.core.configuration.RelatedUsersSourceConfig;
import com.hp.autonomy.frontend.find.core.configuration.UserDetailsFieldConfig;
import com.hp.autonomy.frontend.find.core.configuration.UsersConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.idol.responses.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class IdolUserControllerTest {
    @Mock private ConfigService<IdolFindConfig> configService;
    @Mock private IdolFindConfig config;
    @Mock private UsersConfig usersConfig;
    @Mock private RelatedUsersConfig relatedUsersConfig;
    @Mock private IdolUserSearchService idolUserSearchService;
    private IdolUserController controller;

    @Before
    public void setUp() {
        Mockito.when(configService.getConfig()).thenReturn(config);
        Mockito.when(config.getUsers()).thenReturn(usersConfig);

        Mockito.when(usersConfig.getRelatedUsers()).thenReturn(relatedUsersConfig);
        Mockito.when(relatedUsersConfig.getEnabled()).thenReturn(true);
        Mockito.when(relatedUsersConfig.getInterests()).thenReturn(
            RelatedUsersSourceConfig.builder().userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("int_display_1").build(),
                UserDetailsFieldConfig.builder().name("int_display_2").build()
            )).build());
        Mockito.when(relatedUsersConfig.getExpertise()).thenReturn(
            RelatedUsersSourceConfig.builder().userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("exp_display_1").build(),
                UserDetailsFieldConfig.builder().name("exp_display_2").build()
            )).build());

        controller = new IdolUserController(configService, idolUserSearchService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRelatedToSearch_disabled() {
        Mockito.when(relatedUsersConfig.getEnabled()).thenReturn(false);
        controller.getRelatedToSearch("term1 term2", 33);
    }

    @Test
    public void testGetRelatedToSearch() {
        final User expUser = new User();
        expUser.setUid(123);
        expUser.setUsername("user1");
        expUser.setLastloggedin(new Date());
        expUser.setFields(new ImmutableMap.Builder<String, String>()
            .put("exp_display_1", "A")
            .put("int_display_1", "B")
            .put("hidden", "C")
            .build());

        final User intUser = new User();
        intUser.setUid(7);
        intUser.setUsername("user2");
        intUser.setEmailaddress("user2@example.com");
        intUser.setLastloggedin(new Date());
        intUser.setFields(new ImmutableMap.Builder<String, String>()
            .put("int_display_1", "D")
            .put("exp_display_1", "E")
            .put("hidden", "F")
            .build());

        final List<RelatedUser> mockUsers =
            Arrays.asList(new RelatedUser(expUser, true), new RelatedUser(intUser, false));
        Mockito.doReturn(mockUsers).when(idolUserSearchService)
            .getRelatedToSearch(relatedUsersConfig, "term1 term2", 33);

        final List<RelatedUser> users = controller.getRelatedToSearch("term1 term2", 33);
        Assert.assertEquals(2, users.size());

        Assert.assertTrue(users.get(0).isExpert());
        final User user1 = users.get(0).getUser();
        Assert.assertEquals("should include uid", 123, user1.getUid());
        Assert.assertEquals("should include username", "user1", user1.getUsername());
        Assert.assertNull("should not include lastloggedin", user1.getLastloggedin());
        Assert.assertEquals("should include configured expertise field for expert",
            "A", user1.getFields().get("exp_display_1"));
        Assert.assertNull("should not include missing field",
            user1.getFields().get("exp_display_2"));
        Assert.assertNull("should not include configured interests field for expert",
            user1.getFields().get("int_display_1"));
        Assert.assertNull("should not include unconfigured field", user1.getFields().get("hidden"));

        Assert.assertFalse(users.get(1).isExpert());
        final User user2 = users.get(1).getUser();
        Assert.assertEquals("should include uid", 7, user2.getUid());
        Assert.assertEquals("should include username", "user2", user2.getUsername());
        Assert.assertEquals("should include emailaddress",
            "user2@example.com", user2.getEmailaddress());
        Assert.assertEquals("should include configured interests field for interested user",
            "D", user2.getFields().get("int_display_1"));
        Assert.assertNull("should not include missing field",
            user2.getFields().get("int_display_2"));
        Assert.assertNull("should not include configured expertise field for interested user",
            user2.getFields().get("exp_display_1"));
        Assert.assertNull("should not include unconfigured field", user2.getFields().get("hidden"));
    }

}

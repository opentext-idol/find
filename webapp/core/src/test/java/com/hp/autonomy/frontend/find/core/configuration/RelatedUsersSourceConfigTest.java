/*
 * Copyright 2020 Open Text.
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

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

import java.util.Arrays;

public class RelatedUsersSourceConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder().build();
        config.basicValidate("users");
    }

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder()
            .agentStoreProfilesDatabase("db")
            .namedArea("db")
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                UserDetailsFieldConfig.builder().name("b").build()
            ))
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_dbMissing() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder()
            .agentStoreProfilesDatabase(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_namedAreaMissing() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder()
            .namedArea(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_fieldMissing() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder()
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                null
            ))
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_fieldInvalid() throws ConfigException {
        final RelatedUsersSourceConfig config = RelatedUsersSourceConfig.builder()
            .userDetailsFields(Arrays.asList(
                UserDetailsFieldConfig.builder().name("a").build(),
                UserDetailsFieldConfig.builder().build()
            ))
            .build();
        config.basicValidate("users");
    }

}

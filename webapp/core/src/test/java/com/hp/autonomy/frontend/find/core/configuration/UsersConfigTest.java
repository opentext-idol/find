/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

public class UsersConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final UsersConfig config = UsersConfig.builder().build();
        config.basicValidate("users");
    }

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final UsersConfig config = UsersConfig.builder()
            .relatedUsers(RelatedUsersConfig.builder().build())
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_relatedUsersMissing() throws ConfigException {
        final UsersConfig config = UsersConfig.builder()
            .relatedUsers(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_relatedUsersInvalid() throws ConfigException {
        final UsersConfig config = UsersConfig.builder()
            .relatedUsers(RelatedUsersConfig.builder().enabled(true).interests(null).build())
            .build();
        config.basicValidate("users");
    }

}

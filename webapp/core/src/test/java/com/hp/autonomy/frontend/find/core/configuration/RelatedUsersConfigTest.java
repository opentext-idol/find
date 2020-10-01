/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.configuration;

import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

public class RelatedUsersConfigTest {

    @Test
    public void testBasicValidate_defaults() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder().build();
        config.basicValidate("users");
    }

    @Test
    public void testBasicValidate_disabled() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(false)
            .interests(null)
            .build();
        config.basicValidate("users");
    }

    @Test
    public void testBasicValidate_enabled() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(true)
            .interests(RelatedUsersSourceConfig.builder().build())
            .expertise(RelatedUsersSourceConfig.builder().build())
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_interestsMissing() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(true)
            .interests(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_interestsInvalid() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(true)
            .interests(RelatedUsersSourceConfig.builder().namedArea(null).build())
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_expertiseMissing() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(true)
            .expertise(null)
            .build();
        config.basicValidate("users");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_expertiseInvalid() throws ConfigException {
        final RelatedUsersConfig config = RelatedUsersConfig.builder()
            .enabled(true)
            .expertise(RelatedUsersSourceConfig.builder().namedArea(null).build())
            .build();
        config.basicValidate("users");
    }

}

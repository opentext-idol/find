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

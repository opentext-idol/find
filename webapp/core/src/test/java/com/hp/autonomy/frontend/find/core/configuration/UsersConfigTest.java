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

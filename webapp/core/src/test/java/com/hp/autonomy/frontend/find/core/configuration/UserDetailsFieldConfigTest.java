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

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.configuration.ConfigException;
import org.junit.Test;

import java.util.Collections;

public class UserDetailsFieldConfigTest {

    @Test
    public void testBasicValidate_valid() throws ConfigException {
        final UserDetailsFieldConfig config = UserDetailsFieldConfig.builder()
            .name("the field")
            .build();
        config.basicValidate("details");
    }

    @Test(expected = ConfigException.class)
    public void testBasicValidate_nameMissing() throws ConfigException {
        final UserDetailsFieldConfig config = UserDetailsFieldConfig.builder().build();
        config.basicValidate("details");
    }

}

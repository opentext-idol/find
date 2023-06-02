/*
 * Copyright 2016-2018 Open Text.
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

package com.autonomy.abc.selenium.find.application;

import java.util.Arrays;
import java.util.Optional;

public enum UserRole {
    BIFHI("bifhi"), FIND("find");

    /**
     * The id in the config file of the user with this role.
     */
    private final String configId;

    UserRole(final String configId) {
        this.configId = configId;
    }

    public static Optional<UserRole> fromString(final String value) {
        final String lowerCaseValue = value.toLowerCase();

        return Arrays.stream(values())
                .filter(role -> role.name().toLowerCase().equals(lowerCaseValue))
                .findFirst();
    }

    public static UserRole activeRole() {
        final String maybeProperty = System.getProperty("userRole");

        return Optional.ofNullable(maybeProperty)
                .map(property -> fromString(property).get())
                .orElse(BIFHI);
    }

    public String getConfigId() {
        return configId;
    }
}

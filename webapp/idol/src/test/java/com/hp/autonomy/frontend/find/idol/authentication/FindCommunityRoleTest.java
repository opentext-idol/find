/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.authentication;

import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public class FindCommunityRoleTest {
    @Test
    public void fromValidValue() {
        assertNotNull(FindCommunityRole.fromValue("FindUser"));
    }

    @Test
    public void fromValueDifferingInCase() {
        final String invalidUsername = "findUser";
        try {
            FindCommunityRole.fromValue(invalidUsername);
            fail("Exception should have been thrown");
        } catch(final IllegalArgumentException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       is("Invalid role specified: " + invalidUsername));
        }
    }

    @Test
    public void fromInvalidValue() {
        final String invalidUsername = "bad";
        try {
            FindCommunityRole.fromValue(invalidUsername);
            fail("Exception should have been thrown");
        } catch(final IllegalArgumentException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       is("Invalid role specified: " + invalidUsername));
        }
    }
}

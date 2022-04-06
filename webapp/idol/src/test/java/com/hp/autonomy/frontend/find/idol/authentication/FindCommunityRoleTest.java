/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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

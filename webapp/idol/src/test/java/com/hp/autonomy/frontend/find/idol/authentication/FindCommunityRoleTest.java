package com.hp.autonomy.frontend.find.idol.authentication;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FindCommunityRoleTest {
    @Test
    public void fromValidValue() {
        assertNotNull(FindCommunityRole.fromValue("FindUser"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromValueDifferingInCase() {
        FindCommunityRole.fromValue("findUser");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidValue() {
        FindCommunityRole.fromValue("bad");
    }
}

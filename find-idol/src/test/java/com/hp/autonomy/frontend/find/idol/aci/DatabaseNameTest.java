package com.hp.autonomy.frontend.find.idol.aci;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseNameTest {
    @Test
    public void toStringTest() {
        final String name = "Some Name";
        assertEquals(name, new DatabaseName(name).toString());
    }
}

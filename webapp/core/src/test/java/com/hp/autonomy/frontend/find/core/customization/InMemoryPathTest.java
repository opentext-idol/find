/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.customization;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class InMemoryPathTest {
    @Test
    public void testToString() {
        final InMemoryPath path = new InMemoryPath("./less/");
        assertEquals("./less", path.toString());
    }

    @Test
    public void testToStringWhenPathEmpty() {
        final InMemoryPath path = new InMemoryPath("");
        assertEquals(".", path.toString());
    }

    @Test
    public void testGetParent() {
        final InMemoryPath path = new InMemoryPath("less/hpe-elements/main.less");
        assertEquals(new InMemoryPath("less/hpe-elements"), path.getParent());
    }

    @Test
    public void testGetParentWhenPathEmpty() {
        final InMemoryPath path = new InMemoryPath("");
        assertNull(path.getParent());
    }

    @Test
    public void normalize() {
        final InMemoryPath path = new InMemoryPath("././less/./../less/hpe-elements/../static/../main");
        assertEquals(new InMemoryPath("less/main"), path.normalize());
    }

    @Test
    public void normalizeThrowsIfTraversalBeyondRoot() {
        final InMemoryPath path = new InMemoryPath("./less/../../main");
        try {
            path.normalize();
            fail("Expected exception to have been thrown");
        } catch(final IllegalArgumentException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("Path contains illegal traversal"));
        }
    }

    @Test
    public void resolveString() {
        final InMemoryPath path = new InMemoryPath("./xyz");
        assertEquals(new InMemoryPath("./xyz/../less/main.css"), path.resolve("../less/main.css"));
    }

    @Test
    public void resolvePath() {
        final InMemoryPath path1 = new InMemoryPath("./xyz");
        final InMemoryPath path2 = new InMemoryPath("./abc/../less/main.css");
        assertEquals(new InMemoryPath("./xyz/./abc/../less/main.css"), path1.resolve(path2));
    }
}

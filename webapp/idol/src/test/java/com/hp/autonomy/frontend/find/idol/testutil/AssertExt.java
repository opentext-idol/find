/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.testutil;

import static org.junit.Assert.fail;

/**
 * Additional assertion methods.
 */
public class AssertExt {

    /**
     * A function that might throw.
     */
    public interface MaybeFail {
        void run() throws Throwable;
    }

    /**
     * Assert that function `f` throws a {@link Throwable} of type `excType`.
     *
     * @return The object thrown, if the assertion succeeds.
     */
    public static <T extends Throwable> T assertThrows(Class<T> excType, MaybeFail f) {
        try {
            f.run();
            fail("Expected function to throw");
        } catch (Throwable e) {
            if (excType.isInstance(e)) return (T) e;
            else fail("Expected function to throw " + excType + ", got " + e);
        }
        // can't reach here
        return null;
    }

}

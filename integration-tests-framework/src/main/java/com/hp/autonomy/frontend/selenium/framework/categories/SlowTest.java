package com.hp.autonomy.frontend.selenium.framework.categories;

/**
 * JUnit category for tests that take a long time to run
 *
 * Tests that take a long time to run by design should be
 * tagged with @Category(SlowTest.class).
 *
 * For example:
 * testCreateManyPromotions is a slow test
 * testCreatePromotion is not
 */
public interface SlowTest {
}

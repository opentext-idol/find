package com.autonomy.abc.framework.categories;

/**
 * JUnit Category for core feature tests
 *
 * Basic tests that only test standard behaviour should be
 * tagged with @Category(CoreFeature.class).
 * If a core feature test fails, it suggests an urgent bug
 * ticket should be created, requiring a production hotfix.
 * These tests should have minimal assertions and merely make
 * sure that the feature behaves properly, and that the
 * Selenium tests are able to the feature correctly.
 *
 * For example:
 * testCreatePromotion is a core feature test;
 * testPromotionNotifications is not
 */
public interface CoreFeature {
}

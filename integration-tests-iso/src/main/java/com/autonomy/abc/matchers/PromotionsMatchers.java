package com.autonomy.abc.matchers;

import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class PromotionsMatchers {
    public static Matcher<? super PromotionsDetailPage> triggerList(final Matcher matcher) {
        return new TypeSafeMatcher<PromotionsDetailPage>() {
            @Override
            protected boolean matchesSafely(final PromotionsDetailPage promotionsDetailPage) {
                return matcher.matches(promotionsDetailPage.getTriggerForm().getTriggersAsStrings());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("trigger list is ").appendDescriptionOf(matcher);
            }

            @Override
            public void describeMismatchSafely(final PromotionsDetailPage item, final Description description) {
                matcher.describeMismatch(item.getTriggerForm().getTriggersAsStrings(), description);
            }
        };
    }

    public static Matcher<? super PromotionsPage> promotionsList(final Matcher matcher) {
        return new TypeSafeMatcher<PromotionsPage>() {
            @Override
            protected boolean matchesSafely(final PromotionsPage promotionsPage) {
                return matcher.matches(promotionsPage.promotionsList());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("promotions list is ").appendDescriptionOf(matcher);
            }

            @Override
            public void describeMismatchSafely(final PromotionsPage item, final Description description) {
                matcher.describeMismatch(item.promotionsList(), description);
            }
        };
    }
}

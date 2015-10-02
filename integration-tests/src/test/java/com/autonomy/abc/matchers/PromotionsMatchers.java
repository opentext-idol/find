package com.autonomy.abc.matchers;

import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class PromotionsMatchers {
    public static Matcher<? super PromotionsDetailPage> triggerList(final Matcher matcher) {
        return new TypeSafeMatcher<PromotionsDetailPage>() {
            @Override
            protected boolean matchesSafely(PromotionsDetailPage promotionsDetailPage) {
                return matcher.matches(promotionsDetailPage.getTriggerList());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("trigger list is ").appendDescriptionOf(matcher);
            }

            @Override
            public void describeMismatchSafely(PromotionsDetailPage item, Description description) {
                matcher.describeMismatch(item.getTriggerList(), description);
            }
        };
    }

    public static Matcher<? super PromotionsPage> promotionsList(final Matcher matcher) {
        return new TypeSafeMatcher<PromotionsPage>() {
            @Override
            protected boolean matchesSafely(PromotionsPage promotionsPage) {
                return matcher.matches(promotionsPage.promotionsList());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("promotions list is ").appendDescriptionOf(matcher);
            }

            @Override
            public void describeMismatchSafely(PromotionsPage item, Description description) {
                matcher.describeMismatch(item.promotionsList(), description);
            }
        };
    }
}

package com.hp.autonomy.frontend.selenium.matchers;

import com.hp.autonomy.frontend.selenium.control.Window;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public final class ControlMatchers {
    private ControlMatchers() {}

    public static Matcher<? super Window> urlContains(final String substring) {
        return url(is(containsString(substring)));
    }

    public static Matcher<? super Window> url(final Matcher<String> stringMatcher) {
        return new TypeSafeMatcher<Window>() {
            @Override
            protected boolean matchesSafely(Window window) {
                return stringMatcher.matches(window.getUrl());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("url ").appendDescriptionOf(stringMatcher);
            }

            @Override
            protected void describeMismatchSafely(Window item, Description mismatchDescription) {
                stringMatcher.describeMismatch(item, mismatchDescription);
            }
        };
    }
}

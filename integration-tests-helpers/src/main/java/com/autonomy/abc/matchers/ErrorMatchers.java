package com.autonomy.abc.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.Serializable;

public class ErrorMatchers {
    public static Matcher<Serializable> isError(final Serializable errorMessage) {
        return new TypeSafeMatcher<Serializable>() {
            @Override
            protected boolean matchesSafely(Serializable serializable) {
                return errorMessage.toString().equals(serializable.toString());
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("is the error message ")
                        .appendValue(errorMessage);
            }
        };
    }
}

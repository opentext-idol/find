package com.autonomy.abc.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;

public class StringMatchers {
    public static Matcher<String> stringContainingAnyOf(final Iterable<? extends Serializable> strings) {
        final List<Matcher<? super String>> matchers = new ArrayList<>();
        for (Serializable string : strings) {
            matchers.add(containsString(string));
        }
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String s) {
                return anyOf(matchers).matches(s);
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("a string containing any of ")
                        .appendValue(strings);
            }
        };
    }

    public static Matcher<String> stringContainingAnyOf(final Serializable[] strings) {
        return stringContainingAnyOf(Arrays.asList(strings));
    }

    public static Matcher<String> containsString(Serializable stringLike) {
        return org.hamcrest.Matchers.containsString(stringLike.toString());
    }
}

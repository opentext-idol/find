package com.autonomy.abc.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.Serializable;
import java.util.*;

import static org.hamcrest.Matchers.*;

public class CommonMatchers {
    private CommonMatchers() {}

    public static <T> Matcher<Collection<T>> containsItems(final Collection<? extends T> list) {
        return new TypeSafeMatcher<Collection<T>>() {
            @Override
            protected boolean matchesSafely(Collection<T> container) {
                return everyItem(isIn(container)).matches(list);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a collection containing all of ").appendValueList("{", ", ", "}", list);
            }
        };
    }

    public static <T> Matcher<Iterable<? super T>> containsItems(final Collection<? extends T> list, final Comparator<? super T> comparator) {
        return new TypeSafeMatcher<Iterable<? super T>>() {
            @Override
            protected boolean matchesSafely(Iterable<? super T> item) {
                for (T element : list) {
                    if (!containsItem(element, comparator).matches(item)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a collection containing all of ").appendValueList("{", ", ", "}", list);
            }
        };
    }

    public static <T> Matcher<Iterable<? super T>> containsItem(final T value, final Comparator<? super T> comparator) {
        return hasItem(comparesEqualTo(value, comparator));
    }

    public static <T> Matcher<T> comparesEqualTo(final T value, final Comparator<? super T> comparator) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(T item) {
                return comparator.compare(item, value) == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an item comparing equal to ").appendValue(value);
            }
        };
    }

    public static <T> Matcher<Iterable<? extends T>> hasItemThat(final Matcher<? super T> matcher) {
        return new TypeSafeMatcher<Iterable<? extends T>>() {
            @Override
            protected boolean matchesSafely(Iterable<? extends T> item) {
                for (T value : item) {
                    if (matcher.matches(value)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("a collection containing an item that ")
                        .appendDescriptionOf(matcher);
            }
        };
    }

    public static <T> Matcher<Iterable<? extends T>> containsAnyOf(final Collection<? super T> collection) {
        return hasItemThat(is(isIn(collection)));
    }
    
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

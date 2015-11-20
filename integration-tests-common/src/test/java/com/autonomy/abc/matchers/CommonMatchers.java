package com.autonomy.abc.matchers;

import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;

public class CommonMatchers {
    private CommonMatchers() {}

    public static Matcher<Iterable<String>> containsItems(List<String> list) {
        return hasItems(list.toArray(new String[list.size()]));
    }
}

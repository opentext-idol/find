package com.autonomy.abc.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.Matchers.containsString;

public class ElementMatchers {
    public static Matcher<? super WebElement> containsElement(final By by) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement webElement) {
                return webElement.findElements(by).size() > 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a parent element containing a child ").appendValue(by);
            }

            @Override
        public void describeMismatchSafely(final WebElement webElement, final Description description) {
                description.appendText("no child found inside ").appendValue(webElement);
            }
        };
    }

    public static Matcher<? super WebElement> containsText(final String text) {
        return new TypeSafeMatcher<WebElement>() {
            private Matcher<String> container = containsString(text);

            @Override
            protected boolean matchesSafely(WebElement item) {
                return container.matches(item.getText());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a tag containing ").appendValue(text);
            }

            @Override
            public void describeMismatchSafely(final WebElement item, final Description description) {
                description.appendText("<" + item.getTagName() + "> tag had text ").appendValue(item.getText());
            }
        };
    }

    public static Matcher<? super WebElement> hasAttribute(final String text) {
        return new TypeSafeMatcher<WebElement>() {

            @Override
            protected boolean matchesSafely(WebElement item) {
                return item.getAttribute(text) != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a tag with attribute ").appendValue(text);
            }

            @Override
            public void describeMismatchSafely(final WebElement item, final Description description) {
                description.appendText("element was ").appendText(item.toString());
            }
        };
    }
}

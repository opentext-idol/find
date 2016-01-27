package com.autonomy.abc.matchers;

import com.autonomy.abc.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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

    public static Matcher<? super WebElement> containsTextIgnoringCase(final String text) {
        return new TypeSafeMatcher<WebElement>() {
            private Matcher<String> container = containsString(text.toLowerCase());

            @Override
            protected boolean matchesSafely(WebElement item) {
                return container.matches(item.getText().toLowerCase());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a tag containing '" + text + "' irrelevant of case");
            }

            @Override
            public void describeMismatchSafely(final WebElement item, final Description description) {
                description
                        .appendText("<" + item.getTagName() + "> tag had text ")
                        .appendValue(item.getText())
                        .appendText(" ignoring case");
            }
        };
    }

    public static Matcher<? super WebElement> hasTextThat(final Matcher<String> matcher) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return matcher.matches(item.getText());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a tag whose text is ").appendDescriptionOf(matcher);
            }

            @Override
            protected void describeMismatchSafely(WebElement item, Description mismatchDescription) {
//                mismatchDescription.appendText("<" + item.getTagName() + "> tag had text ").appendValue(item.getText());
                mismatchDescription.appendText("was a <" + item.getTagName() + "> tag whose text ");
                matcher.describeMismatch(item.getText(), mismatchDescription);
            }
        };
    }

    public static Matcher<? super WebElement> hasTagName(final String tagName) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return item.getTagName().equals(tagName);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("<" + tagName + "> tag");
            }

            @Override
            public void describeMismatchSafely(final WebElement item, final Description description) {
                description.appendText("element was ").appendText(item.toString());
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

    public static Matcher<? super WebElement> hasAttribute(final String attribute, final Matcher<String> valueMatcher) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return valueMatcher.matches(item.getAttribute(attribute));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element with attribute ").appendDescriptionOf(valueMatcher);
            }

            @Override
            protected void describeMismatchSafely(WebElement item, Description mismatchDescription) {
                mismatchDescription.appendText("element's " + attribute + " attribute was ").appendValue(item.getAttribute(attribute));
            }
        };
    }

    public static Matcher<? super WebElement> hasAttribute(final String attribute, final String value) {
        return hasAttribute(attribute, equalTo(value));
    }

    public static Matcher<? super WebElement> hasClass(final String className) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return ElementUtil.hasClass(className, item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element with class ").appendValue(className);
            }

            @Override
            protected void describeMismatchSafely(WebElement item, Description mismatchDescription) {
                mismatchDescription.appendText("element's class attribute was ").appendText(item.getAttribute("class"));
            }
        };
    }

    public static Matcher<? super WebElement> disabled() {
        return new TypeSafeMatcher<WebElement>() {

            @Override
            protected boolean matchesSafely(WebElement item) {
                return ElementUtil.isDisabled(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element that is disabled");
            }

            @Override
            public void describeMismatchSafely(final WebElement item, final Description description) {
                description.appendText("element was ").appendText(item.toString());
            }
        };
    }

    public static Matcher<? super AppElement> appearsWithin(final int seconds) {
        return new TypeSafeMatcher<AppElement>() {
            @Override
            protected boolean matchesSafely(AppElement item) {
                try {
                    new WebDriverWait(item.getDriver(), seconds).until(ExpectedConditions.visibilityOf(item));
                    return true;
                } catch (TimeoutException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element appeared within " + seconds + " seconds");
            }

            @Override
            protected void describeMismatchSafely(AppElement item, Description description) {
                description.appendText("element was ").appendText(item.toString());
            }
        };
    }

    public static  Matcher<? super WebElement> appearsWithin(final int seconds, final WebDriver driver) {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                try {
                    new WebDriverWait(driver, seconds).until(ExpectedConditions.visibilityOf(item));
                    return true;
                } catch (TimeoutException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element appeared within " + seconds + " seconds");
            }

            @Override
            protected void describeMismatchSafely(WebElement item, Description description) {
                description.appendText("element was ").appendText(item.toString());
            }
        };
    }

    public static Matcher<? super WebElement> modalIsDisplayed() {
        return new TypeSafeMatcher<WebElement>() {
            @Override
            protected boolean matchesSafely(WebElement item) {
                return !item.findElement(By.xpath("//*")).findElements(By.cssSelector(".modal[aria-hidden='false']")).isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a modal is displayed");
            }

            @Override
            protected void describeMismatchSafely(WebElement item, Description mismatchDescription) {
                mismatchDescription.appendText("no modal was displayed");
            }
        };
    }

}

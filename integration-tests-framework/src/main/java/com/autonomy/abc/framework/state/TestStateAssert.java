package com.autonomy.abc.framework.state;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.hamcrest.Matchers.is;

public class TestStateAssert {
    private static TestState testState = TestState.get();

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        assertThat(testPasses(matcher.toString(), actual, matcher));
    }

    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
       assertThat(testPasses(reason, actual, matcher));
    }

    public static void assertThat(String reason, boolean assertion) {
        assertThat(testPasses(reason, assertion));
    }

    public static <T> boolean verifyThat(T actual, Matcher<? super T> matcher) {
        return verifyThat(testPasses(matcher.toString(), actual, matcher));
    }

    public static <T> boolean verifyThat(String reason, T actual, Matcher<? super T> matcher) {
        return verifyThat(testPasses(reason, actual, matcher));
    }

    public static boolean verifyThat(String reason, boolean assertion) {
        return verifyThat(testPasses(reason, assertion));
    }

    private static void assertThat(AssertionError e) {
        if (e != null) {
            throw e;
        }
    }

    private static boolean verifyThat(AssertionError e) {
        if (e == null) {
            return true;
        } else {
            testState.addException(e);
            return false;
        }
    }

    private static <T> AssertionError testPasses(String testName, T actual, Matcher<? super T> matcher) {
        Description description = new StringDescription();
        TestStatement testStatement = new TestStatement(testName, description);
        boolean success = matcher.matches(actual);
        if (success) {
            testStatement.setState(true);
            description.appendValue(actual).appendText(" ").appendDescriptionOf(matcher);
        } else {
            testStatement.setState(false);
            description.appendText("\nExpected: ").appendDescriptionOf(matcher).appendText("\n     but: ");
            matcher.describeMismatch(actual, description);
        }
        testState.handle(testStatement);
        if (testStatement.passed()) {
            return null;
        } else {
            return new AssertionError(testStatement);
        }
    }

    private static AssertionError testPasses(String reason, boolean assertion) {
        return testPasses(reason, assertion, is(true));
    }
}

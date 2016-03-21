package com.autonomy.abc.framework.state;

import com.autonomy.abc.base.SeleniumTest;
import com.autonomy.abc.selenium.util.Handler;
import org.junit.runner.Description;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestState {
    private SeleniumTest<?, ?> test;
    private Description method;
    private int statementCount;
    private List<Throwable> assertionErrors;
    private List<Handler<TestStatement>> statementHandlers = new ArrayList<>();
    private final String timestamp;

    private static TestState self = new TestState();

    public static TestState get() {
        return self;
    }

    private TestState() {
        timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public void setTest(SeleniumTest<?, ?> newTest) {
        test = newTest;
    }

    public void setMethod(Description description) {
        method = description;
        statementCount = 0;
        assertionErrors = new ArrayList<>();
    }

    public void addStatementHandler(Handler<TestStatement> handler) {
        statementHandlers.add(handler);
    }

    public void handle(TestStatement statement) {
        statement.setId(method.getMethodName(), ++statementCount);
        for (Handler<TestStatement> handler : statementHandlers) {
            handler.handle(statement);
        }
    }

    public void addException(Throwable e) {
        assertionErrors.add(e);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTestName() {
        return test.getClass().getSimpleName() + "#" + method.getMethodName();
    }

    public void throwIfFailed() throws AssertionError {
        if (assertionErrors.size() > 0) {
            throw new AssertionError(assertionErrors.size() + " of " + statementCount + " verifications failed");
        }
    }

    public void finish() {
        statementHandlers.clear();
    }

}

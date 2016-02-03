package com.autonomy.abc.framework;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.framework.statements.StatementHandler;
import org.junit.runner.Description;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestState {
    private ABCTestBase test;
    private Description method;
    private int statementCount;
    private List<Throwable> assertionErrors;
    private List<StatementHandler> statementHandlers = new ArrayList<>();
    private final String timestamp;

    private static TestState self = new TestState();

    public static TestState get() {
        return self;
    }

    private TestState() {
        timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public void setTest(ABCTestBase newTest) {
        test = newTest;
    }

    public void setMethod(Description description) {
        method = description;
        statementCount = 0;
        assertionErrors = new ArrayList<>();
    }

    public void addStatementHandler(StatementHandler handler) {
        statementHandlers.add(handler);
    }

    public void handle(TestStatement statement) {
        statement.setId(method.getMethodName(), ++statementCount);
        for (StatementHandler handler : statementHandlers) {
            handler.handle(statement);
        }
    }

    public void addException(Throwable e) {
        assertionErrors.add(e);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void throwIfFailed() throws AssertionError {
        if (assertionErrors.size() > 0) {
            throw new AssertionError(assertionErrors.size() + " verifications failed");
        }
    }

    public void finish() {
        test.getSessionRegistry().clear();
        statementHandlers.clear();
    }

}

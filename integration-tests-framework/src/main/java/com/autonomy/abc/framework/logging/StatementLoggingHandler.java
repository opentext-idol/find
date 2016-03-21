package com.autonomy.abc.framework.logging;

import com.autonomy.abc.base.SeleniumTest;
import com.autonomy.abc.framework.state.TestStatement;
import com.autonomy.abc.framework.state.StatementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementLoggingHandler implements StatementHandler {
    private Logger logger;

    public StatementLoggingHandler(SeleniumTest<?, ?> test) {
        logger = LoggerFactory.getLogger(test.getClass());
    }

    @Override
    public void handle(TestStatement testStatement) {
        if (testStatement.passed()) {
            logger.info(testStatement.toString());
        } else {
            Throwable e = new AssertionError();
            StackTraceElement rootCause = null;
            for (StackTraceElement el : e.getStackTrace()) {
                if (!el.getClassName().contains("framework")) {
                    rootCause = el;
                    break;
                }
            }
            logger.error(testStatement.toString() + "\n\tat " + rootCause + "\n");
        }
    }
}

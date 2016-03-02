package com.autonomy.abc.framework.statements;

import com.autonomy.abc.config.SeleniumTest;
import com.autonomy.abc.framework.TestStatement;
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
            logger.error(testStatement.toString());
        }
    }
}

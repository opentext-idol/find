package com.autonomy.abc.framework.logging;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnownBugRule extends TestWatcher {
    private final static Logger LOGGER = LoggerFactory.getLogger(KnownBugRule.class);
    private KnownBug knownBug;

    @Override
    protected void failed(Throwable e, Description description) {
        knownBug = description.getAnnotation(KnownBug.class);
        if (knownBug != null) {
            LOGGER.info("Failure may be due to known bug");
            logTickets();
        }
    }

    @Override
    protected void succeeded(Description description) {
        knownBug = description.getAnnotation(KnownBug.class);
        if (knownBug != null) {
            LOGGER.info("Known bug may be resolved");
            logTickets();
        }
    }

    private void logTickets() {
        for (String ticketNumber : knownBug.value()) {
            LOGGER.info(getJiraUrl(ticketNumber));
        }
    }

    private String getJiraUrl(String ticketNumber) {
        return "https://jira.autonomy.com/browse/" + ticketNumber;
    }
}

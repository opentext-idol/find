package com.autonomy.abc.framework.rules;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.framework.PageSourceSaver;
import com.autonomy.abc.framework.ScreenshotSaver;
import com.autonomy.abc.framework.TestState;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;

public class TestArtifactRule extends TestWatcher {
    private String timestamp;
    private ABCTestBase test;

    public TestArtifactRule(ABCTestBase testBase) {
        test = testBase;
        timestamp = TestState.get().getTimestamp();
    }

    private ScreenshotSaver getScreenshotSaver() {
        return new ScreenshotSaver(test.getDriver());
    }

    private PageSourceSaver getPageSourceSaver() {
        return new PageSourceSaver(test.getDriver());
    }

    private String getBaseLocation(Description description) {
        String[] splitName = description.getMethodName().split("\\[");
        return ".output/" + splitName[0] + '/' + timestamp + '[' + splitName[splitName.length - 1];
    }

    private String getPngLocation(Description description) {
        return getBaseLocation(description) + ".png";
    }

    private String getHtmlLocation(Description description) {
        return getBaseLocation(description) + ".html";
    }

    private void handle(Description description) {
        getScreenshotSaver().saveTo(getPngLocation(description));
        getPageSourceSaver().saveTo(getHtmlLocation(description));
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (e instanceof MultipleFailureException) {
            for (Throwable failure : ((MultipleFailureException) e).getFailures()) {
                if (!(failure instanceof AssertionError)) {
                    handle(description);
                    break;
                }
            }
        } else if (!(e instanceof AssertionError)) {
            handle(description);
        }
    }
}

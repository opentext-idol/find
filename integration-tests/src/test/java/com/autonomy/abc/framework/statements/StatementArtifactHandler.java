package com.autonomy.abc.framework.statements;


import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.framework.PageSourceSaver;
import com.autonomy.abc.framework.ScreenshotSaver;
import com.autonomy.abc.framework.TestState;
import com.autonomy.abc.framework.TestStatement;

public class StatementArtifactHandler implements StatementHandler {
    private ScreenshotSaver screenshotSaver;
    private PageSourceSaver pageSourceSaver;
    private String timestamp;

    public StatementArtifactHandler(ABCTestBase test) {
        this.timestamp = TestState.get().getTimestamp();
        screenshotSaver = new ScreenshotSaver(test.getDriver());
        pageSourceSaver = new PageSourceSaver(test.getDriver());
    }

    private String getBaseLocation(TestStatement testStatement) {              String[] splitName = testStatement.getMethodName().split("\\[");
        return ".output/" + splitName[0] + '/' + timestamp + '[' + splitName[splitName.length-1] + "_" + testStatement.getNumber();
    }

    private String getPngLocation(TestStatement testStatement) {
        return getBaseLocation(testStatement) + ".png";
    }

    private String getHtmlLocation(TestStatement testStatement) {
        return getBaseLocation(testStatement) + ".html";
    }

    @Override
    public void handle(TestStatement testStatement) {
        if (testStatement.failed()) {
            screenshotSaver.saveTo(getPngLocation(testStatement));
            pageSourceSaver.saveTo(getHtmlLocation(testStatement));
        }
    }
}

package com.autonomy.abc.framework;


import com.autonomy.abc.config.ABCTestBase;

public class StatementArtifactHandler implements StatementHandler {
    private ScreenshotSaver screenshotSaver;
    private PageSourceSaver pageSourceSaver;
    private String timestamp;

    public StatementArtifactHandler(ABCTestBase test) {
        this.timestamp = TestState.get().getTimestamp();
        screenshotSaver = new ScreenshotSaver(test.getDriver());
        pageSourceSaver = new PageSourceSaver(test.getDriver());
    }

    private String getPngLocation(TestStatement testStatement) {
        return ".output\\" + testStatement.getId() + "_" + timestamp + ".png";
    }

    private String getHtmlLocation(TestStatement testStatement) {
        return ".output\\" + testStatement.getId() + "_" + timestamp + ".html";
    }

    @Override
    public void handle(TestStatement testStatement) {
        if (testStatement.failed()) {
            screenshotSaver.saveTo(getPngLocation(testStatement));
            pageSourceSaver.saveTo(getHtmlLocation(testStatement));
        }
    }
}

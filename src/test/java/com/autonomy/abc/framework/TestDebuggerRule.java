package com.autonomy.abc.framework;

import com.autonomy.abc.config.ABCTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDebuggerRule implements TestRule {
    private String testName;
    private String timestamp;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDebuggerRule.class);
    private ABCTestBase test;

    public TestDebuggerRule(ABCTestBase test) {
        this.test = test;
    }

    private String getPngLocation() {
        return ".output\\" + testName + "-" + timestamp + ".png";
    }

    private String getHtmlLocation() {
        return ".output\\" + testName + "-" + timestamp + ".html";
    }

    public void takeScreenshot() {
        File imageFile = ((TakesScreenshot) test.getDriver()).getScreenshotAs(OutputType.FILE);
        File dest = new File(getPngLocation());
        try {
            FileUtils.moveFile(imageFile, dest);
            LOGGER.info("Saved screenshot: " + getPngLocation());
        } catch (IOException eio) {
            eio.printStackTrace();
        }
    }

    public void log() {
        LOGGER.info("Test exited on " + test.getDriver().getCurrentUrl());
    }

    public void saveCurrentPage() {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(getHtmlLocation()));
            out.print(test.getDriver().getPageSource());
            LOGGER.info("Saved page source: " + getHtmlLocation());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                testName = description.getMethodName();
                timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                base.evaluate();
            }
        };
    }
}

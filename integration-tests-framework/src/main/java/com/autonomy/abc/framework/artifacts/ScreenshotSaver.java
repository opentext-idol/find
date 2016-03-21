package com.autonomy.abc.framework.artifacts;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

class ScreenshotSaver {
    private WebDriver driver;
    private final static Logger LOGGER = LoggerFactory.getLogger(ScreenshotSaver.class);

    public ScreenshotSaver(WebDriver driver) {
        this.driver = driver;
    }

    public void saveTo(String destination) {
        File imageFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File dest = new File(destination);
        try {
            FileUtils.moveFile(imageFile, dest);
            LOGGER.error("Saved screenshot: " + dest.getAbsolutePath());
        } catch (IOException eio) {
            eio.printStackTrace();
        }
    }
}

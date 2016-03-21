package com.hp.autonomy.frontend.selenium.framework.artifacts;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

class PageSourceSaver {
    private WebDriver driver;
    private final static Logger LOGGER = LoggerFactory.getLogger(PageSourceSaver.class);

    public PageSourceSaver(WebDriver driver) {
        this.driver = driver;
    }

    public void saveTo(String destination) {
        File dest = new File(destination);
        try {
            PrintStream out = new PrintStream(new FileOutputStream(dest));
            out.print(driver.getPageSource());
            LOGGER.error("Saved page source: " + dest.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

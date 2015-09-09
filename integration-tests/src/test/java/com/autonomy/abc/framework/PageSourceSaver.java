package com.autonomy.abc.framework;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class PageSourceSaver {
    private WebDriver driver;
    private final static Logger LOGGER = LoggerFactory.getLogger(PageSourceSaver.class);

    public PageSourceSaver(WebDriver driver) {
        this.driver = driver;
    }

    public void saveTo(String destination) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(destination));
            out.print(driver.getPageSource());
            LOGGER.info("Saved page source: " + destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

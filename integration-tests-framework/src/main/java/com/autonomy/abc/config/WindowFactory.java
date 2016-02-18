package com.autonomy.abc.config;

import com.autonomy.abc.selenium.control.Session;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Set;

class WindowFactory implements ParametrizedFactory<Session, Window> {
    private final Dimension resolution;

    WindowFactory(TestConfig config) {
        resolution = config.getResolution();
    }

    @Override
    public Window create(Session context) {
        WebDriver driver = context.getDriver();
        String handle = createNewWindow(driver);
        if (resolution == null) {
            driver.manage().window().maximize();
        } else {
            driver.manage().window().setSize(resolution);
        }
        return context.registerWindow(handle);
    }

    private String createNewWindow(WebDriver driver) {
        Set<String> oldHandles = driver.getWindowHandles();
        ((JavascriptExecutor) driver).executeScript("window.open('', '_blank', 'width=100');");
        Set<String> newHandles = driver.getWindowHandles();
        newHandles.removeAll(oldHandles);
        return newHandles.toArray(new String[1])[0];
    }
}

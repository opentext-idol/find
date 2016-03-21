package com.hp.autonomy.frontend.selenium.config;

import com.hp.autonomy.frontend.selenium.control.Resolution;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Set;

class WindowFactory implements ParametrizedFactory<Session, Window> {
    private final Resolution resolution;

    WindowFactory(TestConfig config) {
        resolution = config.getResolution();
    }

    @Override
    public Window create(Session context) {
        WebDriver driver = context.getDriver();
        String handle = createNewWindow(driver);
        Window window = context.registerWindow(handle);
        window.activate();
        window.resize(resolution);
        return window;
    }

    private String createNewWindow(WebDriver driver) {
        Set<String> oldHandles = driver.getWindowHandles();
        ((JavascriptExecutor) driver).executeScript("window.open('', '_blank', 'width=100');");
        Set<String> newHandles = driver.getWindowHandles();
        newHandles.removeAll(oldHandles);
        return newHandles.toArray(new String[1])[0];
    }
}

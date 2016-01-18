package com.autonomy.abc.selenium.control;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Session implements Iterable<Window> {
    private WebDriver driver;

    Session(WebDriver webDriver) {
        driver = webDriver;
    }

    public Window openWindow(String url) {
        // TODO: move from DriverUtil
        String newHandle = createDriverWindow();
        Window newWindow = registerWindow(newHandle);
        newWindow.goTo(url);
        driver.manage().window().maximize();
        return newWindow;
    }

    private String createDriverWindow() {
        Set<String> oldHandles = driver.getWindowHandles();
        // TODO: do we want new windows, or prefer tabs?
        ((JavascriptExecutor) driver).executeScript("window.open('', '_blank', 'width=100');");
        Set<String> newHandles = driver.getWindowHandles();
        newHandles.removeAll(oldHandles);
        return newHandles.toArray(new String[1])[0];
    }

    public WebDriver getDriver() {
        return driver;
    }

    void end() {
        driver.quit();
        driver = null;
    }

    public Window getActiveWindow() {
        String currentHandle = driver.getWindowHandle();
        return registerWindow(currentHandle);
    }

    private Window registerWindow(String handle) {
        return new Window(this, handle);
    }

    String getUrl(Window window) {
        Window current = getActiveWindow();
        switchWindow(window);
        String url = driver.getCurrentUrl();
        switchWindow(current);
        return url;
    }

    void setUrl(Window window, String url) {
        switchWindow(window);
        driver.get(url);
    }

    void switchWindow(Window window) {
        driver.switchTo().window(window.getHandle());
    }

    void closeWindow(Window window) {
        switchWindow(window);
        driver.close();
    }

    @Override
    protected void finalize() throws Throwable {
        if (driver != null) {
            System.err.println("Session was not closed! Call session.end() to guarantee session ends properly");
            end();
        }
        super.finalize();
    }

    @Override
    public Iterator<Window> iterator() {
        final List<Window> windows = new ArrayList<>();
        for (String handle : driver.getWindowHandles()) {
            windows.add(new Window(this, handle));
        }
        return windows.iterator();
    }
}

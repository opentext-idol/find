package com.autonomy.abc.selenium.control;

import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Session implements Iterable<Window> {
    private WebDriver driver;
    private ParametrizedFactory<Session, Window> windowFactory;

    Session(WebDriver webDriver, ParametrizedFactory<Session, Window> newWindowFactory) {
        driver = webDriver;
        windowFactory = newWindowFactory;
    }

    public Window openWindow(String url) {
        Window newWindow = windowFactory.create(this);
        newWindow.goTo(url);
        return newWindow;
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

    public Window registerWindow(String handle) {
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

    public Window switchWindow(int i) {
        List<String> windowHandles = new ArrayList<>(driver.getWindowHandles());
        Window window = new Window(this, windowHandles.get(i));
        window.activate();
        return window;
    }

    void closeWindow(Window window) {
        switchWindow(window);
        driver.close();
    }

    public int countWindows() {
        return driver.getWindowHandles().size();
    }

    public ExpectedCondition<Boolean> windowCountIs(final int count) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return Session.this.countWindows() == count;
            }
        };
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

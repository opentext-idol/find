package com.hp.autonomy.frontend.selenium.control;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Frame {
    private final Window parent;
    private final WebDriver driver;
    private final WebElement frame;

    public Frame(Window window, WebElement element) {
        parent = window;
        driver = window.getSession().getDriver();
        frame = element;
    }

    public void activate() {
        driver.switchTo().frame(frame);
    }

    public void deactivate() {
        parent.activate();
    }

    /**
     * Get the content of the frame as a WebElement
     * Frame must be activated before, and deactivated after
     * @return the body tag
     */
    public WebElement content() {
        return driver.findElement(By.tagName("body"));
    }

    /**
     * Get the content of the frame as a String
     * Automatically deactivates the frame
     * @return the text inside the frame body
     */
    public String getText() {
        activate();
        String text = content().getText();
        deactivate();
        return text;
    }
}

package com.autonomy.abc.selenium.find.bi;

import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

public class Slider {
    private final WebElement slider;
    private final WebDriver driver;

    public Slider(WebElement slider, WebDriver driver) {
        this.slider = slider;
        this.driver = driver;
    }

    public int getValue() {
        return Integer.parseInt(slider.findElement(By.className("tooltip-inner")).getText());
    }

    public WebElement tooltip() {
        return slider.findElement(By.className("tooltip-main"));
    }

    public void hover() {
        DriverUtil.hover(driver, handle());
    }

    public void dragBy(int percentage) {
        int width = slider.getSize().getWidth();
        Actions builder = new Actions(driver);
        Action dragSlider = builder.clickAndHold(handle()).moveByOffset((width/100)*percentage,0).release().build();
        dragSlider.perform();
    }

    private WebElement handle() {
        return slider.findElement(By.className("min-slider-handle"));
    }

}

package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import org.openqa.selenium.WebDriver;

public interface Application<T> {
    @Deprecated
    T createElementFactory(WebDriver driver);
    T elementFactory();
    ApplicationType getType();
    Application<T> inWindow(Window window);
}

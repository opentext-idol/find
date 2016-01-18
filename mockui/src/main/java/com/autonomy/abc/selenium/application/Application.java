package com.autonomy.abc.selenium.application;

import org.openqa.selenium.WebDriver;

interface Application<T> {
    // will likely be replaced with plain elementFactory() accessor
    T createElementFactory(WebDriver driver);
    ApplicationType getType();
}

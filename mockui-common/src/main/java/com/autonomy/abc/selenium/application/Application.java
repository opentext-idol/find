package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;

public interface Application<T> {
    T elementFactory();
    ApplicationType getType();
    // TODO: remove this once Applications are initialized with a Window
    Application<T> inWindow(Window window);
}

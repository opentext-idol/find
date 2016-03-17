package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;

public interface Application<T extends ElementFactoryBase> {
    T elementFactory();
    boolean isHosted();
    // TODO: remove this once Applications are initialized with a Window
    Application<T> inWindow(Window window);
    LoginService loginService();
    String getName();
}

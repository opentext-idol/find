package com.hp.autonomy.frontend.selenium.application;

import com.hp.autonomy.frontend.selenium.control.Window;

public interface Application<T extends ElementFactoryBase> {
    T elementFactory();
    boolean isHosted();
    // TODO: remove this once Applications are initialized with a Window
    Application<T> inWindow(Window window);
    LoginService loginService();
    String getName();
}

package com.autonomy.abc.selenium.application;

import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.navigation.ElementFactoryBase;
import com.autonomy.abc.selenium.users.LoginService;

public interface Application<T extends ElementFactoryBase> {
    T elementFactory();
    ApplicationType getType();
    // TODO: remove this once Applications are initialized with a Window
    Application<T> inWindow(Window window);
    LoginService loginService();
    String getName();
}

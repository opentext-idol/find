package com.autonomy.abc.selenium.devconsole;

import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.application.LoginService;
import com.autonomy.abc.selenium.users.User;

public class DevConsole implements Application<DevConsoleElementFactory> {
    private DevConsoleElementFactory factory;
    private LoginService loginService;

    public DevConsole() {
    }

    public DevConsole(Window window) {
        inWindow(window);
    }

    @Override
    public DevConsoleElementFactory elementFactory() {
        return factory;
    }

    @Override
    public LoginService loginService() {
        if (loginService == null) {
            loginService = new LoginService(this) {
                @Override
                public void login(User user) {
                    elementFactory().getTopNavBar().loginButton().click();
                    super.login(user);
                }
            };
        }
        return loginService;
    }

    @Override
    public boolean isHosted() {
        return true;
    }

    @Override
    public String getName() {
        return "DevConsole";
    }

    @Override
    public DevConsole inWindow(Window window) {
        this.factory = new DevConsoleElementFactory(window.getSession().getDriver());
        return this;
    }
}

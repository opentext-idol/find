package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.users.HSODUser;
import com.autonomy.abc.selenium.users.LoginService;
import com.autonomy.abc.selenium.users.User;

public class HSODFind implements Application<HSODFindElementFactory> {
    private Window window;
    private HSODFindElementFactory factory;

    public HSODFind() {
    }

    public HSODFind(Window window) {
        inWindow(window);
    }

    @Override
    public LoginService loginService() {
        return new LoginService(this);
    }

    public FindService findService() {
        return new FindService(this);
    }

    @Override
    public HSODFindElementFactory elementFactory() {
        return factory;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public String getName() {
        return "Find";
    }

    @Override
    public Application<HSODFindElementFactory> inWindow(Window window) {
        this.window = window;
        this.factory = new HSODFindElementFactory(window.getSession().getDriver());
        return this;
    }
}

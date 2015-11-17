package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;

public class FindHasLoggedIn implements HasLoggedIn {
    private HSOElementFactory elementFactory;

    public FindHasLoggedIn(HSOElementFactory elementFactory) {
        this.elementFactory = elementFactory;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            elementFactory.getFindPage();
            return true;
        } catch (Exception e) {
            /* NOOP */
        }
        return false;
    }
}

package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.promotions.HsodPromotionService;
import com.autonomy.abc.selenium.users.HsodDeveloperService;
import com.autonomy.abc.selenium.users.HsodUserService;
import com.hp.autonomy.frontend.selenium.control.Window;

public class HSODApplication extends IsoApplication<HSODElementFactory> {
    private final IsoHsodApplication delegate;
    private HSODElementFactory factory;

    public HSODApplication() {
        delegate = new IsoHsodApplication();
    }

    @Override
    public HSODApplication inWindow(Window window) {
        this.factory = new HSODElementFactory(window.getSession().getDriver());
        delegate.inWindow(window);
        return this;
    }

    public HSODElementFactory elementFactory() {
        return factory;
    }

    @Override
    public HsodPromotionService promotionService() {
        return delegate.promotionService();
    }

    @Override
    public HsodUserService userService() {
        return delegate.userService();
    }

    public ConnectionService connectionService() {
        return new ConnectionService(this);
    }

    public IndexService indexService() {
        return new IndexService(this);
    }

    public HsodDeveloperService developerService() {
        return delegate.developerService();
    }

    @Override
    public boolean isHosted() {
        return true;
    }
}

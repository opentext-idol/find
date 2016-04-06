package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.promotions.HSODPromotionService;
import com.autonomy.abc.selenium.users.HSODDeveloperService;
import com.autonomy.abc.selenium.users.HSODUserService;
import com.hp.autonomy.frontend.selenium.control.Window;

public class HSODApplication extends SearchOptimizerApplication<HSODElementFactory> {
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
    public HSODPromotionService promotionService() {
        return delegate.promotionService();
    }

    @Override
    public HSODUserService userService() {
        return delegate.userService();
    }

    public ConnectionService connectionService() {
        return new ConnectionService(this);
    }

    public IndexService indexService() {
        return new IndexService(this);
    }

    public HSODDeveloperService developerService() {
        return delegate.developerService();
    }

    @Override
    public boolean isHosted() {
        return true;
    }
}

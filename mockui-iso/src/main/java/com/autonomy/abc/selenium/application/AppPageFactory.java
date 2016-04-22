package com.autonomy.abc.selenium.application;

import com.hp.autonomy.frontend.selenium.util.AppPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public interface AppPageFactory<T extends AppPage> extends ParametrizedFactory<WebDriver, T> {
    Class<T> getPageType();
}

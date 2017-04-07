package com.autonomy.abc.base;

import com.autonomy.abc.selenium.find.application.FindApplication;
import com.autonomy.abc.selenium.find.application.FindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(Parameterized.class)
public abstract class FindTestBase extends TestBase<FindApplication<? extends FindElementFactory>, FindElementFactory> {
    protected FindTestBase(final TestConfig config) {
        // In HOD mode, choose the default user
        super(config, FindApplication.ofType(config.getType()), config.getType() == ApplicationType.HOSTED ? null : UserRole.activeRole());
    }

    /**
     * Navigate to the given URL.
     * @param relativeUrl Relative to the application's context path as configured in the Selenium config
     */
    protected void navigateToAppUrl(final String relativeUrl) {
        try {
            getDriver().navigate().to(new URL(new URL(getAppUrl()), relativeUrl));
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Malformed app URL in config file", e);
        }
    }
}

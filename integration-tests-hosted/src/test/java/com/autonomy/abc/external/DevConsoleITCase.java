package com.autonomy.abc.external;

import com.autonomy.abc.config.SeleniumTest;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.devconsole.DevConsole;
import com.autonomy.abc.selenium.devconsole.DevConsoleElementFactory;
import com.autonomy.abc.selenium.devconsole.HSODLandingPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;

public class DevConsoleITCase extends SeleniumTest<DevConsole, DevConsoleElementFactory> {
    private HSODLandingPage landingPage;

    public DevConsoleITCase(TestConfig config) {
        super(config, new DevConsole());
        setInitialUrl("http://search.havenondemand.com");
    }

    @Before
    public void setUp(){
        getElementFactory().getTopNavBar().loginButton().click();
        getElementFactory().getLoginPage().loginWith(getConfig().getDefaultUser().getAuthProvider());
        landingPage = getElementFactory().getHSODPage();
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        landingPage.launchSearchOptimizer();
        assertThat(getWindow(), urlContains("search.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        landingPage.launchFind();
        assertThat(getWindow(), urlContains("find.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}
}

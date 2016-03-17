package com.autonomy.abc.external;

import com.autonomy.abc.config.SOConfigLocator;
import com.autonomy.abc.config.SeleniumTest;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.config.TestParameterFactory;
import com.autonomy.abc.selenium.devconsole.DevConsole;
import com.autonomy.abc.selenium.devconsole.DevConsoleElementFactory;
import com.autonomy.abc.selenium.devconsole.HSODLandingPage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import static com.autonomy.abc.framework.TestStateAssert.assertThat;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;

@RunWith(Parameterized.class)
public class DevConsoleITCase extends SeleniumTest<DevConsole, DevConsoleElementFactory> {
    private HSODLandingPage landingPage;

    public DevConsoleITCase(TestConfig config) {
        super(config, new DevConsole());
        setInitialUrl("http://search.havenondemand.com");
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return new TestParameterFactory().create(new SOConfigLocator().getJsonConfig());
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

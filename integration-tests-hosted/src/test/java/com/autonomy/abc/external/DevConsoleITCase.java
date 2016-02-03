package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.DevConsole;
import com.autonomy.abc.selenium.navigation.DevConsoleElementFactory;
import com.autonomy.abc.selenium.page.devconsole.HSODLandingPage;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DevConsoleITCase extends HostedTestBase {
    private DevConsoleElementFactory devFactory;

    private HSODLandingPage devConsole;

    // TODO: this test should get all urls from config
    public DevConsoleITCase(TestConfig config) {
        super(config);
        setInitialUrl("http://search.havenondemand.com");
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp(){
        devFactory = new DevConsole(getMainSession().getActiveWindow()).elementFactory();

        devFactory.getHSODPage().clickLogInButton();

        loginTo(devFactory.getLoginPage(), getDriver(), config.getDefaultUser());
        devConsole = devFactory.getHSODPage();
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        devConsole.launchSearchOptimizer();
        assertThat(getDriver().getCurrentUrl(), containsString("search.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        devConsole.launchFind();
        assertThat(getDriver().getCurrentUrl(), containsString("find.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}
}

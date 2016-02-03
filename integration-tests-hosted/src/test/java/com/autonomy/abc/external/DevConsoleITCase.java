package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.application.DevConsole;
import com.autonomy.abc.selenium.navigation.DevConsoleElementFactory;
import com.autonomy.abc.selenium.page.devconsole.DevConsoleSearchPage;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DevConsoleITCase extends HostedTestBase {
    private DevConsoleElementFactory devFactory;

    private DevConsoleSearchPage devConsole;

    // TODO: this test should get all urls from config
    public DevConsoleITCase(TestConfig config) {
        super(config);
        setInitialUrl("http://search.havenondemand.com");
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp(){
        devFactory = new DevConsole(getMainSession().getActiveWindow()).elementFactory();

        devFactory.getDevConsoleSearchPage().clickLogInButton();

        loginTo(devFactory.getDevConsoleLoginPage(), getDriver(), config.getDefaultUser());
        devConsole = devFactory.getDevConsoleSearchPage();
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

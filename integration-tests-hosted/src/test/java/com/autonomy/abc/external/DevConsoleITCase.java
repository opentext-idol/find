package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.devconsole.DevConsole;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DevConsoleITCase extends HostedTestBase {
    private DevConsole devConsole;

    public DevConsoleITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        setInitialUrl("http://search.havenondemand.com");
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp(){
        getElementFactory().getDevConsole().clickLogInButton();
        getElementFactory().getLoginPage();
        try {
            loginAs(config.getDefaultUser());
        } catch (NoSuchElementException e) {
            /* This happens because it's expecting to sign into Search Optimizer */
        }
        devConsole = getElementFactory().getDevConsole();
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

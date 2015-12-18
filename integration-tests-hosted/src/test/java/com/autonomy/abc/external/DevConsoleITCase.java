package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
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
    private final User loginUser;
    private WebElement launchAppBtn;

    public DevConsoleITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        setInitialUrl("http://search.havenondemand.com");
        setInitialUser(User.NULL);
        loginUser = config.getUser("twitter");
    }

    @Before
    public void setUp(){
        getDriver().findElement(By.id("loginLogout")).click();
        getElementFactory().getLoginPage();
        try {
            loginAs(loginUser);
        } catch (NoSuchElementException e) {
            /* This happens because it's expecting to sign into Search Optimizer */
        }
        launchAppBtn = new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='Launch app']")));
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        launchAppBtn.click();
        getElementFactory().getAnalyticsPage();
        assertThat(getDriver().getCurrentUrl(), containsString("search.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        getDriver().findElement(By.className("hsod-find-button")).click();
        getElementFactory().getFindPage();
        assertThat(getDriver().getCurrentUrl(), containsString("find.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}
}

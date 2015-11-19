package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.users.User;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DevConsoleITCase extends HostedTestBase {
    private final User loginUser;

    public DevConsoleITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        setInitialUrl("http://search.havenondemand.com");
        setInitialUser(User.NULL);
        loginUser = config.getUser("twitter");
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        button("Admin").click();
        getElementFactory().getLoginPage();
        loginAs(loginUser);
        assertThat(getDriver().getCurrentUrl(), containsString("search.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        button("Find").click();
        getElementFactory().getFindLoginPage().loginWith(loginUser.getAuthProvider());
        assertThat(getDriver().getCurrentUrl(), containsString("find.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}

    private WebElement button(String button){
        return getDriver().findElement(By.xpath("//*[text()='"+button+"']/../a"));
    }
}

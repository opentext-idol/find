package com.autonomy.abc.external;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class DevConsoleITCase extends HostedTestBase {

    public DevConsoleITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Override
    public void baseSetUp() throws MalformedURLException {
        regularSetUp();
        getDriver().get("http://search.havenondemand.com");
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        button("Admin").click();
        getElementFactory().getLoginPage();
        hostedLogIn("twitter");
        assertThat(getDriver().getCurrentUrl(), containsString("search.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        button("Find").click();
        getElementFactory().getFindLoginPage().loginWith(config.getUser("twitter").getAuthProvider());
        assertThat(getDriver().getCurrentUrl(), containsString("find.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}

    private WebElement button(String button){
        return getDriver().findElement(By.xpath("//*[text()='"+button+"']/../a"));
    }
}

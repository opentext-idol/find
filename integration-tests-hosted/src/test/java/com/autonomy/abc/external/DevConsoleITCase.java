package com.autonomy.abc.external;

import com.autonomy.abc.config.ABCTestBase;
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

public class DevConsoleITCase extends ABCTestBase {

    public DevConsoleITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Override
    public void baseSetUp() throws MalformedURLException {
        regularSetUp();
        getDriver().get("http://search.preview.havenondemand.com");
    }

    @Test
    public void testLaunchSearch() throws InterruptedException {
        button("Search Admin").click();
        Thread.sleep(2000);
        tryLogIn();
        assertThat(getDriver().getCurrentUrl(), containsString("search.preview.havenapps.io"));
    }

    @Test
    public void testLaunchFind() throws InterruptedException {
        button("Find").click();
        Thread.sleep(2000);
        tryLogIn();
        assertThat(getDriver().getCurrentUrl(),containsString("find.preview.havenapps.io"));
    }

    @Test
    @Ignore
    public void testSignUp(){}

    private WebElement button(String button){
        return getDriver().findElement(By.xpath("//*[text()='"+button+"']/../a"));
    }
}

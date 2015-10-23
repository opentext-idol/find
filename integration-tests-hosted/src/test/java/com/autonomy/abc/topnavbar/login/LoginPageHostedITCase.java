package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LoginPageHostedITCase extends ABCTestBase {

    private Logger LOGGER;

    public LoginPageHostedITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Override
    public void baseSetUp(){
        regularSetUp();
    }

    private void setLoginType(String type){
        System.setProperty("com.autonomy.loginType", type);
    }

    private void tryLogIn() {
        try {
            getElementFactory().getLoginPage().loginWith(getApplication().createCredentials());
            //Wait for page to load
            Thread.sleep(2000);
            // now has side/top bar
            body = getBody();
        } catch (Exception e) {
            LOGGER.error("Unable to login");
            fail("Unable to login");
        }

        assertThat(new AbcHasLoggedIn(getDriver()).hasLoggedIn(), is(true));
    }

    private void setUsernameAndPassword(String username,String password){
        System.setProperty("com.autonomy.username",username);
        System.setProperty("com.autonomy.password",password);
    }

    @Test
    public void testAPIKeyLogin(){
        String apiKey = "";
        setLoginType("apikey");
        System.setProperty("com.autonomy.apikey", apiKey);
        tryLogIn();
    }

    @Test
    public void testGoogleLogin(){
        setLoginType("google");
        setUsernameAndPassword("", "");
        tryLogIn();
    }

    @Test
    public void testTwitterLogin(){
        setLoginType("twitter");
        setUsernameAndPassword("", "");
        tryLogIn();

    }

    @Test
    public void testFacebookLogin(){
        setLoginType("facebook");
        setUsernameAndPassword("", "");
        tryLogIn();

    }

    @Test
    public void testYahooLogin(){
        setLoginType("yahoo");
        setUsernameAndPassword("", "");
        tryLogIn();
    }

    @Test
    public void testOpenIDLogin(){
        String url = "";
        setLoginType("openid");
        System.setProperty("com.autonomy.url", url)
        tryLogIn();
    }

    @Test
    public void testHPPassportLogin(){
        setLoginType("passport");
        setUsernameAndPassword("","");
        tryLogIn();
    }
}

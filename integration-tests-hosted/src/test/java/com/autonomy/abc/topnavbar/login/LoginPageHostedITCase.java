package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.statements.StatementArtifactHandler;
import com.autonomy.abc.framework.statements.StatementLoggingHandler;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;

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
        System.setProperty("com.autonomy.loginType",type);
    }

    @Test
    public void testAPIKeyLogin(){
        setLoginType("apikey");
    }

    @Test
    public void testGoogleLogin(){
        setLoginType("google");

    }

    @Test
    public void testTwitterLogin(){
        setLoginType("twitter");
    }

    @Test
    public void testFacebookLogin(){
        setLoginType("facebook");

    }

    @Test
    public void testYahooLogin(){
        setLoginType("yahoo");

    }

    @Test
    public void testOpenIDLogin(){
        setLoginType("openid");

    }

    @Test
    public void testHPPassportLogin(){
        setLoginType("passport");
    }
}

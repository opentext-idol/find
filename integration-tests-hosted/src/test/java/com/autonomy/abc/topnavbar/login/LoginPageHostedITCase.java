package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import org.junit.Test;
import org.openqa.selenium.Platform;

/*
 * TODO Possibly make sure a gritter with 'Signed in' comes up, correct colour circle etc. May be difficult to do considering it occurs during tryLogIn()
 */
public class LoginPageHostedITCase extends ABCTestBase {

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
        setUsernameAndPassword("hodtesting", "5cUdPhYM");
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
        System.setProperty("com.autonomy.url", url);
        tryLogIn();
    }

    @Test
    public void testHPPassportLogin(){
        setLoginType("passport");
        setUsernameAndPassword("", "");
        tryLogIn();
    }
}

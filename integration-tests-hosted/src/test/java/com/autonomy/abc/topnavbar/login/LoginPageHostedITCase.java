package com.autonomy.abc.topnavbar.login;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.users.User;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.openqa.selenium.lift.Matchers.displayed;

/*
 * TODO Possibly make sure a gritter with 'Signed in' comes up, correct colour circle etc. May be difficult to do considering it occurs during tryLogIn()
 */
public class LoginPageHostedITCase extends HostedTestBase {

    public LoginPageHostedITCase(TestConfig config) {
        super(config);
        setInitialUser(User.NULL);
    }

    @Test   @Ignore("No account")
    public void testAPIKeyLogin(){
       testLogin("api_key");
    }

    @Test
    public void testGoogleLogin(){
        testLogin("google");
    }

    @Test
    public void testTwitterLogin(){
        testLogin("twitter");
    }

    @Test   @Ignore("No account")
    public void testFacebookLogin(){
        testLogin("facebook");
    }

    @Test
    public void testYahooLogin(){
        testLogin("yahoo");
    }

    @Test   @Ignore("No account")
    public void testOpenIDLogin(){
        testLogin("open_id");
    }

    @Test
    public void testHPPassportLogin(){
        testLogin("hp_passport");
    }

    private void testLogin(String account) {
        try {
            loginAs(config.getUser(account));
        } catch (Exception e) {
            throw new AssertionError("unable to log in as " + account, e);
        }
    }

    @Test
    public void testLogInSearchOptimizerToFind(){
        User user = config.getDefaultUser();
        loginAs(user);

        getDriver().navigate().to(config.getFindUrl());
        verifyThat(getElementFactory().getFindPage(), displayed());
    }

    @Test
    public void testLoginFindToSearchOptimizer(){
        getElementFactory().getLoginPage();

        getDriver().navigate().to(config.getFindUrl());
        loginTo(getElementFactory().getFindLoginPage(), getDriver(), config.getDefaultUser());

        getDriver().navigate().to(config.getWebappUrl());
        verifyThat(getElementFactory().getPromotionsPage(), displayed());
    }

    @Test
    public void testLogOutSearchOptimizerToFind(){
        loginAs(config.getDefaultUser());

        getElementFactory().getTopNavBar().logOut();

        getDriver().navigate().to(config.getFindUrl());
        getElementFactory().getFindLoginPage();

        verifyThat(getDriver().findElement(By.linkText("Google")), displayed());
    }

    @Test
    public void testLogOutFindToSearchOptimizer(){
        getElementFactory().getLoginPage();

        getDriver().navigate().to(config.getFindUrl());
        loginTo(getElementFactory().getFindLoginPage(), getDriver(), config.getDefaultUser());

        Find find = getElementFactory().getFindPage();
        find.logOut();

        getElementFactory().getFindLoginPage();

        getDriver().navigate().to(config.getWebappUrl());
        getElementFactory().getLoginPage();

        verifyThat(getDriver().findElement(By.linkText("Google")), displayed());
    }
}

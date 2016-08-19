package com.autonomy.abc.users;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.Matchers.notNullValue;

/*
 * TODO Possibly make sure a gritter with 'Signed in' comes up, correct colour circle etc. May be difficult to do considering it occurs during tryLogIn()
 */
public class HsodLoginPageITCase extends IsoHsodTestBase {

    public HsodLoginPageITCase(final TestConfig config) {
        super(config);
        setInitialUser(User.NULL);
    }

    @Before
    public void setUp() {
        // wait before doing anything
        getElementFactory().getLoginPage();
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

    @Test
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

    private void testLogin(final String account) {
        try {
            getApplication().loginService().login(getConfig().getUser(account));
            verifyThat("on promotions page", getElementFactory().getPromotionsPage(), notNullValue());
        } catch (final Exception e) {
            throw new AssertionError("unable to log in as " + account, e);
        }
    }

}

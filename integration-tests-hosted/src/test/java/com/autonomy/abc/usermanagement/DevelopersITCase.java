package com.autonomy.abc.usermanagement;

import com.autonomy.abc.base.HostedTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.selenium.users.HSODDeveloperService;
import com.autonomy.abc.selenium.users.HsodDevelopersPage;
import com.hp.autonomy.frontend.selenium.users.User;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class DevelopersITCase extends HostedTestBase {
    private HSODDeveloperService developerService;
    private HsodDevelopersPage developersPage;

    public DevelopersITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        developerService = getApplication().developerService();
        developersPage = developerService.goToDevs();
    }

    @Test
    public void testEditDevUsername(){
        User developer = developersPage.getUser(0);
        String originalUsername = developer.getUsername();
        String newUsername = "Jeremy Clarkson";
        try {
            developerService.editUsername(developer, newUsername);
            verifyThat(developersPage.getUsernames(), hasItem(newUsername));
        } finally {
            developerService.editUsername(developer, originalUsername);
        }
    }
}

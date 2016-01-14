package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import com.autonomy.abc.selenium.users.User;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class DevelopersITCase extends HostedTestBase {
    private HSODeveloperService developerService;
    private HSODevelopersPage developersPage;

    public DevelopersITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        developerService = getApplication().createDeveloperService(getElementFactory());
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

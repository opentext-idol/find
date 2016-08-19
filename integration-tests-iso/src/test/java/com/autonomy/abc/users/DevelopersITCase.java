package com.autonomy.abc.users;

import com.autonomy.abc.base.IsoHsodTestBase;
import com.autonomy.abc.selenium.users.HsodDeveloperService;
import com.autonomy.abc.selenium.users.HsodDevelopersPage;
import com.autonomy.abc.selenium.users.table.UserTableRow;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class DevelopersITCase extends IsoHsodTestBase {
    private HsodDevelopersPage developersPage;

    public DevelopersITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        final HsodDeveloperService developerService = getApplication().developerService();
        developersPage = developerService.goToDevs();
    }

    @Test
    public void testEditDevUsername(){
        final UserTableRow row = developersPage.getTable().row(0);
        final String originalUsername = row.getUsername();
        final String newUsername = "Jeremy Clarkson";
        try {
            row.changeUsernameTo(newUsername);
            verifyThat(developersPage.getUsernames(), hasItem(newUsername));
        } finally {
            row.changeUsernameTo(originalUsername);
        }
    }
}

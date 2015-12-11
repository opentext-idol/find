package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class DevelopersITCase extends HostedTestBase {
    private HSODeveloperService devService;
    private HSODevelopersPage devsPage;

    public DevelopersITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        devService = getApplication().createDeveloperService(getElementFactory());
        devsPage = devService.goToDevs();
    }


    @Test
    public void testEditDevUsername(){
        try {
            String newUsername = "Jeremy Clarkson";
            devService.editUsername(devService.DEVELOPER, newUsername);
            verifyThat(devsPage.getUsernames(), hasItem(newUsername));
        } finally {
            devService.editUsername(devService.DEVELOPER, "Aero Bubbles");
        }
    }
}

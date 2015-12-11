package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.users.HSODeveloperService;
import com.autonomy.abc.selenium.users.HSOUser;
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
        HSOUser dev = devService.DEVELOPER;
        String originalUsername = dev.getUsername();
        String newUsername = "Jeremy Clarkson";
        try {
            devService.editUsername(dev, newUsername);
            verifyThat(devsPage.getUsernames(), hasItem(newUsername));
        } finally {
            devService.editUsername(dev, originalUsername);
        }
    }
}

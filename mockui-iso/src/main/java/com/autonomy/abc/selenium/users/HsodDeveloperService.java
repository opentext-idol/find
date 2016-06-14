package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.auth.HsodUserBuilder;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HsodDeveloperService extends ServiceBase<IsoHsodElementFactory> {

    public HsodDeveloperService(final IsoApplication<? extends IsoHsodElementFactory> application){
        super(application);
    }

    public HsodDevelopersPage goToDevs(){
        final HsodDevelopersPage devsPage = getApplication().switchTo(HsodDevelopersPage.class);
        return devsPage;
    }

    public User editUsername(final User user, final String newUsername) {
        goToDevs().getUserRow(user).changeUsernameTo(newUsername);
        Waits.loadOrFadeWait();
        return new HsodUserBuilder(user)
                .setUsername(newUsername)
                .build();
    }
}

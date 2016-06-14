package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.auth.HsodUserBuilder;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;

public class HsodDeveloperService extends ServiceBase<IsoHsodElementFactory> {

    public HsodDeveloperService(final IsoApplication<? extends IsoHsodElementFactory> application){
        super(application);
    }

    public HsodDevelopersPage goToDevs(){
        return getApplication().switchTo(HsodDevelopersPage.class);
    }

    public User editUsername(final User user, final String newUsername) {
        goToDevs().getUserRow(user).changeUsernameTo(newUsername);
        Waits.loadOrFadeWait();
        return new HsodUserBuilder(user)
                .setUsername(newUsername)
                .build();
    }
}

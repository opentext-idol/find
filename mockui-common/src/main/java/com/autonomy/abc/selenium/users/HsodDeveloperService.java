package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.autonomy.abc.selenium.users.HsodDevelopersPage;
import com.autonomy.abc.selenium.users.HsodUserBuilder;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HsodDeveloperService extends ServiceBase<IsoHsodElementFactory> {
    private HsodDevelopersPage devsPage;

    public HsodDeveloperService(SearchOptimizerApplication<? extends IsoHsodElementFactory> application){
        super(application);
    }

    public HsodDevelopersPage goToDevs(){
        devsPage = getApplication().switchTo(HsodDevelopersPage.class);
        return devsPage;
    }

    public User editUsername(User user, String newUsername) {
        devsPage = goToDevs();
        WebElement pencil = devsPage.editUsernameLink(user);
        pencil.click();
        devsPage.editUsernameInput(user).setAndSubmit(newUsername);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(pencil));
        Waits.loadOrFadeWait();
        return new HsodUserBuilder(user)
                .setUsername(newUsername)
                .build();
    }
}

package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.IsoHsodElementFactory;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSODDeveloperService extends ServiceBase<IsoHsodElementFactory> {
    private HSODDevelopersPage devsPage;

    public HSODDeveloperService(SearchOptimizerApplication<? extends IsoHsodElementFactory> application){
        super(application);
    }

    public HSODDevelopersPage goToDevs(){
        devsPage = getApplication().switchTo(HSODDevelopersPage.class);
        return devsPage;
    }

    public User editUsername(User user, String newUsername) {
        devsPage = goToDevs();
        WebElement pencil = devsPage.editUsernameLink(user);
        pencil.click();
        devsPage.editUsernameInput(user).setAndSubmit(newUsername);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(pencil));
        Waits.loadOrFadeWait();
        ((HSODUser) user).setUsername(newUsername);
        return user;
    }
}

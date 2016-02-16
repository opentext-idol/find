package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.hsod.HSODElementFactory;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSODDeveloperService extends ServiceBase<HSODElementFactory> {
    private HSODDevelopersPage devsPage;

    public HSODDeveloperService(SearchOptimizerApplication<? extends HSODElementFactory> application){
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

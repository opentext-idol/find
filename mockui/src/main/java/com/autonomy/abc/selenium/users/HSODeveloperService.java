package com.autonomy.abc.selenium.users;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.config.SearchOptimizerApplication;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSODeveloperService extends ServiceBase<HSOElementFactory> {
    private HSODevelopersPage devsPage;

    public HSODeveloperService(SearchOptimizerApplication application, ElementFactory elementFactory){
        super(application, (HSOElementFactory) elementFactory);
    }

    public HSODevelopersPage goToDevs(){
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.DEVELOPERS);
        devsPage = getElementFactory().getDevsPage();
        return devsPage;
    }

    public User editUsername(User user, String newUsername) {
        devsPage = goToDevs();
        WebElement pencil = devsPage.editUsernameLink(user);
        pencil.click();
        devsPage.editUsernameInput(user).setAndSubmit(newUsername);
        new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOf(pencil));
        Waits.loadOrFadeWait();
        ((HSOUser) user).setUsername(newUsername);
        return user;
    }
}

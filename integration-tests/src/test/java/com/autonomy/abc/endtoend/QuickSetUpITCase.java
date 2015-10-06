package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.PromotionActionFactory;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.HSOAppBody;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.WebDriverWait;

public class QuickSetUpITCase extends ABCTestBase {

    public QuickSetUpITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    GettingStartedPage gettingStarted;

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.GETTING_STARTED);
        body = new HSOAppBody(getDriver());
        gettingStarted = ((HSOElementFactory) getElementFactory()).getGettingStartedPage();
    }

    @Test
    public void testQuickSetUp(){
        String site = "www.cnet.com";
        gettingStarted.addSiteToIndex(site);
        new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Document \"http://" + site + "\" was uploaded successfully"));

        body.getTopNavBar().search(site);
        SearchPage searchPage = getElementFactory().getSearchPage();

        //TODO check right doc

        searchPage.createAPromotion();

        CreateNewPromotionsPage createNewPromotionsPage = getElementFactory().getCreateNewPromotionsPage();

//        PromotionActionFactory promotionActionFactory = new PromotionActionFactory(getApplication(),getElementFactory());
//        promotionActionFactory.

        String trigger = "trigger";

        createNewPromotionsPage.addSpotlightPromotion("",trigger);
    }

    @After
    public void tearDown(){}

}

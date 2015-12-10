package com.autonomy.abc.connections;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

public class IndexWizardITCase extends HostedTestBase {

    private CreateNewIndexPage createNewIndexPage;

    public IndexWizardITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp(){
        body.getSideNavBar().switchPage(NavBarTabId.INDEXES);

        getElementFactory().getIndexesPage().newIndexButton().click();
        body = getBody();

        createNewIndexPage = getElementFactory().getCreateNewIndexPage();
    }

    @Test
    //CSA-1616
    public void testUppercaseFieldNames() {
        createNewIndexPage.inputIndexName("name");

        createNewIndexPage.nextButton().click();

        final List<String> capitals = new ArrayList<String>() {{
            add("London");
            add("Paris");
            add("Washington");
        }};

        List<String> lowercase = new ArrayList<String>() {{
            for(String capital : capitals){
                add(capital.toLowerCase());
            }
        }};

        createNewIndexPage.inputIndexFields(capitals);

        WebElement errorMessage = configErrorMessage(createNewIndexPage.advancedIndexFields());
        String error = "field names can contain only lowercase alphanumeric characters";

        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));

        createNewIndexPage.advancedIndexFields().clear();
        createNewIndexPage.inputIndexFields(lowercase);

        verifyThat(errorMessage, not(displayed()));

        createNewIndexPage.inputParametricFields(capitals);

        errorMessage = configErrorMessage(createNewIndexPage.advancedParametricFields());

        verifyThat(errorMessage, displayed());
        verifyThat(errorMessage, containsText(error));

        createNewIndexPage.advancedParametricFields().clear();
        createNewIndexPage.inputParametricFields(lowercase);

        verifyThat(errorMessage, not(displayed()));
    }

    private WebElement configErrorMessage(WebElement element){
        return ElementUtil.ancestor(element,1).findElement(By.tagName("p"));
    }

    @After
    public void tearDown(){}
}

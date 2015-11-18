package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created by avidan on 10-11-15.
 */
public class WebConnectorTypeITCase extends ConnectorTypeStepBase {
    public WebConnectorTypeITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private FormInput connectorUrl ;
    private FormInput connectorName ;

    @Before
    public void selectStep() {
        selectConnectorType(ConnectorType.WEB);
        connectorUrl = connectorTypeStepTab.connectorUrl();
        connectorName = connectorTypeStepTab.connectorName();
    }

    @Test
    public void testConnectorTypeSelection() {
        for (ConnectorType type : ConnectorType.values()) {
            selectConnectorType(type);
        }
    }

    @Test
    public void testWebConnectorAutoPrefixed() {
        connectorUrl.getElement().click();
        assertThat("The 'http://' prefix was added to the web connector url field", connectorUrl.getElement(), hasAttribute("value", equalTo("http://")));
    }

    /**
     * Test for name extraction from valid URLs without the auto http prefix
     */
    @Test
    public void testWebConnectorValidatorsPositive() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "http://www.foo.com");
        map.put("koo", "http://koo.co.uk");
        map.put("koo2", "https://koo2.co.uk");
        map.put("koo3", "http://koo3.co.uk");
        map.put("koo4", "http://koo4.com/a?a=vv");

        for (String key : map.keySet()) {

            connectorUrl.setValue(map.get(key));
            connectorName.getElement().click();
            assertThat("The url input field is valid ", connectorUrl.getElement(), not(hasClass(INVALID_INPUT_CLASS)));
            assertThat("The name was extracted from the URL", connectorName.getElement().getAttribute("value"), equalTo(key));
            assertThat("The next button in the wizard is enabled", newConnectionPage.nextButton(), not(disabled()));
            connectorUrl.clear();
            connectorName.clear();
        }
    }

    /**
     * Test for name extraction from invalid URLs
     */
    @Test
    public void testWebConnectorValidatorsNegative() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "foo,com");
        map.put("koo", "koo.co.uk");
        map.put("koo2", "ftp://koo.co.uk");
        map.put("koo3", "smb://koo.co.uk");
        map.put("koo4", "nfs://koo.co.uk");
        map.put("koo4", "nfs://koo.co.uk?a=1");

        for (String key : map.keySet()) {
            connectorUrl.setValue(map.get(key));
            connectorName.getElement().click();
            assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorUrl.getElement())), hasClass(INVALID_INPUT_CLASS));

            newConnectionPage.nextButton().click();
            assertThat("The step should be set as error ", newConnectionPage.connectorTypeStepTab(), not(stepIsValid()));
            assertThat("The step should be set as current", newConnectionPage.connectorTypeStepTab(), stepIsCurrent());

            connectorUrl.clear();
            connectorName.clear();
        }
    }

    /**
     * Test that the connector URL and name are required fields
     */
    @Test
    public void testWebConnectorRequiredFields() {
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorUrl.getElement())), hasClass(INVALID_INPUT_CLASS));
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorName.getElement())), hasClass(INVALID_INPUT_CLASS));

        connectorUrl.setValue("http://foo.com");
        connectorName.clear();
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorUrl.getElement())), not(hasClass(INVALID_INPUT_CLASS)));
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorName.getElement())), hasClass(INVALID_INPUT_CLASS));

        connectorUrl.clear();
        connectorName.clear();
        connectorName.setValue("foo");
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorUrl.getElement())), hasClass(INVALID_INPUT_CLASS));
        assertThat("The url input field isn't valid ", AppElement.getParent(AppElement.getParent(connectorName.getElement())), not(hasClass(INVALID_INPUT_CLASS)));
    }

    @Test
    //CSA1562
    public void testAdvancedConfigurations() throws InterruptedException {
        connectorUrl.setValue("http://www.w.ww");
        connectorName.setValue("jeremy");

        newConnectionPage.nextButton().click();

        ConnectorConfigStepTab connectorConfigStep = ConnectorConfigStepTab.make(getDriver());

        connectorConfigStep.advancedConfigurations().click();

        //Let the dropdown open
        Thread.sleep(1000);

        connectorConfigStep.getMaxPagesBox().setValue("9");
        connectorConfigStep.getDurationBox().setValue("59");
        connectorConfigStep.getMaxLinksBox().setValue("-1");
        connectorConfigStep.getMaxPageSizeBox().setValue("-1");
        connectorConfigStep.getMinPageSizeBox().setValue("-1");
        connectorConfigStep.getPageTimeoutBox().setValue("0");
        connectorConfigStep.getDepthBox().setValue("-2");

        newConnectionPage.nextButton().click();
        assertThat(connectorConfigStep.advancedConfigurations().isDisplayed(), is(true));

        checkFormInputError(connectorConfigStep.getMaxPagesBox(), 10);
        checkFormInputError(connectorConfigStep.getDurationBox(), 60);
        checkFormInputError(connectorConfigStep.getMaxLinksBox(), 0);
        checkFormInputError(connectorConfigStep.getMaxPageSizeBox(), 0);
        checkFormInputError(connectorConfigStep.getMinPageSizeBox(), 0);
        checkFormInputError(connectorConfigStep.getPageTimeoutBox(), 1);
        checkFormInputError(connectorConfigStep.getDepthBox(), -1);

        connectorConfigStep.getMaxPagesBox().setValue("10");
        connectorConfigStep.getDurationBox().setValue("60");
        connectorConfigStep.getMaxLinksBox().setValue("0");
        connectorConfigStep.getMaxPageSizeBox().setValue("0");
        connectorConfigStep.getMinPageSizeBox().setValue("0");
        connectorConfigStep.getPageTimeoutBox().setValue("1");
        connectorConfigStep.getDepthBox().setValue("-1");

        newConnectionPage.nextButton().click();
        assertThat(connectorConfigStep.advancedConfigurations().isDisplayed(), is(false));
    }

    private void checkFormInputError(FormInput form, int minimum){
        String visibleError = form.getElement().findElement(By.xpath(".//../../p[not(contains(@class,'ng-hide'))]")).getText();

        assertThat(visibleError,is("Invalid Value (should be "+ minimum +" at minimum)"));
    }
}

package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.disabled;
import static com.autonomy.abc.matchers.ElementMatchers.hasAttribute;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

/**
 * Created by avidan on 10-11-15.
 */
public class WebConnectorTypeITCase extends ConnectorTypeStepBase {
    public WebConnectorTypeITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    private FormInput connectorUrl;
    private FormInput connectorName;

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
     * Test that the connector URL and name are required fields
     */
    @Test
    public void testWebConnectorRequiredFields() {
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorUrl.getElement(), 2), hasClass(INVALID_INPUT_CLASS));
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorName.getElement(), 2), hasClass(INVALID_INPUT_CLASS));

        connectorUrl.setValue("http://foo.com");
        connectorName.clear();
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorUrl.getElement(), 2), not(hasClass(INVALID_INPUT_CLASS)));
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorName.getElement(), 2), hasClass(INVALID_INPUT_CLASS));

        connectorUrl.clear();
        connectorName.clear();
        connectorName.setValue("foo");
        newConnectionPage.nextButton().click();
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorUrl.getElement(), 2), hasClass(INVALID_INPUT_CLASS));
        assertThat("The url input field isn't valid ", ElementUtil.ancestor(connectorName.getElement(), 2), not(hasClass(INVALID_INPUT_CLASS)));
    }

    @Test
    //CSA-1562
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

    @Test
    //CSA-1700
    //CSA-1889
    //CSA-1789
    public void testConnectorName() {
        // Valid URL with Lower/Upper case mix
        updateUrlAndVerifyConnectorName("HTTP://www.ExAMPle.com", "example", true, true);
        updateUrlAndVerifyConnectorName("http://www.TAkingBAckSuNDAy.com", "takingbacksunday", true, true);
        updateUrlAndVerifyConnectorName("HTTP://WWW.FLICKERFLICKERFADE.COM", "flickerflickerfade", true, true);
        updateUrlAndVerifyConnectorName("hTTp://www.itTAKESMORE.com", "ittakesmore", true, true);
        updateUrlAndVerifyConnectorName("http://WWW.LOUDERNOW.com", "loudernow", true, true);

        // Valid URLs
        updateUrlAndVerifyConnectorName("http://www.foo.com", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://koo.co.uk", "koo", true, true);
        updateUrlAndVerifyConnectorName("https://koo2.co.uk", "koo2", true, true);
        updateUrlAndVerifyConnectorName("http://koo3.co.uk", "koo3", true, true);
        updateUrlAndVerifyConnectorName("http://koo4.com/a?a=vv", "koo4", true, true);
        updateUrlAndVerifyConnectorName("http://foo-koo.com/blah_blah", "foo-koo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.foo-koo.com/blah_blah", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_blah", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_blah/#/search", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_blah/#/search", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_blah_(wikipedia)", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_blah_(wikipedia)_(again)", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_(wikipedia)_blah#cite-1", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/unicode_(✪)_in_parens", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/(something)?after=parens", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.com/blah_(wikipedia)#cite-1", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://foo.bar/?q=Test%20URL-encoded%20stuff", "foo", true, true);
        updateUrlAndVerifyConnectorName("http://www.example.com/wpstyle/?p=364", "example", true, true);
        updateUrlAndVerifyConnectorName("https://www.example.com/foo/?bar=baz&inga=42&quux", "example", true, true);
        updateUrlAndVerifyConnectorName("http://code.google.com/events/#&product=browser", "code", true, true);
        updateUrlAndVerifyConnectorName("http://j.mp", "j", true, true);
        updateUrlAndVerifyConnectorName("http://a.com", "a", true, true);
        updateUrlAndVerifyConnectorName("http://subdomain.a.com", "subdomain", true, true);
        updateUrlAndVerifyConnectorName("http://my-site.com", "my-site", true, true);
        updateUrlAndVerifyConnectorName("http://sub-domain.mysite.com", "sub-domain", true, true);
        updateUrlAndVerifyConnectorName("http://subdomain.my-site.com", "subdomain", true, true);

        // Valid long URLs - try to catch regex matcher timeout
        updateUrlAndVerifyConnectorName("http://www.very.very.very.very.very.very.very.very.long.domain.com", "very", true, true);
        updateUrlAndVerifyConnectorName("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", "very", true, true);

        // Valid URL with user information
        updateUrlAndVerifyConnectorName("http://userid:password@example.com:8080", "userid", true, true);
        updateUrlAndVerifyConnectorName("http://userid:password@example.com:8080/", "userid", true, true);
        updateUrlAndVerifyConnectorName("http://userid@example.com", "userid@example", true, false);
        updateUrlAndVerifyConnectorName("http://userid@example.com/", "userid@example", true, false);
        updateUrlAndVerifyConnectorName("http://userid@example.com:8080", "userid@example", true, false);
        updateUrlAndVerifyConnectorName("http://userid@example.com:8080/", "userid@example", true, false);
        updateUrlAndVerifyConnectorName("http://userid:password@example.com", "userid", true, true);
        updateUrlAndVerifyConnectorName("http://userid:password@example.com/", "userid", true, true);

        // Valid URL with special characters
        updateUrlAndVerifyConnectorName("http://✪df.ws/123", "✪df", true, false);
        updateUrlAndVerifyConnectorName("http://☺.damowmow.com/", "☺", true, false);
        updateUrlAndVerifyConnectorName("http://➡.ws/䨹", "➡", true, false);
        updateUrlAndVerifyConnectorName("http://⌘.ws", "⌘", true, false);
        updateUrlAndVerifyConnectorName("http://⌘.ws/", "⌘", true, false);
        updateUrlAndVerifyConnectorName("http://مثال.إختبار", "مثال", true, false);
        updateUrlAndVerifyConnectorName("http://例子.测试", "例子", true, false);
        updateUrlAndVerifyConnectorName("http://उदाहरण.परीक्षा", "उदाहरण", true, false);
        updateUrlAndVerifyConnectorName("http://בדיקה.co.il", "בדיקה", true, false);
        updateUrlAndVerifyConnectorName("http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com", "-", true, true);
        updateUrlAndVerifyConnectorName("http://1337.net", "1337", true, true);
        updateUrlAndVerifyConnectorName("http://a.b-c.de", "a", true, true);

        // Valid URL with IP for domain
        updateUrlAndVerifyConnectorName("http://223.255.255.254", "223", true, true);
        updateUrlAndVerifyConnectorName("http://142.42.1.1/", "142", true, true);
        updateUrlAndVerifyConnectorName("http://142.42.1.1:8080/", "142", true, true);

        // Invalid URL inputs
        updateUrlAndVerifyConnectorName("http://http://", "", false, false);
        updateUrlAndVerifyConnectorName("http://", "", false, false);
        updateUrlAndVerifyConnectorName("http://.", "", false, false);
        updateUrlAndVerifyConnectorName("http://..", "", false, false);
        updateUrlAndVerifyConnectorName("http://../", "", false, false);
        updateUrlAndVerifyConnectorName("http://?", "", false, false);
        updateUrlAndVerifyConnectorName("http://??", "", false, false);
        updateUrlAndVerifyConnectorName("http://??/", "", false, false);
        updateUrlAndVerifyConnectorName("http://#", "", false, false);
        updateUrlAndVerifyConnectorName("http://##", "", false, false);
        updateUrlAndVerifyConnectorName("http://##/", "", false, false);
        updateUrlAndVerifyConnectorName("http://foo.bar?q=Spaces should be encoded", "", false, false);
        updateUrlAndVerifyConnectorName("//", "", false, false);
        updateUrlAndVerifyConnectorName("//a", "", false, false);
        updateUrlAndVerifyConnectorName("///a", "", false, false);
        updateUrlAndVerifyConnectorName("///", "", false, false);
        updateUrlAndVerifyConnectorName("http:///a", "", false, false);
        updateUrlAndVerifyConnectorName("foo.com", "", false, false);
        updateUrlAndVerifyConnectorName("rdar://1234", "", false, false);
        updateUrlAndVerifyConnectorName("h://test", "", false, false);
        updateUrlAndVerifyConnectorName("http:// shouldfail.com", "", false, false);
        updateUrlAndVerifyConnectorName(":// should fail", "", false, false);
        updateUrlAndVerifyConnectorName("http://foo.bar/foo(bar)baz quux", "", false, false);
        updateUrlAndVerifyConnectorName("ftps://foo.bar/", "", false, false);
        updateUrlAndVerifyConnectorName("http://-error-.invalid/", "", false, false);
        updateUrlAndVerifyConnectorName("http://3628126748", "", false, false);
        updateUrlAndVerifyConnectorName("http://.www.foo.bar/", "", false, false);
        updateUrlAndVerifyConnectorName("http://www.foo.bar./", "", false, false);
        updateUrlAndVerifyConnectorName("http://.www.foo.bar./", "", false, false);

        // Invalid long URL input - try to catch regex matcher timeout
        updateUrlAndVerifyConnectorName("http://10.1.1.1", "", false, false);
        updateUrlAndVerifyConnectorName("http://www.very.very.very.very.very.very.very.very$.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", "", false, false);
        updateUrlAndVerifyConnectorName("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.$very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", "", false, false);
        updateUrlAndVerifyConnectorName("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.$very.very.long.domain.com", "", false, false);

        // Invalid IP addresses
        updateUrlAndVerifyConnectorName("http://0.0.0.0", "", false, false);
        updateUrlAndVerifyConnectorName("http://10.1.1.0", "", false, false);
        updateUrlAndVerifyConnectorName("http://10.1.1.255", "", false, false);
        updateUrlAndVerifyConnectorName("http://224.1.1.1", "", false, false);
        updateUrlAndVerifyConnectorName("http://1.1.1.1.1", "", false, false);
        updateUrlAndVerifyConnectorName("http://123.123.123", "", false, false);

        // Invalid protocol
        updateUrlAndVerifyConnectorName("foo,com", "", false, false);
        updateUrlAndVerifyConnectorName("koo.co.uk", "", false, false);
        updateUrlAndVerifyConnectorName("ftp://koo.co.uk", "", false, false);
        updateUrlAndVerifyConnectorName("smb://koo.co.uk", "", false, false);
        updateUrlAndVerifyConnectorName("nfs://koo.co.uk", "", false, false);
        updateUrlAndVerifyConnectorName("nfs://koo.co.uk?a=1", "", false, false);
    }

    private void verifyURL(String url, boolean shouldPass) {
        connectorUrl.setValue(url);

        WebElement error = connectorUrl.getElement().findElement(By.xpath("./../p"));

        if (shouldPass) {
            verifyThat(error, hasClass("ng-hide"));
        } else {
            verifyThat(error, not(hasClass("ng-hide")));
        }
    }

    private void verifyName(String name, boolean shouldPass) {
        verifyThat(connectorName.getValue(), is(name.toLowerCase()));

        // Selecting the second error p tag for invalid connector name
        WebElement error = connectorName.getElement().findElements(By.xpath("./../p[contains(@class,'help-block')]")).get(1);

        if (shouldPass) {
            verifyThat(error, CoreMatchers.not(displayed()));
            verifyThat(error, hasClass("ng-hide"));
        } else {
            verifyThat(error, CoreMatchers.is(displayed()));
            verifyThat(error, not(hasClass("ng-hide")));
        }
    }

    private void updateUrlAndVerifyConnectorName(String url, String name, boolean isUrlValid, boolean isNameValid) {
        /* For debugging -
            System.out.println("************************************************* \n"+url+"************************************************* \n");
        */

        connectorName.clear();
        connectorUrl.setValue(url);
        connectorName.getElement().click();

        verifyURL(url, isUrlValid);
        verifyName(name.toLowerCase(), isNameValid);
        verifyThat(newConnectionPage.nextButton(), not(disabled()));
    }

    private void checkFormInputError(FormInput form, int minimum) {
        String visibleError = form.getElement().findElement(By.xpath(".//../../p[not(contains(@class,'ng-hide'))]")).getText();

        assertThat(visibleError, is("Invalid Value (should be " + minimum + " at minimum)"));
    }
}

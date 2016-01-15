package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.connections.wizard.ConnectorTypeStepBase;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorType;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;

public class WebConnectorTypeITCase extends ConnectorTypeStepBase {
    public WebConnectorTypeITCase(TestConfig config) {
        super(config);
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
    public void testAdvancedConfigurations() {
        connectorUrl.setValue("http://www.w.ww");
        connectorName.setValue("jeremy");

        newConnectionPage.nextButton().click();

        ConnectorConfigStepTab connectorConfigStep = ConnectorConfigStepTab.make(getDriver());

        connectorConfigStep.advancedConfigurations().click();

        //Let the dropdown open
        Waits.loadOrFadeWait();

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
    //CSA-1700 || CSA-1889
    public void testUppercaseUrls(){
        List<WebConnector> uppercase = new ArrayList<WebConnector>() {{
            add(new WebConnector("HTTP://www.ExAMPle.com", "example"));
            add(new WebConnector("http://www.TAkingBAckSuNDAy.com", "takingbacksunday"));
            add(new WebConnector("HTTP://WWW.FLICKERFLICKERFADE.COM", "flickerflickerfade"));
            add(new WebConnector("hTTp://www.itTAKESMORE.com", "ittakesmore"));
            add(new WebConnector("http://WWW.LOUDERNOW.com", "loudernow"));
        }};

        updateUrlAndVerifyConnectorName(uppercase, true, true);
    }

    @Test
    public void testValidUrls(){
        List<WebConnector> valid = new ArrayList<WebConnector>() {{
            add(new WebConnector("http://www.foo.com", "foo"));
            add(new WebConnector("http://koo.co.uk", "koo"));
            add(new WebConnector("https://koo2.co.uk", "koo2"));
            add(new WebConnector("http://koo3.co.uk", "koo3"));
            add(new WebConnector("http://koo4.com/a?a=vv", "koo4"));
            add(new WebConnector("http://foo-koo.com/blah_blah", "foo-koo"));
            add(new WebConnector("http://foo.foo-koo.com/blah_blah", "foo"));
            add(new WebConnector("http://foo.com/blah_blah", "foo"));
            add(new WebConnector("http://foo.com/blah_blah/#/search", "foo"));
            add(new WebConnector("http://foo.com/blah_blah/#/search", "foo"));
            add(new WebConnector("http://foo.com/blah_blah_(wikipedia)", "foo"));
            add(new WebConnector("http://foo.com/blah_blah_(wikipedia)_(again)", "foo"));
            add(new WebConnector("http://foo.com/blah_(wikipedia)_blah#cite-1", "foo"));
            add(new WebConnector("http://foo.com/unicode_(✪)_in_parens", "foo"));
            add(new WebConnector("http://foo.com/(something)?after=parens", "foo"));
            add(new WebConnector("http://foo.com/blah_(wikipedia)#cite-1", "foo"));
            add(new WebConnector("http://foo.bar/?q=Test%20URL-encoded%20stuff", "foo"));
            add(new WebConnector("http://www.example.com/wpstyle/?p=364", "example"));
            add(new WebConnector("https://www.example.com/foo/?bar=baz&inga=42&quux", "example"));
            add(new WebConnector("http://code.google.com/events/#&product=browser", "code"));
            add(new WebConnector("http://j.mp", "j"));
            add(new WebConnector("http://a.com", "a"));
            add(new WebConnector("http://subdomain.a.com", "subdomain"));
            add(new WebConnector("http://my-site.com", "my-site"));
            add(new WebConnector("http://sub-domain.mysite.com", "sub-domain"));
            add(new WebConnector("http://subdomain.my-site.com", "subdomain"));
        }};

        updateUrlAndVerifyConnectorName(valid, true, true);
    }

    @Test
    public void testSpecialCharacterUrls(){
        List<WebConnector> specialCharacters = new ArrayList<WebConnector>() {{
            add(new WebConnector("http://✪df.ws/123", "✪df"));
            add(new WebConnector("http://☺.damowmow.com/", "☺"));
            add(new WebConnector("http://➡.ws/䨹", "➡"));
            add(new WebConnector("http://⌘.ws", "⌘"));
            add(new WebConnector("http://⌘.ws/", "⌘"));
            add(new WebConnector("http://مثال.إختبار", "مثال"));
            add(new WebConnector("http://例子.测试", "例子"));
            add(new WebConnector("http://उदाहरण.परीक्षा", "उदाहरण"));
            add(new WebConnector("http://בדיקה.co.il", "בדיקה"));
        }};

        updateUrlAndVerifyConnectorName(specialCharacters, true, false);

        List<WebConnector> validSpecialCharacters = new ArrayList<WebConnector>() {{
            add(new WebConnector("http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com", "-"));
            add(new WebConnector("http://a.b-c.de", "a"));
            add(new WebConnector("http://1337.net", "1337"));
        }};

        updateUrlAndVerifyConnectorName(validSpecialCharacters, true, true);
    }

    @Test
    public void testValidUrlUserInformation(){
        List<WebConnector> validUrls = new ArrayList<WebConnector>() {{
            add(new WebConnector("http://userid:password@example.com:8080", "userid"));
            add(new WebConnector("http://userid:password@example.com:8080/", "userid"));
            add(new WebConnector("http://userid:password@example.com", "userid"));
            add(new WebConnector("http://userid:password@example.com/", "userid"));
        }};

        updateUrlAndVerifyConnectorName(validUrls, true, true);

        List<WebConnector> invalidUrls = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://userid@example.com", "userid@example"));
            add(new WebConnector("http://userid@example.com/", "userid@example"));
            add(new WebConnector("http://userid@example.com:8080", "userid@example"));
            add(new WebConnector("http://userid@example.com:8080/", "userid@example"));
        }};

        updateUrlAndVerifyConnectorName(invalidUrls, true, false);
    }

    @Test
    public void testInvalidUrls(){
        List<WebConnector> invalid = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://http://", ""));
            add(new WebConnector("http://", ""));
            add(new WebConnector("http://.", ""));
            add(new WebConnector("http://..", ""));
            add(new WebConnector("http://../", ""));
            add(new WebConnector("http://?", ""));
            add(new WebConnector("http://??", ""));
            add(new WebConnector("http://??/", ""));
            add(new WebConnector("http://#", ""));
            add(new WebConnector("http://##", ""));
            add(new WebConnector("http://##/", ""));
            add(new WebConnector("http://foo.bar?q=Spaces should be encoded", ""));
            add(new WebConnector("//", ""));
            add(new WebConnector("//a", ""));
            add(new WebConnector("///a", ""));
            add(new WebConnector("///", ""));
            add(new WebConnector("http:///a", ""));
            add(new WebConnector("foo.com", ""));
            add(new WebConnector("rdar://1234", ""));
            add(new WebConnector("h://test", ""));
            add(new WebConnector("http:// shouldfail.com", ""));
            add(new WebConnector(":// should fail", ""));
            add(new WebConnector("http://foo.bar/foo(bar)baz quux", ""));
            add(new WebConnector("ftps://foo.bar/", ""));
            add(new WebConnector("http://-error-.invalid/", ""));
            add(new WebConnector("http://3628126748", ""));
            add(new WebConnector("http://.www.foo.bar/", ""));
            add(new WebConnector("http://www.foo.bar./", ""));
            add(new WebConnector("http://.www.foo.bar./", ""));
        }};

        updateUrlAndVerifyConnectorName(invalid, false, false);
    }

    @Test
    public void testInvalidIPs(){
        List<WebConnector> invalid = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://0.0.0.0", ""));
            add(new WebConnector("http://10.1.1.0", ""));
            add(new WebConnector("http://10.1.1.255", ""));
            add(new WebConnector("http://224.1.1.1", ""));
            add(new WebConnector("http://1.1.1.1.1", ""));
            add(new WebConnector("http://123.123.123", ""));
        }};

        updateUrlAndVerifyConnectorName(invalid, false, false);
    }

    @Test
    //CSA-1789
    public void testInvalidLongUrls(){
        List<WebConnector> invalid = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://10.1.1.1", ""));
            add(new WebConnector("http://www.very.very.very.very.very.very.very.very$.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", ""));
            add(new WebConnector("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.$very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", ""));
            add(new WebConnector("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.$very.very.long.domain.com", ""));
        }};

        longUrls(invalid, false, false);
    }

    @Test
    public void testInvalidProtocol(){
        List<WebConnector> invalid = new ArrayList<WebConnector>(){{
            add(new WebConnector("foo,com", ""));
            add(new WebConnector("koo.co.uk", ""));
            add(new WebConnector("ftp://koo.co.uk", ""));
            add(new WebConnector("smb://koo.co.uk", ""));
            add(new WebConnector("nfs://koo.co.uk", ""));
            add(new WebConnector("nfs://koo.co.uk?a=1", ""));
        }};

        updateUrlAndVerifyConnectorName(invalid, false, false);
    }

    @Test
    public void testValidIPs(){
        List<WebConnector> valid = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://223.255.255.254", "223"));
            add(new WebConnector("http://142.42.1.1/", "142"));
            add(new WebConnector("http://142.42.1.1:8080/", "142"));
        }};

        updateUrlAndVerifyConnectorName(valid, true, true);
    }

    @Test
    //CSA-1789
    public void testValidLongUrls(){
        final List<WebConnector> valid = new ArrayList<WebConnector>(){{
            add(new WebConnector("http://www.very.very.very.very.very.very.very.very.long.domain.com", "very"));
            add(new WebConnector("http://www.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.very.long.domain.com", "very"));
        }};

        longUrls(valid, true, true);
    }

    private void longUrls(final List<WebConnector> connectors, final boolean urlValid, final boolean nameValid) {
        Runnable runnable = new Thread() {
            @Override
            public void run() {
                updateUrlAndVerifyConnectorName(connectors, urlValid, nameValid);
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(runnable);
        executorService.shutdown();

        try {
            future.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            fail("Method took too long");
        }

        if(!executorService.isTerminated()){
            executorService.shutdownNow();
        }
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

    private void updateUrlAndVerifyConnectorName(List<WebConnector> connectors, boolean isUrlValid, boolean isNameValid) {
        /* For debugging -
            System.out.println("************************************************* \n"+url+"************************************************* \n");
        */

        for(WebConnector connector : connectors) {
            connectorName.clear();
            connectorUrl.setValue(connector.getUrl());
            connectorName.getElement().click();

            verifyURL(connector.getUrl(), isUrlValid);
            verifyName(connector.getName().toLowerCase(), isNameValid);
            verifyThat(newConnectionPage.nextButton(), not(disabled()));
        }
    }

    private void checkFormInputError(FormInput form, int minimum) {
        String visibleError = form.getElement().findElement(By.xpath(".//../../p[not(contains(@class,'ng-hide'))]")).getText();

        assertThat(visibleError, is("Invalid Value (should be " + minimum + " at minimum)"));
    }
}

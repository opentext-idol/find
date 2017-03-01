package com.autonomy.abc.find;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.settings.SettingsPage;
import com.autonomy.abc.selenium.settings.SettingsPanel;
import com.autonomy.abc.shared.SettingsPageTests;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.EnumSet;
import java.util.List;

//TODO restructure directories as part of major refactor as this belongs in neither directory
public class OnPremSettingsPageITCase extends IdolFindTestBase{
    private SettingsPage page;
    private SettingsPageTests testHelper;

    private static final EnumSet<SettingsPage.Panel> SERVER_PANELS = EnumSet.of(
            SettingsPage.Panel.ANSWERSERVER,
            SettingsPage.Panel.COMMUNITY,
            SettingsPage.Panel.CONTENT,
            SettingsPage.Panel.QMS_FIND,
            SettingsPage.Panel.VIEW,
            SettingsPage.Panel.MAPPING,
            SettingsPage.Panel.MMAP,
            SettingsPage.Panel.SAVED_SEARCH,
            SettingsPage.Panel.STATS_FIND
    );

    public OnPremSettingsPageITCase(final TestConfig config) { super(config); }

    @Before
    public void setUp() {
        getElementFactory().getTopNavBar().goToSettings();

        page = getElementFactory().getSettingsPage();
        page.waitForLoad();

        final List<SettingsPanel> initialState = page.getCurrentState(SERVER_PANELS);
        testHelper = new SettingsPageTests(getDriver(), page, SERVER_PANELS, initialState);
    }

    @After
    public void tearDown() {
        testHelper.resetToInitialState();
    }

    @Test
    public void testSaveChangesModal() {
        testHelper.testSaveChangesModal();
    }

    @Test
    public void testRevertChangesModal() {
        testHelper.testRevertChangesModal();
    }

    @Test
    public void testAllSettingsPanelsPresent() {
        testHelper.testAllSettingsPanelsPresent();
    }

    @Test
    public void testRevertChangesPort() {
        testHelper.testRevertChangesPort();
    }

    @Test
    public void  testRevertChangesHostname() {
        testHelper.testRevertChangesHostname();
    }

    @Test
    public void testRevertChangesProtocol() {
        testHelper.testRevertChangesProtocol();
    }

    @Test
    @Ignore
    public void testRevertToNewlySaved() {
        testHelper.testRevertToNewlySaved(getApplication().getName());
    }

    @Test
    public void testEnterBadHostAndPortNames() {
        testHelper.testEnterBadHostAndPortNames();
    }

    @Test
    public void testBlankPortsAndHosts() {
        testHelper.testBlankPortsAndHosts();
    }

    //TODO: test for FIND-759
    //TODO: check if the changed to config take effect
    //E.g. Disabling MMAP and also disable Map
}

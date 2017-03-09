/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.dashboards;

import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;

public class SunburstWidgetITCase extends ClickableDashboardITCase {
    public SunburstWidgetITCase(final TestConfig config) {
        super(config, 1, "Sunburst Dashboard", "Sunburst", "SunburstSearch");
    }

    @Override
    @Before
    public void setUp() {
        page.waitForSunburstWidgetToRender();
    }

    //TODO once config is set up, test:
    //      * sunburst renders segments
    //      * sunburst resizes
    //      * legend is populated (colours, order by counts)
    //      * segment colours and ordering are correct (give segments data-id for testing?)
    //      * the 'too many entries' legend item is displayed when necessary
    //      * legend changes position with resizing
}

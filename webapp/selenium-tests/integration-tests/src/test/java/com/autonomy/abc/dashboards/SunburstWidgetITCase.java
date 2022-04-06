/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
        super.setUp();
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

package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.bi.MapView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;

public class MapITCase extends IdolFindTestBase {
    private FindService findService;
    private IdolFindPage findPage;
    private MapView mapView;

    public MapITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        findPage = getElementFactory().getFindPage();
        mapView = getElementFactory().getMap();
        findService = getApplication().findService();
    }

}

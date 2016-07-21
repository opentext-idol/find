package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import org.junit.Before;
import org.junit.Test;

public class NumericWidgetITCase extends IdolFindTestBase{
    private FindService findService;
    private IdolFindPage findPage;

    public NumericWidgetITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }


    @Test
    public void clickAndDrag(){
        findService.search("face");

        filters().waitForParametricFields();



        MainNumericWidget mainGraph = findPage.mainGraph();

        //+x -> goes right on the x axis
        mainGraph.clickAndDrag(100,0,mainGraph.graph());



    }

    //SHOULD THIS BE IN HERE OR SHOULD IT BE IN IDOLFINDPAGE?!
    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }





}

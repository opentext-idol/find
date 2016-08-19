package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.bi.MapView;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.bi.TableView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.comparison.ResultsComparisonView;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.login.IdolFindLoginPage;
import com.autonomy.abc.selenium.find.save.SearchOptionsBar;
import com.autonomy.abc.selenium.find.save.SearchTabBar;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public class IdolFindElementFactory extends FindElementFactory {
    IdolFindElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public LoginPage getLoginPage() {
        return new IdolFindLoginPage(getDriver());
    }

    @Override
    public IdolFindPage getFindPage() {
        return new IdolFindPage.Factory().create(getDriver());
    }

    @Override
    public IdolFilterPanel getFilterPanel() {
        return new IdolFilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    public SearchTabBar getSearchTabBar() {
        return new SearchTabBar(getDriver());
    }

    public SearchOptionsBar getSearchOptionsBar() {
        return new SearchOptionsBar(getDriver());
    }

    public SunburstView getSunburst() {
        return new SunburstView(getDriver());
    }

    public MapView getMap() {
        return new MapView(getDriver());
    }

    public TopicMapView getTopicMap() {
        return new TopicMapView(getDriver());
    }

    public TableView getTableView() {
        return new TableView(getDriver());
    }

    public ResultsComparisonView getResultsComparison() {
        return new ResultsComparisonView(getDriver());
    }
}

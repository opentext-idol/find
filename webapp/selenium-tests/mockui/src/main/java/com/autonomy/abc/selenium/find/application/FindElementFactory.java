/*
 * (c) Copyright 2015-2016 Micro Focus or one of its affiliates.
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
package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.NavBarSettings;
import com.autonomy.abc.selenium.find.concepts.ConceptsPanel;
import com.autonomy.abc.selenium.find.filters.AppliedFiltersPanel;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.results.RelatedConceptsPanel;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("WeakerAccess")
public abstract class FindElementFactory extends ElementFactoryBase {
    protected FindElementFactory(final WebDriver driver) {
        super(driver, null);
    }

    @Override
    public LoginService.LogoutHandler getLogoutHandler() {
        return getTopNavBar();
    }

    public FindPage getFindPage() {
        return new FindPage.Factory().create(getDriver());
    }

    public NavBarSettings getTopNavBar() {
        return new NavBarSettings(getDriver());
    }

    public ListView getListView() {
        return new ListView(getDriver());
    }

    public RelatedConceptsPanel getRelatedConceptsPanel() {
        return new RelatedConceptsPanel(getDriver());
    }

    public FilterPanel getFilterPanel() {
        return new FilterPanel(new IndexesTree.Factory(), getDriver());
    }

    public ConceptsPanel getConceptsPanel() {
        return new ConceptsPanel(getDriver());
    }

    public SimilarDocumentsView getSimilarDocumentsView() {
        return new SimilarDocumentsView.Factory().create(getDriver());
    }

    public DetailedPreviewPage getDetailedPreview() {return new DetailedPreviewPage.Factory().create(getDriver());}

    public AppliedFiltersPanel getAppliedFiltersPanel() {
        return new AppliedFiltersPanel(getDriver());
    }

    @Override
    public <T extends AppPage> T loadPage(final Class<T> type) {
        throw new UnsupportedOperationException("loadPage does not make sense for a single page application");
    }

    public abstract FormInput getSearchBox();
}

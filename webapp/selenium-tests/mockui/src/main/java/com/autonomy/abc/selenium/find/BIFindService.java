package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.FindApplication;
import com.hp.autonomy.frontend.selenium.util.Waits;

public class BIFindService extends FindService{

    private final BIIdolFindElementFactory elementFactory;

    public BIFindService(final FindApplication<BIIdolFindElementFactory> find) {
        super(find);
        elementFactory = find.elementFactory();
    }

    @Override
    protected void submitSearch(final String term) {
        Waits.loadOrFadeWait();
        elementFactory.getSearchBox().setValue(term);
        elementFactory.getSearchBox().submit();
        if(elementFactory.getConceptsPanel().selectedConcepts().isEmpty()) {
            elementFactory.getSearchBox().setValue(term);
            elementFactory.getSearchBox().submit();
        }
    }

}

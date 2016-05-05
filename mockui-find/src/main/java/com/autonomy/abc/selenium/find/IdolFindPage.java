package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class IdolFindPage extends FindPage {
    public IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public IndexesTree indexesTree() {
        return new IdolDatabaseTree(super.indexesTree());
    }
}

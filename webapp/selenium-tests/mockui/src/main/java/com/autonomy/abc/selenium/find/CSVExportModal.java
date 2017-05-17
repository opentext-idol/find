package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.predicates.HasCssValuePredicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class CSVExportModal extends ModalView {

    private CSVExportModal(final WebElement $el, final WebDriver driver) {
        super($el, driver);
    }

    public List<WebElement> fieldsToExport() {
        return findElements(By.cssSelector(".modal-body [data-id]"));
    }

    public static CSVExportModal make(final WebDriver driver) {
        final CSVExportModal modal = new CSVExportModal(driver.findElement(By.className("modal")), driver);
        new WebDriverWait(driver, 10).until(new HasCssValuePredicate(modal, "opacity", "1"));
        return modal;
    }
}

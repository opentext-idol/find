package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserTable extends AppElement implements Iterable<UserTableRow> {
    public UserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    public Iterator<UserTableRow> iterator() {
        return rows().iterator();
    }

    public List<UserTableRow> rows() {
        List<UserTableRow> rows = new ArrayList<>();
        for (WebElement rowEl : findElements(By.cssSelector("tbody tr"))) {
            rows.add(new UserTableRow(rowEl, getDriver()));
        }
        return rows;
    }

}

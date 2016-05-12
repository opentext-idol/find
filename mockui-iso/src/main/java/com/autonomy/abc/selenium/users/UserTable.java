package com.autonomy.abc.selenium.users;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class UserTable extends AppElement implements Iterable<UserTable.Row> {
    UserTable(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    @Override
    public Iterator<Row> iterator() {
        return rows().iterator();
    }

    public List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        for (WebElement rowEl : findElements(By.cssSelector("tbody tr"))) {
            rows.add(new Row(rowEl, getDriver()));
        }
        return rows;
    }

    static class Row extends AppElement {
        public Row(WebElement element, WebDriver driver) {
            super(element, driver);
        }

        public String getUsername() {
            return findElement(By.className("user-username")).getText().trim();
        }

        public WebElement deleteButton() {
            return findElement(By.className("users-deleteUser"));
        }
    }
}

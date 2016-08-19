package com.autonomy.abc.selenium.users.table;

import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class UserTable<T extends UserTableRow> extends AppElement implements Iterable<T> {
    public UserTable(final WebElement element, final WebDriver driver) {
        super(element, driver);
    }

    @Override
    public Iterator<T> iterator() {
        return rows().iterator();
    }

    public List<String> getUsernames() {
        final List<String> usernames = new ArrayList<>();
        for (final UserTableRow row : this) {
            usernames.add(row.getUsername());
        }
        return usernames;
    }

    public List<T> rows() {
        final List<T> rows = new ArrayList<>();
        for (final WebElement rowEl : findElements(By.cssSelector("tbody tr"))) {
            rows.add(rowForElement(rowEl));
        }
        return rows;
    }

    public T row(final int index) {
        return rows().get(index);
    }

    public abstract T rowFor(User user);

    protected abstract T rowForElement(WebElement element);
}

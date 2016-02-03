package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class PageMapper<T extends Enum<T> & PageMapper.Page> {
    private final Map<Class<?>, T> typeMap = new HashMap<>();

    public PageMapper(Class<T> enumerator) {
        for (T page : EnumSet.allOf(enumerator)) {
            Class<?> type = page.getPageType();
            while (type != Object.class) {
                typeMap.put(type, page);
                type = type.getSuperclass();
            }
        }
    }

    public NavBarTabId getId(Class<?> type) {
        return typeMap.get(type).getId();
    }

    public <S extends AppPage> S load(Class<S> type, WebDriver driver) {
        return typeMap.get(type).safeLoad(type, driver);
    }

    interface Page {
        Class<?> getPageType();

        NavBarTabId getId();

        <T extends AppPage> T safeLoad(Class<T> type, WebDriver driver);
    }
}

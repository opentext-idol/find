package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

class PageMapper<T extends Enum<T> & PageMapper.Page> {
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

    T get(Class<?> type) {
        return typeMap.get(type);
    }

    public NavBarTabId getId(Class<?> type) {
        return typeMap.get(type).getId();
    }

    public <S extends AppPage> S load(Class<S> type, WebDriver driver) {
        if (type.isAssignableFrom(typeMap.get(type).getPageType())) {
            @SuppressWarnings("unchecked")
            S value = (S) typeMap.get(type).loadAsObject(driver);
            return value;
        }
        return null;
    }

    interface Page {
        Class<?> getPageType();
        NavBarTabId getId();
        Object loadAsObject(WebDriver driver);
    }

    interface SwitchStrategy<T> {
        void switchUsing(T context);
    }
}

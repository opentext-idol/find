package com.autonomy.abc.selenium.application;

import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

// "typesafe heterogenous container" (Effective Java #29)
public class PageMapper<T extends Enum<T> & PageMapper.Page> {
    private final Map<Class<?>, T> typeMap = new HashMap<>();

    // can access the enum value representing a page from
    // any of its superclasses, so that can e.g. access
    // PromotionsPage via either PromotionsPage or
    // HSOPromotionsPage depending on what is needed
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

    public <S extends AppPage> S load(Class<S> type, WebDriver driver) {
        if (type.isAssignableFrom(typeMap.get(type).getPageType())) {
            @SuppressWarnings("unchecked")
            S value = (S) typeMap.get(type).loadAsObject(driver);
            return value;
        }
        return null;
    }

    public interface Page {
        Class<?> getPageType();
        Object loadAsObject(WebDriver driver);
    }

    public interface SwitchStrategy<T> {
        void switchUsing(T context);
    }
}

package com.hp.autonomy.frontend.selenium.element;

public interface Removable {
    boolean isRemovable();

    void removeAsync();

    void removeAndWait();

    void removeAndWait(int timeout);

    void click();

    String getText();
}

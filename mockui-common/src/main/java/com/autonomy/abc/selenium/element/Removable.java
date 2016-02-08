package com.autonomy.abc.selenium.element;

public interface Removable {
    boolean isRemovable();

    boolean isRefreshing();

    void removeAsync();

    void removeAndWait();

    void removeAndWait(int timeout);

    void click();

    String getText();
}

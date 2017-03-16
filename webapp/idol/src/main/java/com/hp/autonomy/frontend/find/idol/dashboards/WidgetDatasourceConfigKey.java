package com.hp.autonomy.frontend.find.idol.dashboards;

public enum WidgetDatasourceConfigKey {
    TYPE("type"),
    ID("id");

    private final String id;

    WidgetDatasourceConfigKey(final String id) {
        this.id = id;
    }

    public String getValue() {
        return id;
    }
}

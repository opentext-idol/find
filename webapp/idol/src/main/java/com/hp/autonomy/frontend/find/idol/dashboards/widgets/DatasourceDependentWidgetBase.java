package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.configuration.ConfigException;
import com.hp.autonomy.frontend.find.idol.dashboards.WidgetNameSetting;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class DatasourceDependentWidgetBase<W extends DatasourceDependentWidgetBase<W, WS>, WS extends WidgetSettings<WS>> extends Widget<W, WS> implements DatasourceDependentWidget {
    protected final WidgetDatasource<?> datasource;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    DatasourceDependentWidgetBase(final String name, final String type, final Integer x, final Integer y, final Integer width, final Integer height, final WidgetNameSetting displayWidgetName, final WidgetDatasource<?> datasource, final WS widgetSettings, final String cssClass) {
        super(name, type, x, y, width, height, displayWidgetName, widgetSettings, cssClass);
        this.datasource = datasource;
    }

    @Override
    public void basicValidate(final String section) throws ConfigException {
        if (datasource == null) {
            throw new ConfigException(type, "Datasource must be specified for widget with name " + name + " and type " + type);
        }

        super.basicValidate(section);
    }
}

package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", defaultImpl = SimpleWidget.class, visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(CurrentTimeWidget.class),
        @JsonSubTypes.Type(MapWidget.class),
        @JsonSubTypes.Type(ResultsListWidget.class),
        @JsonSubTypes.Type(StaticContentWidget.class),
        @JsonSubTypes.Type(StaticImageWidget.class),
        @JsonSubTypes.Type(SunburstWidget.class),
        @JsonSubTypes.Type(TimeLastRefreshedWidget.class),
        @JsonSubTypes.Type(TopicMapWidget.class),
        @JsonSubTypes.Type(TrendingWidget.class),
        @JsonSubTypes.Type(VideoWidget.class)
})
public interface WidgetMixins {
}

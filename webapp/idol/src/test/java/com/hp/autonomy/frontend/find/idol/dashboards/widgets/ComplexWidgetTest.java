package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@AutoConfigureJson
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public abstract class ComplexWidgetTest<W extends Widget<W, WS> & DatasourceDependentWidget, WS extends WidgetSettings<WS>> extends DatasourceDependentWidgetTest<W, WS> {
    @Autowired
    TagNameFactory tagNameFactory;

    @Autowired
    private JsonMapper objectMapper;

    @Override
    public void setUp() {
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(TagName.class, new TagNameSerializer());
        objectMapper = objectMapper.rebuild()
                .addModule(simpleModule)
                .addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class)
                .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }
}

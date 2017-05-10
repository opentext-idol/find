package com.hp.autonomy.frontend.find.idol.dashboards.widgets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasource;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.WidgetDatasourceMixins;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.ResolvableType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringRunner.class)
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public abstract class ComplexWidgetTest<W extends Widget<W, WS> & DatasourceDependentWidget, WS extends WidgetSettings<WS>> extends DatasourceDependentWidgetTest<W, WS> {
    @Autowired
    TagNameFactory tagNameFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void setUp() {
        final SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(TagName.class, new TagNameSerializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.addMixIn(WidgetDatasource.class, WidgetDatasourceMixins.class);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        json = new JacksonTester<>(getClass(), ResolvableType.forClass(getType()), objectMapper);
    }
}

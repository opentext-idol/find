package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.frontend.find.core.test.MockConfig;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.test.CoreTestContext;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.searchcomponents.core.test.CoreTestContext.CORE_CLASSES_PROPERTY;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FieldComparatorFactoryImpl.class, CoreTestContext.class}, properties = CORE_CLASSES_PROPERTY, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FieldComparatorFactoryTest {
    @MockBean
    protected ConfigFileService<MockConfig> configService;
    @Autowired
    private TagNameFactory tagNameFactory;
    @Mock
    private MockConfig config;

    @Autowired
    private FieldComparatorFactory fieldComparatorFactory;

    @Before
    public void setUp() {
        when(configService.getConfig()).thenReturn(config);
    }

    @Test
    public void getParametricFieldsWithDefaultSorting() {
        final List<FieldAndValueDetails> fields = mockFields();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsWithExplicitOrder() {
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField2"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField1"))
                .build());
        final List<FieldAndValueDetails> fields = mockFields();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsWithSomeExplicitOrdering() {
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .build());
        final List<FieldAndValueDetails> fields = mockFields();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsAndValuesWithDefaultSorting() {
        final List<QueryTagInfo> fields = mockFieldsAndValues();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsAndValuesWithExplicitOrder() {
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField2"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField1"))
                .build());
        final List<QueryTagInfo> fields = mockFieldsAndValues();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsAndValuesWithSomeExplicitOrdering() {
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .build());
        final List<QueryTagInfo> fields = mockFieldsAndValues();

        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
    }

    private List<FieldAndValueDetails> mockFields() {
        return Stream.of("ParametricField1", "ParametricField2", "ParametricField3")
                .map(tagNameFactory::buildTagName)
                .map(tagName -> FieldAndValueDetails.builder()
                        .id(tagName.getId().getNormalisedPath())
                        .displayName(tagName.getDisplayName())
                        .build())
                .sorted(fieldComparatorFactory.parametricFieldComparator())
                .collect(Collectors.toList());
    }

    private List<QueryTagInfo> mockFieldsAndValues() {
        return Stream.of("ParametricField1", "ParametricField2", "ParametricField3")
                .map(tagNameFactory::buildTagName)
                .map(tagName -> QueryTagInfo.builder()
                        .id(tagName.getId().getNormalisedPath())
                        .displayName(tagName.getDisplayName())
                        .build())
                .sorted(fieldComparatorFactory.parametricFieldAndValuesComparator())
                .collect(Collectors.toList());
    }
}

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.searchcomponents.core.config.FieldInfo;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.core.config.FieldsInfo;
import com.hp.autonomy.searchcomponents.core.fields.FieldsMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindFieldsMapperTest {

    @Mock
    private ConfigService<FindConfig> configService;

    private FieldsMapper fieldsMapper;


    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        final Map<String, String> miceMap = new HashMap<>();
        miceMap.put("TOM_AND_JERRY_JERRY", "Jerry");
        miceMap.put("LOONEY_TOONS_SPEEDY_GONZALES", "Speedy Gonzales");
        miceMap.put("DANGER_MOUSE_DANGER_MOUSE", "Danger Mouse");
        miceMap.put("MIGHTY_MOUSE_MIGHTY_MOUSE", "Mighty Mouse");
        final FieldInfo<String> mice = new FieldInfo<>("mice", "Mice", miceMap, Collections.singleton("TEST/MICE"), FieldType.STRING, true, Collections.EMPTY_LIST);

        final Map<String, String> catMap = new HashMap<>();
        catMap.put("TOM_AND_JERRY_TOM", "Cartoon Cat");
        catMap.put("GARFIELD_GARFIELD", "Cartoon Cat");
        catMap.put("LOONEY_TOONS_SYLVESTER", "Cartoon Cat");
        catMap.put("FAIRY_TAIL_HAPPY", "Cartoon Cat");
        final FieldInfo<String> cats = new FieldInfo<>("cats", "Cats", catMap, Collections.singleton("TEST/CATS"), FieldType.STRING, true, Collections.EMPTY_LIST);

        final FieldsInfo.Builder builder = new FieldsInfo.Builder();
        builder.populateResponseMap("mice", mice);
        builder.populateResponseMap("cats", cats);

        final FieldsInfo fieldsInfo = builder.build();

        final FindConfig findConfig = mock(FindConfig.class);
        when(configService.getConfig()).thenReturn(findConfig);
        when(findConfig.getFieldsInfo()).thenReturn(fieldsInfo);

        fieldsMapper = new FindFieldsMapper(configService);
    }

    @Test
    public void transformFieldName() throws Exception {
        final String result = fieldsMapper.transformFieldName("TEST/MICE");
        assertThat(result, is("Mice"));

        final String result2 = fieldsMapper.transformFieldName("TEST/CATS");
        assertThat(result2, is("Cats"));
    }

    @Test
    public void transformFieldNameNoMapping() throws Exception {
        final String result = fieldsMapper.transformFieldName("TEST/SHARKS");
        assertThat(result, is("Sharks"));

        final String result2 = fieldsMapper.transformFieldName("TEST/SQUIRRELS");
        assertThat(result2, is("Squirrels"));
    }

    @Test
    public void transformFieldValue() throws Exception {
        final String result = fieldsMapper.transformFieldValue("TEST/MICE", "TOM_AND_JERRY_JERRY");
        assertThat(result, is("Jerry"));

        final String result2 = fieldsMapper.transformFieldValue("TEST/CATS", "TOM_AND_JERRY_TOM");
        assertThat(result2, is("Cartoon Cat"));
    }

    @Test
    public void transformFieldValueNoFieldMapping() throws Exception {
        final String result = fieldsMapper.transformFieldValue("TEST/SHARKS", "SHARKY_AND_GEORGE_SHARKY");
        assertThat(result, is("SHARKY_AND_GEORGE_SHARKY"));

        final String result2 = fieldsMapper.transformFieldValue("TEST/SQUIRRELS", "NEUROTICALLY_YOURS_FOAMY");
        assertThat(result2, is("NEUROTICALLY_YOURS_FOAMY"));
    }

    @Test
    public void transformFieldValueNoValueMapping() throws Exception {
        final String result = fieldsMapper.transformFieldValue("TEST/MICE", "AN_AMERICAN_TALE_FIVAL_MOUSEKEWITZ");
        assertThat(result, is("AN_AMERICAN_TALE_FIVAL_MOUSEKEWITZ"));

        final String result2 = fieldsMapper.transformFieldValue("TEST/CATS", "TOP_CAT_TOP_CAT");
        assertThat(result2, is("TOP_CAT_TOP_CAT"));
    }

    @Test
    public void restoreFieldName() throws Exception {
        final String mappedName = "Mice";

        final String result = fieldsMapper.restoreFieldName(mappedName);

        assertThat(result, is("TEST/MICE"));
    }

    @Test
    public void restoreFieldNameNoMapping() throws Exception {
        final String mappedName = "EARTHWORMS";

        final String result = fieldsMapper.restoreFieldName(mappedName);

        assertThat(result, is("EARTHWORMS"));
    }

    @Test
    public void restoreFieldValue() throws Exception {
        final String fieldName = "TEST/MICE";
        final String mappedValue = "Jerry";

        final Collection<String> result = fieldsMapper.restoreFieldValue(fieldName, mappedValue);

        assertThat(result, contains("TOM_AND_JERRY_JERRY"));
    }

    @Test
    public void restoreFieldValueMultipleMapping() throws Exception {
        final String fieldName = "TEST/CATS";
        final String mappedValue = "Cartoon Cat";

        final Collection<String> result = fieldsMapper.restoreFieldValue(fieldName, mappedValue);

        assertThat(result, containsInAnyOrder("TOM_AND_JERRY_TOM", "GARFIELD_GARFIELD", "LOONEY_TOONS_SYLVESTER", "FAIRY_TAIL_HAPPY"));
    }

    @Test
    public void restoreFieldValueNoFieldMapping() throws Exception {
        final String fieldName = "TEST/SQUIRREL";
        final String mappedValue = "Foamy the Squirrel";

        final Collection<String> result = fieldsMapper.restoreFieldValue(fieldName, mappedValue);

        assertThat(result, contains("Foamy the Squirrel"));
    }

    @Test
    public void restoreFieldValueNoValueMapping() throws Exception {
        final String fieldName = "TEST/MICE";
        final String mappedValue = "AN_AMERICAN_TALE_FIVAL_MOUSEKEWITZ";

        final Collection<String> result = fieldsMapper.restoreFieldValue(fieldName, mappedValue);

        assertThat(result, contains("AN_AMERICAN_TALE_FIVAL_MOUSEKEWITZ"));
    }
}
package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class FieldComparatorFactoryImpl implements FieldComparatorFactory {
    private final TagNameFactory tagNameFactory;
    private final ConfigService<? extends FindConfig<?, ?>> configService;

    @Autowired
    public FieldComparatorFactoryImpl(final TagNameFactory tagNameFactory,
                                      final ConfigService<? extends FindConfig<?, ?>> configService) {
        this.tagNameFactory = tagNameFactory;
        this.configService = configService;
    }

    @Override
    public Comparator<FieldAndValueDetails> parametricFieldComparator() {
        final Map<FieldPath, Integer> orderMap = getOrderMap();
        return Comparator.<FieldAndValueDetails, Integer>comparing(x -> orderMap.getOrDefault(tagNameFactory.getFieldPath(x.getId()), Integer.MAX_VALUE))
                .thenComparing(FieldAndValueDetails::getDisplayName);
    }

    @Override
    public Comparator<QueryTagInfo> parametricFieldAndValuesComparator() {
        final Map<FieldPath, Integer> orderMap = getOrderMap();
        return Comparator.<QueryTagInfo, Integer>comparing(x -> orderMap.getOrDefault(tagNameFactory.getFieldPath(x.getId()), Integer.MAX_VALUE))
                .thenComparing(QueryTagInfo::getDisplayName);
    }

    private Map<FieldPath, Integer> getOrderMap() {
        final UiCustomization maybeUiCustomization = configService.getConfig().getUiCustomization();
        final int[] counter = new int[]{0};
        return Optional.ofNullable(maybeUiCustomization)
                .map(uiCustomization -> uiCustomization.getParametricOrder().stream().collect(Collectors.toMap(x -> x, x -> counter[0]++)))
                .orElse(Collections.emptyMap());
    }
}

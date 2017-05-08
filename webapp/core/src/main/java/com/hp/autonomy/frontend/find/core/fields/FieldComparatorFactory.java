package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;

import java.util.Comparator;

public interface FieldComparatorFactory {
    Comparator<FieldAndValueDetails> parametricFieldComparator();

    Comparator<QueryTagInfo> parametricFieldAndValuesComparator();
}

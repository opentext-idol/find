package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import lombok.Data;
import java.util.Set;

@Data
public class ParametricValues {
    private final Set<QueryTagInfo> parametricValues;
    private final Set<QueryTagInfo> numericParametricValues;
}

package com.hp.autonomy.frontend.find.idol.indexes;

import com.hp.autonomy.frontend.find.core.indexes.AbstractIndexesServiceIT;
import org.springframework.test.context.TestPropertySource;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@TestPropertySource(properties = "hp.find.backend = IDOL")
public class IdolIndexesServiceIT extends AbstractIndexesServiceIT {
}

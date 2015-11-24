package com.hp.autonomy.frontend.find.idol.indexes;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.exceptions.FindException;
import com.hp.autonomy.frontend.find.core.indexes.AbstractIndexesServiceIT;
import com.hp.autonomy.types.idol.Database;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.Assert.assertFalse;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@TestPropertySource(properties = "hp.find.backend = IDOL")
public class IdolIndexesServiceIT extends AbstractIndexesServiceIT {
}

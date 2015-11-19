package com.hp.autonomy.frontend.find.hod.indexes;

import com.hp.autonomy.frontend.find.core.indexes.AbstractIndexesServiceIT;
import com.hp.autonomy.hod.client.api.authentication.EntityType;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.api.resource.Resources;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.types.IdolDatabase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class HodIndexesServiceIT extends AbstractIndexesServiceIT<HodIndexesService, HodErrorException> {
    @Autowired
    private TokenProxy<EntityType.Application, TokenType.Simple> tokenProxy;

    @Override
    public void noExcludedIndexes() throws HodErrorException {
        assertTrue(indexesService.listActiveIndexes().isEmpty());

        final List<? extends IdolDatabase> databases = indexesService.listVisibleIndexes();
        assertFalse(databases.isEmpty());

        final Resources resources = indexesService.listIndexes(tokenProxy);
        assertEquals(1, resources.getResources().size()); //we should only have the default index
        assertFalse(resources.getPublicResources().isEmpty());
        assertEquals(resources.getResources().size() + resources.getPublicResources().size(), databases.size());
    }
}

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatedConceptsServiceImpl implements RelatedConceptsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public List<Entity> findRelatedConcepts(final String text, final String indexes) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        parameters.put("indexes", indexes);
        parameters.put("apikey", apiKeyService.getApiKey());

        return restTemplate.getForObject("https://api.idolondemand.com/1/api/sync/findrelatedconcepts/v1?apikey={apikey}&text={text}&indexes={indexes}", Entities.class, parameters).getEntities();
    }
}

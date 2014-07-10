package com.hp.autonomy.frontend.find.search;

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

    @Override
    public List<Entity> findRelatedConcepts(final String text) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        parameters.put("apikey", "XYZ123ABC");

        return restTemplate.getForObject("https://api.idolondemand.com/1/api/sync/findrelatedconcepts/v1?apikey={apikey}&text={text}", Entities.class, parameters).getEntities();
    }
}

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentsServiceImpl implements DocumentsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public List<Document> queryTextIndex(final String text, final int max_results, final String summary, final String indexes) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("text", text);
        parameters.put("max_results", max_results);
        parameters.put("summary", summary);
        parameters.put("indexes", indexes);
        parameters.put("apikey", apiKeyService.getApiKey());

        return restTemplate.getForObject("https://api.idolondemand.com/1/api/sync/querytextindex/v1?apikey={apikey}&max_results={max_results}&text={text}&summary={summary}", Documents.class, parameters).getDocuments();
    }
}

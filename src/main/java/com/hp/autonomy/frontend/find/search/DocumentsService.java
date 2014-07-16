package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface DocumentsService {

    List<Document> queryTextIndex(String text, int max_results, String summary);

}

package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface IndexesService {

    Indexes listIndexes();

    Indexes listIndexes(String apiKey);

    List<Index> listActiveIndexes();

    List<Index> listVisibleIndexes();
}

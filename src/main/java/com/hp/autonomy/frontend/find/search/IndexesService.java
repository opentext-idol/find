package com.hp.autonomy.frontend.find.search;

import java.util.List;

public interface IndexesService {

    List<Index> listIndexes();

    List<Index> listActiveIndexes();

    List<Index> listIndexes(String apiKey);
}

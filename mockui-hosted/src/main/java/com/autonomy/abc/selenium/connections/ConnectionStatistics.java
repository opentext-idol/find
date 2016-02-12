package com.autonomy.abc.selenium.connections;

import java.util.ArrayList;
import java.util.List;

public class ConnectionStatistics {
    List<Integer> detected = new ArrayList<>();
    List<Integer> ingested = new ArrayList<>();

    ConnectionStatistics(List<Integer> statistics){
        detected.add(statistics.get(0));
        detected.add(statistics.get(1));
        detected.add(statistics.get(2));
        ingested.add(statistics.get(3));
        ingested.add(statistics.get(4));
        ingested.add(statistics.get(5));
    }

    public Integer getDetected(){
        return detected.get(0);
    }

    public Integer getToDelete(){
        return detected.get(1);
    }

    public Integer getErrors(){
        return detected.get(2);
    }

    public Integer getIngested(){
        return ingested.get(0);
    }

    public Integer getDeleted(){
        return ingested.get(1);
    }

    public Integer getFailed(){
        return ingested.get(2);
    }
}

package com.dataanalyzer.service;

import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.DatasetProfile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChartDataService {

    public List<String> getColumnLabels(DatasetProfile profile) {
        List<String> labels = new ArrayList<>();
        for (ColumnProfile column : profile.getColumns()) {
            labels.add(column.getName());
        }
        return labels;
    }

    public List<Double> getMissingPercentages(DatasetProfile profile) {
        List<Double> values = new ArrayList<>();
        for (ColumnProfile column : profile.getColumns()) {
            values.add(column.getMissingPercentage());
        }
        return values;
    }

    public Map<String, Long> getTypeCounts(DatasetProfile profile) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ColumnProfile column : profile.getColumns()) {
            counts.merge(column.getInferredType(), 1L, Long::sum);
        }
        return counts;
    }
}

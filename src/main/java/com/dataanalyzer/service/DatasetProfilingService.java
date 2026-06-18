package com.dataanalyzer.service;

import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.CorrelationPair;
import com.dataanalyzer.model.DatasetProfile;
import com.dataanalyzer.model.ParsedTable;
import com.dataanalyzer.util.MissingValueUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class DatasetProfilingService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    private static final Pattern SSN_PATTERN = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9][0-9()\\-\\s]{6,}[0-9]$");

    public DatasetProfile profile(ParsedTable table) {
        List<String> headers = table.getHeaders();
        Map<String, List<String>> columnData = table.getColumnData();
        long rowCount = table.getRowCount();

        long duplicateRowCount = countDuplicateRows(headers, columnData, rowCount);

        List<ColumnProfile> columnProfiles = new ArrayList<>();
        for (String header : headers) {
            List<String> values = columnData.get(header);
            columnProfiles.add(buildColumnProfile(header, values));
        }

        DatasetProfile datasetProfile = new DatasetProfile();
        datasetProfile.setColumnCount(headers.size());
        datasetProfile.setRowCount(rowCount);
        datasetProfile.setColumns(columnProfiles);
        datasetProfile.setDuplicateRowCount(duplicateRowCount);
        datasetProfile.setCorrelations(computeCorrelations(columnData, columnProfiles));
        return datasetProfile;
    }

    private long countDuplicateRows(List<String> headers, Map<String, List<String>> columnData, long rowCount) {
        if (rowCount == 0) {
            return 0;
        }
        Set<String> seenRows = new HashSet<>();
        long duplicates = 0;

        for (int i = 0; i < rowCount; i++) {
            StringBuilder rowKey = new StringBuilder();
            for (String header : headers) {
                List<String> values = columnData.get(header);
                String value = i < values.size() ? values.get(i) : "";
                rowKey.append(value).append('\u0001');
            }
            if (!seenRows.add(rowKey.toString())) {
                duplicates++;
            }
        }
        return duplicates;
    }

    private ColumnProfile buildColumnProfile(String name, List<String> values) {
        ColumnProfile profile = new ColumnProfile();
        profile.setName(name);
        profile.setTotalCount(values.size());

        long missing = 0;
        Set<String> uniqueValues = new HashSet<>();
        List<Double> numericValues = new ArrayList<>();
        boolean allNumeric = true;

        for (String value : values) {
            if (MissingValueUtils.isMissing(value)) {
                missing++;
                continue;
            }
            uniqueValues.add(value);
            try {
                numericValues.add(Double.parseDouble(value.trim()));
            } catch (NumberFormatException e) {
                allNumeric = false;
            }
        }

        long nonMissing = values.size() - missing;
        profile.setMissingCount(missing);
        profile.setMissingPercentage(values.isEmpty() ? 0.0 : (missing * 100.0) / values.size());
        profile.setUniqueCount(uniqueValues.size());

        if (nonMissing > 0 && allNumeric) {
            profile.setInferredType("NUMERIC");
            double min = Collections.min(numericValues);
            double max = Collections.max(numericValues);
            double mean = numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            profile.setMin(min);
            profile.setMax(max);
            profile.setMean(Math.round(mean * 100.0) / 100.0);
            profile.setOutlierCount(countOutliers(numericValues));
        } else if (nonMissing == 0) {
            profile.setInferredType("EMPTY");
        } else {
            profile.setInferredType("TEXT");
            profile.setPiiType(detectPiiType(values));
            applyFormatConsistencyCheck(profile, values);
        }

        List<String> samples = new ArrayList<>();
        for (String value : values) {
            if (samples.size() >= 5) {
                break;
            }
            if (!MissingValueUtils.isMissing(value)) {
                samples.add(value);
            }
        }
        profile.setSampleValues(samples);

        return profile;
    }

    private long countOutliers(List<Double> numericValues) {
        if (numericValues.size() < 4) {
            return 0;
        }
        List<Double> sorted = new ArrayList<>(numericValues);
        Collections.sort(sorted);

        double q1 = percentile(sorted, 25);
        double q3 = percentile(sorted, 75);
        double iqr = q3 - q1;
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        long count = 0;
        for (double value : numericValues) {
            if (value < lowerBound || value > upperBound) {
                count++;
            }
        }
        return count;
    }

    private double percentile(List<Double> sortedValues, double percentileRank) {
        int n = sortedValues.size();
        int index = (int) Math.ceil((percentileRank / 100.0) * n) - 1;
        index = Math.max(0, Math.min(index, n - 1));
        return sortedValues.get(index);
    }

    private String detectPiiType(List<String> values) {
        long nonMissing = 0;
        long emailMatches = 0;
        long ssnMatches = 0;
        long phoneMatches = 0;

        for (String value : values) {
            if (MissingValueUtils.isMissing(value)) {
                continue;
            }
            nonMissing++;
            String trimmed = value.trim();
            if (EMAIL_PATTERN.matcher(trimmed).matches()) {
                emailMatches++;
            }
            if (SSN_PATTERN.matcher(trimmed).matches()) {
                ssnMatches++;
            }
            if (PHONE_PATTERN.matcher(trimmed).matches()) {
                phoneMatches++;
            }
        }

        if (nonMissing == 0) {
            return null;
        }

        double threshold = nonMissing * 0.5;
        if (emailMatches >= threshold) {
            return "EMAIL";
        }
        if (ssnMatches >= threshold) {
            return "SSN_LIKE";
        }
        if (phoneMatches >= threshold) {
            return "PHONE";
        }
        return null;
    }

    private void applyFormatConsistencyCheck(ColumnProfile profile, List<String> values) {
        Map<String, Set<String>> grouped = new HashMap<>();
        for (String value : values) {
            if (MissingValueUtils.isMissing(value)) {
                continue;
            }
            String key = value.trim().toLowerCase();
            grouped.computeIfAbsent(key, k -> new HashSet<>()).add(value.trim());
        }

        long inconsistentGroups = 0;
        for (Set<String> variants : grouped.values()) {
            if (variants.size() > 1) {
                inconsistentGroups++;
            }
        }

        profile.setFormatIssueCount(inconsistentGroups);
        profile.setInconsistentFormatting(inconsistentGroups > 0);
    }

    private List<CorrelationPair> computeCorrelations(Map<String, List<String>> columnData, List<ColumnProfile> columnProfiles) {
        List<String> numericColumns = new ArrayList<>();
        for (ColumnProfile profile : columnProfiles) {
            if ("NUMERIC".equals(profile.getInferredType())) {
                numericColumns.add(profile.getName());
            }
        }

        List<CorrelationPair> correlations = new ArrayList<>();

        for (int i = 0; i < numericColumns.size(); i++) {
            for (int j = i + 1; j < numericColumns.size(); j++) {
                String colA = numericColumns.get(i);
                String colB = numericColumns.get(j);

                Double correlation = pearsonCorrelation(columnData.get(colA), columnData.get(colB));
                if (correlation != null) {
                    CorrelationPair pair = new CorrelationPair();
                    pair.setColumnA(colA);
                    pair.setColumnB(colB);
                    pair.setCorrelation(Math.round(correlation * 1000.0) / 1000.0);
                    correlations.add(pair);
                }
            }
        }

        return correlations;
    }

    private Double pearsonCorrelation(List<String> valuesA, List<String> valuesB) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        int size = Math.min(valuesA.size(), valuesB.size());
        for (int i = 0; i < size; i++) {
            Double x = tryParse(valuesA.get(i));
            Double y = tryParse(valuesB.get(i));
            if (x != null && y != null) {
                xList.add(x);
                yList.add(y);
            }
        }

        int n = xList.size();
        if (n < 2) {
            return null;
        }

        double meanX = xList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double meanY = yList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double numerator = 0.0;
        double sumSqX = 0.0;
        double sumSqY = 0.0;

        for (int i = 0; i < n; i++) {
            double dx = xList.get(i) - meanX;
            double dy = yList.get(i) - meanY;
            numerator += dx * dy;
            sumSqX += dx * dx;
            sumSqY += dy * dy;
        }

        double denominator = Math.sqrt(sumSqX * sumSqY);
        if (denominator == 0.0) {
            return null;
        }

        return numerator / denominator;
    }

    private Double tryParse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

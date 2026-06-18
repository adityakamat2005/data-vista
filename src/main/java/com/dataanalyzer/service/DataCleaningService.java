package com.dataanalyzer.service;

import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.ParsedTable;
import com.dataanalyzer.util.MissingValueUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCleaningService {

    public byte[] generateCleanedCsv(ParsedTable table, List<ColumnProfile> columnProfiles) throws IOException {
        List<String> headers = table.getHeaders();
        Map<String, List<String>> columnData = table.getColumnData();
        long rowCount = table.getRowCount();

        Map<String, ColumnProfile> profileByName = new HashMap<>();
        for (ColumnProfile profile : columnProfiles) {
            profileByName.put(profile.getName(), profile);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[0])).build())) {

            for (int i = 0; i < rowCount; i++) {
                Object[] row = new Object[headers.size()];
                for (int colIndex = 0; colIndex < headers.size(); colIndex++) {
                    String header = headers.get(colIndex);
                    List<String> values = columnData.get(header);
                    String rawValue = i < values.size() ? values.get(i) : "";

                    if (MissingValueUtils.isMissing(rawValue)) {
                        row[colIndex] = fillValueFor(profileByName.get(header));
                    } else {
                        row[colIndex] = rawValue;
                    }
                }
                printer.printRecord(row);
            }
        }

        return outputStream.toByteArray();
    }

    private String fillValueFor(ColumnProfile profile) {
        if (profile == null) {
            return "";
        }
        if ("NUMERIC".equals(profile.getInferredType()) && profile.getMean() != null) {
            return String.valueOf(profile.getMean());
        }
        return "Unknown";
    }
}

package com.dataanalyzer.service;

import com.dataanalyzer.model.ParsedTable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FileParsingService {

    private final ObjectMapper objectMapper;

    public FileParsingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedTable parse(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String lowerName = fileName == null ? "" : fileName.toLowerCase();

        if (lowerName.endsWith(".json")) {
            return parseJson(file);
        }
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return parseExcel(file);
        }
        return parseCsv(file);
    }

    private ParsedTable parseCsv(MultipartFile file) throws IOException {
        List<String> headers = new ArrayList<>();
        Map<String, List<String>> columnData = new LinkedHashMap<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (InputStream rawInput = new BufferedInputStream(file.getInputStream())) {
            rawInput.mark(3);
            byte[] possibleBom = new byte[3];
            int bytesRead = rawInput.read(possibleBom);
            boolean hasBom = bytesRead == 3
                    && possibleBom[0] == (byte) 0xEF
                    && possibleBom[1] == (byte) 0xBB
                    && possibleBom[2] == (byte) 0xBF;
            if (!hasBom) {
                rawInput.reset();
            }

            try (InputStreamReader reader = new InputStreamReader(rawInput, StandardCharsets.UTF_8);
                 CSVParser parser = format.parse(reader)) {

                headers.addAll(parser.getHeaderNames());
                for (String header : headers) {
                    columnData.put(header, new ArrayList<>());
                }

                for (CSVRecord record : parser) {
                    for (String header : headers) {
                        String value = record.isSet(header) ? record.get(header) : "";
                        columnData.get(header).add(value);
                    }
                }
            }
        }

        return buildTable(headers, columnData);
    }

    private ParsedTable parseJson(MultipartFile file) throws IOException {
        JsonNode root = objectMapper.readTree(file.getInputStream());

        if (!root.isArray()) {
            throw new IOException("JSON file must contain an array of objects, e.g. [{...}, {...}]");
        }

        Set<String> headerSet = new LinkedHashSet<>();
        for (JsonNode record : root) {
            Iterator<String> fieldNames = record.fieldNames();
            while (fieldNames.hasNext()) {
                headerSet.add(fieldNames.next());
            }
        }

        List<String> headers = new ArrayList<>(headerSet);
        Map<String, List<String>> columnData = new LinkedHashMap<>();
        for (String header : headers) {
            columnData.put(header, new ArrayList<>());
        }

        for (JsonNode record : root) {
            for (String header : headers) {
                JsonNode value = record.get(header);
                String stringValue;
                if (value == null || value.isNull()) {
                    stringValue = "";
                } else if (value.isTextual()) {
                    stringValue = value.asText();
                } else {
                    stringValue = value.toString();
                }
                columnData.get(header).add(stringValue);
            }
        }

        return buildTable(headers, columnData);
    }

    private ParsedTable parseExcel(MultipartFile file) throws IOException {
        try {
            List<String> headers = new ArrayList<>();
            Map<String, List<String>> columnData = new LinkedHashMap<>();

            try (InputStream inputStream = file.getInputStream();
                 Workbook workbook = WorkbookFactory.create(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);

                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new IOException("The first sheet has no header row.");
                }

                int columnCount = headerRow.getLastCellNum();
                if (columnCount < 0) {
                    columnCount = 0;
                }

                for (int i = 0; i < columnCount; i++) {
                    Cell cell = headerRow.getCell(i);
                    String headerName = cell != null ? extractCellValue(cell) : "";
                    if (headerName == null || headerName.trim().isEmpty()) {
                        headerName = "column" + (i + 1);
                    }
                    headers.add(headerName);
                    columnData.put(headerName, new ArrayList<>());
                }

                int lastRowNum = sheet.getLastRowNum();
                for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                        String header = headers.get(colIndex);
                        String value = "";
                        if (row != null) {
                            Cell cell = row.getCell(colIndex);
                            if (cell != null) {
                                value = extractCellValue(cell);
                            }
                        }
                        columnData.get(header).add(value);
                    }
                }
            }

            return buildTable(headers, columnData);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Could not read Excel file: " + e.getMessage(), e);
        }
    }

    private String extractCellValue(Cell cell) {
        CellType type = cell.getCellType();

        switch (type) {
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case NUMERIC:
                return formatNumericCell(cell);
            case FORMULA:
                return formatFormulaCell(cell);
            case BLANK:
            case ERROR:
            default:
                return "";
        }
    }

    private String formatNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(cell.getDateCellValue());
        }
        double numericValue = cell.getNumericCellValue();
        if (!Double.isInfinite(numericValue) && numericValue == Math.floor(numericValue)) {
            return String.valueOf((long) numericValue);
        }
        return String.valueOf(numericValue);
    }

    private String formatFormulaCell(Cell cell) {
        try {
            return formatNumericCell(cell);
        } catch (Exception e) {
            try {
                return cell.getStringCellValue().trim();
            } catch (Exception ex) {
                return "";
            }
        }
    }

    private ParsedTable buildTable(List<String> headers, Map<String, List<String>> columnData) {
        long rowCount = 0;
        for (String header : headers) {
            rowCount = Math.max(rowCount, columnData.get(header).size());
        }

        ParsedTable table = new ParsedTable();
        table.setHeaders(headers);
        table.setColumnData(columnData);
        table.setRowCount(rowCount);
        return table;
    }
}

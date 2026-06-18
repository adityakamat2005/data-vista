package com.dataanalyzer.model;

import java.util.List;
import java.util.Map;

public class ParsedTable {

    private List<String> headers;
    private Map<String, List<String>> columnData;
    private long rowCount;

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public Map<String, List<String>> getColumnData() {
        return columnData;
    }

    public void setColumnData(Map<String, List<String>> columnData) {
        this.columnData = columnData;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }
}

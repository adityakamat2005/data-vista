package com.dataanalyzer.model;

public class ColumnComparisonRow {

    private String name;
    private Double missingA;
    private Double missingB;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMissingA() {
        return missingA;
    }

    public void setMissingA(Double missingA) {
        this.missingA = missingA;
    }

    public Double getMissingB() {
        return missingB;
    }

    public void setMissingB(Double missingB) {
        this.missingB = missingB;
    }
}

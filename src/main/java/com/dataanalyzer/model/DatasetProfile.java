package com.dataanalyzer.model;

import java.util.List;

public class DatasetProfile {

    private Long id;
    private String fileName;
    private long rowCount;
    private int columnCount;
    private List<ColumnProfile> columns;
    private String aiInsight;

    private long duplicateRowCount;
    private double completenessScore;
    private double duplicateScore;
    private double structureScore;
    private double healthScore;
    private String healthLabel;

    private List<CorrelationPair> correlations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public List<ColumnProfile> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnProfile> columns) {
        this.columns = columns;
    }

    public String getAiInsight() {
        return aiInsight;
    }

    public void setAiInsight(String aiInsight) {
        this.aiInsight = aiInsight;
    }

    public long getDuplicateRowCount() {
        return duplicateRowCount;
    }

    public void setDuplicateRowCount(long duplicateRowCount) {
        this.duplicateRowCount = duplicateRowCount;
    }

    public double getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(double completenessScore) {
        this.completenessScore = completenessScore;
    }

    public double getDuplicateScore() {
        return duplicateScore;
    }

    public void setDuplicateScore(double duplicateScore) {
        this.duplicateScore = duplicateScore;
    }

    public double getStructureScore() {
        return structureScore;
    }

    public void setStructureScore(double structureScore) {
        this.structureScore = structureScore;
    }

    public double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(double healthScore) {
        this.healthScore = healthScore;
    }

    public String getHealthLabel() {
        return healthLabel;
    }

    public void setHealthLabel(String healthLabel) {
        this.healthLabel = healthLabel;
    }

    public List<CorrelationPair> getCorrelations() {
        return correlations;
    }

    public void setCorrelations(List<CorrelationPair> correlations) {
        this.correlations = correlations;
    }
}

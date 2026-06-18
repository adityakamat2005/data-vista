package com.dataanalyzer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class DatasetProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String fileName;
    private long rowCount;
    private int columnCount;
    private long duplicateRowCount;
    private double completenessScore;
    private double duplicateScore;
    private double structureScore;
    private double healthScore;
    private String healthLabel;

    @Column(columnDefinition = "VARCHAR(10000)")
    private String aiInsight;

    @Column(columnDefinition = "VARCHAR(20000)")
    private String columnsJson;

    @Column(columnDefinition = "VARCHAR(10000)")
    private String correlationsJson;

    @Column(columnDefinition = "VARCHAR(200000)")
    private String rawDataJson;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public String getAiInsight() {
        return aiInsight;
    }

    public void setAiInsight(String aiInsight) {
        this.aiInsight = aiInsight;
    }

    public String getColumnsJson() {
        return columnsJson;
    }

    public void setColumnsJson(String columnsJson) {
        this.columnsJson = columnsJson;
    }

    public String getCorrelationsJson() {
        return correlationsJson;
    }

    public void setCorrelationsJson(String correlationsJson) {
        this.correlationsJson = correlationsJson;
    }

    public String getRawDataJson() {
        return rawDataJson;
    }

    public void setRawDataJson(String rawDataJson) {
        this.rawDataJson = rawDataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

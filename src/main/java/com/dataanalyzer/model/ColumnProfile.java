package com.dataanalyzer.model;

import java.util.List;

public class ColumnProfile {

    private String name;
    private String inferredType;
    private long totalCount;
    private long missingCount;
    private double missingPercentage;
    private long uniqueCount;
    private Double min;
    private Double max;
    private Double mean;
    private List<String> sampleValues;

    private long outlierCount;
    private String piiType;
    private boolean inconsistentFormatting;
    private long formatIssueCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInferredType() {
        return inferredType;
    }

    public void setInferredType(String inferredType) {
        this.inferredType = inferredType;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getMissingCount() {
        return missingCount;
    }

    public void setMissingCount(long missingCount) {
        this.missingCount = missingCount;
    }

    public double getMissingPercentage() {
        return missingPercentage;
    }

    public void setMissingPercentage(double missingPercentage) {
        this.missingPercentage = missingPercentage;
    }

    public long getUniqueCount() {
        return uniqueCount;
    }

    public void setUniqueCount(long uniqueCount) {
        this.uniqueCount = uniqueCount;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public List<String> getSampleValues() {
        return sampleValues;
    }

    public void setSampleValues(List<String> sampleValues) {
        this.sampleValues = sampleValues;
    }

    public long getOutlierCount() {
        return outlierCount;
    }

    public void setOutlierCount(long outlierCount) {
        this.outlierCount = outlierCount;
    }

    public String getPiiType() {
        return piiType;
    }

    public void setPiiType(String piiType) {
        this.piiType = piiType;
    }

    public boolean isInconsistentFormatting() {
        return inconsistentFormatting;
    }

    public void setInconsistentFormatting(boolean inconsistentFormatting) {
        this.inconsistentFormatting = inconsistentFormatting;
    }

    public long getFormatIssueCount() {
        return formatIssueCount;
    }

    public void setFormatIssueCount(long formatIssueCount) {
        this.formatIssueCount = formatIssueCount;
    }
}

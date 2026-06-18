package com.dataanalyzer.service;

import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.DatasetProfile;
import org.springframework.stereotype.Service;

@Service
public class HealthScoreService {

    public void calculate(DatasetProfile profile) {
        double completeness = calculateCompletenessScore(profile);
        double duplicateScore = calculateDuplicateScore(profile);
        double structureScore = calculateStructureScore(profile);

        double overall = (completeness * 0.5) + (duplicateScore * 0.3) + (structureScore * 0.2);

        profile.setCompletenessScore(round(completeness));
        profile.setDuplicateScore(round(duplicateScore));
        profile.setStructureScore(round(structureScore));
        profile.setHealthScore(round(overall));
        profile.setHealthLabel(labelFor(overall));
    }

    private double calculateCompletenessScore(DatasetProfile profile) {
        if (profile.getColumns() == null || profile.getColumns().isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (ColumnProfile column : profile.getColumns()) {
            total += (100.0 - column.getMissingPercentage());
        }
        return total / profile.getColumns().size();
    }

    private double calculateDuplicateScore(DatasetProfile profile) {
        if (profile.getRowCount() == 0) {
            return 100.0;
        }
        double duplicateRatio = (double) profile.getDuplicateRowCount() / profile.getRowCount();
        return Math.max(100.0 - (duplicateRatio * 100.0), 0.0);
    }

    private double calculateStructureScore(DatasetProfile profile) {
        if (profile.getColumns() == null || profile.getColumns().isEmpty()) {
            return 0.0;
        }
        long problematic = 0;
        for (ColumnProfile column : profile.getColumns()) {
            boolean isConstant = column.getUniqueCount() <= 1 && column.getTotalCount() > 1;
            boolean isEmpty = "EMPTY".equals(column.getInferredType());
            if (isConstant || isEmpty) {
                problematic++;
            }
        }
        double problematicRatio = (double) problematic / profile.getColumns().size();
        return Math.max(100.0 - (problematicRatio * 100.0), 0.0);
    }

    private String labelFor(double score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 50) return "Fair";
        return "Poor";
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

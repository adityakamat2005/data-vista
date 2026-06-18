package com.dataanalyzer.service;

import com.dataanalyzer.entity.DatasetProfileEntity;
import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.CorrelationPair;
import com.dataanalyzer.model.DatasetProfile;
import com.dataanalyzer.model.ParsedTable;
import com.dataanalyzer.repository.DatasetProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DatasetPersistenceService {

    private final DatasetProfileRepository repository;
    private final ObjectMapper objectMapper;

    public DatasetPersistenceService(DatasetProfileRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Long save(DatasetProfile profile, ParsedTable table, String userEmail) {
        try {
            DatasetProfileEntity entity = new DatasetProfileEntity();
            entity.setUserEmail(userEmail);
            entity.setFileName(profile.getFileName());
            entity.setRowCount(profile.getRowCount());
            entity.setColumnCount(profile.getColumnCount());
            entity.setDuplicateRowCount(profile.getDuplicateRowCount());
            entity.setCompletenessScore(profile.getCompletenessScore());
            entity.setDuplicateScore(profile.getDuplicateScore());
            entity.setStructureScore(profile.getStructureScore());
            entity.setHealthScore(profile.getHealthScore());
            entity.setHealthLabel(profile.getHealthLabel());
            entity.setAiInsight(profile.getAiInsight());
            entity.setColumnsJson(objectMapper.writeValueAsString(profile.getColumns()));
            entity.setCorrelationsJson(objectMapper.writeValueAsString(profile.getCorrelations()));
            entity.setRawDataJson(objectMapper.writeValueAsString(table));
            entity.setCreatedAt(LocalDateTime.now());

            DatasetProfileEntity saved = repository.save(entity);
            return saved.getId();
        } catch (JsonProcessingException e) {
            System.err.println("Failed to persist dataset profile: " + e.getMessage());
            return null;
        }
    }

    public Page<DatasetProfileEntity> getHistory(String userEmail, String search, String healthLabel, Pageable pageable) {
        boolean hasSearch = search != null && !search.trim().isEmpty();
        boolean hasLabel = healthLabel != null && !healthLabel.trim().isEmpty() && !healthLabel.equalsIgnoreCase("All");

        if (hasSearch && hasLabel) {
            return repository.findAllByUserEmailAndFileNameContainingIgnoreCaseAndHealthLabelOrderByCreatedAtDesc(userEmail, search, healthLabel, pageable);
        } else if (hasSearch) {
            return repository.findAllByUserEmailAndFileNameContainingIgnoreCaseOrderByCreatedAtDesc(userEmail, search, pageable);
        } else if (hasLabel) {
            return repository.findAllByUserEmailAndHealthLabelOrderByCreatedAtDesc(userEmail, healthLabel, pageable);
        } else {
            return repository.findAllByUserEmailOrderByCreatedAtDesc(userEmail, pageable);
        }
    }

    public List<DatasetProfileEntity> getAllHistory(String userEmail) {
        return repository.findAllByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public DatasetProfile getById(Long id, String userEmail) {
        return toDatasetProfile(getEntityById(id, userEmail));
    }

    public DatasetProfileEntity getEntityById(Long id, String userEmail) {
        return repository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new IllegalArgumentException("No dataset profile found with id " + id + " for this user"));
    }

    public void delete(Long id, String userEmail) {
        DatasetProfileEntity entity = getEntityById(id, userEmail);
        repository.delete(entity);
    }

    public DatasetProfile toDatasetProfile(DatasetProfileEntity entity) {
        DatasetProfile profile = new DatasetProfile();
        profile.setId(entity.getId());
        profile.setFileName(entity.getFileName());
        profile.setRowCount(entity.getRowCount());
        profile.setColumnCount(entity.getColumnCount());
        profile.setDuplicateRowCount(entity.getDuplicateRowCount());
        profile.setCompletenessScore(entity.getCompletenessScore());
        profile.setDuplicateScore(entity.getDuplicateScore());
        profile.setStructureScore(entity.getStructureScore());
        profile.setHealthScore(entity.getHealthScore());
        profile.setHealthLabel(entity.getHealthLabel());
        profile.setAiInsight(entity.getAiInsight());

        try {
            List<ColumnProfile> columns = objectMapper.readValue(
                    entity.getColumnsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ColumnProfile.class)
            );
            profile.setColumns(columns);
        } catch (Exception e) {
            profile.setColumns(List.of());
        }

        try {
            List<CorrelationPair> correlations = objectMapper.readValue(
                    entity.getCorrelationsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CorrelationPair.class)
            );
            profile.setCorrelations(correlations);
        } catch (Exception e) {
            profile.setCorrelations(List.of());
        }

        return profile;
    }

    public ParsedTable getRawTable(DatasetProfileEntity entity) {
        try {
            return objectMapper.readValue(entity.getRawDataJson(), ParsedTable.class);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.dataanalyzer.service;

import com.dataanalyzer.model.DatasetProfile;
import com.dataanalyzer.model.ParsedTable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DatasetAnalysisService {

    private final FileParsingService fileParsingService;
    private final DatasetProfilingService profilingService;
    private final HealthScoreService healthScoreService;
    private final GeminiInsightService insightService;
    private final DatasetPersistenceService persistenceService;

    public DatasetAnalysisService(FileParsingService fileParsingService,
                                   DatasetProfilingService profilingService,
                                   HealthScoreService healthScoreService,
                                   GeminiInsightService insightService,
                                   DatasetPersistenceService persistenceService) {
        this.fileParsingService = fileParsingService;
        this.profilingService = profilingService;
        this.healthScoreService = healthScoreService;
        this.insightService = insightService;
        this.persistenceService = persistenceService;
    }

    public DatasetProfile analyze(MultipartFile file, String userEmail) throws IOException {
        ParsedTable table = fileParsingService.parse(file);

        DatasetProfile profile = profilingService.profile(table);
        profile.setFileName(file.getOriginalFilename());

        healthScoreService.calculate(profile);

        String insight = insightService.generateInsight(profile);
        profile.setAiInsight(insight);

        Long savedId = persistenceService.save(profile, table, userEmail);
        profile.setId(savedId);

        return profile;
    }
}

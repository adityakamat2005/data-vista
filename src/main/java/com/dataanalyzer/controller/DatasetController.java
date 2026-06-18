package com.dataanalyzer.controller;

import com.dataanalyzer.entity.DatasetProfileEntity;
import com.dataanalyzer.model.DatasetProfile;
import com.dataanalyzer.service.DatasetAnalysisService;
import com.dataanalyzer.service.DatasetPersistenceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetAnalysisService analysisService;
    private final DatasetPersistenceService persistenceService;

    public DatasetController(DatasetAnalysisService analysisService, DatasetPersistenceService persistenceService) {
        this.analysisService = analysisService;
        this.persistenceService = persistenceService;
    }

    @PostMapping("/profile")
    public DatasetProfile profileDataset(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        return analysisService.analyze(file, authentication.getName());
    }

    @GetMapping("/history")
    public List<DatasetProfileEntity> getHistory(Authentication authentication) {
        return persistenceService.getAllHistory(authentication.getName());
    }

    @GetMapping("/{id}")
    public DatasetProfile getById(@PathVariable Long id, Authentication authentication) {
        return persistenceService.getById(id, authentication.getName());
    }
}

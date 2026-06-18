package com.dataanalyzer.controller;

import com.dataanalyzer.entity.DatasetProfileEntity;
import com.dataanalyzer.model.ColumnComparisonRow;
import com.dataanalyzer.model.DatasetProfile;
import com.dataanalyzer.model.ParsedTable;
import com.dataanalyzer.service.ChartDataService;
import com.dataanalyzer.service.DataCleaningService;
import com.dataanalyzer.service.DatasetAnalysisService;
import com.dataanalyzer.service.DatasetPersistenceService;
import com.dataanalyzer.service.PdfReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final DatasetAnalysisService analysisService;
    private final DatasetPersistenceService persistenceService;
    private final ChartDataService chartDataService;
    private final DataCleaningService dataCleaningService;
    private final PdfReportService pdfReportService;

    public WebController(DatasetAnalysisService analysisService,
                          DatasetPersistenceService persistenceService,
                          ChartDataService chartDataService,
                          DataCleaningService dataCleaningService,
                          PdfReportService pdfReportService) {
        this.analysisService = analysisService;
        this.persistenceService = persistenceService;
        this.chartDataService = chartDataService;
        this.dataCleaningService = dataCleaningService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/upload";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model, Authentication authentication) {
        model.addAttribute("userEmail", authentication.getName());
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model, Authentication authentication) throws IOException {
        DatasetProfile profile = analysisService.analyze(file, authentication.getName());
        addResultModel(model, profile, authentication.getName());
        return "result";
    }

    @GetMapping("/history")
    public String showHistory(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String label,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String compareError,
                               Model model,
                               Authentication authentication) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<DatasetProfileEntity> historyPage = persistenceService.getHistory(authentication.getName(), search, label, pageable);

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("label", label == null ? "All" : label);
        model.addAttribute("userEmail", authentication.getName());
        model.addAttribute("compareError", compareError != null);
        return "history";
    }

    @GetMapping("/history/{id}")
    public String showHistoryDetail(@PathVariable Long id, Model model, Authentication authentication) {
        DatasetProfile profile = persistenceService.getById(id, authentication.getName());
        addResultModel(model, profile, authentication.getName());
        return "result";
    }

    @PostMapping("/history/{id}/delete")
    public String deleteHistory(@PathVariable Long id, Authentication authentication) {
        persistenceService.delete(id, authentication.getName());
        return "redirect:/history";
    }

    @GetMapping("/history/{id}/cleaned-csv")
    public ResponseEntity<byte[]> downloadCleanedCsv(@PathVariable Long id, Authentication authentication) throws IOException {
        DatasetProfileEntity entity = persistenceService.getEntityById(id, authentication.getName());
        DatasetProfile profile = persistenceService.toDatasetProfile(entity);
        ParsedTable table = persistenceService.getRawTable(entity);

        byte[] csvBytes = dataCleaningService.generateCleanedCsv(table, profile.getColumns());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cleaned-" + entity.getFileName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @GetMapping("/history/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, Authentication authentication) throws IOException {
        DatasetProfile profile = persistenceService.getById(id, authentication.getName());
        byte[] pdfBytes = pdfReportService.generatePdf(profile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/compare")
    public String compare(@RequestParam(required = false) List<Long> selected, Model model, Authentication authentication) {
        if (selected == null || selected.size() != 2) {
            return "redirect:/history?compareError=true";
        }

        try {
            DatasetProfile profileA = persistenceService.getById(selected.get(0), authentication.getName());
            DatasetProfile profileB = persistenceService.getById(selected.get(1), authentication.getName());

            model.addAttribute("profileA", profileA);
            model.addAttribute("profileB", profileB);
            model.addAttribute("columnComparisons", buildColumnComparisons(profileA, profileB));
            model.addAttribute("userEmail", authentication.getName());
            return "compare";
        } catch (IllegalArgumentException e) {
            return "redirect:/history?compareError=true";
        }
    }

    private List<ColumnComparisonRow> buildColumnComparisons(DatasetProfile profileA, DatasetProfile profileB) {
        Map<String, com.dataanalyzer.model.ColumnProfile> columnsB = new HashMap<>();
        for (com.dataanalyzer.model.ColumnProfile col : profileB.getColumns()) {
            columnsB.put(col.getName(), col);
        }

        List<ColumnComparisonRow> rows = new ArrayList<>();
        for (com.dataanalyzer.model.ColumnProfile colA : profileA.getColumns()) {
            com.dataanalyzer.model.ColumnProfile colB = columnsB.get(colA.getName());
            if (colB != null) {
                ColumnComparisonRow row = new ColumnComparisonRow();
                row.setName(colA.getName());
                row.setMissingA(colA.getMissingPercentage());
                row.setMissingB(colB.getMissingPercentage());
                rows.add(row);
            }
        }
        return rows;
    }

    private void addResultModel(Model model, DatasetProfile profile, String userEmail) {
        model.addAttribute("profile", profile);
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("chartLabels", chartDataService.getColumnLabels(profile));
        model.addAttribute("chartMissing", chartDataService.getMissingPercentages(profile));
        model.addAttribute("chartTypeCounts", chartDataService.getTypeCounts(profile));
    }
}

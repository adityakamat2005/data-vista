package com.dataanalyzer.service;

import com.dataanalyzer.model.ColumnProfile;
import com.dataanalyzer.model.CorrelationPair;
import com.dataanalyzer.model.DatasetProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiInsightService {

    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiInsightService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateInsight(DatasetProfile profile) {
        try {
            String prompt = buildPrompt(profile);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            String url = GEMINI_ENDPOINT + "?key=" + apiKey;

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

            return extractText(response);
        } catch (RestClientException e) {
            return "AI insight unavailable right now (" + e.getMessage() + ").";
        }
    }

    private String buildPrompt(DatasetProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a data analyst. Based on the dataset profiling statistics below, ")
          .append("write a short summary (4-6 sentences) of the dataset's overall quality, ")
          .append("call out the most important issues, and suggest 2-3 concrete cleaning steps.\n\n");
        sb.append("File: ").append(profile.getFileName()).append("\n");
        sb.append("Rows: ").append(profile.getRowCount())
          .append(", Columns: ").append(profile.getColumnCount()).append("\n");
        sb.append("Health Score: ").append(profile.getHealthScore())
          .append(" (").append(profile.getHealthLabel()).append(")\n");
        sb.append("Duplicate rows: ").append(profile.getDuplicateRowCount()).append("\n\n");

        for (ColumnProfile col : profile.getColumns()) {
            sb.append("- ").append(col.getName())
              .append(" [").append(col.getInferredType()).append("]")
              .append(": missing=").append(col.getMissingPercentage()).append("%")
              .append(", unique=").append(col.getUniqueCount());
            if (col.getMin() != null) {
                sb.append(", min=").append(col.getMin())
                  .append(", max=").append(col.getMax())
                  .append(", mean=").append(col.getMean())
                  .append(", outliers=").append(col.getOutlierCount());
            }
            if (col.getPiiType() != null) {
                sb.append(", possible PII type=").append(col.getPiiType());
            }
            if (col.isInconsistentFormatting()) {
                sb.append(", has inconsistent text formatting (").append(col.getFormatIssueCount()).append(" groups)");
            }
            sb.append("\n");
        }

        if (profile.getCorrelations() != null && !profile.getCorrelations().isEmpty()) {
            sb.append("\nNotable correlations between numeric columns:\n");
            for (CorrelationPair pair : profile.getCorrelations()) {
                if (Math.abs(pair.getCorrelation()) >= 0.5) {
                    sb.append("- ").append(pair.getColumnA()).append(" vs ").append(pair.getColumnB())
                      .append(": r=").append(pair.getCorrelation()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> body) {
        if (body == null) {
            return "No response from AI service.";
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return "No insight generated.";
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            return "No insight generated.";
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return "No insight generated.";
        }
        Object text = parts.get(0).get("text");
        return text != null ? text.toString() : "No insight generated.";
    }
}

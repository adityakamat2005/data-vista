# DataVista

A Spring Boot web app that profiles CSV, JSON, and Excel datasets, scores their data
quality, flags issues automatically, and generates AI-written insights.

## Stack

Java 17, Spring Boot 3.3.4, Maven, Spring Security (form login), Spring Data JPA + H2
(file-based), Thymeleaf, Chart.js, Google Gemini API for AI insights, openhtmltopdf for
PDF export.

## Running it

1. Set the `GEMINI_API_KEY` environment variable (IntelliJ run configuration ->
   Environment variables), using a free-tier Google AI Studio key.
2. `cd` into the folder containing `pom.xml` and run `mvn spring-boot:run`, or run the
   app directly from IntelliJ.
3. Visit `http://localhost:8082`, register an account, and log in.

The H2 console is available at `http://localhost:8082/h2-console`
(JDBC URL `jdbc:h2:file:./data/datasetanalyzer`, user `sa`, blank password).

## Features

- Upload CSV, JSON, or Excel (.xlsx/.xls) files for profiling. JSON must be an array
  of flat objects, e.g. `[{"id": 1, "name": "Alice"}, ...]`. Excel files are read from
  the first sheet, with row 1 treated as headers.
- Per-column stats: type inference (numeric/text/empty), missing %, unique count,
  min/max/mean for numeric columns.
- Duplicate row detection.
- Outlier detection on numeric columns using the IQR method.
- PII detection (email, phone, SSN-like patterns) on text columns.
- Format-consistency checks (flags values that only differ by casing/whitespace).
- Pearson correlation between every pair of numeric columns.
- A weighted data health score (0-100) with a label (Excellent/Good/Fair/Poor).
- AI-written insight summary via Google Gemini, including outlier/PII/correlation
  context in the prompt.
- Inline charts on the result page (missing-value bar chart, column-type doughnut).
- Download a cleaned CSV (missing numeric values imputed with the column mean,
  missing text values filled with "Unknown") regenerated on demand from stored data.
- Download a PDF report in a separate navy/blue print theme.
- History page with filename search, health-label filter, pagination, and delete.
- Pick any two history entries and compare them side by side, including a
  column-level missing-% delta for columns that exist in both.
- Drag-and-drop file upload.
- Account page to change your password.
- Per-user data scoping — everyone only sees their own uploads.

## Known limitation

Excel cells formatted as dates are converted to plain `yyyy-MM-dd` strings rather
than being treated as a distinct date type — they'll currently profile as TEXT
columns rather than NUMERIC. Worth knowing if you're analyzing spreadsheets with
date columns; flag it if you'd like that handled differently.

## REST API (for curl/testing)

- `POST /api/datasets/profile` (multipart `file`, HTTP Basic auth) — analyze a file.
- `GET /api/datasets/history` — your past analyses.
- `GET /api/datasets/{id}` — a specific past analysis.

## Notes on persistence

Each analysis stores the full original parsed data (`rawDataJson`) alongside the
computed stats, so cleaned-CSV and PDF exports can be regenerated at any time from
history, not just right after upload. H2's `ddl-auto=update` will add the new
columns to your existing database automatically — old rows from before this update
just won't have correlation/raw data populated, which only affects exporting from
those specific older entries.

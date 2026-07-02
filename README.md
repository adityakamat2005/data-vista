# 📊 DataVista

> AI-powered dataset profiling and data quality analysis — upload a file, get back health scores, outlier flags, correlations, and an AI-written summary.

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-file--based-1316BF?style=for-the-badge&logo=h2&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Chart.js](https://img.shields.io/badge/Chart.js-4.4-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google_Gemini-AI-4285F4?style=for-the-badge&logo=google&logoColor=white)

---

## 📖 Overview

DataVista is a full-stack Spring Boot web application that lets users upload CSV, JSON, or Excel datasets and instantly receive a comprehensive data quality report. It performs per-column profiling, outlier and PII detection, Pearson correlation analysis, and a weighted health score — all surfaced through a clean dark-themed dashboard. Users can also download PDF reports, export cleaned CSVs with imputed missing values, compare two datasets side by side, and receive AI-generated insight summaries powered by Google Gemini.

---

## ✨ Features

- **Multi-format upload** — supports CSV (UTF-8 with BOM), JSON (array of flat objects), and Excel (.xlsx / .xls)
- **Per-column profiling** — type inference (NUMERIC / TEXT / EMPTY), missing %, unique count, min / max / mean
- **Missing-value sentinel recognition** — correctly identifies `NA`, `N/A`, `NULL`, `NaN`, `NONE`, `MISSING`, `-`, `?` as missing (not just blank cells)
- **Outlier detection** — IQR-based outlier count per numeric column
- **PII flagging** — regex detection of email addresses, phone numbers, and SSN-like patterns in text columns
- **Format-consistency checks** — flags columns where the same value appears in multiple casings (e.g. `Engineering` vs `engineering`)
- **Pearson correlation matrix** — computed across all numeric column pairs
- **Weighted data health score** — 0–100 score (Completeness 50% + Duplicate 30% + Structure 20%) with Excellent / Good / Fair / Poor label
- **Duplicate row detection**
- **AI insight generation** — Google Gemini API writes a 4–6 sentence quality summary including outlier, PII, and correlation context
- **Chart.js visualizations** — missing-values bar chart and column-type doughnut on the result page
- **PDF report export** — navy/blue print theme rendered via openhtmltopdf
- **Cleaned CSV export** — missing numeric values imputed with column mean, missing text values filled with "Unknown"
- **History** — paginated list of past analyses with filename search and health-label filter
- **Dataset comparison** — side-by-side delta view of any two past analyses including column-level missing % difference
- **Delete** — remove any past analysis from history
- **Drag-and-drop upload**
- **User authentication** — register / login with BCrypt-hashed passwords
- **Per-user data scoping** — users only see their own analyses
- **Account settings** — change password from within the app

---

## 🏗️ Architecture

```
                        ┌─────────────────────────────────────────┐
                        │              Browser (Client)            │
                        │   Thymeleaf pages + Chart.js charts      │
                        └────────────┬──────────────┬─────────────┘
                                     │ HTTP          │ HTTP
                              (Web)  │               │  (REST /api/**)
                        ┌────────────▼───────────────▼─────────────┐
                        │           Spring Boot 3.3.4               │
                        │                                           │
                        │  ┌─────────────┐   ┌──────────────────┐  │
                        │  │WebController│   │DatasetController │  │
                        │  │AccountCtrl  │   │  (REST API)      │  │
                        │  │AuthController    └────────┬─────────┘  │
                        │  └──────┬──────┘            │            │
                        │         │                   │            │
                        │  ┌──────▼──────────────────▼──────────┐ │
                        │  │        DatasetAnalysisService       │ │
                        │  └──┬──────────┬──────────┬───────────┘ │
                        │     │          │          │             │
                        │  ┌──▼───┐ ┌───▼────┐ ┌──▼──────────┐  │
                        │  │File  │ │Profiling│ │GeminiInsight│  │
                        │  │Parse │ │Service  │ │Service      │  │
                        │  │Svc   │ └───┬─────┘ └──────┬──────┘  │
                        │  └──────┘     │              │          │
                        │          ┌────▼──────┐       │          │
                        │          │HealthScore│       │          │
                        │          │Service    │       │          │
                        │          └────┬──────┘       │          │
                        │               │              │          │
                        │  ┌────────────▼──────────────▼───────┐  │
                        │  │       DatasetPersistenceService    │  │
                        │  └────────────────┬───────────────────┘  │
                        │                   │                      │
                        └───────────────────┼──────────────────────┘
                                            │
                    ┌───────────────────────▼──────────────────────┐
                    │         H2 File-based Database                │
                    │   jdbc:h2:file:./data/datasetanalyzer         │
                    │   Tables: dataset_profile_entity, user        │
                    └───────────────────────────────────────────────┘
                                            │
                    ┌───────────────────────▼──────────────────────┐
                    │          Google Gemini API                     │
                    │  gemini-2.5-flash:generateContent             │
                    └───────────────────────────────────────────────┘
```

**Request flow (upload):**

1. User drags/drops or selects a file on the upload page and submits the form
2. `WebController` receives the `MultipartFile` and calls `DatasetAnalysisService.analyze()`
3. `FileParsingService` detects the file extension and parses it into a `ParsedTable` (CSV → Apache Commons CSV, JSON → Jackson, Excel → Apache POI)
4. `DatasetProfilingService` iterates columns: infers types, counts missing values (including sentinel strings like `NA`), computes stats, runs IQR outlier detection, PII regex matching, format-consistency checks, and Pearson correlations
5. `HealthScoreService` computes the weighted 0–100 health score
6. `GeminiInsightService` builds a structured prompt with all column stats and posts it to the Gemini API
7. `DatasetPersistenceService` serializes the full `DatasetProfile` and raw `ParsedTable` as JSON into H2 and returns the saved entity ID
8. `WebController` adds chart data via `ChartDataService` and renders `result.html`

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 (BCrypt, form login) |
| Persistence | Spring Data JPA + H2 (file-based) |
| Templating | Thymeleaf 3 |
| Frontend charts | Chart.js 4.4 (CDN) |
| CSV parsing | Apache Commons CSV 1.11.0 |
| Excel parsing | Apache POI 5.2.5 (poi-ooxml) |
| JSON parsing | Jackson (bundled with Spring Boot) |
| PDF export | openhtmltopdf-pdfbox 1.0.10 |
| AI insights | Google Gemini API (gemini-2.5-flash) |
| Build tool | Maven |
| Database | H2 (file-based, auto-schema via ddl-auto=update) |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- A free Google Gemini API key from [Google AI Studio](https://aistudio.google.com/)

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/adityakamat2005/datavista.git
cd datavista
```

**2. Set the Gemini API key environment variable**

On Windows (PowerShell):
```powershell
$env:GEMINI_API_KEY="your_api_key_here"
```

On macOS/Linux:
```bash
export GEMINI_API_KEY=your_api_key_here
```

In IntelliJ IDEA: Run → Edit Configurations → Environment variables → add `GEMINI_API_KEY=your_api_key_here`

**3. Run the application**
```bash
mvn spring-boot:run
```

**4. Open in browser**
```
http://localhost:8082
```

Register an account, log in, and upload a CSV, JSON, or Excel file to get started.

**H2 Console** (for inspecting the database directly):
```
http://localhost:8082/h2-console
JDBC URL: jdbc:h2:file:./data/datasetanalyzer
Username: sa
Password: (leave blank)
```

### Key `application.properties` values

```properties
spring.application.name=DataVista
server.port=8082
gemini.api.key=${GEMINI_API_KEY}
spring.datasource.url=jdbc:h2:file:./data/datasetanalyzer
spring.jpa.hibernate.ddl-auto=update
```

---

## 📡 API Endpoints

### Web (MVC — returns HTML pages)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/upload` | Upload page with drag-and-drop zone |
| POST | `/upload` | Analyze an uploaded file |
| GET | `/history` | Paginated history with search and label filter |
| GET | `/history/{id}` | View a specific past analysis |
| POST | `/history/{id}/delete` | Delete a past analysis |
| GET | `/history/{id}/cleaned-csv` | Download imputed/cleaned CSV |
| GET | `/history/{id}/pdf` | Download PDF report |
| GET | `/compare?selected={id}&selected={id}` | Side-by-side dataset comparison |
| GET | `/account` | Account settings page |
| POST | `/account/password` | Change password |
| GET | `/login` | Login page |
| GET | `/register` | Register page |
| POST | `/register` | Create a new account |

### REST API (JSON — HTTP Basic auth)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/datasets/profile` | Analyze a file (multipart), returns JSON profile |
| GET | `/api/datasets/history` | All past analyses for authenticated user |
| GET | `/api/datasets/{id}` | A specific past analysis by ID |

---

## 🗂️ Project Structure

```
dataset-analyzer/
├── pom.xml
├── README.md
├── .gitignore
├── sample.csv
└── src/
    └── main/
        ├── java/com/dataanalyzer/
        │   ├── DatasetAnalyzerApplication.java
        │   ├── controller/
        │   │   ├── AuthController.java
        │   │   ├── AccountController.java
        │   │   ├── DatasetController.java
        │   │   └── WebController.java
        │   ├── entity/
        │   │   ├── DatasetProfileEntity.java
        │   │   └── User.java
        │   ├── model/
        │   │   ├── ColumnProfile.java
        │   │   ├── ColumnComparisonRow.java
        │   │   ├── CorrelationPair.java
        │   │   ├── DatasetProfile.java
        │   │   └── ParsedTable.java
        │   ├── repository/
        │   │   ├── DatasetProfileRepository.java
        │   │   └── UserRepository.java
        │   ├── security/
        │   │   ├── CustomUserDetailsService.java
        │   │   └── SecurityConfig.java
        │   ├── service/
        │   │   ├── ChartDataService.java
        │   │   ├── DataCleaningService.java
        │   │   ├── DatasetAnalysisService.java
        │   │   ├── DatasetPersistenceService.java
        │   │   ├── DatasetProfilingService.java
        │   │   ├── FileParsingService.java
        │   │   ├── GeminiInsightService.java
        │   │   ├── HealthScoreService.java
        │   │   └── PdfReportService.java
        │   └── util/
        │       └── MissingValueUtils.java
        └── resources/
            ├── application.properties
            ├── static/
            │   └── css/
            │       └── style.css
            └── templates/
                ├── account.html
                ├── compare.html
                ├── history.html
                ├── login.html
                ├── register.html
                ├── report-pdf.html
                ├── result.html
                ├── upload.html
                └── fragments/
                    └── layout.html
```

---

## 📌 Roadmap

- [ ] **Excel date column support** — detect date-formatted Excel cells as a distinct DATE type rather than TEXT
- [ ] **Deployment** — host on Railway or Render with a persistent PostgreSQL database replacing H2
- [ ] **Chart export** — allow downloading the Chart.js visualizations as PNG images alongside the PDF report
- [ ] **Column-level cleaning controls** — let users choose per-column imputation strategies (mean / median / mode / drop) before downloading the cleaned CSV

---

## 👤 Author

**Aditya Kamat**
GitHub: [@adityakamat2005](https://github.com/adityakamat2005)

---

## 📄 License

This is a personal portfolio project. No license is applied — all rights reserved.

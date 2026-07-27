# Halo

Halo is an interactive stargazing application developed by **Star Dictators**
for CSC207 at the University of Toronto.

## Technology

- Java 17
- Java Swing and Java2D
- Maven
- JUnit 5

## Current milestone

The current goal is to establish the application's Clean Architecture
foundation, create its major views and ViewModels, add in-memory data access
objects, and begin implementing tested use cases.

## Viewing quality rating (Check Conditions)

Halo rates how suitable forecast weather is for stargazing. The Check
Conditions use case fetches weather for the selected location and time, then
scores it with a shared domain rubric in `ViewingQualityRating`. The Rank
Forecast Days use case reuses the same scoring formula so nights can be compared
consistently.

Weather failure does not block the mathematical sky map. The score is an
**estimate**, not a guarantee of observing conditions.

### Inputs

Each forecast moment is represented as a `WeatherCondition` with four factors
from Open-Meteo:

| Factor | Field | Units |
| --- | --- | --- |
| Cloud cover | `cloudCoverPercent` | 0–100 % |
| Visibility | `visibilityMeters` | metres |
| Precipitation probability | `precipitationProbabilityPercent` | 0–100 % |
| Weather code | `weatherCode` | WMO weather interpretation code |

### Overall score formula

Each factor is first converted into a **0–100 sub-score**, then combined with
fixed weights:

```text
overallScore =
    cloudCoverScore        * 0.35
  + weatherCodeScore       * 0.30
  + precipitationScore     * 0.20
  + visibilityScore        * 0.15
```

Weights reflect stargazing priorities: clear skies matter most, then the general
sky state (WMO code), then rain/snow chance, then how far you can see through
haze or fog.

### Continuous factor curves (piecewise-linear)

Cloud cover, visibility, and precipitation use piecewise-linear interpolation
between anchor points. Values outside the first/last anchor clamp to the end
score.

**Cloud cover** (lower is better):

| Cloud cover (%) | Score |
| ---: | ---: |
| 0 | 100 |
| 10 | 90 |
| 30 | 65 |
| 60 | 40 |
| 100 | 0 |

**Visibility** (higher is better):

| Visibility (m) | Score |
| ---: | ---: |
| 0 | 0 |
| 2,000 | 15 |
| 5,000 | 45 |
| 10,000 | 75 |
| 20,000 | 100 |

**Precipitation probability** (lower is better):

| Precip. probability (%) | Score |
| ---: | ---: |
| 0 | 100 |
| 10 | 85 |
| 30 | 55 |
| 50 | 25 |
| 100 | 0 |

### Weather code sub-score (WMO)

The weather code maps to a discrete 0–100 score. Unrecognized codes fall back to
a neutral **50** so scoring never crashes on an unknown code.

| Codes | Meaning (approx.) | Score |
| --- | --- | ---: |
| 0 | Clear sky | 100 |
| 1 | Mainly clear | 90 |
| 2 | Partly cloudy | 65 |
| 3 | Overcast | 40 |
| 45, 48 | Fog / depositing rime fog | 20 |
| 51–57 | Drizzle (incl. freezing) | 20 |
| 61–67 | Rain (incl. freezing) | 10 |
| 71–77 | Snow / snow grains | 10 |
| 80–86 | Rain/snow showers | 5 |
| 95, 96, 99 | Thunderstorm | 0 |
| other | Unrecognized | 50 |

### Rating buckets

The overall 0–100 score is bucketed into a label for display:

| Score range | Rating |
| --- | --- |
| ≥ 75 | EXCELLENT |
| ≥ 50 and &lt; 75 | GOOD |
| ≥ 25 and &lt; 50 | FAIR |
| &lt; 25 | POOR |

### Where this lives in the code

- `entity.weather.WeatherCondition` — holds the four weather factors
- `entity.weather.ViewingQualityRating` — pure scoring/rubric (unit-testable, no network)
- `use_case.check_conditions.CheckConditionsInteractor` — fetch → score → present one moment
- `use_case.rank_forecast_days.RankForecastDaysInteractor` — scores each selected night with the same rubric, then sorts by overall score (descending)

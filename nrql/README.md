# NRQL Validation Guidance

## Incident Context

- Incident ID: NRQL-19700101-0000
- Error: `INVALID_INPUT` (`NRDB:1102004`) from NerdGraph at path `actor.account.nrql`
- Message: `Unknown function coalesce()`

## What This Means

NRQL does **not** support the SQL function `coalesce()`.

Any dashboard, alert condition, runtime configuration, or API workflow that executes an NRQL query containing `coalesce()` will fail validation at NerdGraph and return no results.

## Recommended Fix Pattern (Example)

Replace `coalesce(attributeA, attributeB)` with a supported NRQL pattern.

If the intent is conditional aggregation, prefer `filter()`:

```sql
SELECT
  filter(count(*), WHERE message IS NOT NULL) AS withMessage,
  filter(count(*), WHERE message IS NULL AND `error.class` IS NOT NULL) AS withErrorClass
FROM Log
SINCE 30 minutes ago
```

## Preventive Measures

- Add a CI/CD gate to validate configured NRQL queries via NerdGraph.
- Add automated tests to ensure queries do not return `INVALID_INPUT` / `NRDB:1102004`.
- Add monitoring/alerting for spikes of `NRDB:1102004`.

## Data Gaps

- Exact failing NRQL query string: Information not available
- Source of the query (dashboard/alert/runtime config/API caller): Information not available

# Fix: Validate/Normalize NRQL Investigation Time Window

## Problem
New Relic rejects NRQL queries when the start time is not before the end time.

Observed error:
- "Your query's start time must be before its end time."

RCA indicates:
- `investigationStartEpoch == investigationEndEpoch == 1773128104511`

## Proposed Guard Clause (implementation snippet)
```js
// before building/executing NRQL
if (investigationEndEpoch <= investigationStartEpoch) {
  investigationEndEpoch = investigationStartEpoch + 60000; // minimum 60s window
  // OR: throw a validation error to the caller and require corrected inputs
}
// proceed to query with SINCE investigationStartEpoch UNTIL investigationEndEpoch
```

## Recommended Tests
- start == end
- end < start
- valid ranges

## Notes / Data gaps
- Code module/file where NRQL is built/executed: Information not available (codeAnalysis missing).

# Production Quality Gate

Run the same blocking checks used by CI:

```powershell
./scripts/quality-gate.ps1
```

The gate rejects tracked secrets and generated files, then runs backend tests,
Checkstyle, SpotBugs, and JaCoCo. Line coverage must be at least 50 percent.
The HTML coverage report is generated under `target/site/jacoco`.

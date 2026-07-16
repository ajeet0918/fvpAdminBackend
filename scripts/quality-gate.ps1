param(
    [switch]$SkipRepositoryCheck
)

$ErrorActionPreference = "Stop"

if (-not $SkipRepositoryCheck) {
    & "$PSScriptRoot/check-repository.ps1"
}

mvn --batch-mode --no-transfer-progress -Pquality verify
if ($LASTEXITCODE -ne 0) {
    throw "Backend production quality gate failed."
}

Write-Host "Backend production quality gate passed."

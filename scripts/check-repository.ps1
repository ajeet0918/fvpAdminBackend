param()

$ErrorActionPreference = "Stop"

$trackedFiles = @(git ls-files)
if ($LASTEXITCODE -ne 0) {
    throw "Unable to read tracked files."
}

$forbiddenFiles = @($trackedFiles | Where-Object {
    $_ -match '(^|/)\.env($|\.)' -and $_ -notmatch '(^|/)\.env\.example$' -or
    $_ -match '\.(pem|key|p12|pfx|jks)$' -or
    $_ -match '(^|/)(target|uploads)/' -or
    $_ -match '\.log$'
})

if ($forbiddenFiles.Count -gt 0) {
    Write-Error "Forbidden generated or sensitive files are tracked: $($forbiddenFiles -join ', ')"
}

$privateKeyFiles = @()
foreach ($file in $trackedFiles) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        continue
    }
    if (Select-String -LiteralPath $file -Pattern '-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----' -Quiet) {
        $privateKeyFiles += $file
    }
}

if ($privateKeyFiles.Count -gt 0) {
    Write-Error "Private key material detected in tracked files: $($privateKeyFiles -join ', ')"
}

$unsafeSecretDefaults = @()
$configurationFiles = @($trackedFiles | Where-Object { $_ -match '\.(properties|ya?ml)$' })
foreach ($file in $configurationFiles) {
    $lineNumber = 0
    foreach ($line in Get-Content -LiteralPath $file) {
        $lineNumber++
        if ($line -match '\$\{[A-Z0-9_]*(PASSWORD|SECRET|PRIVATE_KEY|CLIENT_SECRET)[A-Z0-9_]*:([^}\s]+)\}') {
            $unsafeSecretDefaults += "${file}:${lineNumber}"
        }
    }
}

if ($unsafeSecretDefaults.Count -gt 0) {
    Write-Error "Non-empty secret defaults detected at: $($unsafeSecretDefaults -join ', ')"
}

Write-Host "Repository hygiene and secret-default checks passed."

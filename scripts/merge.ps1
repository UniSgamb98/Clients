$ErrorActionPreference = "Stop"

$sourceDirectory = Join-Path $PSScriptRoot "../notes to merge"
$output = Join-Path $PSScriptRoot "../txt data/tutte_le_note.txt"

if (-not (Test-Path -LiteralPath $sourceDirectory -PathType Container)) {
    throw "Cartella delle note non trovata: $sourceDirectory"
}

$outputDirectory = Split-Path -Parent $output
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

if (Test-Path -LiteralPath $output) {
    Remove-Item -LiteralPath $output -Force
}

$xmlFiles = @(Get-ChildItem -LiteralPath $sourceDirectory -Filter "*.xml" -File | Sort-Object Name)

foreach ($xmlFile in $xmlFiles) {
    Add-Content -LiteralPath $output -Encoding UTF8 -Value ""
    Add-Content -LiteralPath $output -Encoding UTF8 -Value "===== FILE: $($xmlFile.Name) ====="
    Add-Content -LiteralPath $output -Encoding UTF8 -Value ""
    Get-Content -LiteralPath $xmlFile.FullName -Raw | Add-Content -LiteralPath $output -Encoding UTF8
    Add-Content -LiteralPath $output -Encoding UTF8 -Value ""
    Add-Content -LiteralPath $output -Encoding UTF8 -Value "===== END FILE: $($xmlFile.Name) ====="
    Add-Content -LiteralPath $output -Encoding UTF8 -Value ""
}

if ($xmlFiles.Count -eq 0) {
    New-Item -ItemType File -Path $output -Force | Out-Null
    Write-Warning "Nessun file XML trovato in: $sourceDirectory"
}

Write-Host "Uniti $($xmlFiles.Count) file XML in: $output"

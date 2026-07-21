$files = Get-ChildItem -Path "C:\Users\hrida\StudioProjects\aShellYou\app\src\main\java" -Recurse -Filter "*.kt"
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $modified = $false

    if ($content -match "``in``.hridayan.ashell.core.common.LocalSettings") {
        $content = $content -replace "``in``.hridayan.ashell.core.common.LocalSettings", "LocalSettings"
        if (-not ($content -match "import in\.hridayan\.ashell\.core\.common\.LocalSettings")) {
            $content = $content -replace "(?m)^(package .*?
)", "${1}
import in.hridayan.ashell.core.common.LocalSettings
"
        }
        $modified = $true
    }

    if ($content -match "``in``.hridayan.ashell.core.common.SettingsKeys") {
        $content = $content -replace "``in``.hridayan.ashell.core.common.SettingsKeys", "SettingsKeys"
        if (-not ($content -match "import in\.hridayan\.ashell\.core\.common\.SettingsKeys")) {
            $content = $content -replace "(?m)^(package .*?
)", "${1}
import in.hridayan.ashell.core.common.SettingsKeys
"
        }
        $modified = $true
    }

    if ($modified) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Cleaned up: $($file.FullName)"
    }
}

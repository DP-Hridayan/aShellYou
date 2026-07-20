$files = Get-ChildItem -Path 'app\src\main\java' -Recurse -Filter '*.kt'
foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $original = $content

    $content = $content -replace 'DialogKey\.None', 'null'
    $content = $content -replace 'DialogKey\.Shell\.None', 'null'

    $replacements = @(
        @{ Pattern='DialogKey\.Settings\.(\w+)'; Replace='SettingsDialogKey.$1'; Import='in.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey' },
        @{ Pattern='DialogKey\.Shell\.(\w+)'; Replace='ShellDialogKey.$1'; Import='in.hridayan.ashell.shell.common.presentation.components.dialog.ShellDialogKey' },
        @{ Pattern='DialogKey\.CommandExamples\.(\w+)'; Replace='CommandExamplesDialogKey.$1'; Import='in.hridayan.ashell.commandexamples.presentation.component.dialog.CommandExamplesDialogKey' },
        @{ Pattern='DialogKey\.Home\.(\w+)'; Replace='HomeDialogKey.$1'; Import='in.hridayan.ashell.home.presentation.components.dialog.HomeDialogKey' },
        @{ Pattern='DialogKey\.Pair\.(\w+)'; Replace='PairDialogKey.$1'; Import='in.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairDialogKey' }
    )

    foreach ($repl in $replacements) {
        if ($content -match $repl.Pattern) {
            $content = [regex]::Replace($content, $repl.Pattern, $repl.Replace)
            
            if ($content -notmatch "import $($repl.Import)") {
                $lines = $content -split "
|
"
                $lastImport = -1
                for ($i = 0; $i -lt $lines.Length; $i++) {
                    if ($lines[$i].StartsWith('import ')) {
                        $lastImport = $i
                    }
                }
                
                if ($lastImport -ne -1) {
                    $newLines = @()
                    for ($i = 0; $i -le $lastImport; $i++) { $newLines += $lines[$i] }
                    $newLines += "import $($repl.Import)"
                    for ($i = $lastImport + 1; $i -lt $lines.Length; $i++) { $newLines += $lines[$i] }
                    $content = $newLines -join "
"
                } else {
                    $content = "import $($repl.Import)
" + $content
                }
            }
        }
    }

    if ($original -cne $content) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Updated $($file.FullName)"
    }
}

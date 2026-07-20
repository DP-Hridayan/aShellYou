import os
import re

def process_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # DialogKey.None
    content = content.replace('DialogKey.None', 'null')
    content = content.replace('DialogKey.Shell.None', 'null')

    # Replacements and imports
    replacements = [
        (r'DialogKey\.Settings\.(\w+)', r'SettingsDialogKey.\1', 'in.hridayan.ashell.settings.presentation.components.dialog.SettingsDialogKey'),
        (r'DialogKey\.Shell\.(\w+)', r'ShellDialogKey.\1', 'in.hridayan.ashell.shell.common.presentation.components.dialog.ShellDialogKey'),
        (r'DialogKey\.CommandExamples\.(\w+)', r'CommandExamplesDialogKey.\1', 'in.hridayan.ashell.commandexamples.presentation.component.dialog.CommandExamplesDialogKey'),
        (r'DialogKey\.Home\.(\w+)', r'HomeDialogKey.\1', 'in.hridayan.ashell.home.presentation.components.dialog.HomeDialogKey'),
        (r'DialogKey\.Pair\.(\w+)', r'PairDialogKey.\1', 'in.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog.PairDialogKey'),
    ]

    for pattern, repl, import_stat in replacements:
        if re.search(pattern, content):
            content = re.sub(pattern, repl, content)
            
            # check if import exists
            if import_stat not in content:
                # Add import after the last import line
                lines = content.split('\n')
                last_import = -1
                for i, line in enumerate(lines):
                    if line.startswith('import '):
                        last_import = i
                
                if last_import != -1:
                    lines.insert(last_import + 1, f'import {import_stat}')
                else:
                    lines.insert(0, f'import {import_stat}')
                
                content = '\n'.join(lines)

    if original != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {file_path}")

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))

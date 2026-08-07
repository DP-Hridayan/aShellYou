#!/usr/bin/env python3
"""
find_obsolete_strings.py

Finds string identifiers in translation locale XML files (`values-*/strings.xml`)
that are no longer present in the source base `strings.xml` (`values/strings.xml`).

Features:
- Scans all Android resource `res/` directories in the project.
- Checks `<string>`, `<plurals>`, and `<string-array>` elements.
- Displays report of obsolete string identifiers grouped by locale.
- Optional `--remove` / `--clean` flag to remove obsolete entries from translation files.
- Optional `--json` flag for machine-readable JSON output.
"""

import os
import sys
import glob
import argparse
import json
import xml.etree.ElementTree as ET
from pathlib import Path

# Ensure UTF-8 output encoding for standard output on Windows terminals
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')



def parse_xml_keys(xml_path):
    """
    Parses an Android strings.xml file and returns a tuple:
    (tree, root, dict_of_keys_to_elements)
    where dict_of_keys_to_elements maps (tag_name, key_name) -> element
    """
    try:
        parser = ET.XMLParser(encoding='utf-8')
        tree = ET.parse(xml_path, parser=parser)
        root = tree.getroot()
        keys = {}
        for child in root:
            # We track <string>, <plurals>, <string-array>
            if child.tag in ('string', 'plurals', 'string-array'):
                name = child.attrib.get('name')
                if name:
                    keys[(child.tag, name)] = child
        return tree, root, keys
    except Exception as e:
        print(f"Error parsing XML file {xml_path}: {e}", file=sys.stderr)
        return None, None, {}


def find_res_directories(root_path):
    """
    Finds all 'res' directories that contain 'values/strings.xml'.
    """
    res_dirs = []
    for dirpath, dirnames, filenames in os.walk(root_path):
        # Exclude build, .git, .gradle directories
        dirnames[:] = [d for d in dirnames if d not in ('build', '.git', '.gradle', '.idea', 'node_modules')]
        if Path(dirpath).name == 'values' and 'strings.xml' in filenames:
            parent_res = Path(dirpath).parent
            if parent_res not in res_dirs:
                res_dirs.append(parent_res)
    return res_dirs


def process_res_directory(res_dir, remove_obsolete=False):
    """
    Processes a single res directory:
    - Source: res/values/strings.xml
    - Locales: res/values-*/strings.xml
    Returns a dict with findings.
    """
    source_file = res_dir / 'values' / 'strings.xml'
    if not source_file.exists():
        return None

    _, _, source_keys = parse_xml_keys(source_file)
    source_key_set = set(source_keys.keys())

    locale_results = {}

    # Find all values-* directories
    values_pattern = str(res_dir / 'values-*' / 'strings.xml')
    locale_files = sorted(glob.glob(values_pattern))

    for loc_file_str in locale_files:
        loc_file = Path(loc_file_str)
        locale_name = loc_file.parent.name.replace('values-', '')
        
        tree, root, loc_keys = parse_xml_keys(loc_file)
        if loc_keys is None:
            continue

        obsolete_keys = []
        obsolete_elements = []

        for (tag, name), elem in loc_keys.items():
            if (tag, name) not in source_key_set:
                text_val = elem.text.strip() if (elem.text and elem.text.strip()) else ''
                obsolete_keys.append({
                    'tag': tag,
                    'name': name,
                    'text': text_val
                })
                obsolete_elements.append(elem)

        if obsolete_keys:
            locale_results[locale_name] = {
                'path': str(loc_file),
                'obsolete_count': len(obsolete_keys),
                'obsolete_keys': obsolete_keys
            }

            if remove_obsolete:
                for elem in obsolete_elements:
                    root.remove(elem)
                # Save modified XML with indent formatting
                if hasattr(ET, 'indent'):
                    ET.indent(tree, space="    ", level=0)
                tree.write(loc_file, encoding='utf-8', xml_declaration=True)
                print(f"Removed {len(obsolete_elements)} obsolete strings from {loc_file}")

    return {
        'res_dir': str(res_dir),
        'source_file': str(source_file),
        'total_source_keys': len(source_key_set),
        'locales': locale_results
    }


def main():
    parser = argparse.ArgumentParser(
        description="Find string identifiers in translation locales not present in source strings.xml"
    )
    parser.add_argument(
        '--root', '-r',
        default='.',
        help="Root directory of the project (default: current directory)"
    )
    parser.add_argument(
        '--clean', '--remove',
        action='store_true',
        help="Automatically remove obsolete string entries from the locale strings.xml files"
    )
    parser.add_argument(
        '--json',
        action='store_true',
        help="Output results in JSON format"
    )

    args = parser.parse_args()

    root_path = Path(args.root).resolve()
    if not root_path.exists():
        print(f"Error: Path {root_path} does not exist.", file=sys.stderr)
        sys.exit(1)

    res_dirs = find_res_directories(root_path)
    if not res_dirs:
        print(f"No res/values/strings.xml found under {root_path}", file=sys.stderr)
        sys.exit(1)

    all_results = []
    total_obsolete_count = 0

    for res_dir in res_dirs:
        res_data = process_res_directory(res_dir, remove_obsolete=args.clean)
        if res_data:
            all_results.append(res_data)
            for loc_info in res_data['locales'].values():
                total_obsolete_count += loc_info['obsolete_count']

    if args.json:
        print(json.dumps({
            'total_obsolete_count': total_obsolete_count,
            'modules': all_results
        }, indent=2, ensure_ascii=False))
        return

    # Readable Terminal Summary Output
    print("=" * 80)
    print(" OBSOLETE TRANSLATION STRINGS REPORT")
    print("=" * 80)
    print(f"Project Root: {root_path}")
    print(f"Modules scanned: {len(all_results)}")
    print(f"Total obsolete string entries found: {total_obsolete_count}")
    print("=" * 80)

    if total_obsolete_count == 0:
        print("\nAll translation locales are clean! No obsolete strings found.")
        print("=" * 80)
        return

    for res_data in all_results:
        print(f"\nResource Directory: {res_data['res_dir']}")
        print(f"Source Base File:   {res_data['source_file']} ({res_data['total_source_keys']} string identifiers)")
        
        if not res_data['locales']:
            print("  - All locale files are in sync with source.")
            continue

        for locale_name, loc_data in res_data['locales'].items():
            print(f"\n  Locale [{locale_name}] ({loc_data['obsolete_count']} obsolete items):")
            print(f"  File: {loc_data['path']}")
            for item in loc_data['obsolete_keys']:
                text_preview = item['text'][:50] + "..." if len(item['text']) > 50 else item['text']
                if text_preview:
                    print(f"    - <{item['tag']} name=\"{item['name']}\">{text_preview}</{item['tag']}>")
                else:
                    print(f"    - <{item['tag']} name=\"{item['name']}\" />")

    print("\n" + "=" * 80)
    if args.clean:
        print("Clean operation complete. Obsolete strings removed from locale files.")
    else:
        print("Tip: Run with '--clean' or '--remove' flag to automatically delete these obsolete strings.")
    print("=" * 80)


if __name__ == '__main__':
    main()

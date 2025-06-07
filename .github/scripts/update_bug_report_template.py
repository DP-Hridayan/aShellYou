import yaml
import subprocess

template_path = ".github/ISSUE_TEMPLATE/bug_report.yml"

try:
    tags = subprocess.check_output(["git", "tag", "--sort=-v:refname"]).decode().splitlines()
except subprocess.CalledProcessError:
    tags = []

if not tags:
    tags = ["v0.9.0 (Initial release)"]

latest_tag = tags[0]

version_dropdown = {
    "type": "dropdown",
    "id": "version",
    "attributes": {
        "label": "Version",
        "description": "What version of aShellYou are you running?",
        "options": [f"{tag} (Latest)" if tag == latest_tag else tag for tag in tags],
        "default": 0
    },
    "validations": {
        "required": True
    }
}

with open(template_path, "r") as f:
    template = yaml.safe_load(f)

if "body" in template:
    for i, field in enumerate(template["body"]):
        if isinstance(field, dict) and field.get("type") == "dropdown" and field.get("id") == "version":
            template["body"][i] = version_dropdown
            break

with open(template_path, "w") as f:
    yaml.dump(template, f, sort_keys=False)

print("Dropdown updated with tags:", tags)

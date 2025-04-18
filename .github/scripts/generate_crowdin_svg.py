import requests
import os

TOKEN = os.environ.get("CROWDIN_PERSONAL_TOKEN")
PROJECT_ID = os.environ.get("CROWDIN_PROJECT_ID")

headers = {
    'Authorization': f'Bearer {TOKEN}'
}

url = f"https://api.crowdin.com/api/v2/projects/{PROJECT_ID}/languages/progress"

response = requests.get(url, headers=headers)
response.raise_for_status()

data = response.json()["data"]

svg_lines = []
bar_width = 300
bar_height = 12
spacing = 25
width = 600
height = spacing * len(data) + 20

svg_lines.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}">')
svg_lines.append('<style>text { font-family: sans-serif; font-size: 14px; }</style>')

y = 20
for lang in data:
    name = lang["data"]["language"]["name"]
    progress = lang["data"]["translationProgress"]
    filled = int((progress / 100) * bar_width)
    svg_lines.append(f'<text x="10" y="{y}">{name}</text>')
    svg_lines.append(f'<rect x="180" y="{y - 12}" width="{bar_width}" height="{bar_height}" fill="#ddd" rx="4" ry="4" />')
    svg_lines.append(f'<rect x="180" y="{y - 12}" width="{filled}" height="{bar_height}" fill="#4caf50" rx="4" ry="4" />')
    svg_lines.append(f'<text x="{180 + bar_width + 10}" y="{y}">{progress}%</text>')
    y += spacing

svg_lines.append('</svg>')

with open("translations.svg", "w", encoding="utf-8") as f:
    f.write("\n".join(svg_lines))

print("SVG generated successfully!")

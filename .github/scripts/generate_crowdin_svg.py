import requests
import os

TOKEN = os.environ.get("CROWDIN_PERSONAL_TOKEN")
PROJECT_ID = os.environ.get("CROWDIN_PROJECT_ID")

headers = {'Authorization': f'Bearer {TOKEN}'}
url = f"https://api.crowdin.com/api/v2/projects/{PROJECT_ID}/languages/progress"
response = requests.get(url, headers=headers)
response.raise_for_status()
data = response.json()["data"]

themes = {
    "light": {
        "bg": "#00000000",
        "text": "#1C1B1F",
        "bar_bg": "#E0E0E0",
        "red": "#B3261E",
        "green": "#0F6B0F"
    },
    "dark": {
        "bg": "#00000000",
        "text": "#ffffff",
        "bar_bg": "#2C2F33",
        "red": "#FFB4AB",
        "green": "#9EE69D"
    }
}

def interpolate_color(progress, red_hex, green_hex):
    def hex_to_rgb(hex_code): return tuple(int(hex_code[i:i+2], 16) for i in (1, 3, 5))
    def rgb_to_hex(rgb_tuple): return '#{:02x}{:02x}{:02x}'.format(*rgb_tuple)
    red_rgb = hex_to_rgb(red_hex)
    green_rgb = hex_to_rgb(green_hex)
    mix = lambda r, g: int(r + (g - r) * (progress / 100))
    mixed = tuple(mix(r, g) for r, g in zip(red_rgb, green_rgb))
    return rgb_to_hex(mixed)

for theme_name, theme in themes.items():
    svg = []
    name_width = 150
    bar_width = 300
    percent_width = 150
    spacing = 30
    bar_height = 14
    width = name_width + bar_width + percent_width
    height = spacing * len(data) + 20

    svg.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" style="background:{theme["bg"]}">')
    svg.append(f'<style>text {{ font-family: sans-serif; font-size: 14px; fill: {theme["text"]}; }}</style>')

    y = 25
    for lang in data:
        name = lang["data"]["language"]["name"]
        progress = lang["data"]["translationProgress"]
        filled = int((progress / 100) * bar_width)
        fill_color = interpolate_color(progress, theme["red"], theme["green"])

        svg.append(f'<text x="{name_width - 10}" y="{y}" text-anchor="end"><![CDATA[{name}]]></text>')
        svg.append(f'<rect x="{name_width}" y="{y - 14}" width="{bar_width}" height="{bar_height}" fill="{theme["bar_bg"]}" rx="8" ry="8" />')
        svg.append(f'<rect x="{name_width}" y="{y - 14}" width="{filled}" height="{bar_height}" fill="{fill_color}" rx="8" ry="8" />')
        svg.append(f'<text x="{name_width + bar_width + 10}" y="{y}" text-anchor="start">{progress}%</text>')

        y += spacing

    svg.append('</svg>')

    with open(f"docs/translations-{theme_name}.svg", "w", encoding="utf-8") as f:
        f.write("\n".join(svg))

print("Material-colored SVGs for light and dark generated successfully!")

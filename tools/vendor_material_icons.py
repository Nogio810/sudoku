"""Vendor the five Material Symbols used by the app, including their Apache license."""

from pathlib import Path
from urllib.request import urlopen
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = "https://raw.githubusercontent.com/google/material-design-icons/master"
NAMES = ("home", "undo", "redo", "edit_note", "ink_eraser")


def main():
    resources = {}
    for name in NAMES:
        url = f"{BASE}/symbols/android/{name}/materialsymbolsoutlined/{name}_24px.xml"
        with urlopen(url, timeout=30) as response:
            source = response.read().decode("utf-8")
        assert ET.fromstring(source).tag == "vector"
        # Compose supplies the icon tint; omit the platform theme tint attribute.
        source = source.replace('\n    android:tint="?attr/colorControlNormal"', "")
        resources[name] = source
    with urlopen(f"{BASE}/LICENSE", timeout=30) as response:
        license_text = response.read().decode("utf-8")
    drawable = ROOT / "app/src/main/res/drawable"
    for name, source in resources.items():
        (drawable / f"ic_{name}.xml").write_text(source, encoding="utf-8")
    licenses = ROOT / "app/src/main/assets/licenses"
    licenses.mkdir(parents=True, exist_ok=True)
    (licenses / "material-symbols.txt").write_text(license_text, encoding="utf-8")
    print(f"Vendored {len(resources)} icons and license")


if __name__ == "__main__":
    main()

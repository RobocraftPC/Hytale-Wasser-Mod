#!/usr/bin/env python3
"""Erzeugt die Platzhaltertexturen des Mods.

Bewusst als Skript statt als Binaerdateien im Repo: so ist nachvollziehbar,
wie die Texturen entstanden sind, und sie lassen sich schnell anpassen.
Spaeter werden sie durch handgezeichnete Texturen ersetzt.

    python3 tools/make_textures.py
"""

import os
import struct
import zlib

SIZE = 16
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
TEXTURES = os.path.join(ROOT, "src/main/resources/Common/BlockTextures")
ICONS = os.path.join(ROOT, "src/main/resources/Common/Icons/ItemsGenerated")


def write_png(path, pixels):
    """Schreibt RGBA-Pixel als PNG - ohne externe Abhaengigkeit."""
    raw = b"".join(b"\x00" + b"".join(bytes(px) for px in row) for row in pixels)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body))

    header = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", header)
           + chunk(b"IDAT", zlib.compress(raw, 9))
           + chunk(b"IEND", b""))

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)
    print("geschrieben:", os.path.relpath(path, ROOT))


def channel():
    """Steintrog mit Wasser darin."""
    stone_dark = (86, 92, 99, 255)
    stone = (118, 125, 133, 255)
    water = (58, 141, 204, 255)
    water_light = (96, 178, 232, 255)

    rows = []
    for y in range(SIZE):
        row = []
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            if edge == 0:
                row.append(stone_dark)
            elif edge == 1:
                row.append(stone)
            elif (x + y * 2) % 7 == 0:
                row.append(water_light)
            else:
                row.append(water)
        rows.append(row)
    return rows


def pump():
    """Metallblock mit heller Duese in der Mitte."""
    metal_dark = (63, 70, 78, 255)
    metal = (99, 108, 118, 255)
    metal_light = (136, 146, 156, 255)
    jet = (110, 200, 240, 255)

    center = (SIZE - 1) / 2.0
    rows = []
    for y in range(SIZE):
        row = []
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            distance = ((x - center) ** 2 + (y - center) ** 2) ** 0.5
            if edge == 0:
                row.append(metal_dark)
            elif distance < 3.2:
                row.append(jet)
            elif distance < 4.4:
                row.append(metal_light)
            elif (x + y) % 5 == 0:
                row.append(metal_light)
            else:
                row.append(metal)
        rows.append(row)
    return rows


def main():
    for name, pixels in (("Stroemwerk_Wasserkanal", channel()),
                         ("Stroemwerk_Wasserpumpe", pump())):
        write_png(os.path.join(TEXTURES, name + ".png"), pixels)
        write_png(os.path.join(ICONS, name + ".png"), pixels)


if __name__ == "__main__":
    main()

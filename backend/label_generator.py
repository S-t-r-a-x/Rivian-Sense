import json
import os
import math

from PIL import Image
import numpy as np

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset"))
VIDEO_FOLDERS = ["A", "B", "C", "D"]


# ---------- IMAGE ANALYSIS FOR SCENE ----------

def analyze_image_for_scene(image_path):
    """
    Returns simple stats: green_ratio, gray_ratio, brightness.
    Used to distinguish forest / city / offroad-style scenes.
    """
    try:
        img = Image.open(image_path).convert("RGB")
    except Exception:
        return 0.0, 0.0, 0.0  # if frame missing or unreadable

    # downscale for speed
    img = img.resize((160, 90))
    arr = np.array(img).astype("float32")

    r = arr[:, :, 0]
    g = arr[:, :, 1]
    b = arr[:, :, 2]

    # lots of green -> forest / nature
    green_mask = (g > r + 15) & (g > b + 15)
    green_ratio = green_mask.mean()

    # gray-ish buildings / asphalt -> city-like
    gray_mask = (
        (np.abs(r - g) < 10) &
        (np.abs(g - b) < 10) &
        (r > 50) & (r < 220)
    )
    gray_ratio = gray_mask.mean()

    brightness = arr.mean() / 255.0

    return float(green_ratio), float(gray_ratio), float(brightness)


# ---------- MOOD LABEL RULES ----------

def compute_mood_label(m):
    """
    Driver mood:
    0 = Relaxed
    1 = Focused
    2 = Tired
    3 = Stressed
    4 = Distracted
    """
    speed = m.get("displaySpeed", 0) or 0
    pitch = abs(m.get("pitchAngle", 0) or 0)
    roll = abs(m.get("rollAngle", 0) or 0)
    power = abs(m.get("powerMeter", 0) or 0)

    # 3) Stressed: strong forces or high power demand
    if power > 40 or pitch > 8 or roll > 8:
        return 3  # Stressed

    # 2) Tired: low speed but car slightly unstable
    if speed < 20 and (pitch > 5 or roll > 5):
        return 2  # Tired

    # 0) Relaxed: very calm, low speed, very stable, low power
    if speed < 10 and pitch < 3 and roll < 3 and power < 10:
        return 0  # Relaxed

    # 1) Focused: normal driving range, moderate speed, fairly stable
    if 10 <= speed <= 80 and pitch < 5 and roll < 5:
        return 1  # Focused

    # 4) Distracted: weird combination (not fitting above but with some motion)
    return 4  # Distracted


# ---------- SCENE LABEL RULES ----------

def compute_scene_label(m, green_ratio, gray_ratio):
    """
    Scene type:
    0 = City
    1 = Highway
    2 = Forest
    3 = Residential
    4 = Offroad
    """
    speed = m.get("displaySpeed", 0) or 0
    pitch = abs(m.get("pitchAngle", 0) or 0)
    roll = abs(m.get("rollAngle", 0) or 0)

    # 1) Highway: high speed, stable car
    if speed > 80 and pitch < 5 and roll < 5:
        return 1  # Highway

    # 4) Offroad: very low speed + bumpy car
    if speed < 15 and (pitch > 6 or roll > 6):
        return 4  # Offroad

    # 0) City: medium speed and a lot of gray-ish areas (buildings/asphalt)
    if 30 <= speed <= 70 and gray_ratio > 0.15:
        return 0  # City

    # 3) Residential: low-ish speed, some motion
    if 15 <= speed < 40:
        return 3  # Residential

    # 2) Forest / scenic: lots of green
    if green_ratio > 0.25:
        return 2  # Forest

    # Fallbacks:
    if gray_ratio > 0.20:
        return 0  # City-ish fallback

    # Otherwise assume scenic
    return 2  # Forest


# ---------- PROCESSING MAPPING FILES ----------

def process_mapping_file(mapping_path):
    with open(mapping_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    frames_root = os.path.dirname(mapping_path)

    for entry in data:
        m = entry.get("metadata", {})

        frame_name = entry["frame"]
        frame_path = os.path.join(frames_root, frame_name)

        green_ratio, gray_ratio, brightness = analyze_image_for_scene(frame_path)

        entry["mood_label"] = compute_mood_label(m)
        entry["scene_label"] = compute_scene_label(m, green_ratio, gray_ratio)

    labeled_path = mapping_path.replace("mapping.json", "mapping_labeled.json")
    with open(labeled_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4)

    print(f"✅ Labeled file written to: {labeled_path}")


if __name__ == "__main__":
    print("Dataset root:", DATASET_ROOT)
    for folder in VIDEO_FOLDERS:
        folder_path = os.path.join(DATASET_ROOT, folder)
        mapping_path = os.path.join(folder_path, "mapping.json")
        if os.path.exists(mapping_path):
            print(f"\n=== Processing {folder} ===")
            process_mapping_file(mapping_path)
        else:
            print(f"\n⚠ Skipping {folder}: no mapping.json found")

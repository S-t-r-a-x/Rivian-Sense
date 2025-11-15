import json
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset"))

# Scene classes:
SCENE = {
    "city": 0,
    "highway": 1,
    "forest": 2,
    "garage": 3,
    "offroad": 4,
    "traffic": 5    # <-- new class
}

# Mood classes:
MOOD = {
    "relaxed": 0,
    "focused": 1,
    "stressed": 2,
    "tired": 3,
    "distracted": 4
}

# -------------------------------
# LABEL DEFINITIONS FOR EACH FOLDER
# -------------------------------

LABELS = {
    "A": [
        (0, 110,  "garage",  "relaxed"),
        (111, 221, "city",    "relaxed"),
    ],

    "B": [
        (0, 40,    "city",    "relaxed"),
        (40, 115,  "city",    "focused"),
        (115, 140, "city",    "stressed"),
        (140, 300, "highway", "focused"),
        (300, 330, "city",    "focused"),
        (331, 370, "city",    "stressed"),
        (370, 450, "highway", "focused"),
        (450, 550, "city",    "focused"),
        (551, 599, "city",    "relaxed"),
        (600, 761, "city",    "focused"),     # NEW LABEL RANGE
    ],

    "C": [
        (0, 308, "forest", "relaxed"),
    ],

    "D": [
        (0, 115,   "forest", "relaxed"),
        (116, 240, "city",   "relaxed"),
        (241, 254, "city",   "relaxed"),
        (255, 599, "traffic","stressed"),
        (600, 899, "traffic","stressed"),     # NEW LABEL RANGE
    ]
}


# -------------------------------
# Apply labels to mapping.json
# -------------------------------

def apply_labels(folder):
    folder_path = os.path.join(DATASET_ROOT, folder)
    mapping_path = os.path.join(folder_path, "mapping.json")

    if not os.path.exists(mapping_path):
        print(f"⚠ Skipping {folder}: mapping.json not found")
        return

    with open(mapping_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Build a dict: frame index → (scene_label, mood_label)
    label_map = {}

    for (start, end, scene_name, mood_name) in LABELS[folder]:
        scene_label = SCENE[scene_name]
        mood_label = MOOD[mood_name]

        for idx in range(start, end + 1):
            label_map[idx] = (scene_label, mood_label)

    # Apply labels
    for entry in data:
        frame_name = entry["frame"]       # "frame_123.jpg"
        frame_index = int(frame_name.split("_")[1].split(".")[0])

        if frame_index in label_map:
            scene_label, mood_label = label_map[frame_index]
            entry["scene_label"] = scene_label
            entry["mood_label"] = mood_label
        else:
            print(f"⚠ Frame {frame_index} in {folder} has no assigned label!")

    # Save new file
    out_path = os.path.join(folder_path, "mapping_hardcoded.json")
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4)

    print(f"✅ Saved labeled file: {out_path}")


if __name__ == "__main__":
    print("Dataset root:", DATASET_ROOT)
    for folder in ["A", "B", "C", "D"]:
        print(f"\n=== Processing {folder} ===")
        apply_labels(folder)

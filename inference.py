import os
import json
import torch
from PIL import Image
from torchvision import transforms
from train import RivianModel

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_A = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset", "A"))
MAPPING = os.path.join(DATASET_A, "mapping_hardcoded.json")

MOOD_LABELS = ["Relaxed", "Focused", "Stressed", "Tired", "Distracted"]
SCENE_LABELS = ["City", "Highway", "Forest", "Garage", "Offroad", "Traffic"]

tf = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor()
])

def predict_entry(entry, frame_dir):
    img_path = os.path.join(frame_dir, entry["frame"])
    img = Image.open(img_path).convert("RGB")
    img = tf(img).unsqueeze(0)

    model = RivianModel()
    model.load_state_dict(torch.load(os.path.join(SCRIPT_DIR, "model.pth")))
    model.eval()

    with torch.no_grad():
        mood_logits, scene_logits = model(img)

    mood_idx = mood_logits.argmax(dim=1).item()
    scene_idx = scene_logits.argmax(dim=1).item()

    return MOOD_LABELS[mood_idx], SCENE_LABELS[scene_idx]


if __name__ == "__main__":
    DATASET_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset"))
    FOLDERS = ["A", "B", "C", "D"]

    print("\n=== GENERATING PREDICTION FILES FOR ALL FOLDERS ===\n")

    for folder in FOLDERS:
        mapping_path = os.path.join(DATASET_ROOT, folder, "mapping_hardcoded.json")

        if not os.path.exists(mapping_path):
            print(f"⚠ Skipping {folder}: mapping_hardcoded.json not found\n")
            continue

        frames_root = os.path.dirname(mapping_path)

        # Load dataset
        with open(mapping_path, "r") as f:
            data = json.load(f)

        # Output file path
        out_file = os.path.join(SCRIPT_DIR, f"{folder}_predictions.txt")

        print(f"📄 Writing predictions to: {out_file}")

        with open(out_file, "w") as out:
            out.write(f"=== Predictions for folder {folder} ===\n\n")

            limit = min(1000, len(data))   # <-- change number if needed

            for i in range(limit):
                entry = data[i]
                mood, scene = predict_entry(entry, frames_root)
                line = f"{i:03d} | {entry['frame']} -> {mood} / {scene}"
                out.write(line + "\n")

        print(f"✅ Done with {folder}\n")

    print("\n🎉 All prediction files generated!\n")


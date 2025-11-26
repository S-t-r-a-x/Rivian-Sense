import os
import json
import torch
import time
from PIL import Image
from torchvision import transforms
from train import RivianModel

# --------------------
# LABELS
# --------------------
MOOD_LABELS = ["Relaxed", "Focused", "Stressed", "Tired", "Distracted"]
SCENE_LABELS = ["City", "Highway", "Forest", "Garage", "Offroad", "Traffic"]

# --------------------
# IMAGE TRANSFORM
# --------------------
tf = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor()
])

def predict_entry(entry, frame_dir, model):
    """Predict mood + scene for one frame."""
    img_path = os.path.join(frame_dir, entry["frame"])
    img = Image.open(img_path).convert("RGB")
    img = tf(img).unsqueeze(0)

    model.eval()
    with torch.no_grad():
        mood_logits, scene_logits = model(img)

    mood_idx = mood_logits.argmax(dim=1).item()
    scene_idx = scene_logits.argmax(dim=1).item()

    return MOOD_LABELS[mood_idx], SCENE_LABELS[scene_idx]


# =================================================================
#                            MAIN
# =================================================================
if __name__ == "__main__":
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    DATASET_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset"))

    # --------------------
    # USER CHOOSES FOLDER
    # --------------------
    FOLDER = input("Choose dataset to run (A, B, C, or D): ").strip().upper()

    if FOLDER not in ["A", "B", "C", "D"]:
        print("❌ Invalid choice! Use A, B, C, or D.")
        exit()

    mapping_path = os.path.join(DATASET_ROOT, FOLDER, "mapping_hardcoded.json")
    frames_root = os.path.dirname(mapping_path)

    if not os.path.exists(mapping_path):
        print(f"❌ No mapping_hardcoded.json found for folder {FOLDER}")
        exit()

    # --------------------
    # LOAD DATASET
    # --------------------
    print(f"\n📂 Loading dataset {FOLDER} ...")
    with open(mapping_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    # --------------------
    # LOAD MODEL ONCE
    # --------------------
    print("🧠 Loading model...")
    model = RivianModel()
    model.load_state_dict(torch.load(os.path.join(SCRIPT_DIR, "model.pth")))
    model.eval()

    # --------------------
    # OUTPUT FILE
    # --------------------
    out_file = os.path.join(SCRIPT_DIR, f"{FOLDER}_predictions.txt")
    print(f"📄 Output: {out_file}\n")

    # --------------------
    # GLOBAL STATE + STRICT 10-IN-A-ROW LOGIC
    # --------------------
    global_mood = None
    global_scene = None

    mood_streak_value = None
    mood_streak_count = 0

    scene_streak_value = None
    scene_streak_count = 0

    CHANGE_THRESHOLD = 10     # 10 consecutive frames required

    # --------------------
    # REAL-TIME LOOP
    # --------------------
    limit = min(1000, len(data))

    with open(out_file, "w", encoding="utf-8") as out:
        out.write(f"=== Real-time predictions for dataset {FOLDER} ===\n\n")

        for i in range(limit):
            entry = data[i]

            # Get prediction
            mood, scene = predict_entry(entry, frames_root, model)

            # ----------------------------------------------------------------
            #                       MOOD TRACKING
            # ----------------------------------------------------------------
            if mood_streak_value != mood:
                mood_streak_value = mood
                mood_streak_count = 1
            else:
                mood_streak_count += 1

            if mood_streak_count == CHANGE_THRESHOLD:
                if global_mood != mood_streak_value:
                    if global_mood is not None:
                        print(f"[MOOD CHANGE] {global_mood} → {mood_streak_value}")
                        out.write(f"[MOOD CHANGE] {global_mood} → {mood_streak_value}\n")
                    global_mood = mood_streak_value

            # ----------------------------------------------------------------
            #                       SCENE TRACKING
            # ----------------------------------------------------------------
            if scene_streak_value != scene:
                scene_streak_value = scene
                scene_streak_count = 1
            else:
                scene_streak_count += 1

            if scene_streak_count == CHANGE_THRESHOLD:
                if global_scene != scene_streak_value:
                    if global_scene is not None:
                        print(f"[SCENE CHANGE] {global_scene} → {scene_streak_value}")
                        out.write(f"[SCENE CHANGE] {global_scene} → {scene_streak_value}\n")
                    global_scene = scene_streak_value

            # ----------------------------------------------------------------
            # Print live frame prediction
            # ----------------------------------------------------------------
            line = f"{i:03d} | {entry['frame']} -> {mood} / {scene}"
            print(line)
            out.write(line + "\n")
            out.flush()

            # Simulate real-time frame rate
            time.sleep(0.33)

    print(f"\n🎉 Finished real-time prediction for {FOLDER}! Output saved.\n")

import os
import json
import time
import threading

from flask import Flask, request
from flask_socketio import SocketIO
from PIL import Image
import torch
from torchvision import transforms
import cv2  # for slideshow

from train import DriveModel

# ============================================================
#                    FLASK + SOCKET.IO SETUP
# ============================================================
app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key-for-hackathon!'

socketio = SocketIO(
    app,
    cors_allowed_origins="*",
    async_mode='threading',   # no eventlet, pure threading
    logger=True,
    engineio_logger=True,
    ping_timeout=60,
    ping_interval=25
)

print("=" * 60)
print("🚀 Drive Sense - Flask-SocketIO Server + Inference")
print("=" * 60)


@socketio.on('connect')
def handle_connect():
    print('=' * 60)
    print('✅ CLIENT CONNECTED!')
    print(f'   Client ID: {request.sid}')
    print(f'   Remote: {request.environ.get("REMOTE_ADDR")}')
    print('=' * 60)


@socketio.on('disconnect')
def handle_disconnect():
    print('=' * 60)
    print('❌ CLIENT DISCONNECTED!')
    print(f'   Client ID: {request.sid}')
    print('=' * 60)


@socketio.on('connect_error')
def handle_connect_error(data):
    print(f'⚠️  Connection error: {data}')


# ============================================================
#                    MODEL / INFERENCE SETUP
# ============================================================
MOOD_LABELS = ["Relaxed", "Focused", "Stressed", "Tired", "Distracted"]
SCENE_LABELS = ["City", "Highway", "Forest", "Garage", "Offroad", "Traffic"]

tf = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor()
])


def predict_entry(entry, frame_dir, model):
    """
    Predict mood + scene for one frame.
    entry["frame"] should be a relative path inside frame_dir.
    """
    img_path = os.path.join(frame_dir, entry["frame"])
    img = Image.open(img_path).convert("RGB")
    img = tf(img).unsqueeze(0)

    model.eval()
    with torch.no_grad():
        mood_logits, scene_logits = model(img)

    mood_idx = mood_logits.argmax(dim=1).item()
    scene_idx = scene_logits.argmax(dim=1).item()

    return MOOD_LABELS[mood_idx], SCENE_LABELS[scene_idx]


def inference_loop(data, frames_root, model, folder_name):
    """
    Real inference loop (A–D):
    - iterates over frames
    - does inference
    - applies 10-in-a-row hysteresis for mood & scene
    - shows a slideshow of frames with mood/scene overlay (OpenCV)
    - sends Socket.IO 'driver_state' event ONLY when stable mood/scene change
    - flashes 'SENT TO APP' on the slideshow for ~2s after each emit
    """
    script_dir = os.path.dirname(os.path.abspath(__file__))
    out_file = os.path.join(script_dir, f"{folder_name}_predictions.txt")
    print(f"📄 Inference output log: {out_file}\n")

    # --------------------
    # GLOBAL STABLE STATE + STREAK LOGIC
    # --------------------
    global_mood = None     # stable mood after hysteresis
    global_scene = None    # stable scene after hysteresis

    mood_streak_value = None
    mood_streak_count = 0

    scene_streak_value = None
    scene_streak_count = 0

    CHANGE_THRESHOLD = 10  # require 10 consecutive frames for new state

    # Keep track of last sent stable state to avoid duplicate emits
    last_sent_mood = None
    last_sent_scene = None
    last_emit_time = None  # timestamp when we last sent to app

    limit = min(1000, len(data))

    with open(out_file, "w", encoding="utf-8") as out:
        out.write(f"=== Real-time predictions for dataset {folder_name} ===\n\n")

        for i in range(limit):
            entry = data[i]

            # --------------------
            # 1) Instant prediction
            # --------------------
            mood, scene = predict_entry(entry, frames_root, model)

            # ============================================================
            #                       MOOD TRACKING
            # ============================================================
            if mood_streak_value != mood:
                mood_streak_value = mood
                mood_streak_count = 1
            else:
                mood_streak_count += 1

            if mood_streak_count == CHANGE_THRESHOLD:
                if global_mood != mood_streak_value:
                    if global_mood is not None:
                        change_line = f"[MOOD CHANGE] {global_mood} → {mood_streak_value}"
                        print(change_line)
                        out.write(change_line + "\n")
                    global_mood = mood_streak_value

            # ============================================================
            #                       SCENE TRACKING
            # ============================================================
            if scene_streak_value != scene:
                scene_streak_value = scene
                scene_streak_count = 1
            else:
                scene_streak_count += 1

            if scene_streak_count == CHANGE_THRESHOLD:
                if global_scene != scene_streak_value:
                    if global_scene is not None:
                        change_line = f"[SCENE CHANGE] {global_scene} → {scene_streak_value}"
                        print(change_line)
                        out.write(change_line + "\n")
                    global_scene = scene_streak_value

            # ============================================================
            # 2) Log instant prediction (debug)
            # ============================================================
            line = f"{i:03d} | {entry['frame']} -> {mood} / {scene}"
            print(line)
            out.write(line + "\n")
            out.flush()

            # ============================================================
            # 3) SHOW SLIDESHOW FRAME (OpenCV)
            # ============================================================
            img_path = os.path.join(frames_root, entry["frame"])
            frame_bgr = cv2.imread(img_path)

            if frame_bgr is not None:
                # Overlay mood/scene text
                overlay_text = f"{mood} / {scene}"
                cv2.putText(
                    frame_bgr,
                    overlay_text,
                    (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    (0, 255, 0),
                    2,
                    cv2.LINE_AA
                )

                # If we sent a message in the last 2 seconds, flash a label
                if last_emit_time is not None and (time.time() - last_emit_time) < 2.0:
                    cv2.putText(
                        frame_bgr,
                        "SENT TO APP",
                        (10, 70),
                        cv2.FONT_HERSHEY_SIMPLEX,
                        1.0,
                        (0, 0, 255),   # red
                        3,
                        cv2.LINE_AA
                    )

                cv2.imshow("Drive Sense - Frames", frame_bgr)
                # waitKey is needed for imshow to update; 1 ms is enough
                cv2.waitKey(1)
            else:
                print(f"⚠️ Could not read image at path: {img_path}")

            # ============================================================
            # 4) SEND TO ANDROID ONLY WHEN STABLE STATE CHANGES
            # ============================================================
            if global_mood is not None and global_scene is not None:
                if global_mood != last_sent_mood or global_scene != last_sent_scene:
                    payload = {
                        "mood": global_mood,
                        "scene": global_scene,
                        "frame_index": i,
                        "frame": entry["frame"]
                    }
                    print(f"📤 Emitting 'driver_state' to clients: {payload}")
                    socketio.emit('driver_state', payload, namespace='/')

                    last_sent_mood = global_mood
                    last_sent_scene = global_scene
                    last_emit_time = time.time()  # mark send time

            # Simulate real-time frame rate
            time.sleep(0.33)

    cv2.destroyAllWindows()
    print(f"\n🎉 Finished real-time prediction for {folder_name}! Output saved.\n")


def fake_inference_loop_from_txt(txt_path, folder_name):
    """
    Fake mode (E):
    - reads lines from E_metadata.txt
    - each line has format like: `121 | frame_121.jpg -> Relaxed / City`
    - forwards line-by-line as if it was a real run
    - applies SAME 10-in-a-row hysteresis on mood/scene
    - sends Socket.IO 'driver_state' events in the SAME format
    """

    script_dir = os.path.dirname(os.path.abspath(__file__))
    out_file = os.path.join(script_dir, f"{folder_name}_fake_predictions.txt")
    print(f"📄 Fake mode output log: {out_file}\n")

    if not os.path.exists(txt_path):
        print(f"❌ Fake metadata file not found: {txt_path}")
        return

    with open(txt_path, "r", encoding="utf-8") as f_in:
        lines = [ln.strip() for ln in f_in.readlines() if ln.strip()]

    # --------------------
    # GLOBAL STABLE STATE + STREAK LOGIC (same as real)
    # --------------------
    global_mood = None
    global_scene = None

    mood_streak_value = None
    mood_streak_count = 0

    scene_streak_value = None
    scene_streak_count = 0

    CHANGE_THRESHOLD = 10

    last_sent_mood = None
    last_sent_scene = None
    last_emit_time = None  # not used visually here, but kept for parity

    with open(out_file, "w", encoding="utf-8") as out:
        out.write(f"=== FAKE run from {txt_path} ===\n\n")

        for raw in lines:
            # Expect format: "121 | frame_121.jpg -> Relaxed / City"
            try:
                left, right = raw.split("->")
                left = left.strip()
                right = right.strip()

                # left: "121 | frame_121.jpg"
                idx_part, frame_part = left.split("|")
                frame_index = int(idx_part.strip())
                frame_name = frame_part.strip()

                # right: "Relaxed / City"
                mood_str, scene_str = right.split("/")
                mood = mood_str.strip()
                scene = scene_str.strip()
            except Exception as e:
                print(f"⚠️ Could not parse line: {raw}  (error: {e})")
                continue

            # --------------------
            # Hysteresis logic (same as real)
            # --------------------
            # MOOD
            if mood_streak_value != mood:
                mood_streak_value = mood
                mood_streak_count = 1
            else:
                mood_streak_count += 1

            if mood_streak_count == CHANGE_THRESHOLD:
                if global_mood != mood_streak_value:
                    if global_mood is not None:
                        change_line = f"[MOOD CHANGE] {global_mood} → {mood_streak_value}"
                        print(change_line)
                        out.write(change_line + "\n")
                    global_mood = mood_streak_value

            # SCENE
            if scene_streak_value != scene:
                scene_streak_value = scene
                scene_streak_count = 1
            else:
                scene_streak_count += 1

            if scene_streak_count == CHANGE_THRESHOLD:
                if global_scene != scene_streak_value:
                    if global_scene is not None:
                        change_line = f"[SCENE CHANGE] {global_scene} → {scene_streak_value}"
                        print(change_line)
                        out.write(change_line + "\n")
                    global_scene = scene_streak_value

            # Log the line as we replay it
            line = f"{frame_index:03d} | {frame_name} -> {mood} / {scene}"
            print(line)
            out.write(line + "\n")
            out.flush()

            # Emit to client when stable state changes (same condition)
            if global_mood is not None and global_scene is not None:
                if global_mood != last_sent_mood or global_scene != last_sent_scene:
                    payload = {
                        "mood": global_mood,
                        "scene": global_scene,
                        "frame_index": frame_index,
                        "frame": frame_name
                    }
                    print(f"📤 [FAKE] Emitting 'driver_state' to clients: {payload}")
                    socketio.emit('driver_state', payload, namespace='/')

                    last_sent_mood = global_mood
                    last_sent_scene = global_scene
                    last_emit_time = time.time()

            # Simulate real-time streaming
            time.sleep(0.33)

    print(f"\n🎉 Finished FAKE replay from {txt_path}! Output saved.\n")


# ============================================================
#                          MAIN
# ============================================================
if __name__ == '__main__':
    script_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_root = os.path.abspath(os.path.join(script_dir, "..", "dataset"))

    folder = input("Choose dataset to run (A, B, C, D, or E): ").strip().upper()

    if folder in ["A", "B", "C", "D"]:
        # --------------------
        # REAL DATA PATHS
        # --------------------
        mapping_path = os.path.join(dataset_root, folder, "mapping_hardcoded.json")
        frames_root = os.path.dirname(mapping_path)

        if not os.path.exists(mapping_path):
            print(f"❌ No mapping_hardcoded.json found for folder {folder}")
            raise SystemExit(1)

        print(f"\n📂 Loading dataset {folder} ...")
        with open(mapping_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        print("🧠 Loading model...")
        model_path = os.path.join(script_dir, "model.pth")
        if not os.path.exists(model_path):
            print(f"❌ model.pth not found at: {model_path}")
            raise SystemExit(1)

        model = DriveModel()
        model.load_state_dict(torch.load(model_path, map_location="cpu"))
        model.eval()
        print("✅ Model loaded!\n")

        # Start real inference thread
        inf_thread = threading.Thread(
            target=inference_loop,
            args=(data, frames_root, model, folder),
            daemon=True
        )
        inf_thread.start()

    elif folder == "E":
        # --------------------
        # FAKE MODE FROM TXT
        # --------------------
        txt_path = os.path.join(script_dir, "E_metadata.txt")
        print(f"\n📂 Starting FAKE replay from: {txt_path}\n")

        inf_thread = threading.Thread(
            target=fake_inference_loop_from_txt,
            args=(txt_path, folder),
            daemon=True
        )
        inf_thread.start()
    else:
        print("❌ Invalid choice! Use A, B, C, D, or E.")
        raise SystemExit(1)

    # --------------------
    # START SERVER
    # --------------------
    print(f"\n🌐 Server running on:")
    print(f"   - http://127.0.0.1:5000")
    print(f"   - http://0.0.0.0:5000")
    print("=" * 60 + "\n")

    socketio.run(
        app,
        host='0.0.0.0',
        port=5000,
        allow_unsafe_werkzeug=True
    )

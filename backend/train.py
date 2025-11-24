import os
import json
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from torchvision import models, transforms
from PIL import Image

# -------------------------------
# Paths
# -------------------------------
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "dataset"))

FOLDERS = ["A", "B", "C", "D"]

# -------------------------------
# Model Definition
# -------------------------------
class RivianModel(nn.Module):
    def __init__(self, num_moods=5, num_scenes=6):
        super().__init__()

        self.base = models.resnet18(weights=None)
        self.base.fc = nn.Identity()  # remove final classifier

        self.mood_head = nn.Linear(512, num_moods)
        self.scene_head = nn.Linear(512, num_scenes)

    def forward(self, x):
        features = self.base(x)
        mood_logits = self.mood_head(features)
        scene_logits = self.scene_head(features)
        return mood_logits, scene_logits


# -------------------------------
# Dataset Loader
# -------------------------------
class RivianDataset(Dataset):
    def __init__(self, mapping_file, frame_dir):
        with open(mapping_file, "r") as f:
            self.data = json.load(f)

        self.frame_dir = frame_dir

        self.tf = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor()
        ])

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        entry = self.data[idx]

        img_path = os.path.join(self.frame_dir, entry["frame"])
        img = Image.open(img_path).convert("RGB")
        img = self.tf(img)

        mood = entry["mood_label"]
        scene = entry["scene_label"]

        return img, mood, scene


# -------------------------------
# Load all datasets into one
# -------------------------------
def load_all_datasets():
    datasets = []

    for folder in FOLDERS:
        folder_path = os.path.join(DATASET_ROOT, folder)
        mapping_path = os.path.join(folder_path, "mapping_hardcoded.json")

        if not os.path.exists(mapping_path):
            print(f"⚠ Missing mapping_hardcoded.json for {folder}, skipping")
            continue

        datasets.append(RivianDataset(mapping_path, folder_path))

    # Combine datasets
    return torch.utils.data.ConcatDataset(datasets)


# -------------------------------
# Training
# -------------------------------
if __name__ == "__main__":
    dataset = load_all_datasets()
    loader = DataLoader(dataset, batch_size=16, shuffle=True)

    model = RivianModel()
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)
    criterion = nn.CrossEntropyLoss()

    EPOCHS = 4

    for epoch in range(EPOCHS):
        running_loss = 0.0

        for imgs, mood, scene in loader:
            optimizer.zero_grad()

            mood_logits, scene_logits = model(imgs)

            loss_mood = criterion(mood_logits, mood)
            loss_scene = criterion(scene_logits, scene)
            loss = loss_mood + loss_scene

            loss.backward()
            optimizer.step()

            running_loss += loss.item()

        print(f"Epoch {epoch} | Loss: {running_loss:.4f}")

    # Save model
    out_path = os.path.join(SCRIPT_DIR, "model.pth")
    torch.save(model.state_dict(), out_path)
    print(f"✅ Saved model to {out_path}")

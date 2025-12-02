import json
import os
from torch.utils.data import Dataset
from PIL import Image
import torch


class DriveDataset(Dataset):
    def __init__(self, mapping_path, transform=None):
        self.mapping_path = mapping_path

        with open(mapping_path, "r") as f:
            self.data = json.load(f)

        self.frames_root = os.path.dirname(mapping_path)
        self.transform = transform

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        entry = self.data[idx]

        # Image
        frame_file = os.path.join(self.frames_root, entry["frame"])
        img = Image.open(frame_file).convert("RGB")
        if self.transform:
            img = self.transform(img)

        # Metadata vector
        m = entry["metadata"]
        meta_vector = torch.tensor([
            m["altitude"],
            m["displaySpeed"],
            m["pitchAngle"],
            m["rollAngle"],
            m["powerMeter"],
            m["regenCapabilityPct"],
            m["propulsionCapabilityPct"],
            m["latitude"],
            m["longitude"],
        ], dtype=torch.float32)

        mood_label = torch.tensor(entry["mood_label"], dtype=torch.long)
        scene_label = torch.tensor(entry["scene_label"], dtype=torch.long)

        return img, meta_vector, mood_label, scene_label

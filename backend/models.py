import torch.nn as nn
import torchvision.models as models
import torch


class ImageEncoder(nn.Module):
    def __init__(self):
        super().__init__()
        base = models.efficientnet_b0(weights="DEFAULT")
        self.feature_extractor = nn.Sequential(
            base.features,
            nn.AdaptiveAvgPool2d(1)
        )
        self.fc = nn.Linear(1280, 256)

    def forward(self, x):
        x = self.feature_extractor(x)
        x = x.flatten(1)
        return self.fc(x)


class MetadataEncoder(nn.Module):
    def __init__(self, input_dim=9):
        super().__init__()
        self.model = nn.Sequential(
            nn.Linear(input_dim, 32),
            nn.ReLU(),
            nn.Linear(32, 64)
        )

    def forward(self, x):
        return self.model(x)


class DriveModel(nn.Module):
    def __init__(self, num_mood_classes=5, num_scene_classes=5):
        super().__init__()
        self.image_encoder = ImageEncoder()
        self.meta_encoder = MetadataEncoder()

        fused_dim = 256 + 64

        # Head 1: driver mood
        self.mood_head = nn.Sequential(
            nn.Linear(fused_dim, 128),
            nn.ReLU(),
            nn.Linear(128, num_mood_classes)
        )

        # Head 2: scene type
        self.scene_head = nn.Sequential(
            nn.Linear(fused_dim, 128),
            nn.ReLU(),
            nn.Linear(128, num_scene_classes)
        )

    def forward(self, img, meta):
        img_f = self.image_encoder(img)
        meta_f = self.meta_encoder(meta)
        fused = torch.cat([img_f, meta_f], dim=1)

        mood_logits = self.mood_head(fused)
        scene_logits = self.scene_head(fused)

        return mood_logits, scene_logits

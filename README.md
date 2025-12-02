# Drive Sense

Drive Sense is an intelligent, context-aware Android application designed to enhance driver safety and comfort. By analyzing real-time video feeds and vehicle telemetry, the system detects the driver's emotional state and the surrounding environment to provide tailored, proactive assistance.

## Features

- **Real-time Mood Detection**: Utilizes computer vision to classify driver states (e.g., Nervous, Tired, Neutral) in real-time.
- **Environmental Context Awareness**: Identifies the driving environment (Highway, City, Forest, Parking) to adjust recommendations accordingly.
- **Smart Action Engine**: Prioritizes safety and comfort recommendations based on the combined context of driver mood and road conditions.
    - *Example*: If a driver is detected as "Tired" on a "Highway", the system prioritizes suggesting the nearest rest stop.
- **Safety-First UX**: Non-critical notifications and reminders are queued and displayed only when the vehicle comes to a complete stop.
- **Gamification**: Integrated badge system to encourage safe driving habits and usage of wellness features.

## System Architecture

The project consists of a Python-based backend for inference and a native Android frontend.

### AI Backend
The core of the system is a dual-head deep learning model based on **EfficientNet-B0**.
- **Input**: 224x224 RGB video frames and vehicle telemetry (speed, pitch, roll, GPS).
- **Processing**:
    - **Vision Head**: Extracts features from video frames.
    - **Metadata Head**: Processes vehicle telemetry via a fully connected network.
    - **Fusion**: Combines visual and telemetry features to predict both **Mood** and **Scene** simultaneously.
- **Inference Server**: A Flask-SocketIO server handles real-time communication, broadcasting predictions to the mobile client via WebSockets.

### Android Application
The mobile application is built with **Kotlin** and **Jetpack Compose**, following the MVVM architecture.
- **Real-time Data**: Consumes WebSocket streams for immediate UI updates.
- **Contextual Logic**: A local `ContextualActionManager` determines the appropriate user interventions based on the stream data.
- **Tech Stack**:
    - **UI**: Material3 Design
    - **Concurrency**: Coroutines & Flow
    - **Networking**: Socket.IO Client

## Tech Stack

### Backend
- **Language**: Python 3.10+
- **ML Frameworks**: PyTorch, torchvision
- **Server**: Flask, Flask-SocketIO
- **Image Processing**: Pillow

### Mobile
- **Language**: Kotlin 1.9
- **UI Framework**: Jetpack Compose (Material3)
- **Build System**: Gradle

## Getting Started

### Prerequisites
- Python 3.10+
- Android Studio (for app deployment)
- Android Device or Emulator (API 34+)

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend/
   ```
2. Install dependencies:
   ```bash
   pip install torch torchvision flask-socketio pillow
   ```
3. Start the inference server:
   ```bash
   python inference.py
   ```
   The server will start on port 5000.

### App Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend/
   ```
2. Build and install the application:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   *Note: Ensure the Android app is configured to connect to your backend IP (default: `192.168.40.152:5000`).*

## Contributors

- [S-t-r-a-x](https://github.com/S-t-r-a-x)
- [jovan1414](https://github.com/jovan1414)
- [Marko-Milinkovic](https://github.com/Marko-Milinkovic)
- [MatejaMitic](https://github.com/MatejaMitic)
- [vuckovick](https://github.com/vuckovick)

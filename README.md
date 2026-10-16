# 🌱 PlantCare AI

> **A modern native Android plant health assistant powered by Google Gemma 4 E2B and Jetpack Compose.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.03.01-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![LiteRT-LM](https://img.shields.io/badge/Google%20Edge%20AI-Gemma%204%20E2B-green.svg)](https://ai.google.dev/edge)
[![Material 3](https://img.shields.io/badge/Design-Material%203-34A853.svg)](https://m3.material.io)

---

## 📖 Overview

**PlantCare AI** is a native Android application designed to assist gardeners, plant enthusiasts, and farmers in diagnosing and caring for their plants. Built like a clean ChatGPT-style conversational assistant, it allows users to take or upload plant photos, ask natural health questions, and receive detailed, nuanced diagnostic guidance directly on their device.

---

## ✨ Features

- 💬 **ChatGPT-Style Chatbot**: Clean conversational interface with streaming message bubbles and typing indicators.
- 📸 **Multimodal Photo Analysis**: Take a photo with the camera or choose one from your gallery to inspect leaf health.
- 🧠 **On-Device AI with Gemma 4 E2B**: Uses Google's recommended LiteRT-LM runtime for local, private on-device inference without third-party cloud APIs.
- 🔄 **Multi-Turn Dialogue Context**: Ask follow-up questions (e.g. *"Why are these leaves turning yellow?"* followed by *"I water it every day. Could that be the reason?"*).
- 🌿 **Epistemic Humility Guidelines**: Does not claim disease certainty from photos alone. Provides differential causes (watering, nutrients, light, fungal/bacterial pathogens) and recommends consulting local agricultural extensions for critical issues.
- 💾 **Full Offline History**: Multi-conversation management backed by Room SQLite database.
- ⚡ **Built-in Assistant Fallback**: Fully functional out of the box with an intelligent built-in plant health engine while local Gemma neural weights are being downloaded.
- ⚙️ **Settings & Model Manager**: Inspect model load status, adjust temperature and token generation, and rescan for model weights.

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin 2.3.20 |
| **UI Framework** | Jetpack Compose (BOM 2026.03.01) |
| **Design System** | Material 3 (Nature-inspired palette, dark mode support) |
| **Navigation** | AndroidX Navigation3 (1.0.1) |
| **AI Runtime** | Google LiteRT-LM (`com.google.ai.edge.litertlm:litertlm-android`) |
| **Database** | Room SQLite (2.7.1) + Kotlin Symbol Processing (KSP 2.3.12) |
| **Image Loading** | Coil Compose 3.2.0 |
| **Min SDK / Compile SDK** | Android 8.0 (API 26) / Android 16 (API 36) |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat / Quail or newer
- Android SDK 36 (target) / minSdk 26
- Java 17+

### Building and Running
1. Clone this repository:
   ```bash
   git clone https://github.com/airmon1101/PlantCare-AI.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync and click **Run** (`Shift + F10`) on your connected device or emulator.
   
Alternatively, from the command line:
```bash
./gradlew installDebug
```

---

## 🧠 Gemma 4 E2B Model Setup (Optional for Full Neural Weights)

The app works immediately out of the box in **Demo Mode** using the built-in plant care assistant. To enable the local **Gemma 4 E2B** neural weights:

1. Download the Gemma 4 E2B model file (`gemma-4-e2b-it.litertlm`) from [HuggingFace LiteRT Community](https://huggingface.co/litert-community).
2. Push the weights to the app's internal files directory:
   ```bash
   adb push gemma-4-e2b-it.litertlm /data/data/com.plantcareai/files/models/
   ```
3. Open the app's **Settings (⚙️)** screen and tap **Check / Reload Model Weights**.

---

## 📄 License

This project is licensed under the terms included in the [LICENSE](LICENSE) file.

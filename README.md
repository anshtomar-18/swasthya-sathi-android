# SwasthyaSathi AI — Native Android App 📱⚡

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin%201.9-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI Toolkit](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Performance](https://img.shields.io/badge/Performance-60%2B%20FPS%20GPU%20Optimized-00A896)](#performance--60-fps-optimizations)
[![Hackathon](https://img.shields.io/badge/SIH%202026-Problem%20Statement%2026181-FF6B4A)](https://www.sih.gov.in)
[![Privacy](https://img.shields.io/badge/Privacy-100%25%20On--Device%20Evaluation-035657)](#zero-telemetry-leakage-privacy-guarantee)

> **Smart India Hackathon 2026 — Problem Statement 26181**  
> **Personalized Environmental Health Monitoring & Early-Warning Mobile System**

---

## 📖 Overview

**SwasthyaSathi AI** is a native Android application designed to bridge the critical gap between macro-level meteorological alerts and individualized human physiological vulnerability. 

Standard weather forecasts report ambient temperature and city-wide air quality index (AQI). However, an AQI of 220 combined with a 42°C heat index creates drastically different threats for an elderly cardiovascular patient, an outdoor construction laborer, an asthmatic child, and a young student. 

SwasthyaSathi AI continuously evaluates local microclimate hazards against user-specific demographic and physiological sensitivities to deliver **deterministic, actionable, and real-time health protection protocols**.

---

## 🌟 Key Features

### 1. ⚡ 60 FPS Real-Time Physiological & Atmospheric Dashboard
- **Zero-Allocation Animated ECG Waveform**: Hardware-accelerated dual-layer neon canvas trace with lead pulse dot anchored to cardiac R-peaks. Zero memory allocations inside draw cycles ensure jitter-free 60/120 Hz rendering.
- **Ventricular Pulsating Heart**: 5-stage keyframe cardiac contraction physics (`1f → 1.22f → 1.05f → 1.25f → 1f`) dynamically driven by calculated heart workload.
- **Spring Physics Interactions (`bounceClick`)**: Subtle GPU layer scaling and tactile haptic feedback on all interactive touch targets.

### 2. 🎛️ 60 FPS Interactive Climate Stress Simulator
- **Live Scrubbing Sliders**: Interactively scrub Ambient Temperature ($20^\circ\text{C} - 52^\circ\text{C}$) and Particulate AQI ($25 - 500$).
- **Instantaneous Re-evaluation**: Recomputes health risk tier and alerts in **$<2\text{ms}$** right on device.
- **Quick Stress Presets**: 1-tap simulation of Heatwave (46°C), Severe Smog (450 AQI), Crisis (48°C, 480 AQI), and Clean Air (28°C).

### 3. 🧠 Interactive Symptom Triage Body Selector
- Tap-to-triage chips for immediate clinical guidance:
  - 🧠 **Headache / Migraine** (Thermal vasodilation & cranial tension)
  - 🫁 **Chest Wheezing** (Acute PM2.5 bronchial hyperreactivity)
  - ❤️ **Heart Palpitations** (Cardiovascular thermal workload strain)
  - 💧 **Heat Cramps & Fatigue** (Electrolyte deficit & fluid dosing)
  - 👁️ **Burning Airway & Eyes** (Toxic ozone and pollutant mucosal irritation)

### 4. 🛡️ 2-Step Emergency SOS Protocol
- Real-time simulation of encrypted distress packets containing GPS coordinates, ambient microclimate, and physiological risk status dispatched to designated caregivers.

### 5. 👥 Personalized Demographic & Sensitivity Profiling
- **Archetypes**: Pre-configured profiles for **Ramesh** (Outdoor laborer), **Devendra** (Senior citizen, cardiovascular), **Pooja** (Asthma/respiratory), and **Kabir** (Baseline student).
- **Custom Sensitivity Toggles**: Heat, Respiratory, Cardiovascular, and None.
- **Local Persistence**: Zero cloud storage; settings stay strictly on your device.

---

## 🔒 Zero-Telemetry-Leakage Privacy Guarantee

- **Deterministic Edge Computation**: The complete clinical risk calculation engine ([`RiskEngine.kt`](app/src/main/java/com/swasthyasathi/app/data/engine/RiskEngine.kt)) executes natively inside the Android client.
- **Zero Medical Data Upload**: Health sensitivities, emergency contact details, and vital baselines are never transmitted over the internet.
- **Offline Resilience**: Automatically falls back to cached thresholds when cellular data drops in rural or low-connectivity zones.

---

## 🏗️ Architecture & Technology Stack

```
com.swasthyasathi.app/
├── data/
│   ├── engine/          # On-device deterministic RiskEngine.kt
│   ├── model/           # Telemetry, Risk, Profile, and Chat data models
│   ├── network/         # Retrofit2 HTTP Client & Open-Meteo API definitions
│   └── repository/      # WeatherRepository & ProfileRepository
├── ui/
│   ├── components/      # EcgWaveform, VitalsCard, RiskCard, TelemetryCard, DisasterCard
│   ├── screens/         # DashboardScreen, OverviewScreen, ProfileScreen, AiAssistantSheet, SosDialog
│   └── theme/           # Clinical Material 3 color tokens, typography & shape
└── viewmodel/           # HealthViewModel (StateFlow, Coroutines, State Management)
```

- **Language**: Kotlin 1.9.0+
- **UI Toolkit**: Jetpack Compose with Material 3
- **Asynchronous Architecture**: Kotlin Coroutines & `StateFlow`
- **Networking**: Retrofit 2.9 + OkHttp3 + Gson
- **Hardware Acceleration**: Compose `Canvas`, `graphicsLayer`, and Android `Vibrator` / `HapticFeedback`
- **Target SDK**: Android 14 (API 34) | **Min SDK**: Android 8.0 (API 26)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Hedgehog / Iguana / Jellyfish** or **VS Code** with Android extensions.
- **JDK 17** or higher.
- **Android SDK Platform 34** and **Build-Tools 34.0.0**.
- An Android device or emulator running **Android 8.0 (API 26)+**.

---

### Building & Running via Command Line

1. **Clone the repository**:
   ```bash
   git clone https://github.com/anshtomar-18/swasthya-sathi-android.git
   cd swasthya-sathi-android
   ```

2. **Configure Android SDK location**:
   Create a `local.properties` file in the root folder with the path to your Android SDK:
   ```properties
   # Windows example:
   sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk

   # macOS/Linux example:
   sdk.dir=/Users/<YourUsername>/Library/Android/sdk
   ```

3. **Build Debug APK**:
   ```bash
   # Windows:
   .\gradlew.bat assembleDebug

   # macOS / Linux:
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. **Install & Run on Connected Device / Emulator**:
   ```bash
   # Windows:
   .\gradlew.bat installDebug

   # macOS / Linux:
   ./gradlew installDebug
   ```

---

### Opening in Android Studio

1. Open Android Studio.
2. Select **Open** and choose this repository directory (`swasthya-sathi-android`).
3. Let Gradle sync dependencies.
4. Select your target device and click **Run (Shift + F10)**.

---

## 🌐 Companion Python Backend (Optional)

For real-time AI triage queries (`/ask`) and verified microclimate caching, the companion FastAPI backend can be launched from the companion backend folder:

```bash
cd backend
pip install -r requirements.txt
python run.py
```
Forward the port to your connected Android phone via ADB:
```bash
adb reverse tcp:8000 tcp:8000
```

---

## 👥 Contributors

- **Ansh Tomar** ([@anshtomar-18](https://github.com/anshtomar-18)) — Lead Developer
- **Smart India Hackathon 2026 Team** — Problem Statement 26181

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

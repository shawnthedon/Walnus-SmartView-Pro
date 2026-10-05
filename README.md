# SmartView Pro™ — Android Foundation

> **Product**: SmartView Pro™  
> **Ecosystem**: Walnus SmartCoat™ System  
> **Company**: Walnus Global Ventures  
> **Platform**: Android (Kotlin, Jetpack Compose, Material 3)  
> **Milestone**: SV-001 Greenfield Foundation  

---

## 1. Project Purpose

SmartView Pro™ is an intelligent construction-site surveying and project intelligence application engineered for high-precision field evaluation, spatial surface scanning, substrate classification, and automated coating estimations for the Walnus SmartCoat™ product family.

Task **SV-001** establishes the clean, production-grade Android foundation, core architecture, navigation graph, domain models, repository contracts, and testing infrastructure upon which future camera capture, ARCore spatial computing, and AI reasoning systems will be built.

---

## 2. Technology Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Navigation**: Jetpack Navigation Compose (`androidx.navigation.compose`)
- **Architecture**: Clean Architecture / MVVM with unidirectional data flow (UDF)
- **Concurrency**: Kotlin Coroutines & `StateFlow`
- **Build System**: Android Gradle Plugin (AGP 9.1.1) with Gradle Kotlin DSL (`.gradle.kts`)
- **Testing**: JUnit 4, Robolectric 4.16.1, AndroidX Compose UI Test
- **Target SDK**: Android 15 (API 36) / Minimum SDK: Android 7.0 (API 24)

---

## 3. Architecture & Project Structure

The project strictly adheres to separation of concerns:
```
PRESENTATION (Compose UI & ViewModels)
     ↓
DOMAIN (Entities & Business Contracts)
     ↓
DATA (Repository Abstractions)
     ↓
HARDWARE & SUBSYSTEM BOUNDARIES (Camera, Spatial, Geometry, AI, Reporting)
```

### Source Tree Organization

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Host Activity & lifecycle logger
└── smartview/
    ├── core/
    │   ├── di/
    │   │   └── SmartViewContainer.kt    # Dependency injection & service container
    │   ├── dispatchers/
    │   │   └── DispatcherProvider.kt    # Coroutine dispatcher abstractions
    │   ├── log/
    │   │   └── SmartViewLogger.kt       # Structured diagnostic logging
    │   └── result/
    │       └── AppResult.kt             # Sealed result monad
    ├── domain/
    │   └── model/
    │       └── ProjectModels.kt         # Project, Survey, and Measurement models
    ├── data/
    │   └── repository/
    │       ├── ProjectRepository.kt     # Project data access interface
    │       ├── SurveyRepository.kt      # Survey session data access interface
    │       └── InMemoryProjectRepository.kt # Foundational repository implementation
    ├── ui/
    │   ├── SmartViewApp.kt              # App shell & Navigation bar host
    │   ├── SmartViewViewModelFactory.kt # ViewModel factory
    │   ├── components/
    │   │   ├── SmartViewHeader.kt       # Standard technical header
    │   │   ├── TechnicalStatusBadge.kt  # System readiness status badge
    │   │   └── ProfessionalCard.kt      # High-contrast bordered card
    │   ├── navigation/
    │   │   └── SmartViewDestination.kt  # Home, Projects, Survey, Settings routes
    │   ├── screens/
    │   │   ├── home/                    # Dashboard & "Start Survey" action
    │   │   ├── projects/                # Site directory & project creation
    │   │   ├── survey/                  # Survey engine placeholder (SV-002 boundary)
    │   │   └── settings/                # Field units & diagnostic settings
    │   └── theme/
    │       ├── Color.kt                 # Technical obsidian/slate & field cyan palette
    │       ├── Theme.kt                 # Dark/Light technical color schemes
    │       └── Type.kt                  # Technical sans-serif and monospace typography
    ├── camera/
    │   └── CameraContract.kt            # Boundary: Camera capture & lifecycle (SV-002)
    ├── spatial/
    │   └── SpatialContract.kt           # Boundary: ARCore session & tracking (SV-003+)
    ├── geometry/
    │   └── GeometryContract.kt          # Boundary: 3D point cloud & surface geometry
    ├── measurement/
    │   └── MeasurementContract.kt       # Boundary: Real-time distance, area & volume
    ├── construction/
    │   └── ConstructionContract.kt      # Boundary: SmartCoat™ coverage & loss factors
    ├── ai/
    │   └── AiIntelligenceContract.kt    # Boundary: Substrate defect classification
    └── reporting/
        └── ReportingContract.kt         # Boundary: PDF survey reports & BIM exports
```

---

## 4. Current Capabilities (SV-001)

1. **Application Shell**: Displays high-contrast field interface communicating SMARTVIEW PRO™ and Walnus Global branding.
2. **Navigation Graph**: Fully decoupled navigation between Home, Projects, Survey, and Settings.
3. **Primary Action**: Prominent "Start Survey" action guiding users into the survey workflow.
4. **Data Layer**: In-memory repository with real-time reactive flow updates for projects and surveys.
5. **Architectural Boundaries**: Explicit, formal interfaces reserving the camera, spatial, geometry, measurement, construction, AI, and reporting domains for subsequent milestones.
6. **Diagnostics**: Zero-leakage diagnostic logging (`SmartViewLogger`) tracking startup, navigation, and lifecycles.
7. **Accessibility & Design**: Material 3 compliance, 48dp touch targets, semantic test tags on all interactive elements.

---

## 5. Known Limitations & Future Work

As strictly mandated by SV-001 specification:
- **Camera Capture**: Not implemented in SV-001. Explicit architectural boundary reserved for **SV-002**.
- **ARCore / Spatial Tracking**: Not implemented in SV-001. Contract boundary reserved for **SV-003+**.
- **Calculations & AI**: Mock calculations and simulated sensor data are prohibited; real calculation engines will be integrated in subsequent tasks.

---

## 6. Verification & How to Build / Test

### Build Debug APK:
```bash
gradle :app:assembleDebug
```

### Run Unit and Robolectric Tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 7. Next Development Task

**SV-002 — Camera Subsystem & Real-Time Frame Capture Pipeline**
- Camera permission handling using Jetpack Compose Activity Result APIs
- CameraX / Camera2 lifecycle binding
- Low-latency preview viewfinder and frame analysis stream for computer vision

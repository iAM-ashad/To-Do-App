# FoxToDo

FoxToDo is a modern, clean, and fully offline Android to‑do application built using **Kotlin**, **Jetpack Compose**, **Hilt**, **Room**, and **MVVM with Clean Architecture principles**. It includes a rich calendar view, task management with priorities, categories, and a profile dashboard with analytics.

This README describes the architecture, features, modules, technology stack, and how to run and extend the project.

---

## ✨ Features

### ✔️ Task Management

* Add, edit, delete tasks.
* Optional description, category, date & time.
* Three priority levels (Low, Medium, High).
* Mark tasks complete/incomplete.
* Undo delete (via snackbar).

### 📅 Calendar View

* Custom paginated monthly calendar built with Compose.
* Task indicators (dots) for days containing tasks.
* Agenda list for the selected day.
* Separate tab for tasks without a date.

### 🏠 Home Screen

* Shows tasks filtered by the currently selected date.
* Filter by **All / Completed / Pending**.
* Daily progress insights.
* Swipe-to-delete with UNDO.

### 🔍 Task Detail Screen

* View and edit any task.
* Beautiful priority and status chips.
* Edit mode with date/time pickers.
* Delete confirmation dialog.

### 👤 Profile Screen

* User avatar & placeholder profile info.
* Weekly task completion analytics.
* Most-used categories.
* Dark mode toggle.
* Dynamic theme toggle.
* Placeholder “Notifications” & “Sign out” actions.

### 🎨 Theming

* Material 3 with dynamic color support.
* Light/Dark mode switch.
* Custom priority colors.

### 💾 Offline‑First Data Layer

* Local storage powered by **Room Database**.
* Repository pattern with domain model mapping.

---

## 🏗 Architecture Overview

FoxToDo uses a clean and modular architecture:

### **Data Layer**

* `Room` database (`AppDatabase`, `TaskDao`).
* `TaskEntity` is the local persistence model.
* Mappers convert between `TaskEntity` and domain `Task` models.
* Repository implementation: `TaskRepositoryImpl`.

### **Domain Layer**

* Contains pure data models (`Task`).
* Defines `TaskRepository` as an abstraction.

### **Presentation Layer**

* Each feature screen has:

  * `ViewModel` (state holder, business logic)
  * `UiState` data class
  * Composables for UI

Screens include:

* Home
* Add Task
* Task Detail
* Calendar
* Profile

### **Navigation**

* Jetpack Navigation Compose.
* Bottom bar navigation for Home, Calendar, Profile.
* Safe argument passing (e.g., taskId via route).

### **Dependency Injection**

* All core objects are provided through Hilt DI.

---

## 📂 Project Structure

```
app/
├── data/
│   ├── local/ (Room DB)
│   ├── mapper/ (Entity <-> Domain)
│   └── repository/
├── domain/
│   ├── model/
│   └── repository/
├── ui/
│   ├── add/
│   ├── calendar/
│   ├── detail/
│   ├── home/
│   ├── nav/
│   ├── profile/
│   ├── theme/
│   └── components/
└── di/
```

---

## 🔧 Tech Stack

### **Languages & Frameworks**

* Kotlin
* Jetpack Compose (Material 3)
* Navigation Compose
* Room
* Hilt (Dagger)
* ViewModel + StateFlow
* Kotlin Coroutines

### **Design & UX**

* Material You theming
* Dynamic Colors support
* Custom calendar UI

### **Build & Tooling**

* Gradle Kotlin DSL
* AndroidX BOM
* KSP (for Room & Hilt)

---

## ▶️ Running the Project

### Prerequisites

* Android Studio **Ladybug (or newer)**
* JDK 17+
* Android SDK 26+

### Build Steps

1. Clone the repo:

   ```bash
   git clone https://github.com/iAM-ashad/To-Do-App
   ```
2. Open in Android Studio.
3. Wait for Gradle sync.
4. Run the app on emulator or device.

---

## 🚀 Roadmap

Potential features to expand:

* Real reminders with WorkManager + Notifications.
* DataStore persistence for theme & profile settings.
* Categories management system.
* Cloud sync using Firebase.
* Weekly/Monthly analytics dashboard.
* Drag-and-drop task reordering.
* Widgets for quick task creation.

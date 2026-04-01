AXIOM — Personal Expense Intelligence

"Android" (https://img.shields.io/badge/Platform-Android-green?style=for-the-badge)
"Kotlin" (https://img.shields.io/badge/Kotlin-1.9-blue?style=for-the-badge)
"Jetpack Compose" (https://img.shields.io/badge/Jetpack-Compose-orange?style=for-the-badge)
"Architecture" (https://img.shields.io/badge/Architecture-MVVM-purple?style=for-the-badge)
"Offline" (https://img.shields.io/badge/Mode-Offline-critical?style=for-the-badge)

---

✦ Overview

AXIOM is a minimalist, behavior-driven expense tracker built for clarity, control, and discipline.

No clutter. No charts.
Just precise budgeting, real-time feedback, and accountability.

---

✦ Core Philosophy

«What you track shapes how you spend.»

AXIOM doesn’t just record expenses—it guides decisions through subtle constraints and intelligent feedback.

---

✦ Features

◉ Budget System

- Set a global monthly budget
- Allocate into custom categories
- Prevent over-allocation automatically

◉ Category Types

- DAILY → Adaptive per-day allowance
- VARIABLE → Flexible monthly cap
- ONE_TIME → Locks after completion

◉ Smart Daily Logic

- Dynamic daily allowance calculation
- Real-time “today remaining” tracking
- Soft warnings when exceeding limits

◉ Transaction System

- Add expenses with notes
- Full transaction history
- Long-press to delete (auto recalculates budget)

◉ Timeline History

- Clean date-wise grouping
- Minimal, readable structure
- No unnecessary noise

◉ Edit & Control

- Edit categories safely
- Budget integrity maintained
- Locked categories handled intelligently

◉ Monthly Reset Engine

- Auto reset at new month
- Preserves structure, clears usage

---

✦ UI & Experience

- Built with Jetpack Compose
- Fiery accent palette (orange / red spectrum)
- Subtle behavioral cues (color shifts, feedback)
- Designed for speed, not distraction

---

✦ Tech Stack

Layer| Technology
Language| Kotlin
UI| Jetpack Compose
Architecture| MVVM
Database| Room
Storage| DataStore
State Mgmt| StateFlow / Flow
Platform| Android (API 24+)

---

✦ Project Structure

axiom/
├── data/
│   ├── local/        # Room DB, DAO, Entities
│   ├── repository/   # Data abstraction layer
│
├── ui/
│   ├── screens/      # Home, History
│   ├── components/   # Cards, dialogs, sheets
│   ├── theme/        # Colors, typography
│
├── viewmodel/        # Business logic

---

✦ Key Highlights

- Fully offline-first
- Reactive UI with Flow
- Clean separation of concerns
- Designed for real-world usage, not demos

---

✦ Installation

git clone https://github.com/yourusername/axiom.git

Open in Android Studio → Run on device/emulator.

---

✦ Future Scope

- Home screen widget (quick expense entry)
- Smart spending insights
- Export / backup system
- Behavioral nudges

---

✦ Author

Parth
B.Tech ECE (IoT), IIIT Nagpur

---

✦ Closing Line

«Discipline is quiet. AXIOM is quieter.»

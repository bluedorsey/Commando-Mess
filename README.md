# Commando Mess

A modern Android app for managing mess (canteen) operations — built for hostel and institutional mess administrators who need to track student meals, manage employees, and keep financial records in one place.

## What It Does

Commando Mess replaces manual registers and spreadsheets with a real-time, Firebase-backed mobile app. It serves two types of users:

**For Admins**, the app provides a full dashboard to mark meal attendance by student ID, manage today's menu (breakfast, lunch, dinner), track remaining meal balances per student, handle plan renewals and payments, manage employees with salary and advance tracking, view attendance records by day/week/month with calendar navigation, and export reports as CSV files.

**For Users (Students)**, the app shows today's menu with meal timings at a glance.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose with animated transitions |
| Backend | Firebase Firestore (real-time sync) |
| Architecture | Repository pattern with Compose state |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Project Structure

```
app/src/main/java/com/example/mymess/
├── MainActivity.kt                  # Entry point, edge-to-edge setup, high refresh rate
├── screenalign.kt                   # NavHost with role-based bottom navigation
├── data/
│   └── StudentRepository.kt         # Firestore CRUD, real-time listeners, business logic
├── navigation/
│   └── Screen.kt                    # Route definitions
├── ui/auth/
│   ├── AuthViewModel.kt             # Login state management
│   └── LoginScreen.kt               # Admin/User login
├── ui_for_admin/
│   ├── AdminDashboardScreen.kt       # Attendance marking, menu editing, quick actions
│   ├── student_name.kt               # Student list with search
│   ├── Student_profile.kt            # Per-student profile, renewal, payment history
│   ├── EmployeeScreens.kt            # Employee CRUD, salary, advances
│   └── RecordsScreen.kt              # Calendar-based attendance records, CSV export
├── ui_for_user/
│   └── UserHomeScreen.kt             # Today's menu display
├── ui/theme/                          # Material 3 theme, colors, typography
└── utils/
    └── CsvExporter.kt                # Daily & master report CSV generation
```

## Key Features

**Meal Attendance System** — Admins enter a student's ID number to mark attendance for the current meal (auto-detected by time of day). The system validates against duplicate markings, remaining balance, and Sunday meal limits.

**Plan Management** — Each student has separate counters for breakfast, lunch, and dinner. Admins can renew plans with custom meal counts, and the app tracks payment records tied to each renewal.

**Employee Management** — Full employee lifecycle: add, edit, delete. Track monthly salary, record advance payments, revoke advances, and process salary payouts (net of advances).

**Records & Reports** — Browse attendance logs by day, week, or month using an interactive calendar. Search by student name. Export daily reports or master reports as CSV files saved to the device.

**Real-time Sync** — All data flows through Firestore snapshot listeners, so changes made on one device appear instantly on others.

**Menu Management** — Admins can update today's breakfast, lunch, and dinner menus in real-time. Students see the updated menu immediately.

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- A Firebase project with Firestore enabled

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/bluedorsey/Commando-Mess.git
   ```

2. Open the project in Android Studio.

3. Add your `google-services.json` file to the `app/` directory. You can generate this from the [Firebase Console](https://console.firebase.google.com/) after creating a project and adding an Android app with package name `com.example.mymess`.

4. In Firestore, the app expects the following collections (created automatically on first use):
   - `Student_detail` — student records
   - `Employee` — employee records
   - `employeeAdvances` — advance/salary payment logs
   - `payments` — student payment history
   - `attendanceLogs` — meal attendance entries
   - `TodaysMenu` — current menu items

5. Build and run on a device or emulator (API 24+).



## Pre-built APK

A release APK is available at `app/release/app-release.apk` for quick testing without building from source.

## License

This project does not currently specify a license. Contact the repository owner for usage permissions.

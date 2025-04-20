# HabbitTracker

A modern, feature-rich habit tracking application built with Jetpack Compose and Material Design 3.

![HabbitTracker](https://github.com/oxelmango/habbitTracker/blob/main/images/icon.png?raw=true)

## Features

- **Habit Management**: Create, track, and manage daily, weekly, or monthly habits
- **Category Organization**: Organize habits with customizable categories
- **Icon Selection**: Choose from 60+ icons for visual habit identification
- **Progress Tracking**: View completion history and statistics
- **Calendar View**: Track completion through an intuitive weekly calendar
- **Dark Mode**: Toggle between light and dark themes
- **Local Persistence**: All data persists across app restarts
- **Custom Start Dates**: Set habit tracking from any date
- **Notifications**: Configurable daily reminders
- **Profile Statistics**: View detailed progress and achievement metrics
- **Data Import/Export**: Backup and restore your habit data

## Architecture

HabbitTracker uses a modern Android architecture:

- **UI**: Jetpack Compose with Material Design 3
- **State Management**: ViewModel with StateFlow
- **Persistence**: DataStore for preferences and habit data
- **Serialization**: Kotlinx.serialization for JSON handling
- **Background Processing**: WorkManager for notifications
- **Navigation**: Simple state-based navigation

## Screenshots

<table>
  <tr>
    <td><img src="https://github.com/oxelmango/habbitTracker/blob/main/images/screen1.jpg?raw=true" width="180"/></td>
    <td><img src="https://github.com/oxelmango/habbitTracker/blob/main/images/screen2.jpg?raw=true" width="180"/></td>
    <td><img src="https://github.com/oxelmango/habbitTracker/blob/main/images/screen3.jpg?raw=true" width="180"/></td>
  </tr>
  <tr>
    <td>Habit Calendar</td>
    <td>Categories</td>
    <td>Profile</td>
  </tr>
</table>

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Kotlin 1.9.0 or later

### Installation

1. Clone this repository
   ```bash
   git clone https://github.com/username/HabbitTracker.git
   ```

2. Open the project in Android Studio

3. Sync Gradle files and build the project

4. Run on a device or emulator running Android 8.0 (API 26) or higher

## Development

### Project Structure

```
app/src/main/java/com/magic/habbittracker/
├── data/                  # Data handling and repositories
│   ├── HabitDataRepository.kt
│   ├── HabitIcons.kt
│   ├── MotivationalPhrases.kt
│   ├── NotificationHelper.kt
│   └── UserPreferencesRepository.kt
├── models/                # Domain models
│   └── HabitModel.kt
├── ui/                    # UI components
│   ├── components/        # Reusable composables
│   ├── screens/           # Main screens
│   └── theme/             # Theme configuration
├── viewmodels/            # ViewModels
│   ├── HabitViewModel.kt
│   └── HabitViewModelFactory.kt
├── MainActivity.kt        # Entry point
└── HabbitTrackerApplication.kt
```

## License

See the LICENSE file for details

## Acknowledgments

- Icons from Material Design Icons
- Motivational phrases from various sources
- Built with Jetpack Compose and Material 3

---

Made with ❤️ by Magic Team

# Myappai

Android app project for a media library and playlist manager built with Kotlin, Jetpack Compose, Material 3, and Firebase.

## Overview

This app lets users:
- sign in with Firebase authentication
- manage and organize media items
- create and edit playlists
- filter media by category or favorites
- search across titles, artists, notes, and tags
- open details for each item and add it to playlists

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Firebase Auth
- Firebase Firestore
- Gradle

## APK Release

The project includes a GitHub Actions workflow that builds a debug APK and publishes it as a GitHub release asset.

### Latest APK

Download the latest APK from the Releases page:

https://github.com/Chandandiji/Myappai/releases/latest

### Release workflow

The workflow file is:

- .github/workflows/build-apk.yml

It performs the following:
- sets up Java 17
- generates a temporary debug keystore
- builds the debug APK with Gradle
- verifies the APK exists and is valid
- uploads it as a GitHub Actions artifact
- creates/publishes a GitHub release with the APK attached

## Local Setup

### Prerequisites

- Android Studio
- JDK 17+
- Android SDK
- Firebase project configuration

### Build

```bash
gradle :app:assembleDebug
```

The output APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```text
.
├── app/
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/workflows/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── firebase.json
├── firestore.rules
├── .gitignore
└── README.md
```

## Notes

- This workflow is configured for a debug APK build for testing and distribution.
- It is not intended to replace a production Play Store release build.

# AgriPlus Interview Cheat Sheet

## 1-Minute Intro

AgriPlus is an Android app built with Kotlin and Jetpack Compose for detecting leaf health conditions from images. The app lets users sign in with Google using Firebase Authentication, upload or capture a leaf photo, send it for prediction, and view disease or nutrient-deficiency details such as cause, symptoms, and treatment. It also stores each scan in Firebase Realtime Database so users can view their history later.

## 30-Second Version

AgriPlus is a smart agriculture Android app that identifies leaf conditions from images. I built it using Kotlin, Jetpack Compose, Firebase Auth, Firebase Realtime Database, OkHttp, and a prediction pipeline that currently uses a remote API.

## 2-Minute Project Explanation

The app starts with a splash screen that checks whether the user is already logged in. If not, the user signs in with Google through Firebase Authentication. After login, the user reaches the home screen, where they can either upload a leaf image from the gallery or capture one with the camera.

Once an image is selected, the app converts it into a bitmap, compresses it, and sends it to a prediction endpoint using OkHttp multipart upload. The API returns a predicted class label and confidence score. The app validates the confidence and then maps the label to a richer agricultural information object that contains the disease name, cause, symptoms, and treatment.

After a successful scan, the result is stored in Firebase Realtime Database under the current user's UID. The history screen reads that data back, sorts it by timestamp, and lets users review, expand, and delete past scans.

## Main Technologies

- Kotlin
- Jetpack Compose
- Navigation Compose
- Firebase Authentication
- Firebase Realtime Database
- Google Sign-In
- OkHttp
- Coil
- Lottie
- TensorFlow Lite

## Main Features

- Splash screen with auth check
- Google login
- Upload image from gallery
- Capture image with camera
- Scan and predict leaf condition
- Show confidence
- Show cause, symptoms, and treatment
- Save scan history per user
- Delete single history item
- Clear all history

## Strong One-Liners for Interview

### What is this app?

AgriPlus is a Compose-based Android app for leaf disease and nutrient-deficiency detection with Firebase-based login and scan history.

### What problem does it solve?

It helps users detect leaf health conditions quickly and gives them understandable treatment guidance instead of just raw classification labels.

### What is the core technical flow?

Image input -> bitmap conversion -> API prediction -> label mapping -> Firebase history storage -> history display.

### What architecture does it use?

It is a single-activity Compose app with screen-level state management and helper classes for Firebase and networking.

### Is it fully offline?

No. The active scan flow is cloud-based. The project includes a local TensorFlow Lite classifier, but it is not currently integrated into the main scan screen.

## Most Important Truths to Remember

### Say this confidently

- The app supports both camera and gallery input.
- Each authenticated user has separate scan history.
- The app maps raw prediction labels into readable disease information.
- The history data is stored in Firebase Realtime Database.
- Google Sign-In is implemented through Firebase Authentication.

### Say this carefully

- The current prediction flow uses a remote API.
- `LeafClassifier.kt` exists, but the active screen flow does not use it yet.
- The app is not fully MVVM yet.
- Base64 image storage is simple, but not the most scalable design.

## User Flow

1. Splash screen checks auth state
2. User logs in with Google
3. User enters home screen
4. User uploads or captures image
5. App sends image for prediction
6. App gets class + confidence
7. App maps class to disease details
8. App saves result to Firebase
9. User opens history and reviews past scans

## File-by-File Memory Map

### `MainActivity.kt`

Navigation and app entry point

### `SplashScreen.kt`

Startup screen and auth-based routing

### `LoginScreen.kt`

Google Sign-In and Firebase Auth

### `HomeScreen.kt`

Main scan flow, image input, API call, result display, save to history

### `HistoryScreen.kt`

Shows saved scans and supports delete/clear

### `DiseaseInfo.kt`

Disease model and label-to-info mapping

### `HistoryItem.kt`

Saved result model

### `RealTimeManager.kt`

Firebase Realtime Database operations

### `ApiClient.kt`

Multipart upload to prediction API

### `LeafClassifier.kt`

Local TensorFlow Lite classifier, currently unused in active flow

## Best Strengths to Mention

- Real-world agriculture use case
- End-to-end working flow
- Good integration of Android, Firebase, and ML concepts
- User-friendly disease explanation layer
- Per-user cloud history

## Best Improvement Points to Mention

- Move to MVVM with ViewModels and repositories
- Integrate offline TFLite scanning fully
- Move image storage to Firebase Storage
- Replace manual JSON parsing with typed models
- Add stronger tests
- Clean duplicate dependencies and unused files

## If Interviewer Says "Explain Your Role"

You can say:

I designed and implemented the Android app flow, including authentication, image acquisition, prediction integration, result rendering, and history persistence. I also structured the disease mapping layer so raw classification outputs become meaningful treatment guidance for users.

## If Interviewer Says "What Was the Hardest Part?"

You can say:

The hardest part was connecting multiple layers cleanly: image input, prediction logic, authentication, and per-user history storage, while making the output understandable for non-technical users.

## If Interviewer Says "What Would You Improve Next?"

You can say:

I would refactor the project toward MVVM, fully integrate the local TensorFlow Lite classifier for offline support, move image storage to Firebase Storage, and add proper automated tests.

## 5 Quick Answers to Memorize

### Why Firebase Auth?

Because it simplifies secure login and gives a unique UID for user-specific history.

### Why Realtime Database?

Because it is simple for hierarchical per-user history storage and quick to integrate for a prototype.

### Why use Compose?

Because it gives a modern declarative UI model and reduces XML boilerplate.

### Why confidence threshold?

To avoid showing weak predictions as reliable results.

### Why disease mapping?

Because users need understandable advice, not only raw ML labels.

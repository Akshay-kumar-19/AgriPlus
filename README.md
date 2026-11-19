# AgriPlus

AgriPlus is an Android app built with Kotlin and Jetpack Compose for identifying plant leaf health conditions from images. A user signs in with Google, uploads or captures a leaf image, sends that image to a prediction service, sees a readable result with cause, symptoms, and treatment, and can review previous scans from history.

This README is meant to help you understand the project deeply enough to explain almost any part of it in an interview or project discussion.

## 1. Project Summary

At a high level, the app does these things:

1. Authenticates the user with Google Sign-In through Firebase Authentication.
2. Lets the user choose a leaf image from the gallery or capture one with the camera.
3. Sends the image to a prediction system.
4. Converts the raw prediction label into user-friendly disease or deficiency information.
5. Stores successful scan history in Firebase Realtime Database per user.

One strong short explanation is:

> AgriPlus is a Jetpack Compose Android app for plant leaf condition detection. Users sign in with Google, upload or capture a leaf image, the app predicts the condition using a remote inference API, maps the result into readable agricultural guidance, and stores scan history in Firebase Realtime Database.

## 2. Problem the App Solves

The app is built to help users detect plant leaf health issues earlier and more easily.

Instead of requiring the user to manually identify symptoms, the app provides:

- image-based input
- automated prediction
- confidence score
- readable condition details
- cause, symptoms, and treatment guidance
- saved scan history

So the app is not only a classifier. It is also a decision-support tool.

## 3. Main User Flow

The end-to-end flow is:

1. App opens on the splash screen.
2. Splash screen checks whether the user is already authenticated.
3. If not signed in, the user goes to the login screen.
4. User signs in with Google through Firebase Authentication.
5. User reaches the home screen.
6. User uploads a leaf image from gallery or captures one with the camera.
7. App converts the image into a bitmap and prepares it for prediction.
8. App sends the image to the prediction API.
9. API returns a class label and confidence score.
10. App validates the confidence.
11. App maps the label into readable disease information.
12. App shows the result on the home screen.
13. App saves the scan in Firebase Realtime Database.
14. User can open the history screen and review past scans.

## 4. Main Features

- Splash screen with startup animation and auth-state routing
- Google login using Firebase Authentication
- Gallery image selection
- Camera image capture
- Leaf condition prediction
- Confidence-based validation
- Readable output with cause, symptoms, and treatment
- User-specific history storage
- Delete one history item
- Clear all history

## 5. Technology Stack

### Language

- Kotlin

### UI

- Jetpack Compose
- Material 3
- Lottie Compose
- Coil

### Navigation

- Navigation Compose

### Authentication and Cloud

- Firebase Authentication
- Google Sign-In
- Firebase Realtime Database

### Networking

- OkHttp

### ML Related

- Remote prediction API
- TensorFlow Lite files also exist in the project for local inference groundwork

## 6. High-Level Architecture

This project is not a full MVVM architecture yet. It is closer to a small-to-medium Compose application with screen-level state management and helper classes.

The practical architecture looks like this:

### Presentation Layer

Compose screens such as:

- `SplashScreen.kt`
- `LoginScreen.kt`
- `HomeScreen.kt`
- `HistoryScreen.kt`

### Helper and Data Layer

- Firebase history manager
- disease information mapping
- network upload helper
- data models for saved results

### ML Layer

- active remote prediction flow
- local TensorFlow Lite classifier files exist, but are not fully wired into the current UI flow

### Important architecture truth

The app currently mixes UI logic and business logic more than an ideal production app should.

Examples:

- response parsing happens in the screen layer
- scan orchestration happens in `HomeScreen.kt`
- screen state is mostly managed with `remember`

So if an interviewer asks whether this is full clean architecture or full MVVM, the honest answer is no.

## 7. Screen-by-Screen Understanding

### Splash Screen

Purpose:

- gives a branded startup experience
- checks whether the user is already logged in
- redirects to `home` or `login`

Key concept:

- it uses Firebase auth state to decide the next screen

### Login Screen

Purpose:

- handles Google Sign-In
- exchanges Google account token for Firebase credentials
- routes user into the app after success

Key concept:

- Google Sign-In is the entry point, but Firebase Authentication is what maintains the app session

### Home Screen

Purpose:

- acts as the main feature screen
- shows user info
- accepts image input
- triggers prediction
- displays result
- saves scan history
- provides logout and navigation to history

Key concept:

This is the most important screen in the project because it orchestrates most of the app behavior.

### History Screen

Purpose:

- fetches saved scans for the current user
- sorts them by latest first
- displays result history
- supports expand, delete, and clear-all

Key concept:

- history is stored per authenticated user under Firebase Realtime Database

## 8. Authentication Flow

Authentication is implemented using Google Sign-In plus Firebase Authentication.

The flow is:

1. User taps Google Sign-In.
2. App launches the Google account picker.
3. Google returns the selected account token.
4. App converts that token into a Firebase credential.
5. Firebase signs the user in.
6. App navigates to the home screen.

Why this matters:

- Firebase gives a persistent user session
- Firebase gives a UID
- the UID is used to keep each user's history separate

## 9. Image Input Flow

The app supports two image sources:

### Gallery

- Uses `ActivityResultContracts.GetContent()`
- lets the user pick an image from storage

### Camera

- Uses `ActivityResultContracts.TakePicturePreview()`
- returns a preview bitmap directly

Important note:

`TakePicturePreview()` is simple, but it gives a lower-resolution image than capturing a full photo file. That is acceptable for a prototype, but full-resolution capture would usually be better for ML quality.

## 10. Prediction Flow

The current active prediction path is remote.

The flow is:

1. Selected image is converted into a bitmap.
2. Bitmap is compressed.
3. Image bytes are uploaded through OkHttp as multipart data.
4. Remote API returns a JSON response.
5. App extracts the predicted class and confidence.
6. App checks whether the confidence is strong enough.
7. App maps the raw label into readable disease information.

### Confidence threshold

The app uses a confidence threshold so weak predictions are not shown as reliable results.

This is important because:

- it reduces misleading output
- it gives a more trustworthy experience

### Disease mapping

The model output is not shown directly as-is.

Instead, the app uses a disease repository to convert a label into richer information:

- name
- cause
- symptoms
- treatment

This is one of the best concepts to explain in an interview, because it shows that the app adds real user value beyond raw prediction.

## 11. Remote API vs Local TensorFlow Lite

One of the most important project concepts is that the repo contains two prediction directions:

### Active now

- remote inference through an API

### Present in the repo but not fully active in the main flow

- local TensorFlow Lite model
- `model.tflite`
- `labels.txt`
- `LeafClassifier.kt`

### What this means

The project appears to have:

- current cloud-based prediction
- future or experimental offline prediction support

### Tradeoff explanation

Remote inference:

- easier to update the model centrally
- depends on internet
- exposes more backend dependency

Local inference:

- can support offline usage
- can improve privacy and reduce network dependence
- requires on-device preprocessing and model performance tuning

If asked why both exist, you can say the project has a migration path from cloud inference to on-device inference.

## 12. Data and Firebase Storage Design

The app stores history in Firebase Realtime Database.

Logical structure:

```json
users: {
  "uid123": {
    "history": {
      "pushKey1": {
        "result": "leaf_rust",
        "confidence": 0.91,
        "imageBase64": "...",
        "timestamp": 1712345678901
      }
    }
  }
}
```

### What is stored

Each saved history item contains:

- result label
- confidence score
- image encoded as Base64
- timestamp

### Why this design works

- simple to implement
- user data stays separated by UID
- easy to fetch all history for one user

### Weakness in current design

Base64 image storage in Realtime Database is not ideal for production because:

- it increases payload size
- it is less efficient than object storage
- database reads become heavier

Better design:

- upload images to Firebase Storage
- store only metadata and image URL in the database

## 13. Important Project Files

### `MainActivity.kt`

- app entry point
- sets Compose content
- defines navigation graph

### `SplashScreen.kt`

- startup animation
- auth-state check
- initial routing

### `LoginScreen.kt`

- Google Sign-In
- Firebase credential exchange
- login navigation

### `HomeScreen.kt`

- image selection and capture
- prediction request
- response parsing
- result display
- save history

### `HistoryScreen.kt`

- reads and displays user history
- supports delete and clear-all

### `DiseaseInfo.kt`

- data model for readable disease information

### `DiseaseRepository`

- maps raw labels into meaningful explanations

### `HistoryItem.kt`

- model for saved history records

### `RealtimeManager.kt`

- handles Firebase Realtime Database operations

### `ApiClient.kt`

- performs multipart upload to the prediction API

### `LeafClassifier.kt`

- local TensorFlow Lite classifier implementation
- not fully connected to the active scan screen

## 14. What `HomeScreen.kt` Really Does

If you want to explain the heart of the app, this is the file to understand best.

It handles:

- current user display
- image selection from gallery
- image capture from camera
- bitmap conversion
- image compression
- API upload
- response parsing
- confidence validation
- disease mapping
- result display
- saving to Firebase
- logout
- navigation to history

This is why `HomeScreen.kt` is both powerful and also a place where technical debt exists. It is doing too many responsibilities in one screen.

## 15. Strengths of the Project

These are good points to mention confidently:

- clear real-world use case
- complete user flow from login to history
- strong integration of Android UI, Firebase, networking, and prediction concepts
- user-friendly explanation layer instead of just raw labels
- support for both gallery and camera input
- per-user saved history

## 16. Weaknesses and Honest Improvement Areas

These are important because good interviews often reward honest engineering awareness.

### Architecture

- no proper ViewModels yet
- too much logic inside composables
- no clean repository separation

### Networking

- raw JSON parsing happens in the UI layer
- could use typed DTOs and cleaner abstraction

### Storage

- Base64 images in Realtime Database are inefficient

### Security

- API access should be protected better if secrets are present client-side

### Prediction Strategy

- remote and local inference both exist, but the app has not fully standardized on one path

### Testing

- meaningful automated tests are limited or absent

### Code Cleanup

- some files are unused or incomplete
- duplicate or unused dependencies may exist

## 17. Best Next Improvements

If asked what you would improve next, a strong answer is:

1. Refactor the app toward MVVM.
2. Move networking and Firebase operations into repositories.
3. Replace manual response parsing with typed models.
4. Move image storage to Firebase Storage.
5. Decide on one main prediction strategy: remote or full local TFLite.
6. Add proper unit tests and UI tests.
7. Clean unused files and duplicate dependencies.

## 18. Interview-Ready Answers

### What problem does the app solve?

It helps users identify plant leaf health conditions quickly and gives understandable guidance instead of only raw classification labels.

### What architecture does the app use?

It uses a single-activity Compose architecture with screen-level state management and helper classes. It is not full MVVM yet.

### How does authentication work?

The app uses Google Sign-In to get a Google account token, converts that to a Firebase credential, and Firebase maintains the signed-in session.

### How is history stored?

History is stored in Firebase Realtime Database under `users/{uid}/history`, so each authenticated user has separate scan records.

### Is the app offline?

Not fully. The active prediction flow is remote, but the project also contains a local TensorFlow Lite classifier as groundwork for offline inference.

### Why use a confidence threshold?

To avoid showing weak predictions as if they were reliable.

### Why is disease mapping important?

Because users need readable and actionable information such as cause, symptoms, and treatment, not just a raw model label.

## 19. Best One-Minute Explanation

AgriPlus is a Kotlin-based Android app built with Jetpack Compose for detecting plant leaf conditions from images. The user signs in with Google through Firebase Authentication, uploads or captures a leaf image, and the app sends that image to a prediction service. The returned class label and confidence are validated, then mapped into readable agricultural information such as disease name, cause, symptoms, and treatment. Successful scans are saved in Firebase Realtime Database per user, and the history screen lets users review previous scans. Architecturally, the project is a Compose-first app with screen-level state and helper classes, and the next big improvements would be MVVM refactoring, better storage design, and deciding between remote inference and full local TensorFlow Lite support.

## 20. Study Focus

If you want to explain almost anything in this project, focus on these first:

1. Understand the full user flow from splash to history.
2. Understand `HomeScreen.kt` deeply because it is the main orchestration file.
3. Understand Google Sign-In plus Firebase Authentication.
4. Understand how the app stores user history in Firebase Realtime Database.
5. Understand the difference between remote prediction and the local TFLite path.
6. Understand why the app maps raw labels into readable disease guidance.
7. Be ready to explain both strengths and weaknesses honestly.

## 21. Related Notes

For extra interview prep:

- `INTERVIEW_CHEATSHEET.md`
- `INTERVIEW_QA.md`

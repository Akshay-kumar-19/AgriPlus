# AgriPlus Interview Questions and Answers

## Project Basics

### 1. Tell me about your project.

AgriPlus is an Android app built with Kotlin and Jetpack Compose for detecting leaf health conditions from images. A user signs in with Google using Firebase Authentication, uploads or captures a leaf image, and the app sends that image to a prediction service. The prediction result is then mapped into user-friendly disease or nutrient-deficiency information such as cause, symptoms, and treatment. The app also stores scan history per user in Firebase Realtime Database.

### 2. What problem does your project solve?

It helps identify plant leaf conditions early and more easily. In agriculture, disease identification often depends on experience or expert help. This app speeds up first-level screening by using image-based prediction and then translating the result into readable treatment guidance.

### 3. Why did you choose this project?

I wanted to build something that combines mobile development with a real-world domain problem. Agriculture is a meaningful space, and this project let me work on Android UI, Firebase integration, image handling, and ML-related prediction flow in one application.

### 4. Who are the users of this application?

The intended users are farmers, agriculture students, field workers, or anyone who wants a quick first-level understanding of leaf health conditions from an image.

### 5. What is the one-line summary of your app?

It is a Kotlin + Jetpack Compose Android app for leaf disease and nutrient-deficiency detection with Firebase-based login and per-user history storage.

## Architecture and Design

### 6. What architecture does your project use?

The project currently uses a lightweight single-activity architecture with Compose screens and screen-level state management. Navigation is handled through Navigation Compose, and helper classes manage networking and Firebase operations. It is not a full MVVM architecture yet, but it can be evolved into one.

### 7. Why do you say it is not full MVVM?

Because the UI screens directly hold and manage most of the state using `remember`, and business logic like response parsing is still inside the screen layer. There are no ViewModels or repository abstractions coordinating state cleanly yet.

### 8. If you had more time, how would you improve the architecture?

I would introduce ViewModels for each screen, move networking and Firebase logic into repositories, use sealed UI state classes, replace raw callbacks with coroutines, and separate response parsing from the UI layer.

### 9. Why did you use a single activity?

Single-activity architecture works very well with Compose and Navigation Compose. It keeps navigation centralized and reduces the complexity of managing multiple activities.

### 10. Why did you use Jetpack Compose instead of XML layouts?

Compose gives a declarative UI model, reduces boilerplate, improves readability for state-driven screens, and speeds up iteration compared to traditional XML-based Android UI development.

## Flow and Screens

### 11. Explain the overall app flow.

The app starts on a splash screen, which checks the current Firebase auth session. If the user is already logged in, the app navigates directly to the home screen; otherwise it goes to login. On the login screen, the user signs in with Google. On the home screen, the user selects a leaf image from the gallery or camera and scans it. The app gets a prediction result, shows disease details, and saves that result into Firebase history. The history screen then displays all previous scans for that user.

### 12. What does the splash screen do?

It performs two jobs: branding and auth-state routing. It shows an animation, waits briefly, checks `FirebaseAuth.currentUser`, and navigates to either the login or home screen based on whether the user is authenticated.

### 13. What does the login screen do?

It handles Google Sign-In, receives the Google account token, converts it to a Firebase credential, signs the user into Firebase, and then navigates to the home screen.

### 14. What does the home screen do?

It is the core feature screen. It shows the signed-in user, supports gallery upload and camera capture, previews the selected image, sends the image for prediction, displays the disease result, and stores valid results in Firebase.

### 15. What does the history screen do?

It fetches the current user's scan history from Firebase Realtime Database, sorts the entries by timestamp, shows them in cards, lets the user expand a card to see image and details, and supports deleting one item or clearing all history.

## Authentication

### 16. How does authentication work in your app?

The app uses Google Sign-In with Firebase Authentication. On the login screen, the app creates `GoogleSignInOptions`, requests an ID token and email, launches the sign-in intent, gets the selected Google account, converts it to a Firebase credential using `GoogleAuthProvider`, and signs in using `FirebaseAuth.signInWithCredential`.

### 17. Why did you use Google Sign-In?

It simplifies login for users, reduces the need to manage passwords manually, and integrates smoothly with Firebase Authentication.

### 18. How do you identify each user's data?

After authentication, Firebase provides a unique user ID. The app stores history under `users/{uid}/history`, so each user's data is isolated.

### 19. What happens if the user is already logged in?

The splash screen detects that and routes the user directly to the home screen.

### 20. How does logout work?

The home screen calls `FirebaseAuth.signOut()` and then navigates back to the login screen.

## Image Handling

### 21. How does the app take an image from the camera?

It uses `ActivityResultContracts.TakePicturePreview()` through Compose's `rememberLauncherForActivityResult`. Before launching the camera, the app requests the `CAMERA` permission.

### 22. How does the app pick an image from the gallery?

It uses `ActivityResultContracts.GetContent()` with `"image/*"` so the user can select an image from available gallery providers.

### 23. Why do you convert the image into a Bitmap?

Because the scan flow needs image data in memory for compression and upload, and the local TensorFlow Lite classifier also expects bitmap-based preprocessing.

### 24. How do you handle different Android versions for image decoding?

[For Android P and above, the app uses `ImageDecoder`. For older ve]\s network efficiency and makes the request lighter.

## Prediction and ML

### 26. How does leaf prediction work in your current app?

The current active flow sends the image to a remote prediction API using multipart upload through OkHttp. The API returns a JSON response containing predictions, including the predicted class and confidence.

### 27. Where is the prediction response processed?

It is processed in `HomeScreen.kt`, where the JSON is parsed manually using `JSONObject`.

### 28. What do you do after getting the class label?

The app uses `DiseaseRepository.getInfo(label)` to convert the raw label into a richer `DiseaseInfo` object that contains a human-readable name, cause, symptoms, and treatment.

### 29. Why did you add a confidence threshold?

To avoid showing low-confidence predictions as trustworthy results. If confidence is below `0.60`, the app asks the user to retry with a clearer image.

### 30. Is your app fully offline?

No. The current active scan flow depends on a remote API. However, the project also contains a bundled TensorFlow Lite model and a local classifier class, which means it is partially prepared for offline inference.

### 31. Why is there a TensorFlow Lite model if the app uses a remote API?

That suggests the project either experimented with on-device inference or was designed with future offline support in mind. The local classifier class is implemented, but it is not currently integrated into the active scan path in `HomeScreen`.

### 32. Explain how `LeafClassifier.kt` works.

It loads `model.tflite` from assets, reads labels from `labels.txt`, resizes the bitmap to `300 x 300`, normalizes RGB values, writes them into a `ByteBuffer`, runs inference using TensorFlow Lite `Interpreter`, and returns the label with the highest confidence.

### 33. What are the classes your model or prediction system supports?

The labels include `healthy`, `leaf_rust`, `phoma`, `mite`, and multiple nutrient deficiencies like `nitrogen`, `phosphorus`, `potassium`, `calcium`, `iron`, `magnesium`, `manganese`, and `boron`.

### 34. Is it correct to call all outputs "diseases"?

Not fully. Some outputs are diseases and some are nutrient deficiencies, so a more accurate description is that the app detects leaf health conditions.

### 35. What is the difference between local inference and cloud inference in your app context?

Local inference would use the bundled TensorFlow Lite model directly on the device, which can support offline usage and lower latency after model loading. Cloud inference sends the image to a remote service, which can make model updates easier but depends on internet connectivity and server availability.

## Networking

### 36. What networking library did you use?

I used OkHttp.

### 37. Why did you use multipart upload?

Because the prediction endpoint expects the image as a file upload, and multipart form-data is the correct format for that kind of request.

### 38. Why didn't you use Retrofit?

The networking requirement was small and straightforward, so OkHttp was enough for the initial implementation. If the app grows, Retrofit with typed models would be a better structure.

### 39. What is one weakness in the current networking approach?

The API response is returned as a raw string and parsed manually in the UI layer. That couples the networking and presentation logic too tightly.

### 40. How would you improve the networking layer?

I would create typed DTOs, move parsing into a repository or data source layer, and expose cleaner result objects to the UI.

## Firebase and Persistence

### 41. How do you store history?

The app stores each scan as a `HistoryItem` in Firebase Realtime Database under `users/{uid}/history/{autoGeneratedKey}`.

### 42. What data is saved in each history item?

The predicted result label, confidence score, Base64-encoded image, and timestamp.

### 43. Why did you choose Realtime Database?

Because it is simple to integrate for hierarchical per-user data, works well with callback-based listeners, and was enough for the scan history use case.

### 44. Why did you store images as Base64?

It was a simple implementation choice that keeps image data directly attached to each record. However, for production it is not ideal because it increases database size.

### 45. What would be a better way to store images?

I would upload the image to Firebase Storage and save only the image URL or storage reference in the database.

### 46. How do you fetch history?

The `RealtimeManager` attaches a value event listener to the current user's history path and converts each snapshot item into a `HistoryItem`.

### 47. How do you delete one history item?

The current implementation scans the records and deletes the one whose timestamp matches the selected item.

### 48. Is deleting by timestamp ideal?

Not really. It works for simple cases, but a stronger design would keep the Firebase push key and delete by exact database key.

### 49. How do you clear all history?

The app calls `removeValue()` on the user's history node.

## Code Quality and Improvement

### 50. What are the main strengths of your project?

The app solves a real-world problem, combines Android UI with Firebase and ML-related prediction flow, supports complete login-to-history flow, and converts raw prediction outputs into actionable information for users.

### 51. What are the main weaknesses?

The architecture is still lightweight, the active flow is cloud-based rather than fully offline, the local classifier is not integrated, response parsing happens inside the UI, images are stored inefficiently as Base64, and there is very little automated testing.

### 52. Are there any unused or incomplete files?

Yes. `ResultScreen.kt` is empty and unused, and `ScanResult.kt` is present but not used in the active flow. The theme files also exist, but the app is not consistently wrapped in `AgriPlusTheme`.

### 53. Why is that important to mention?

Because it shows honesty and engineering awareness. It is better to explain what is implemented, what is partially implemented, and what still needs cleanup.

### 54. Do you have tests?

Only default template tests are present right now. There are no meaningful feature or business logic tests yet.

### 55. What tests would you add?

I would add tests for disease label mapping, response parsing, repository logic, and UI tests for major user flows like login, scan, and history.

## Security and Scalability

### 56. What security concerns exist in the current project?

One concern is that the prediction API key is hardcoded in the app code, which is risky in production because mobile apps can be reverse engineered. Another concern is making sure Firebase rules are configured so users can only access their own data.

### 57. How would you improve API key security?

I would move the prediction request behind a backend or serverless function that keeps the real API key on the server side instead of bundling it inside the mobile app.

### 58. What scalability issue exists in your current storage design?

Storing Base64 image data directly in Realtime Database will increase data size significantly and is not efficient at scale.

### 59. What scalability improvement would you make?

Use Firebase Storage for images, keep only metadata and URLs in the database, and consider Firestore if query flexibility becomes important.

## Scenario and Follow-Up Questions

### 60. If the interviewer asks, "What exactly did you build yourself?"

You can say:

I implemented the Android app flow including Compose screens, navigation, authentication integration, image acquisition from camera and gallery, prediction request handling, result display logic, and Firebase-based history persistence.

### 61. If the interviewer asks, "What was the hardest technical part?"

You can say:

The hardest part was combining multiple layers smoothly: image capture and conversion, authentication, prediction response handling, and user-specific history management while keeping the app usable and understandable.

### 62. If the interviewer asks, "What would you do differently now?"

You can say:

I would structure the app more cleanly from the start using MVVM, repositories, typed network models, and proper image storage. I would also make a clear decision between local and remote inference.

### 63. If the interviewer asks, "What did this project teach you?"

You can say:

It taught me how to build a real end-to-end Android app that connects UI, authentication, networking, cloud data, and ML-style prediction flow, and it also showed me where architectural cleanup matters as projects grow.

### 64. If the interviewer asks, "What part are you most proud of?"

You can say:

I am most proud of turning a raw prediction output into a complete user-facing flow with sign-in, image scanning, disease explanation, and saved history, because that made the project feel like a complete product instead of just a model demo.

### 65. If the interviewer asks, "Why should we consider this a good project?"

You can say:

Because it demonstrates practical Android development, Compose UI, Firebase integration, image processing, API communication, and the ability to solve a real-world problem through a full application flow.

## HR + Project Blend Questions

### 66. What was your role in the team?

If this was your own project, say:

I handled the app design and implementation, including the UI flow, authentication, prediction integration, and persistence layer.

### 67. How did you decide what features to include first?

I focused first on the core user journey: login, image selection, prediction, readable output, and saved history. Once the end-to-end flow worked, additional improvements could be layered on.

### 68. How did you validate that the idea was useful?

The idea is useful because crop health detection is a practical problem, and a mobile app is an accessible way to provide faster first-level analysis and treatment guidance.

### 69. What makes your solution user-friendly?

It does not stop at showing a raw label. It also shows cause, symptoms, and treatment, supports both gallery and camera, and stores history so users can revisit previous scans.

### 70. What would be your next milestone for this project?

The next milestone would be fully integrating on-device TensorFlow Lite inference, improving architecture, and optimizing image storage and testing.

## Best Final Answer to Memorize

If the interviewer says, "Give me a complete explanation of your project," you can answer:

AgriPlus is an Android app developed in Kotlin with Jetpack Compose for identifying leaf health conditions from images. The app starts with a splash screen that checks whether the user is already authenticated. If not, the user logs in using Google Sign-In integrated with Firebase Authentication. On the home screen, the user can either upload a leaf image from the gallery or capture one through the camera. The selected image is converted to a bitmap, compressed, and sent to a prediction API using OkHttp multipart upload. The response returns a predicted label and confidence score. The app then maps that raw label into meaningful agricultural information like disease name, cause, symptoms, and treatment through a local repository layer. If the prediction is valid, the result is stored in Firebase Realtime Database under the current user's UID, along with confidence, timestamp, and encoded image data. The history screen reads these saved results, displays them in reverse chronological order, and allows users to expand, delete, or clear entries. The project also contains a local TensorFlow Lite classifier and labels file, which means it is partially prepared for offline inference, although the current active flow still uses a remote prediction API. From an engineering perspective, the project demonstrates Android UI, Compose navigation, authentication, networking, cloud persistence, and ML integration concepts, while also leaving clear next steps like MVVM refactoring, offline inference integration, Firebase Storage adoption, and better testing.

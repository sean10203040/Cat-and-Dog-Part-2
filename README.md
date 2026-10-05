# Cat/Dog Android deployment - CS663 Part 2

A Kotlin Android app for YOUR Part 1 sigmoid cat/dog CNN. Select a photo,
then see `Dog X%` and `Cat Y%`. Inference runs locally on a background thread.
No camera feature is required by the supplied Part 2 instructions; this app
uses Android's photo picker and does not request broad photo-library access.
It is newly written, not based on the prohibited older Java sample.

## Status - read first

Source project prepared; the actual trained model is NOT included yet.
No APK build, unit-test execution, emulator test, or physical-device test has
been performed in the authoring environment (no Android SDK / Gradle installed,
and dependency downloads were unavailable). Do not report Part 2 testing as
complete until you build and test it yourself using TESTING.md.

## 1. Get your Part 1 model from Drive

Your recorded successful training session saved it at:
`My Drive/Cat_Dog/runs/20260918T221819_706084Z/export/cat_dog.tflite`

Download that file from Drive. Copy it into:
`app/src/main/assets/cat_dog.tflite`

Do not rename a .keras file to .tflite. Your Part 1 notebook already converted
and saved the real .tflite model. Keep the .keras model in Drive too.
The app displays a useful error if the model is absent or has incompatible tensors.

## 2. Open and build on Windows

1. Install Android Studio. In SDK Manager install Android SDK Platform 35
   and Build Tools 35.0.0. Use JDK 17 or Android Studio's compatible bundled JDK.
2. Extract this ZIP into a writable folder, such as `Documents/CatDogAndroid`.
3. Open a PowerShell terminal in that folder and run:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\setup-windows.ps1
   ```

   This downloads Gradle 8.11.1 from its official distribution service, checks
   its published SHA-256, and generates `gradlew`, `gradlew.bat`, and the wrapper JAR.
   The execution-policy override applies only to this invocation. It does not
   change your system policy. Review the supplied script before running it.
4. In Android Studio choose **Open** and select this project's root folder
   (the folder with `settings.gradle.kts`). Wait for Gradle sync.
5. If asked, select your Android SDK location and Gradle JDK.
6. Use a phone or emulator running Android 9 / API 28 or newer.
7. Press **Run**. Choose a cat/dog photo and record the displayed percentages.

For macOS/Linux, install Gradle 8.11.1 and JDK 17, then run
`gradle wrapper --gradle-version 8.11.1` before opening the project.

After setup, terminal build commands on Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
If using terminal-only builds, set ANDROID_HOME to your SDK directory or let
Android Studio create `local.properties` first. Do not commit machine-specific paths.

## 3. Model behavior

- Input: float32 `[1,128,128,3]`, RGB 0..255.
- Rescaling by 1/255 is already in the model: the app does not normalize twice.
- Output: one sigmoid probability `p` for dog.
- Display: `Dog = 100*p`, `Cat = 100*(1-p)`, rounded to one decimal.
- Full image is resized, with no center crop. The float bilinear resize uses
  TensorFlow's default half-pixel sampling rule.
- Android ImageDecoder applies image orientation and converts to sRGB. Very
  large photos are decoded down to at most 2048 pixels per side for memory use;
  this can introduce small differences from a full-resolution Colab decode.
- Photos outside the cat/dog classes still receive two scores; this binary
  model cannot reliably detect unknown classes or images containing both animals.

The runtime is standalone LiteRT 1.4.2 with the Interpreter API. Google's current
Android overview lists 1.4.2 as the latest 1.x Interpreter release. The newer 2.x
CompiledModel API is another supported route; it is not required for this CPU CNN.
The `org.tensorflow.lite` package names remain in the compatible Interpreter API.
No pretrained model or Play services dependency is substituted for your model.

Pinned build tools: AGP 8.9.2, Gradle 8.11.1, Kotlin 2.1.20, compile/target SDK 35.
These are selected as a compatible set, not claimed as the newest build tools.

## 4. Files

- `MainActivity.kt`: photo selection, image display, lifecycle, background inference.
- `CatDogClassifier.kt`: model loading, input/output validation, inference.
- `ModelMath.kt`: float RGB resize and Dog/Cat percentage calculation.
- `ModelMathTest.kt`: tests for label mapping, no double normalization, interpolation.
- `TESTING.md`: device-testing steps and results table to fill with real observations.

## References checked September 21, 2026

- LiteRT Android: https://developers.google.com/edge/litert/android
- LiteRT migration: https://developers.google.com/edge/litert/migration
- Conversion: https://developers.google.com/edge/litert/conversion/tensorflow/convert_tf
- Android photo picker: https://developer.android.com/training/data-storage/shared/photo-picker
- ImageDecoder: https://developer.android.com/reference/android/graphics/ImageDecoder
- AGP compatibility: https://developer.android.com/build/releases/agp-8-9-0-release-notes
- Kotlin compatibility: https://developer.android.com/build/kotlin-support

The professor's page could not be fetched during preparation. This project is
based on the Part 2 instructions pasted into the conversation.

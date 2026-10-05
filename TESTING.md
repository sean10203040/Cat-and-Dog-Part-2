# Part 2 test record - fill in after running on Android

Status: NOT RUN. No device results or screenshots have been fabricated.

## Build and automated checks

Run `gradlew.bat testDebugUnitTest` and `gradlew.bat assembleDebug`.
Record the build result, Android Studio version, device/emulator, and Android version.
Unit tests verify RGB input values, resize coordinates, and class-probability mapping.
They do not validate your actual trained model or native runtime integration.

## Device checks

1. Add the real `cat_dog.tflite` asset; verify the app loads without errors.
2. Select at least five different cat photos and five dog photos, ideally held-out
   test images or new photos rather than training images. Capture at least one
   cat and one dog result, each with the selected image and both percentages.
3. Record all predictions, including mistakes. The two scores should sum to
   approximately 100% after rounding; they are model scores, not measured accuracy.
4. Classify another photo repeatedly; confirm the results update without stale values.
5. Cancel photo selection; confirm no crash and the existing result stays visible.
6. Rotate the phone; confirm the selected image is restored/reclassified when its
   URI remains readable. Try a portrait photo with EXIF rotation.
7. Try a large photo and a damaged image. Check memory behavior and error handling.
8. Choose a locally stored photo in airplane mode. Inference should work offline.
   A photo stored only in the cloud may need a network connection to download first.

| Photo ID | Actual class | Dog % | Cat % | Predicted class | Correct? |
|---|---|---|---|---|---|
| cat-1 | Cat | | | | |
| cat-2 | Cat | | | | |
| cat-3 | Cat | | | | |
| cat-4 | Cat | | | | |
| cat-5 | Cat | | | | |
| dog-1 | Dog | | | | |
| dog-2 | Dog | | | | |
| dog-3 | Dog | | | | |
| dog-4 | Dog | | | | |
| dog-5 | Dog | | | | |

Device / Android version:
Build result:
Unit-test result:
Correct predictions / images tested:
Observed failures:
Group members and contributions:

## Suggested evidence for your group

Keep source, model asset, debug APK, real screenshots, and this completed record.
The supplied Part 2 text does not prescribe a particular submission format;
check your class submission page for the required files.

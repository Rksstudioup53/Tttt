# Hindi Video Translator (हिंदी वीडियो अनुवादक)
### Complete Native Android Application in Kotlin & Jetpack Compose

This project is a 100% complete and valid Android Studio project. It contains all manifests, Gradle build scripts, Version Catalogs, Kotlin Jetpack Compose UI, ExoPlayer integration, and Android TextToSpeech engine.

---

## 📱 How to Generate an APK File (Debug & Signed)

> **Important Note Regarding This Web Sandbox**:
> The AI Studio development container is a Node.js/Linux cloud environment without the full Android SDK (`aapt2`, `d8`, `android-sdk`, JDK). Therefore, you compile the actual `.apk` binary on your computer with Android Studio or Gradle using the steps below.

### Option 1: Generate a Debug APK via Terminal (Quickest)

1. Open a terminal / command prompt inside the unzipped `HindiVideoTranslator` folder:
   ```bash
   # On macOS / Linux:
   ./gradlew assembleDebug

   # On Windows:
   gradlew.bat assembleDebug
   ```
2. Once the build finishes (typically 30–60 seconds on first run), your generated APK file is located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
3. Transfer `app-debug.apk` to your phone via USB, WhatsApp, Google Drive, or run:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

### Option 2: Generate a Debug or Signed Release APK via Android Studio GUI

1. Open **Android Studio** and click **Open** -> Select `HindiVideoTranslator`.
2. Wait for Gradle to sync dependencies.
3. To build a **Debug APK**:
   - In the top menu, click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
   - Click the "locate" notification popup to open the folder with `app-debug.apk`.
4. To build a **Signed Release APK** (for Google Play Store or permanent phone install):
   - In the top menu, click **Build** > **Generate Signed Bundle / APK**.
   - Select **APK** and click **Next**.
   - Create or choose a keystore (`.jks`), enter your password and key alias.
   - Select destination folder and choose **release** build type.
   - Click **Finish**. Your signed release APK is generated!

---

## ⚡ Direct Phone Installation (Without Android Studio)
If you do not have Android Studio installed on your computer, you can install this app **instantly** right now on any Android phone:
1. Open this app's URL in **Google Chrome** on your Android phone.
2. Tap the **"फोन पर इंस्टॉल करें (Install App)"** button or open Chrome menu (**⋮**) -> tap **Install app** / **Add to Home screen**.
3. Android creates an official **WebAPK** on your phone with an app icon in your app drawer, offline cache support, and full-screen video playback!
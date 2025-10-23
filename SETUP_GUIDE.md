# PrivacySMS - Quick Setup Guide

## 🚀 Getting Started

### Step 1: Open in Android Studio
1. Open Android Studio
2. Select **File → Open**
3. Navigate to the `PrivacySMS` folder
4. Click **OK**

### Step 2: Sync Project
- Android Studio will automatically start syncing Gradle
- Wait for "Gradle sync finished" notification
- If sync fails, click **File → Sync Project with Gradle Files**

### Step 3: Connect Device or Emulator
- **Physical Device**: 
  - Enable Developer Options and USB Debugging
  - Connect via USB
  - Allow USB debugging prompt on phone
  
- **Emulator**: 
  - Create a new AVD (Android Virtual Device)
  - Minimum API 26 (Android 8.0)
  - Recommended: Pixel 5 with API 33 or 34

### Step 4: Build & Run
1. Click the **Run** button (green play icon) or press **Shift+F10**
2. Select your device/emulator
3. Wait for the app to install

### Step 5: Grant Permissions
On first launch:
1. Grant **SMS permissions** when prompted
2. Set PrivacySMS as **default SMS app** when prompted
3. Grant **notification permissions** (Android 13+)

### Step 6: Configure Emergency Bypass (Optional)
1. Open the app menu (three dots)
2. Select **Settings**
3. Enable **Emergency Bypass**
4. Set your secret keyword
5. Click **Save Keyword**

## 📝 Important Notes

### FOSSify Integration
The current build includes a placeholder for FOSSify. To use the actual FOSSify library:

1. Replace this line in `app/build.gradle.kts`:
   ```kotlin
   implementation("com.github.moezbhatti:qksms:3.9.4")
   ```
   With the actual FOSSify dependency when available:
   ```kotlin
   implementation("org.fossify:sms:x.x.x")
   ```

2. Update the import statements in:
   - `SmsReceiver.kt`
   - `SmsManagerHelper.kt`

### API Keys
No API keys are needed! The app:
- ✅ Runs completely offline
- ✅ Uses only Android system APIs
- ✅ Requires no external services
- ✅ Has no server dependencies

### Launcher Icons
The current project uses default Android icons. To add custom icons:

1. Right-click `res` folder → **New → Image Asset**
2. Select **Launcher Icons (Adaptive and Legacy)**
3. Choose your icon image
4. Click **Next** → **Finish**

## 🔧 Troubleshooting

### Gradle Sync Failed
**Error**: `Could not resolve dependencies`

**Solution**:
```bash
# In Android Studio Terminal:
./gradlew clean
./gradlew build --refresh-dependencies
```

### Build Failed - Missing SDK
**Error**: `Failed to find target with hash string 'android-34'`

**Solution**:
1. Open **Tools → SDK Manager**
2. Select **Android 14.0 (API 34)** under SDK Platforms
3. Click **Apply** and wait for download

### App Crashes on Launch
**Possible Causes**:
1. Minimum SDK version - Requires Android 8.0+
2. Permissions not granted - Check app permissions in Settings
3. Not set as default SMS app - Set in system settings

### Can't Receive Messages
**Checklist**:
- [ ] App is set as default SMS app
- [ ] All SMS permissions granted
- [ ] Device has active SIM card
- [ ] No other SMS app is intercepting messages

### Database Encryption Error
If you see encryption errors:
1. Clear app data: Settings → Apps → PrivacySMS → Storage → Clear Data
2. Uninstall and reinstall the app
3. Note: This will delete all messages

## 🏗️ Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```
- Located at: `app/build/outputs/apk/debug/app-debug.apk`
- Debuggable, larger file size
- For development and testing

### Release Build
```bash
./gradlew assembleRelease
```
- Located at: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Optimized, smaller file size
- Requires signing for installation

### Signing the Release Build
1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore privacy-sms.keystore -alias privacy-sms -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Add to `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("path/to/privacy-sms.keystore")
               storePassword = "your-password"
               keyAlias = "privacy-sms"
               keyPassword = "your-password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               // ... other config
           }
       }
   }
   ```

3. Build signed APK:
   ```bash
   ./gradlew assembleRelease
   ```

## 🧪 Testing

### Manual Testing Checklist
- [ ] Send SMS to another device
- [ ] Receive SMS from another device
- [ ] Archive conversation with 1-hour timeout
- [ ] Long-press conversation to see options
- [ ] Test emergency bypass keyword
- [ ] Check notifications appear
- [ ] Verify messages are encrypted in database
- [ ] Test app as non-default SMS app (should prompt user)

### Testing Emergency Bypass
1. Archive a conversation
2. Send SMS with emergency keyword to your device
3. Verify conversation is unarchived
4. Check notification appears

## 📱 Installation on Device

### Via Android Studio
1. Connect device with USB debugging enabled
2. Click **Run** in Android Studio
3. Select device from list

### Via APK File
1. Build APK: `./gradlew assembleDebug`
2. Transfer APK to device
3. Enable **Install from Unknown Sources** in Settings
4. Open APK file and install

### Via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ Customization

### Change App Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="purple_500">#FF6200EE</color>  <!-- Primary color -->
<color name="teal_200">#FF03DAC5</color>    <!-- Accent color -->
```

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change Archive Durations
Edit `MainActivity.kt`, find `showConversationOptions()` method and modify the options array and corresponding minutes.

## 🔒 Security Best Practices

1. **Never commit keystore files** to version control
2. **Use strong emergency keywords** (avoid common words)
3. **Enable device encryption** in Android settings
4. **Set screen lock** on your device
5. **Regularly update** Android system

## 📚 Additional Resources

- [Android SMS Documentation](https://developer.android.com/reference/android/telephony/SmsManager)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Android KeyStore System](https://developer.android.com/training/articles/keystore)
- [Material Design Guidelines](https://m3.material.io/)

## ❓ Common Questions

**Q: Can I use this as my daily SMS app?**
A: Yes, but remember it's a privacy-focused app. Back up important messages elsewhere.

**Q: Will archived messages be backed up?**
A: No, the app explicitly disables cloud backups for privacy.

**Q: Can I migrate from another SMS app?**
A: The app will read existing SMS from the system database after you grant permissions.

**Q: Is the emergency keyword secure?**
A: Yes, it's stored as a SHA-256 hash, not plaintext.

**Q: What happens if I forget my emergency keyword?**
A: You'll need to manually unarchive conversations from the Archive section.

---

Need more help? Check the main README.md for detailed documentation!

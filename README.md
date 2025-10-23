# PrivacySMS - Privacy-Focused Android SMS App

A privacy-first SMS messaging application built with Kotlin, featuring encrypted local storage, message archiving, and emergency bypass functionality.

## Features

### 🔐 Privacy & Security
- **End-to-End Encryption**: All messages encrypted using Android KeyStore and AES-256-GCM
- **SQLCipher Database**: Encrypted local database with no cloud dependencies
- **No Server Communication**: Completely offline operation
- **Secure Key Management**: Android KeyStore integration for cryptographic operations

### 📱 Core Functionality
- **SMS Sending & Receiving**: Full SMS/MMS support
- **Conversation Management**: Threaded conversations with contact integration
- **Read Receipts**: Track read/unread message status
- **Message Search**: Find conversations and messages quickly

### 🗂️ Advanced Archiving
- **Time-Based Archiving**: Hide conversations for custom durations:
  - 1 hour
  - 6 hours
  - 24 hours
  - 7 days
  - Custom duration (configurable in settings)
- **Automatic Unarchiving**: Conversations automatically restore after timeout
- **Archive Management**: View and manually restore archived conversations

### 🚨 Emergency Bypass Feature
- **Keyword Trigger**: Set a secret keyword that instantly reveals all archived conversations
- **Hashed Storage**: Emergency keyword stored as SHA-256 hash for security
- **Toggle Enable/Disable**: Control emergency bypass activation
- **Notification**: Get notified when bypass is triggered

### 🎨 Material Design UI
- Material Design 3 components
- Light/Dark theme support
- Smooth animations and transitions
- Intuitive conversation threading

## Technical Stack

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)

### Architecture & Libraries
- **Architecture**: MVVM with Repository pattern
- **Database**: Room + SQLCipher for encryption
- **Async Operations**: Kotlin Coroutines + Flow
- **UI**: ViewBinding + Material Components
- **Background Tasks**: WorkManager for scheduled operations
- **Dependency Injection**: Manual DI (easily upgradeable to Hilt/Koin)

### Key Dependencies
```kotlin
// Database & Encryption
androidx.room:room-runtime:2.6.1
net.zetetic:android-database-sqlcipher:4.5.4

// Material Design
com.google.android.material:material:1.11.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// WorkManager
androidx.work:work-runtime-ktx:2.9.0
```

## Project Structure

```
com.privacy.sms/
├── database/
│   ├── PrivacySMSDatabase.kt    # Room database with SQLCipher
│   ├── MessageDao.kt             # Message data access
│   ├── ConversationDao.kt        # Conversation data access
│   └── ArchiveSettingsDao.kt     # Settings data access
├── model/
│   ├── Message.kt                # Message entity
│   ├── Conversation.kt           # Conversation entity
│   └── ArchiveSettings.kt        # Settings entity
├── repository/
│   └── SmsRepository.kt          # Data layer abstraction
├── security/
│   └── EncryptionManager.kt      # Android KeyStore integration
├── ui/
│   ├── MainActivity.kt           # Conversation list
│   ├── ConversationActivity.kt   # Message thread view
│   ├── ArchiveActivity.kt        # Archived conversations
│   ├── SettingsActivity.kt       # App settings
│   └── adapter/
│       ├── ConversationAdapter.kt
│       └── MessageAdapter.kt
├── receiver/
│   ├── SmsReceiver.kt            # SMS broadcast receiver
│   └── MmsReceiver.kt            # MMS broadcast receiver
└── util/
    ├── NotificationHelper.kt      # Notification management
    ├── SmsManagerHelper.kt        # SMS sending utility
    └── ArchiveExpirationWorker.kt # Background archive checking
```

## Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK with minimum API 26

### Build Instructions

1. **Clone the project**:
   ```bash
   # The project files are already structured in the PrivacySMS directory
   ```

2. **Open in Android Studio**:
   - File → Open → Select the PrivacySMS directory

3. **Sync Gradle**:
   - Android Studio will automatically sync Gradle dependencies

4. **Build the app**:
   ```bash
   ./gradlew assembleDebug
   # or use Android Studio's Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

5. **Install on device**:
   ```bash
   ./gradlew installDebug
   # or use Android Studio's Run button
   ```

### Configuration

#### Required Permissions
The app requests these permissions at runtime:
- `SEND_SMS` - Send text messages
- `RECEIVE_SMS` - Receive incoming messages
- `READ_SMS` - Read existing messages
- `READ_CONTACTS` - Display contact names

#### Default SMS App
The app must be set as the default SMS application to receive and send messages properly. Users will be prompted on first launch.

## Usage Guide

### Setting Up Emergency Bypass

1. Navigate to **Settings** from the main menu
2. Enable **Emergency Bypass** toggle
3. Enter a secret keyword (this will be hashed and stored securely)
4. Click **Save Keyword**

⚠️ **Important**: Choose a keyword you'll remember but that's unlikely to appear in normal conversations.

### Archiving Conversations

**Quick Archive**:
1. Long-press on any conversation
2. Select archive duration from the dialog
3. Conversation is hidden until the timeout expires

**Manual Unarchive**:
1. Open **Archive** from the main menu
2. Long-press the archived conversation
3. Select **Unarchive**

### Emergency Bypass Activation

To trigger emergency bypass and reveal all archived conversations:
1. Send an SMS containing only your emergency keyword
2. The app will automatically unarchive all conversations
3. You'll receive a notification confirming activation

## Security Considerations

### Encryption Details
- **Message Body**: AES-256-GCM encryption via Android KeyStore
- **Database**: SQLCipher with AES-256 encryption
- **Emergency Keyword**: SHA-256 hashed, not stored in plaintext
- **Key Storage**: Android KeyStore (hardware-backed when available)

### Privacy Features
- No network communication (except standard SMS protocol)
- No cloud backups (explicitly disabled)
- No analytics or telemetry
- All data stays on device

### Limitations
- ⚠️ Device backup/restore may not include encrypted database
- ⚠️ Uninstalling the app will delete all messages permanently
- ⚠️ Root access could compromise encryption keys

## Troubleshooting

### App Not Receiving Messages
1. Ensure PrivacySMS is set as default SMS app
2. Check SMS permissions are granted
3. Restart the device

### Messages Not Decrypting
1. This shouldn't happen in normal operation
2. If it does, the encryption key may have been lost
3. Unfortunately, encrypted data cannot be recovered

### Archive Timer Not Working
1. Check that battery optimization is disabled for the app
2. Verify WorkManager is functioning: Settings → Apps → PrivacySMS → Battery → Unrestricted

## Future Enhancements

Potential features for future versions:
- [ ] Biometric authentication for app access
- [ ] Scheduled message sending
- [ ] Message backup/export (encrypted)
- [ ] Custom notification sounds per conversation
- [ ] Group MMS support
- [ ] Contact blocking
- [ ] Message templates

## FOSSify Integration Note

The current implementation uses standard Android SMS APIs. To fully integrate FOSSify SMS library:

1. Replace the FOSSify dependency with the actual library when available:
   ```kotlin
   implementation("org.fossify:sms:x.x.x")
   ```

2. Implement FOSSify's SMS handling interfaces in `SmsReceiver.kt` and `SmsManagerHelper.kt`

3. Update the receiver registrations in `AndroidManifest.xml`

## License

This project is intended for private use. Please ensure compliance with local regulations regarding SMS applications and encryption.

## Contributing

This is a privacy-focused application built for personal use. If you fork this project:
- Maintain the security and privacy features
- Test thoroughly on multiple devices
- Never compromise user data

## Support

For issues or questions:
- Review the troubleshooting section
- Check Android SMS app development documentation
- Ensure all permissions are properly granted

---

**⚠️ Important Privacy Notice**: This app stores all messages locally in an encrypted database. Back up your data regularly, and remember that uninstalling the app will permanently delete all messages.

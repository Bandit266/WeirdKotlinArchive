# PrivacySMS Build Instructions

## 🚀 Features Implemented

This sophisticated SMS app includes:

### ✨ Security Features
- **Biometric Authentication** - Fingerprint/Face unlock support
- **PIN Authentication** - Secure numeric PIN option
- **Pattern Lock** - Android-style pattern authentication
- **Encrypted Vault** - AES-256 encrypted message storage

### 🎨 UI/UX Features  
- **Animated Gradient Background** - Liquid-like organic animations
- **Sophisticated Animations** - Bounce, pulse, glow, shake effects
- **Purple/Pink Theme** - Crypto/girly/hacker aesthetic
- **Hamburger Menu** - Slide-out navigation drawer
- **Dark Mode** - Full dark theme with curved edges

### 📱 App Features
- **Profile Management** - Photo upload, username, bio
- **Settings Dashboard** - Statistics (uptime, SMS blocked)
- **Encrypted Vault** - Secure message storage with blur effect
- **Message Inbox** - SMS list with search functionality
- **In Development Modal** - Professional placeholder for compose

## 🛠️ Prerequisites

To build this app, you need:

1. **Java Development Kit (JDK) 17**
   - Download from: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - Set JAVA_HOME environment variable

2. **Android Studio**
   - Download from: https://developer.android.com/studio
   - This will install Android SDK automatically

## 📦 Building the APK

### Option 1: Using Android Studio (Recommended)

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the PrivacySMS folder
4. Wait for Gradle sync to complete
5. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
6. APK will be in `app/build/outputs/apk/debug/`

### Option 2: Command Line

#### Windows:
```bash
cd PrivacySMS
.\gradlew.bat assembleDebug
```

#### Mac/Linux:
```bash
cd PrivacySMS
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## 📲 Installing the APK

1. Enable "Install from Unknown Sources" on your Android device
2. Transfer the APK to your device
3. Open the APK file to install
4. Grant necessary permissions when prompted

## 🔑 First Launch

1. App opens with authentication screen
2. Choose authentication method:
   - **Biometric** - Use fingerprint/face
   - **PIN** - Set a 4-8 digit PIN
   - **Pattern** - Draw a pattern
3. After authentication, access the inbox
4. Explore features via hamburger menu (top-left)

## 🎯 Key Features to Test

1. **Authentication Flow**
   - Try all three authentication methods
   - Notice sophisticated unlock animations

2. **Hamburger Menu**
   - Swipe from left or tap menu icon
   - Semi-transparent overlay effect
   - Access Profile, Contacts, Settings, Vault

3. **Profile Page**
   - Upload profile photo
   - Edit username and bio
   - Privacy-focused local storage

4. **Settings Page**
   - View uptime statistics
   - Toggle dark mode
   - Configure notifications
   - Security settings

5. **Encrypted Vault**
   - Heavy blur effect when locked
   - Sophisticated unlock animation
   - Secure message storage

6. **Visual Effects**
   - Liquid gradient backgrounds
   - Pulse and glow animations
   - Purple/pink theme throughout

## 🐛 Troubleshooting

- **Gradle sync failed**: Update Android Studio and Gradle
- **JDK not found**: Set JAVA_HOME to JDK 17 path
- **Build failed**: Clean project (Build → Clean Project) and rebuild
- **App crashes**: Check device has Android 8.0+ (API 26+)

## 📄 Permissions Required

- SMS permissions (send, receive, read)
- Camera (profile photo)
- Biometric authentication
- Contacts access
- Internet (future features)

## 🔗 Repository

Code available at: https://github.com/Bandit266/WeirdKotlinArchive

## ⚡ Performance Notes

- Optimized animations using hardware acceleration
- Efficient RecyclerView for message lists
- Lazy loading for better performance
- Background gradient uses GPU rendering

## 🎨 Design Philosophy

The app combines:
- **Crypto aesthetics** - Dark backgrounds, neon accents
- **Girly elements** - Pink/purple gradients, soft curves
- **Hacker vibes** - Terminal-like elements, security focus

All with sophisticated animations and a premium feel!

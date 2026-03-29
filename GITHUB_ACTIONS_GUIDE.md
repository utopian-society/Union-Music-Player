# 🚀 GitHub Actions Build Guide

## ✅ Workflow Updated!

Your GitHub Actions workflow has been updated to work with your new UnionMusicPlayer app.

---

## 📁 File Location

```
.github/workflows/build_release_apk.yml
```

---

## 🔧 What Changed

### Updated Configuration:

1. **Simplified checkout** - Removed submodule references (no longer needed)
2. **Android SDK** - Updated to API 34 (matches your app's targetSdk)
3. **Build tools** - Using stable version 34.0.0
4. **Removed NDK/CMake** - Not needed for your current app
5. **Added validation** - Gradle wrapper version check

### What It Does:

```yaml
on:
  workflow_dispatch:  # Manual trigger from GitHub Actions tab
    inputs:
      version:        # You enter version when triggering
        required: true
      build_type:     # Choose 'release' or 'debug'
        required: true
```

---

## 🎯 How to Use

### Step 1: Go to Actions Tab
```
GitHub Repository → Actions → "Build APK" workflow
```

### Step 2: Click "Run workflow"
```
Branch: main (or your default branch)
```

### Step 3: Fill in Parameters
```
Version number: 1.0.0 (or your version)
Build type: release (for signed APK) or debug (for testing)
```

### Step 4: Wait for Build
```
Takes ~5-10 minutes
```

### Step 5: Download APK
```
For release: Check GitHub Releases page
For debug: Download from workflow artifacts
```

---

## 📦 Build Outputs

### Release Build
- **Location**: GitHub Releases
- **File**: `UnionMusicPlayer-v{version}-release.apk`
- **Signed**: Yes (requires keystore secrets)
- **Ready for**: Distribution to users

### Debug Build
- **Location**: Workflow artifacts
- **File**: `UnionMusicPlayer-v{version}-debug.apk`
- **Signed**: No (debug signature)
- **Ready for**: Testing only

---

## 🔐 Release Signing (Optional)

To sign release builds, add these **GitHub Secrets**:

### Required Secrets:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `ANDROID_KEYSTORE_BASE64` | Your keystore file (base64 encoded) | `MII...` |
| `KEYSTORE_PASSWORD` | Keystore password | `your_password` |
| `KEY_ALIAS` | Key alias name | `upload` |
| `KEY_PASSWORD` | Key password | `your_key_password` |
| `GH_TOKEN` | GitHub token for creating releases | `ghp_...` |

### How to Add Secrets:

```
GitHub Repository → Settings → Secrets and variables → Actions → New repository secret
```

### How to Create Keystore:

```bash
keytool -genkey -v -keystore union-music-player.keystore \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

---

## 🏗️ Workflow Steps Explained

### 1. Checkout Repository
```yaml
- name: Checkout repository
  uses: actions/checkout@v4
```
**What it does**: Downloads your code from GitHub

---

### 2. Set up JDK 25
```yaml
- name: Set up JDK 25
  uses: actions/setup-java@v4
  with:
    java-version: '25'
```
**What it does**: Installs Java 25 for building

---

### 3. Setup Android SDK
```yaml
- name: Setup Android SDK
  uses: android-actions/setup-android@v3
```
**What it does**: Installs Android command-line tools

---

### 4. Install SDK Components
```yaml
- name: Install required SDK components
  run: |
    sdkmanager --install "platforms;android-34" \
      "build-tools;34.0.0" \
      "platform-tools"
```
**What it does**: Installs Android API 34 and build tools

---

### 5. Update Version
```yaml
- name: Update version in build.gradle.kts
  run: |
    sed -i "s/versionCode.../" app/build.gradle.kts
```
**What it does**: Updates versionCode and versionName from your input

---

### 6. Build APK
```yaml
- name: Build Release APK
  run: ./gradlew assembleRelease
```
**What it does**: Compiles and packages your app

---

### 7. Create GitHub Release (Release only)
```yaml
- name: Create GitHub Release
  uses: softprops/action-gh-release@v2
```
**What it does**: Creates a new release on GitHub with APK attached

---

## 📊 Build Status Badges

Add these to your README.md:

```markdown
<!-- Release Build Status -->
[![Build APK](https://github.com/bibichan/Union-Music-Player/actions/workflows/build_release_apk.yml/badge.svg)](https://github.com/bibichan/Union-Music-Player/actions/workflows/build_release_apk.yml)

<!-- Latest Release -->
[![Latest Release](https://img.shields.io/github/v/release/bibichan/Union-Music-Player)](https://github.com/bibichan/Union-Music-Player/releases)
```

---

## 🐛 Troubleshooting

### Build fails with "SDK not found"
**Solution**: The workflow automatically installs SDK. Check the logs for specific errors.

### Build fails with "Keystore not found"
**Solution**: Either:
1. Add the keystore secrets, OR
2. Build debug version instead

### Build fails with Gradle errors
**Solution**: Check your `build.gradle.kts` for syntax errors

### Release doesn't appear on GitHub
**Solution**: Ensure `GH_TOKEN` secret is set with repo permissions

---

## 📱 App Icon Placement

### Current Setup (Vector Icon)

Your app uses a **vector drawable** icon:

```
app/src/main/res/
├── drawable/ic_launcher_foreground.xml    ← Music note vector
├── values/colors.xml                       ← Green background
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml                     ← Adaptive icon config
    └── ic_launcher_round.xml
```

### To Use Custom Icon Image:

**Option 1: Android Studio (Easiest)**
1. Open Android Studio
2. Right-click `res` folder → New → Image Asset
3. Select your logo image
4. Click Finish (auto-generates all sizes)

**Option 2: Manual Placement**
```
app/src/main/res/
├── mipmap-hdpi/ic_launcher_foreground.png      (121x121 px)
├── mipmap-mdpi/ic_launcher_foreground.png      (81x81 px)
├── mipmap-xhdpi/ic_launcher_foreground.png     (162x162 px)
├── mipmap-xxhdpi/ic_launcher_foreground.png    (243x243 px)
└── mipmap-xxxhdpi/ic_launcher_foreground.png   (324x324 px)
```

**For detailed icon guide, see**: [`ICON_GUIDE.md`](./ICON_GUIDE.md)

---

## 📋 Pre-Build Checklist

Before running the build:

- [ ] `build.gradle.kts` has correct syntax
- [ ] `AndroidManifest.xml` exists and is valid
- [ ] All resource files are in place (icons, strings, themes)
- [ ] `changelog.md` exists (for release notes)
- [ ] Keystore secrets added (for release builds)

---

## 🔗 Useful Links

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Android Actions](https://github.com/android-actions/setup-android)
- [Gradle on GitHub Actions](https://github.com/actions/setup-java)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)

---

## 🎯 Next Steps

1. **Test the workflow**: Run a debug build first
2. **Check the output**: Download and test the APK
3. **Set up signing**: Add keystore for release builds
4. **Customize icon**: Replace with your own logo (see ICON_GUIDE.md)
5. **Automate releases**: Set up CI/CD pipeline

---

*Your workflow is ready! Go to Actions tab and trigger your first build!* 🚀

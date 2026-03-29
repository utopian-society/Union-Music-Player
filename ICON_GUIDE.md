# 🖼️ App Icon Placement Guide

## Quick Answer

For **adaptive icons** (Android 8.0+), you need to place images in these locations:

```
app/src/main/res/
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml           ← Adaptive icon config (already created)
│   └── ic_launcher_round.xml     ← Round variant config
│
├── mipmap-hdpi/       (48x48 px)
├── mipmap-mdpi/       (72x72 px)
├── mipmap-xhdpi/      (96x96 px)
├── mipmap-xxhdpi/     (144x144 px)
└── mipmap-xxxhdpi/    (192x192 px)
```

---

## 📐 Icon Requirements

### For Adaptive Icons (Recommended)

Adaptive icons have **two layers**:

1. **Background layer** - Solid color or gradient
2. **Foreground layer** - Your logo/symbol

### Image Specifications

| Density | Folder | Size (foreground) | Size (full icon) |
|---------|--------|-------------------|------------------|
| mdpi | `mipmap-mdpi/` | 81x81 px | 108x108 px |
| hdpi | `mipmap-hdpi/` | 121.5x121.5 px | 162x162 px |
| xhdpi | `mipmap-xhdpi/` | 162x162 px | 216x216 px |
| xxhdpi | `mipmap-xxhdpi/` | 243x243 px | 324x324 px |
| xxxhdpi | `mipmap-xxxhdpi/` | 324x324 px | 432x432 px |

### Safe Zone

```
┌─────────────────────────────┐
│                             │
│    ┌─────────────────┐      │
│    │                 │      │
│    │   SAFE ZONE     │      │  ← Keep your logo within this area
│    │   (108dp)       │      │
│    │                 │      │
│    └─────────────────┘      │
│                             │
│    Full canvas (162dp)      │
└─────────────────────────────┘
```

---

## 🎨 Current Icon Setup

Your project currently uses **vector drawable** for the icon:

```
app/src/main/res/
├── drawable/ic_launcher_foreground.xml  ← Music note vector (white)
├── values/colors.xml                     ← Green background (#4CAF50)
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml                   ← Combines background + foreground
    └── ic_launcher_round.xml
```

### Current Icon Appearance

- **Background**: Green (#4CAF50)
- **Foreground**: White music note symbol
- **Style**: Simple, clean vector design

---

## 📝 How to Replace with Your Own Icon

### Option 1: Keep Vector (Recommended for Simple Icons)

1. **Edit the vector file**:
   ```
   app/src/main/res/drawable/ic_launcher_foreground.xml
   ```

2. **Change the path data** to your own vector path
   - Export from Illustrator/Figma as Android Vector Drawable
   - Replace the `<path>` element

3. **Change the background color**:
   ```
   app/src/main/res/values/colors.xml
   <color name="ic_launcher_background">#YOUR_COLOR</color>
   ```

### Option 2: Use PNG Images (For Complex Logos)

1. **Create your icon** at all required sizes (see table above)

2. **Place PNG files** in each density folder:
   ```
   app/src/main/res/
   ├── mipmap-hdpi/ic_launcher_foreground.png
   ├── mipmap-mdpi/ic_launcher_foreground.png
   ├── mipmap-xhdpi/ic_launcher_foreground.png
   ├── mipmap-xxhdpi/ic_launcher_foreground.png
   └── mipmap-xxxhdpi/ic_launcher_foreground.png
   ```

3. **Update the adaptive icon XML**:
   ```xml
   <!-- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml -->
   <adaptive-icon>
       <background android:drawable="@color/ic_launcher_background"/>
       <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
   </adaptive-icon>
   ```

### Option 3: Use Image Asset Studio (Easiest!)

1. **Open Android Studio**

2. **Right-click** on `res` folder → **New** → **Image Asset**

3. **Configure**:
   - **Icon Type**: Adaptive and Legacy
   - **Foreground Layer**: 
     - Choose Image (PNG, SVG, etc.)
     - Or select Clip Art
   - **Background Layer**: 
     - Solid color or gradient
   - **Name**: `ic_launcher`

4. **Click Next** → **Finish**

Android Studio will automatically generate all required files!

---

## 🖼️ Icon Templates

### Download Templates

- **Android Studio Template**: [Download from Android Developers](https://developer.android.com/studio/write/images/icon-designer)
- **Figma Template**: [Adaptive Icon Template](https://www.figma.com/community/file/814502969256923810)

### Canvas Setup

```
Artboard: 432x432 px (for xxxhdpi)
Safe Zone: 324x324 px (centered)
Full Icon: 432x432 px
```

---

## 🔍 Testing Your Icon

### 1. Build and Run
```bash
./gradlew installDebug
```

### 2. Check on Emulator/Device
- Home screen
- App drawer
- Settings → Apps
- Recent apps

### 3. Check Different Shapes
Different Android devices use different icon shapes:
- Circle (Pixel)
- Squircle (Samsung)
- Rounded Square (OnePlus)
- Teardrop (Some launchers)

Make sure your icon looks good in all shapes!

---

## 📱 Legacy Icon Support

For Android 7.1 and below, you need legacy icons:

```
app/src/main/res/
├── mipmap-hdpi/ic_launcher.png      ← 48x48 px
├── mipmap-mdpi/ic_launcher.png      ← 72x72 px
├── mipmap-xhdpi/ic_launcher.png     ← 96x96 px
├── mipmap-xxhdpi/ic_launcher.png    ← 144x144 px
└── mipmap-xxxhdpi/ic_launcher.png   ← 192x192 px
```

**Note**: The current setup uses adaptive icons which automatically fall back on older devices.

---

## 🎯 Best Practices

1. **Keep it simple** - Complex details get lost at small sizes
2. **Use high contrast** - Make sure it's visible on all backgrounds
3. **Test on dark/light wallpapers** - Ensure visibility
4. **Avoid text** - Text becomes unreadable at small sizes
5. **Center important elements** - Keep within safe zone
6. **Use vector when possible** - Scales to any size without quality loss

---

## 🔧 Troubleshooting

### Icon appears white/blank
- Check that `ic_launcher_foreground.xml` has valid path data
- Ensure the path has `android:fillColor` set

### Icon appears stretched
- Make sure images are square (1:1 aspect ratio)
- Check that you placed files in correct density folders

### Icon has wrong colors
- Verify `colors.xml` has correct hex value
- Check that PNG files are not pre-multiplied with alpha

### Icon doesn't update after changes
- Clean and rebuild: `./gradlew clean assembleDebug`
- Uninstall app from device and reinstall

---

## 📚 Resources

- [Android Icon Design Guidelines](https://developer.android.com/guide/practices/ui_guidelines/icon_design_launcher)
- [Adaptive Icons Guide](https://developer.android.com/guide/topics/ui/look-and-feel/adaptive-icons)
- [Material Design Icons](https://fonts.google.com/icons)
- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)

---

*For Union Music Player, the current green + white music note icon matches the app's color scheme (GreenPrimary = #4CAF50).*

# Patchwork

**Your Android device, supercharged.** 🚀

Patchwork is an all-in-one power user toolkit that unlocks the full potential of your Android device. Built with Kotlin and Jetpack Compose, it brings together essential tools, advanced customizations, and clever workarounds to enhance your Android experience—all in one beautiful Material 3 app.

> **Note**: This app uses advanced system APIs and permissions. While designed for safety, some features are experimental. Use at your own discretion.

**Developer**: Tino Britty J  
**GitHub**: [github.com/brittytino](https://github.com/brittytino)

<p align="center">
  <a href="https://github.com/brittytino/patchwork/releases/latest">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/brittytino/patchwork?style=for-the-badge&logo=github&logoColor=white&labelColor=black&color=blue" />
  </a>
  <a href="https://github.com/brittytino/patchwork/blob/main/LICENSE">
    <img alt="License" src="https://img.shields.io/github/license/brittytino/patchwork?style=for-the-badge&logo=opensourceinitiative&logoColor=white&labelColor=black&color=green" />
  </a>
  <a href="https://github.com/brittytino/patchwork/releases/latest">
    <img alt="GitHub Downloads" src="https://img.shields.io/github/downloads/brittytino/patchwork/total?style=for-the-badge&logo=download&logoColor=white&labelColor=black&color=orange" />
  </a>
  <a href="https://github.com/brittytino/patchwork/stargazers">
    <img alt="GitHub Stars" src="https://img.shields.io/github/stars/brittytino/patchwork?style=for-the-badge&logo=starship&logoColor=white&labelColor=black&color=yellow" />
  </a>
</p>

<p align="center">
  <a href="https://github.com/brittytino/patchwork/issues/new?assignees=&labels=bug&template=bug_report.yml">
    <img alt="Report Bug" src="https://img.shields.io/badge/Report_Bug-red?style=for-the-badge&logo=openbugbounty&logoColor=white" />
  </a>
  <a href="https://github.com/brittytino/patchwork/issues/new?assignees=&labels=enhancement&template=feature_request.yml">
    <img alt="Request Feature" src="https://img.shields.io/badge/Request_Feature-orange?style=for-the-badge&logo=apachespark&logoColor=white" />
  </a>
  <a href="https://crowdin.com/project/patchwork-android">
    <img alt="Translate" src="https://img.shields.io/badge/Translate-30%2B_Languages-cyan?style=for-the-badge&logo=crowdin&logoColor=white" />
  </a>
  <a href="https://t.me/tidwib">
    <img alt="Join Community" src="https://img.shields.io/badge/Join_Community-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white" />
  </a>
</p>

## Navigation

- [What is Patchwork?](#what-is-patchwork)
- [Features](#features)
- [Requirements](#requirements)
- [Screenshots](#screenshots)
- [Shell Providers (Shizuku & Root)](#shell-providers-shizuku--root)
- [Accessibility Permissions](#how-to-grant-accessibility-permissions)
- [Localization](#localization)
- [Contributing](#contributing)

## What is Patchwork?

Patchwork is a feature-rich Android utility app designed for power users, enthusiasts, and anyone who wants more control over their device. Whether you're looking to automate tasks, customize your interface, enhance privacy, or unlock hidden system features—Patchwork has you covered.

### Why Patchwork?

- **🎯 All-in-One Solution**: No need for multiple apps. Patchwork consolidates dozens of useful tools into one cohesive experience.
- **🛡️ Privacy-First**: No data collection, no ads, no tracking. Everything runs locally on your device.
- **⚡ Performance Optimized**: Built with modern Android best practices, minimal battery impact.
- **🎨 Beautiful Design**: Material 3 design with full dark mode and pitch black theme support.
- **🔓 Open Source**: Fully transparent, community-driven development.
- **🌍 Multi-Language**: Available in 30+ languages thanks to community translators.
- **📱 Universal Compatibility**: Works on ALL Android devices (Android 8.0+), not just Pixel or Samsung!

### Perfect For:

- **All Android Users**: Works universally on Pixel, Samsung, Xiaomi, OnePlus, Motorola, and more!
- **Power Users**: Advanced customization and automation capabilities
- **Privacy-Conscious**: Control exactly what your device does and when
- **Developers**: Test and debug with enhanced tools
- **Everyone**: Improve daily usability with smart shortcuts and enhancements

# Features

Patchwork packs a powerful punch with features organized into intuitive categories:

## 🛠️ Productivity & Tools

### **Screen Off Widget** 📱
Invisible home screen widget that locks your device instantly—no more reaching for the power button.
- Customizable haptic feedback
- Multiple widget sizes
- Instant lock with accessibility permission
- *Requires: Accessibility Service*

### **Caffeinate** ☕
Keep your screen awake indefinitely or for a specific duration.
- Quick Settings tile for instant access
- Duration presets available
- Automatic timeout management
- Battery-conscious with proper handling
- *Requires: POST_NOTIFICATIONS permission*

### **Maps Power Saving Mode** 🗺️
Automatically triggers Google Maps power-saving mode, optimizing battery life when your screen turns off.
- Real-time power-saving detection
- Works with notification listener
- *Requires: Shizuku or Root + Notification Listener*

### **Location Reached (Travel Alarm)** 📍
Never miss your stop again. Set any destination and get intelligent proximity alerts.
- Real-time distance tracking
- Background location monitoring
- Lock screen notifications
- *Requires: Location + Background Location permissions*

### **DIY Automation** 🤖
Create powerful custom automations without coding:
- **Triggers**: Screen state changes, charging status, device unlock
- **Actions**: Launch apps, toggle settings, control flashlight, play sounds, send notifications
- **Conditions**: Combine multiple triggers with logic
- Full automation management with enable/disable controls
- App-based triggering support

### **Button Remap** 🎮
Transform your hardware buttons into powerful shortcuts:
- Long-press volume buttons (even when screen is off!)
- Configurable actions: Flashlight, and custom shortcuts
- Screen-on and screen-off profiles
- Works with Accessibility service or Shizuku/Root for enhanced functionality
- Customizable haptic feedback
- *Requires: Accessibility Service + Shizuku/Root*

### **App Freezing** ❄️
Pause rarely-used apps to save battery and free up system resources:
- One-tap freeze/unfreeze
- Auto-freeze on screen lock
- Frozen app shortcuts for quick access
- Batch freeze operations
- System app warnings to prevent issues
- *Requires: Shizuku or Root*

### **Watermark** 📷
Add custom watermarks to your photos:
- Text watermarks with customizable options
- EXIF data preservation
- Position and transparency controls
- Batch processing support

## 🔒 Security & Privacy

### **App Lock** 🔐
Protect sensitive apps with biometric authentication:
- Per-app locking with fingerprint/face unlock
- Quick Settings tile for easy enable/disable
- Auto-unlock until screen turns off
- Strong authentication methods only (FIDO2 certified)
- *Note: This is a 3rd party solution. For robust security, use Android's built-in Private Space or Secure Folder*

### **Screen Locked Security** 🛡️
Prevent unauthorized access to sensitive controls when your device is locked:
- Disable Quick Settings tiles when locked
- Block power menu access
- Protect network settings (WiFi, Mobile Data, Airplane Mode)
- Works on ALL Android devices with Device Admin capability
- *Requires: Accessibility Service + WRITE_SECURE_SETTINGS + Device Admin*

## 🎨 Visual Customization

### **Status Bar Icons** 📊
Control which system icons appear in your status bar with universal device support:
- Show/hide: WiFi, Mobile Data, Battery, Bluetooth, VPN, Alarm, and more
- Per-icon toggles with device-specific mapping
- Works on all Android manufacturers (Pixel, Samsung, Xiaomi, OnePlus, etc.)
- Automatic detection of available icons per device
- Persistent across reboots
- *Requires: WRITE_SECURE_SETTINGS permission (via Shizuku or ADB)*

### **Notification Lighting** 💡
Beautiful, customizable ambient lighting effects for notifications on ALL devices:
- **Styles**: Glow, Spinner, Stroke, Pulse
- Per-app color customization
- Flashlight pulse option
- Works universally with overlay system
- Screen-off notifications
- *Requires: Display Over Other Apps + Accessibility + Notification Listener*

### **Dynamic Night Light** 🌙
Automatically enable/disable Night Light based on the active app - works across all brands:
- Per-app Night Light profiles
- Supports multiple OEM implementations (AOSP, Samsung, Xiaomi, OnePlus, Motorola)
- Smooth transitions with automatic fallback
- Automatic scheduling
- Great for reading apps, social media, or any late-night usage
- *Requires: Accessibility Service + WRITE_SECURE_SETTINGS*

## ⚡ Quick Settings Tiles**Works on all Android devices:**

### System Controls
- **UI Blur**: Toggle system-wide blur effects (with fallback for unsupported devices)
- **Sensitive Content**: Hide notification details on lock screen
- **Tap to Wake**: Enable/disable tap-to-wake
- **AOD**: Toggle Always-On Display (where supported)
- **Mono Audio**: Switch to mono audio output
- **NFC**: Quick NFC toggle
- **Stay Awake**: Developer option for keeping screen on while charging
- **USB Debugging**: Quick toggle for USB Debugging
- **Private DNS**: Manage Private DNS settings

### Enhanced Controls
- **Caffeinate**: Keep screen awake with duration control
- **Sound Mode**: Cycle through Ring/Vibrate/Silent
- **Bubbles**: Toggle notification bubbles
- **Flashlight**: Quick flashlight toggle (with intensity control on Android 13+)
- **Flashlight Pulse**: Pulse flashlight for notifications

### Patchwork Features
- **Notification Lighting**: Toggle lighting service
- **Locked Security**: Toggle screen lock security
- **Dynamic Night Light**: Per-app Night Light control (universal)
- **App Freezing**: Quick freeze toggle (with Shizuku/Root)
- **App Lock**: Toggle app lock protection
- **Maps Power Saving**: Instant access to power-saving mode (with Shizuku/Root)

*Note: Most tiles work universally; WRITE_SECURE_SETTINGS required for some systemo power-saving mode

*Requires: WRITE_SECURE_SETTINGS for most tiles*

## 🎹 Bonus Features

### **System Keyboard** ⌨️
A fully functional Material 3 custom keyboard:
- Material 3 design with modern aesthetics
- Customizable keyboard height and padding
- Haptic feedback support
- Multiple language layout support
- Clipboard integration

### **Snooze System Notifications** 🔕
Automatically snooze persistent system notifications that can't be dismissed:
- Configurable snooze duration
- Smart notification handling
- Battery optimization integration
- *Requires: Notification Listener*

### **Batteries Monitor** 🔋
Track all your device battery levels in one place:
- Phone battery with detailed stats
- Connected Bluetooth devices battery levels
- Battery percentage indicators
- Real-time device monitoring
- *Requires: Bluetooth Connect + Bluetooth Scan permissions*

### **Sound Mode Tile** 🔊
Quick access to sound profile switching without entering settings.

# Requirements

### Minimum Requirements
- **Android 8.0 (Oreo)** or higher
- 50 MB free storage space

### Recommended
- **Shizuku or Root** for advanced features (optional but recommended)
- Works optimally on: Pixel, Samsung, Xiaomi, OnePlus, Motorola, Realme, Oppo, Vivo, Nothing Phone, and all AOSP-based ROMss work on any Android)
- **Shizuku or Root** for advanced features (optional but recommended)

### Permissions
Patchwork requires various permissions depending on which features you use:
- **Accessibility Service**: Required for Screen Off Widget, Button Remap, App Lock, Screen Locked Security, Dynamic Night Light, Notification Lighting
- **Notification Listener**: Required for Notification Lighting, Maps Power Saving, Snooze Notifications
- **Display Over Other Apps**: Required for overlays and lighting effects
- **Location**: Required for Location Reached (Travel Alarm)
- **Background Location**: Required for Location Reached background updates
- **Bluetooth**: Required for Batteries monitor
- **POST_NOTIFICATIONS**: Required for Caffeinate
- **WRITE_SECURE_SETTINGS**: Required for Status Bar Icons, Quick Settings Tiles, Dynamic Night Light (granted via Shizuku or ADB)
- **DEVICE_ADMIN**: Required for Screen Locked Security

**All permissions are optional** - grant only what you need for the features you want to use.

### Device Compatibility
Many of Patchwork's advanced features require elevated system privileges. You can choose between two methods:

## Shizuku (Recommended)

Shizuku provides a safe way to grant elevated permissions without rooting your device.

### Setup:
1. Download Shizuku from a maintained fork:
   - **Recommended**: [yangFenTuoZi/Shizuku](https://github.com/yangFenTuoZi/Shizuku)
   - ⚠️ **Avoid**: Older unmaintained versions (especially for Android 16+)

## Root Access

If your device is rooted, Patchwork can use root privileges directly.

### Advantages:
- No need for Shizuku
- Persistent access across reboots
- Slightly faster execution for some features

### Setup:
1. Ensure your device is rooted (Magisk recommended)
2. Grant Patchwork root permission when prompted
3. Root access will be used automatically for compatible features

## Without Shizuku or Root

Most Patchwork features work perfectly fine without elevated permissions! You'll still have access to:
- Quick Settings Tiles (basic)
- Visual customizations (Status Bar, Notification Lighting, Dynamic Night Light with limitations)
- Notification features (Snooze Notifications)
- Location Reached (Travel Alarm)
- Screen Off Widget
- Button Remap (limited functionality)
- App Lock
- Watermark
- System Keyboard
- Batteries Monitor
- And much more!

Only features specifically marked "Requires: Shizuku or Root" (Maps Power Saving, App Freezing) need elevated access.

**Note**: Patchwork is designed with universal compatibility in mind. All features work across different Android manufacturers with automatic device detection and graceful fallbacks for OEM-specific limitations.

# Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/a6a574ac-a1cb-44d5-b8c7-4fbca08647f1" width="19%" />
  <img src="https://github.com/user-attachments/assets/e2b20eff-a232-420c-be90-b1308363f4f7" width="19%" />
  <img src="https://github.com/user-attachments/assets/7fb98791-e4b4-43f1-98a8-4ab6aeb1d0f0" width="19%" />
  <img src="https://github.com/user-attachments/assets/f126681d-ba53-4e45-911c-c9cb69350848" width="19%" />
  <img src="https://github.com/user-attachments/assets/fc1593ec-ebf4-485a-bef4-7dd8ae6edb2a" width="19%" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/36dfaed1-89b5-4998-987b-40d5203598a7" width="19%" />
  <img src="https://github.com/user-attachments/assets/22e9deea-6fb8-43bc-9923-f711f5447c70" width="19%" />
  <img src="https://github.com/user-attachments/assets/d0eb5302-9432-4618-896b-7d9e5b7cf9cd" width="19%" />
  <img src="https://github.com/user-attachments/assets/50d762a5-5bc6-46e7-a9ce-9ccfc207957f" width="19%" />
  <img src="https://github.com/user-attachments/assets/d77c91c0-a1b3-45e4-9123-eee0f09a558f" width="19%" />
</p>

# Shell Providers (Shizuku & Root)

- Patchwork supports both **Shizuku** and **Root** as shell providers for executing advanced system-level commands.
- **Shizuku**: Make sure to get the latest version of Shizuku preferably from a fork such as [yangFenTuoZi/Shizuku](https://github.com/yangFenTuoZi/Shizuku) or other not from the Google Play as it is no longer well supported especially with Android 16 QPR1 up.
- **Root**: If your device is rooted, Patchwork can bypass Shizuku and use root privileges directly for features like Button Remap and App Freezing.

# How to grant accessibility permissions

<img width="1280" height="696" alt="image" src="https://github.com/user-attachments/assets/685115e7-4caa-4add-9196-d2e1e2c126a6" />

# Localization

Help us bring Patchwork to more people around the world! If you're fluent in another language, you can contribute by translating the app on Crowdin.

[![Crowdin](https://badges.crowdin.net/patchwork-android/localized.svg)](https://crowdin.com/project/patchwork-android)

[Support translation on Crowdin](https://crowdin.com/project/patchwork-android)

Patchwork is currently available in **30+ languages**, all thanks to our amazing community translators. If you're fluent in another language or want to improve existing translations, we'd love your contribution!

### How to Contribute:
1. Visit our [Crowdin project page](https://crowdin.com/project/patchwork-android)
2. Select your language (or request a new one)
3. Start translating!

No technical knowledge required—just language skills and a few minutes of your time. Every translation helps make Patchwork accessible to more users worldwide.

**Current Languages**: English, Spanish, French, German, Italian, Portuguese, Russian, Chinese, Japanese, Korean, Arabic, Hindi, and many more!

# Contributing

We welcome contributions from the community! 🤝

Whether you're:
- 🐛 Fixing bugs
- ✨ Adding new features
- 📝 Improving documentation
- 🌍 Translating to new languages
- 🎨 Enhancing UI/UX

Your help is greatly appreciated!

### Getting Started:
1. Read our [CONTRIBUTING.md](CONTRIBUTING.md) guide
2. Check out the [open issues](https://github.com/brittytino/patchwork/issues)
3. Join our [Telegram community](https://t.me/tidwib) for discussions
4. Fork the repo and start coding!

### Code of Conduct:
Please review our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing. We strive to maintain a welcoming and inclusive community.

### Feature Requests & Bug Reports:
- 🐛 **Found a bug?** [Report it here](https://github.com/brittytino/patchwork/issues/new?template=bug_report.md)
- 💡 **Have an idea?** [Request a feature](https://github.com/brittytino/patchwork/issues/new?template=feature_request.md)

## Project Philosophy

Patchwork exists because I wanted to extract the maximum potential from my Android device without root, while having a beautiful and cohesive experience. As a long-time Tasker user and Android enthusiast, I built this as an all-in-one solution for everything I wanted on my phone.

**Core Principles:**
- 🔓 **Freedom**: Users should have full control over their devices
- 🛡️ **Privacy**: No data collection, no telemetry, no analytics
- 🎯 **Quality**: Well-designed, performant, and reliable
- 🌍 **Community**: Open source, transparent development
- ⚡ **Innovation**: Push the boundaries of what's possible on Android

Built with Kotlin, Jetpack Compose, and lots of ☕ by [Tino Britty J](https://github.com/brittytino).

## Support the Project

If you find Patchwork useful, consider:
- ⭐ **Starring** this repository
- 🐛 **Reporting bugs** to help improve stability
- 💡 **Suggesting features** you'd like to see
- 🌍 **Contributing translations** in your language
- 📢 **Sharing** with fellow Android enthusiasts
- ☕ **Supporting development** (links on website)

Every bit of support helps keep the project alive and growing!

# Stars <3

<a href="https://star-history.com/#brittytino/patchwork&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=brittytino/patchwork&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=brittytino/patchwork&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=brittytino/patchwork&type=Date" />
 </picture>
</a>

---

<p align="center">
  Last updated: 2026-02-11
</p>


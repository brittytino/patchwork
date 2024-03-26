# Guardian Launcher

A strict Android launcher for children (ages 3–18) with parent-enforced control.

## What This Is

Guardian Launcher is a security-focused Android launcher designed to give parents complete control over their children's device usage. It replaces the default home screen with a locked-down environment that only allows parent-approved apps and activities.

### Key Features

- **Parent-Controlled App Access**: Only approved apps are visible and launchable
- **Time-Based Rules**: Set daily limits, allowed hours, and different rules for weekdays/weekends
- **Offline-First Architecture**: Core functionality works without internet connection
- **Device Owner Integration**: Support for full device management on dedicated devices
- **Usage Monitoring**: Track app usage and screen time
- **Minimal UI**: Clean, distraction-free interface for children

## What This Is NOT

- Not a game or entertainment app
- Not a reward system with gamification
- Not negotiable by the child
- Not designed for casual parental control (this is serious security)
- Not a toy - this is a professional-grade parental control solution

## Core Principles

1. **Parent Controls Everything**: No child access to settings or configuration
2. **Rules Over Personalization**: Restrictions cannot be bypassed
3. **Offline-First**: All core features work without internet
4. **Security by Design**: Blocks unauthorized access at multiple layers
5. **Age-Appropriate**: Scales from toddlers to teenagers

## System Requirements

### Minimum Requirements
- Android 8.0 (API 26) or higher
- 50 MB free storage
- 2 GB RAM minimum

### Recommended
- Android 10.0 (API 29) or higher
- Device Owner mode for maximum security
- Biometric authentication support for parent mode

### Supported Devices
- Android smartphones
- Android tablets  
- Android TV (limited support)

## Installation

### For End Users

1. Download the APK from [Releases](https://github.com/yourusername/guardian-launcher/releases)
2. Install on the child's device
3. Grant required permissions (Usage Stats, Device Admin)
4. Set as default launcher
5. Configure parent PIN/password
6. Set up approved apps and rules

### For Developers

```bash
# Clone the repository
git clone https://github.com/yourusername/guardian-launcher.git
cd guardian-launcher

# Build the project
./gradlew build

# Install on connected device
./gradlew installDebug
```

## Configuration

### Setting Up Parent Mode

1. Open Guardian Launcher
2. Access Parent Mode (default: hold home for 5 seconds)
3. Set up authentication (PIN, password, or biometric)
4. Configure approved apps
5. Set time rules and limits

### Configuring Time Rules

```kotlin
// Example: 2 hours daily, only between 9 AM - 6 PM on weekdays
TimeRuleBuilder()
    .name("School Days")
    .daysOfWeek(setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY))
    .timeRange(LocalTime.of(9, 0), LocalTime.of(18, 0))
    .maxDailyMinutes(120)
    .build()
```

### App-Specific Rules

```kotlin
// Example: Educational app with no time limit
AppRuleBuilder()
    .packageName("com.example.educationalapp")
    .allowed(true)
    .build()

// Example: Game with 30-minute daily limit
AppRuleBuilder()
    .packageName("com.example.game")
    .allowed(true)
    .maxDailyMinutes(30)
    .build()
```

## Architecture

### Project Structure

```
guardian-launcher/
├── app/src/main/java/com/guardian/launcher/
│   ├── core/          # Security, permissions, device owner
│   ├── launcher/      # Launcher UI (child mode)
│   ├── parent/        # Parent mode UI and controls
│   ├── child/         # Child mode enforcement
│   ├── rules/         # Rule engine and definitions
│   ├── data/          # Database and repositories
│   └── ui/            # Compose UI components
```

### Key Components

- **MainApplication**: Initializes security and rule systems
- **LauncherActivity**: Main child-facing home screen
- **ParentActivity**: Protected parent control interface
- **RuleEngine**: Evaluates all access rules
- **ChildModeController**: Enforces restrictions

## Security Features

- No backup allowed (prevents rule bypass)
- No cleartext traffic
- Parent authentication required for configuration
- Usage stats monitoring
- Device owner integration for maximum control
- Prevents uninstallation in child mode

## Privacy & Data

- All data stored locally on device
- No analytics or tracking
- No cloud services required
- No personal data collection
- Parents have full data access

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.

### Development Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Kotlin 1.9.20+
- Android SDK 34

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

### Why Apache 2.0?

We chose Apache 2.0 to encourage adoption and contribution while protecting contributors from liability. This allows commercial use and distribution while requiring attribution.

## Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/guardian-launcher/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/guardian-launcher/discussions)
- **Security**: See [SECURITY.md](SECURITY.md) for reporting vulnerabilities

## Roadmap

### Version 0.1.0 (MVP)
- [x] Basic launcher functionality
- [x] Parent authentication
- [x] App whitelist
- [ ] Time rules
- [ ] Usage tracking

### Version 0.2.0
- [ ] Internet filtering
- [ ] Content restrictions
- [ ] Emergency override
- [ ] Backup/restore settings

### Version 1.0.0
- [ ] Device Owner integration
- [ ] Remote management (optional)
- [ ] Multiple child profiles
- [ ] Usage reports and analytics

## FAQ

**Q: Can a child uninstall this?**  
A: Not if properly configured with Device Owner mode. Otherwise, parents should disable uninstallation in device settings.

**Q: Does this work offline?**  
A: Yes. All core features work without internet connection.

**Q: Can this block inappropriate content?**  
A: It controls which apps can be launched. Content filtering within approved apps requires additional solutions.

**Q: Is this suitable for teenagers?**  
A: Yes, but rules should be age-appropriate. Teenagers need more autonomy than toddlers.

## Credits

Built with:
- Kotlin
- Jetpack Compose
- Room Database
- Material Design 3

## Disclaimer

This software is provided as-is. Parents are responsible for monitoring their children's device usage and configuring appropriate restrictions. This app is a tool, not a replacement for parental supervision.

---

**Guardian Launcher** - Because children's safety is not negotiable.

<!-- commit on 2024-01-01 -->

<!-- commit on 2024-01-03 -->

<!-- commit on 2024-01-05 -->

<!-- commit on 2024-01-08 -->

<!-- commit on 2024-01-10 -->

<!-- commit on 2024-01-13 -->

<!-- commit on 2024-01-15 -->

<!-- commit on 2024-01-17 -->

<!-- commit on 2024-01-19 -->

<!-- commit on 2024-01-20 -->

<!-- commit on 2024-01-22 -->

<!-- commit on 2024-01-24 -->

<!-- commit on 2024-01-26 -->

<!-- commit on 2024-01-27 -->

<!-- commit on 2024-01-29 -->

<!-- commit on 2024-01-31 -->

<!-- commit on 2024-02-01 -->

<!-- commit on 2024-02-03 -->

<!-- commit on 2024-02-06 -->

<!-- commit on 2024-02-08 -->

<!-- commit on 2024-02-10 -->

<!-- commit on 2024-02-11 -->

<!-- commit on 2024-02-13 -->

<!-- commit on 2024-02-14 -->

<!-- commit on 2024-02-15 -->

<!-- commit on 2024-02-17 -->

<!-- commit on 2024-02-18 -->

<!-- commit on 2024-02-20 -->

<!-- commit on 2024-02-22 -->

<!-- commit on 2024-02-24 -->

<!-- commit on 2024-02-25 -->

<!-- commit on 2024-02-27 -->

<!-- commit on 2024-02-29 -->

<!-- commit on 2024-03-01 -->

<!-- commit on 2024-03-02 -->

<!-- commit on 2024-03-03 -->

<!-- commit on 2024-03-04 -->

<!-- commit on 2024-03-05 -->

<!-- commit on 2024-03-06 -->

<!-- commit on 2024-03-07 -->

<!-- commit on 2024-03-08 -->

<!-- commit on 2024-03-09 -->

<!-- commit on 2024-03-10 -->

<!-- commit on 2024-03-11 -->

<!-- commit on 2024-03-12 -->

<!-- commit on 2024-03-13 -->

<!-- commit on 2024-03-15 -->

<!-- commit on 2024-03-17 -->

<!-- commit on 2024-03-19 -->

<!-- commit on 2024-03-21 -->

<!-- commit on 2024-03-23 -->

<!-- commit on 2024-03-26 -->

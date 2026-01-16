# Contributing to Guardian Launcher

Thank you for your interest in contributing to Guardian Launcher. This is a serious security-focused project for child safety, so we have strict standards.

## Code of Conduct

### Our Standards

- Professional and respectful communication
- Focus on child safety above all else
- No political or social agendas
- Technical merit drives decisions
- Security first, features second

### Unacceptable Behavior

- Inappropriate content or language
- Attempts to weaken security features
- Harassment or discrimination
- Politicizing the project
- Adding tracking or telemetry without explicit consent

## How to Contribute

### Reporting Bugs

1. Check if the bug already exists in [Issues](https://github.com/brittytino/guardian-launcher/issues)
2. Create a new issue with:
   - Clear description of the bug
   - Steps to reproduce
   - Expected vs actual behavior
   - Device and Android version
   - Relevant logs (sanitized of personal data)

### Suggesting Features

1. Check [Discussions](https://github.com/brittytino/guardian-launcher/discussions) first
2. Open a new discussion with:
   - Clear use case
   - Why it's needed
   - How it improves child safety
   - Potential implementation approach

### Pull Requests

#### Before You Start

1. Discuss major changes in an issue first
2. Check that your change aligns with project principles
3. Ensure you can test on a real Android device
4. Read the codebase to understand the architecture

#### Development Setup

```bash
# Fork and clone
git clone https://github.com/brittytino/guardian-launcher.git
cd guardian-launcher

# Create a branch
git checkout -b feature/your-feature-name

# Build and test
./gradlew build
./gradlew test
```

#### Code Standards

**Kotlin Style**
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Document public APIs

**Security Standards**
- Never weaken existing security features
- All user data must be encrypted at rest
- No cleartext credentials or sensitive data
- Validate all inputs
- Assume adversarial usage

**Testing Standards**
- Write unit tests for business logic
- Test security-critical paths thoroughly
- Include edge cases and error conditions
- Test on multiple Android versions

#### Commit Messages

```
type(scope): brief description

Detailed explanation of what changed and why.

Closes #issue_number
```

Types: `feat`, `fix`, `security`, `refactor`, `docs`, `test`, `chore`

Examples:
- `feat(rules): add internet filtering rule type`
- `fix(auth): prevent parent PIN bypass on Android 12`
- `security(launcher): block task switching in child mode`

#### Pull Request Process

1. Update documentation if needed
2. Add tests for new functionality
3. Ensure all tests pass locally
4. Update CHANGELOG.md
5. Submit PR with clear description
6. Respond to review feedback promptly

**PR Description Template:**

```markdown
## Description
What does this PR do?

## Motivation
Why is this change needed?

## Testing
How was this tested?

## Screenshots (if UI change)
Before/After images

## Checklist
- [ ] Tests pass locally
- [ ] Documentation updated
- [ ] No security regressions
- [ ] Follows code style
- [ ] CHANGELOG.md updated
```

## Architecture Guidelines

### Package Structure

```
com.guardian.launcher/
├── core/          # Non-UI fundamentals only
├── launcher/      # Child-facing UI
├── parent/        # Parent-facing UI
├── child/         # Child mode enforcement
├── rules/         # Rule engine
├── data/          # Persistence layer
└── ui/            # Reusable UI components
```

**Rules:**
- No UI code in `core/`
- No business logic in `ui/`
- Each package should be independently understandable

### Dependencies

We are **extremely conservative** with dependencies:

**Allowed:**
- AndroidX libraries (official Google support)
- Kotlin stdlib
- Room, Compose, Lifecycle (Jetpack)
- Security-crypto library

**Not Allowed Without Discussion:**
- Third-party analytics
- Crash reporting (unless opt-in)
- Ad libraries
- Social media SDKs
- Unnecessary utility libraries

**Adding a Dependency:**
1. Justify why it's absolutely necessary
2. Verify the license is compatible
3. Check for known security vulnerabilities
4. Consider the maintenance burden

### Database Schema Changes

1. Always provide a migration path
2. Never lose user data
3. Test migration from all previous versions
4. Document schema in comments

### Security Critical Areas

These areas require extra scrutiny:

- Parent authentication
- Rule evaluation
- Child mode enforcement
- Device owner integration
- Permission handling
- Data encryption

**For changes to these areas:**
- Security review is mandatory
- Multiple developers must review
- Test on multiple Android versions
- Consider adversarial scenarios

## Testing Requirements

### Unit Tests
- All business logic must have unit tests
- Aim for >80% code coverage
- Test edge cases and error conditions

### Integration Tests
- Test component interactions
- Test database operations
- Test rule evaluation

### Manual Testing Checklist
- Test on real devices (not just emulators)
- Test on different Android versions
- Try to break security features
- Test parent and child modes separately

## Documentation

### Code Documentation

```kotlin
/**
 * Brief description of what this does.
 *
 * More detailed explanation if needed.
 *
 * @param paramName Description of parameter
 * @return Description of return value
 * @throws ExceptionType When this exception is thrown
 */
fun functionName(paramName: Type): ReturnType
```

### User Documentation
- Update README.md for user-facing changes
- Add examples for new features
- Keep language clear and simple
- Assume parents are not developers

## Review Process

1. **Automated Checks**: CI must pass
2. **Code Review**: At least one maintainer approval
3. **Security Review**: Required for security-critical changes
4. **Testing**: All tests must pass
5. **Documentation**: Must be complete and clear

## Questions?

- Open a [Discussion](https://github.com/brittytino/guardian-launcher/discussions)
- Comment on relevant issues
- Be patient - we prioritize thoroughness over speed

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

---

Thank you for helping make Guardian Launcher better and keeping children safer.

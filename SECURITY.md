# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 0.1.x   | :white_check_mark: |
| < 0.1   | :x:                |

## Reporting a Vulnerability

**DO NOT** report security vulnerabilities through public GitHub issues.

### How to Report

Send a detailed report to: **security@brittytino.com**

Or use GitHub Security Advisories:
https://github.com/brittytino/guardian-launcher/security/advisories/new

### What to Include

1. **Description**: What is the vulnerability?
2. **Impact**: What can an attacker do?
3. **Affected Versions**: Which versions are vulnerable?
4. **Steps to Reproduce**: Detailed reproduction steps
5. **Proof of Concept**: Code or screenshots if applicable
6. **Suggested Fix**: If you have one

### Example Report Template

```
Summary: Child can bypass launcher by [method]

Impact: HIGH - Child can exit child mode without parent authentication

Affected Versions: 0.1.0 - 0.1.5

Steps to Reproduce:
1. Enable child mode
2. Do [specific action]
3. Device exits to normal home screen

Expected Behavior: Child should remain in launcher

Environment:
- Device: Samsung Galaxy A52
- Android Version: 12
- Guardian Launcher Version: 0.1.3

Suggested Fix: Add check for [specific scenario]
```

## Vulnerability Categories

### Critical

- Child can exit launcher without authentication
- Child can uninstall app
- Child can access parent mode
- Child can modify rules
- Parent authentication bypass
- Data encryption weakness

### High

- Child can access unapproved apps
- Time limits can be bypassed
- Usage tracking can be disabled
- Rule enforcement failures

### Medium

- UI reveals sensitive information
- Logging exposes data
- Permission issues
- Crash leads to escape

### Low

- UI/UX issues that reduce security
- Documentation errors
- Non-security bugs

## Response Timeline

- **Critical**: Response within 24 hours, fix within 7 days
- **High**: Response within 48 hours, fix within 14 days
- **Medium**: Response within 5 days, fix within 30 days
- **Low**: Response within 7 days, fix in next release

## Disclosure Policy

### Coordinated Disclosure

We follow coordinated disclosure:

1. **Report received**: We acknowledge receipt
2. **Investigation**: We verify and assess impact
3. **Fix development**: We develop and test a fix
4. **Pre-release**: We notify the reporter
5. **Public disclosure**: 90 days after fix release or by mutual agreement

### Public Disclosure

After a fix is released:

1. We publish a security advisory
2. We credit the reporter (if they wish)
3. We update CHANGELOG.md
4. We notify users to update

## Security Features

### Current Security Measures

1. **Authentication**
   - Parent mode requires PIN/password/biometric
   - No default credentials
   - Rate limiting on authentication attempts

2. **App Control**
   - Whitelist-only approach
   - Package name validation
   - Intent filtering

3. **Data Protection**
   - Encrypted data at rest
   - No cloud storage by default
   - No backup allowed

4. **System Integration**
   - Launcher lock mode
   - Device owner support
   - Usage stats monitoring

5. **Code Security**
   - ProGuard enabled for release builds
   - No hardcoded secrets
   - Input validation throughout

### Known Limitations

1. **Without Device Owner Mode**
   - Child can uninstall via Settings (if allowed by device)
   - System Settings accessible if child knows how
   - Can switch launchers if device allows

2. **Android Version Specific**
   - Some features require Android 10+
   - Device owner setup varies by manufacturer
   - Some OEMs have workarounds we can't prevent

3. **Physical Access**
   - Factory reset will remove app
   - Safe mode boot may bypass
   - ADB access can bypass if enabled

### Recommended Setup

For maximum security:

1. Enable Device Owner mode
2. Disable ADB debugging
3. Disable developer options
4. Remove parent app from child's view
5. Use biometric authentication
6. Regularly update the app

## Security Hardening Checklist

### For Users

- [ ] Device Owner mode enabled
- [ ] Developer options disabled
- [ ] Unknown sources disabled
- [ ] USB debugging disabled
- [ ] Factory reset protection enabled
- [ ] Screen lock configured
- [ ] Guardian Launcher set as default
- [ ] Parent authentication configured
- [ ] Regular updates installed

### For Developers

- [ ] No hardcoded credentials
- [ ] All inputs validated
- [ ] Sensitive data encrypted
- [ ] Secure random for crypto
- [ ] Certificate pinning (if network features)
- [ ] ProGuard enabled
- [ ] No debug logging in release
- [ ] Security tests passing

## Bug Bounty

Currently, we do not have a formal bug bounty program. However:

- We deeply appreciate security research
- We will credit researchers in release notes
- We may offer recognition or swag for significant findings
- We're exploring a formal program for the future

## Contact

- **Security Email**: security@brittytino.com
- **GitHub Security**: Use private security advisories
- **PGP Key**: [If you set one up, link here]

## Past Security Advisories

None yet - this is the initial release.

Future advisories will be listed here and published at:
https://github.com/brittytino/guardian-launcher/security/advisories

## Security-First Development

This project is built with security as a core principle:

- Security review required for critical changes
- Multiple reviewers for authentication code
- Adversarial thinking in design
- Regular security audits (planned)
- Penetration testing (future)

## Questions?

For non-security questions, use:
- GitHub Discussions
- GitHub Issues (non-security only)

For security questions:
- Email security@brittytino.com
- Use private channels only

---

**Remember**: This app protects children. Security issues are treated as highest priority.

Thank you for helping keep Guardian Launcher secure.

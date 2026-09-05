# Bizora Release Checklist

## Required for every release

- Update the version in `pom.xml` and visible release notes.
- Run `./mvnw clean test` and confirm all tests pass.
- Push a version tag such as `v1.0.0` and confirm the `Publish Release` workflow succeeds.
- Download the EXE, DEB, RPM, and checksum files from the resulting GitHub Release.
- Install on clean Windows 10 and Windows 11 machines without a separate Java installation.
- Install the DEB on a clean supported Ubuntu/Debian system and the RPM on a clean supported Fedora/RHEL system.
- Complete a sale and verify stock updates immediately.
- Export and open a PDF report.
- Create and restore a backup using disposable test data.
- Test 58 mm and 80 mm receipt printing on the printer models being supported.
- Verify the database is stored under `%APPDATA%\Bizora`, not in the installation folder.
- Scan the installer with Microsoft Defender and verify its SHA-256 file.
- Sign the Windows installer before broad public distribution.

## Signing secrets for GitHub Actions

- `WINDOWS_CERTIFICATE_BASE64`: Base64-encoded code-signing PFX file.
- `WINDOWS_CERTIFICATE_PASSWORD`: Password for that PFX file.

The workflow still creates a usable unsigned installer when these secrets are
absent. Windows SmartScreen will warn users and some enterprise policies may
block it, so code-sign before broad commercial distribution.

## Release acceptance

- Confirm business settings, tax, currency, invoice prefix, and optional printer settings persist after restart.
- Confirm upgrading keeps the existing `%APPDATA%\Bizora\bizora.db` database and backups.
- Confirm uninstalling the application does not delete business data.
- Record the installer name, version, SHA-256, signing certificate, and test machines used.

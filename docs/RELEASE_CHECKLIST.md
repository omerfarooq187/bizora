# Bizora Release Checklist

## Required for every release

- Update the version in `pom.xml` and visible release notes.
- Run `./mvnw clean test` and confirm all tests pass.
- Create the Windows installer using the `Build Windows Installer` workflow.
- Install on clean Windows 10 and Windows 11 machines without a separate Java installation.
- Complete a sale and verify stock updates immediately.
- Export and open a PDF report.
- Create and restore a backup using disposable test data.
- Test 58 mm and 80 mm receipt printing on the printer models being supported.
- Verify the database is stored under `%APPDATA%\Bizora`, not in the installation folder.
- Scan the installer with Microsoft Defender and verify its SHA-256 file.
- Sign the installer before public distribution.

## Signing secrets for GitHub Actions

- `WINDOWS_CERTIFICATE_BASE64`: Base64-encoded code-signing PFX file.
- `WINDOWS_CERTIFICATE_PASSWORD`: Password for that PFX file.

The workflow still creates an unsigned test installer when these secrets are
absent. Do not publish that artifact as a production release because Windows
SmartScreen will warn users and enterprise policies may block it.

## Release acceptance

- Confirm business settings, tax, currency, invoice prefix, and optional printer settings persist after restart.
- Confirm upgrading keeps the existing `%APPDATA%\Bizora\bizora.db` database and backups.
- Confirm uninstalling the application does not delete business data.
- Record the installer name, version, SHA-256, signing certificate, and test machines used.

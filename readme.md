# Bizora

Bizora is an offline-first business management application designed for small businesses and shopkeepers.

## Download

Download the newest installers from the
**[Bizora Releases page](https://github.com/omerfarooq187/bizora/releases/latest)**.

| Platform | Download | Requirements |
|---|---|---|
| Windows | `Bizora-1.0.1.exe` | 64-bit Windows 10 or 11 |
| Ubuntu / Debian | `bizora_1.0.1-1_amd64.deb` | 64-bit Linux |
| Fedora / RHEL | `bizora-1.0.1-1.x86_64.rpm` | 64-bit Linux |

Java is bundled inside every installer. Windows may display an “Unknown
publisher” warning until the Windows release is code-signed.

### Install on Windows

Download `Bizora-1.0.1.exe`, copy it by USB if needed, and run it. The setup
wizard can create Start Menu and desktop shortcuts.

### Install on Ubuntu or Debian

```bash
sudo apt install ./bizora_1.0.1-1_amd64.deb
```

### Install on Fedora or RHEL

```bash
sudo dnf install ./bizora-1.0.1-1.x86_64.rpm
```

## Tech Stack

- Java 21
- JavaFX 21
- Maven
- SQLite
- JUnit
- Ikonli
- ControlsFX

## Architecture

Bizora follows a layered architecture:

Controller
→ Service
→ Repository
→ Database

## Features

- Product management
- Inventory management
- Purchase management
- Supplier management
- Customer management
- Sales management
- Payments
- Expenses
- Inventory movements
- Daily local backups and guided restore
- Date-range PDF financial reports
- Optional 58 mm and 80 mm receipt printing

## Database

Bizora uses SQLite for local offline-first storage.

Database schema is managed through versioned migrations.

## Development

Requirements:

- JDK 21
- Maven

Run:

```bash
./mvnw clean javafx:run
```

Run the test suite:

```bash
./mvnw test
```

JavaFX view-loading tests require a graphical environment. They are skipped
automatically on headless machines; use Xvfb or a desktop session to include
them in a CI run.

## Application Data

Bizora stores its SQLite database in the current user's application-data
directory:

- Linux and macOS: `~/.local/share/Bizora/bizora.db`
- Windows: `%APPDATA%\\Bizora\\bizora.db`

Business identity, currency, tax, and receipt preferences are persisted using
the operating system's Java preferences store.

## Building installers

Bizora installers are self-contained. Users do not need to install Java separately.

### Recommended Windows package

Use the `.exe` installer for normal Windows 10 and Windows 11 customers. It
provides the familiar setup wizard, optional desktop shortcut, Start Menu entry,
per-user installation, and an embedded Java runtime.

An `.msi` can be generated for businesses that specifically require managed or
silent enterprise deployment.

Windows packaging must run on Windows with JDK 21 and WiX Toolset 3 installed:

```powershell
.\packaging\windows\build-installer.ps1 -Type exe
```

The output and its SHA-256 checksum are written to `target\installer`. The
`Build Windows Installer` GitHub Actions workflow performs the same clean build
on a Windows runner when manually started.

Build Linux `.deb` and `.rpm` installers on Linux with:

```bash
./packaging/linux/build-installers.sh
```

The `Publish Release` workflow builds all three formats and attaches them to a
GitHub Release when a matching version tag such as `v1.0.0` is pushed.

For a production release, configure the signing secrets documented in
[`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md). Unsigned Windows
installers remain usable, but should be signed before broad commercial
distribution to avoid SmartScreen warnings.

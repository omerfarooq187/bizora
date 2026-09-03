# Bizora

Bizora is an offline-first business management application designed for small businesses and shopkeepers.

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

## Packaging

Bizora is distributed as a self-contained Windows installer. Users do not need
to install Java separately.

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
on a Windows runner whenever it is manually started or a `v*` tag is pushed.

For a production release, configure the signing secrets documented in
[`docs/RELEASE_CHECKLIST.md`](docs/RELEASE_CHECKLIST.md). Do not distribute an
unsigned installer to customers.

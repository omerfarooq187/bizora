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

Create a trimmed runtime image with:

```bash
./mvnw clean javafx:jlink
```

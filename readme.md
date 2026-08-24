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
mvn clean javafx:run
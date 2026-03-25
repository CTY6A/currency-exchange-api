# 💱 Currency Exchange REST API

A REST API service for managing currencies and calculating exchange rates. This project implements a classic multi-layer MVCS architecture.

## 📋 Overview

A REST API for describing currencies and exchange rates. Allows viewing and editing lists of currencies and exchange rates, and performing conversion calculations of arbitrary amounts from one currency to another.

## 📡 API Endpoints

### Currencies

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/currencies` | Get list of all currencies |
| `GET` | `/currency/EUR` | Get specific currency by code |
| `POST` | `/currencies` | Add new currency (x-www-form-urlencoded) |

### Exchange Rates

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/exchangeRates` | Get list of all exchange rates |
| `GET` | `/exchangeRate/USDEUR` | Get specific exchange rate |
| `POST` | `/exchangeRates` | Add new exchange rate (x-www-form-urlencoded) |
| `PATCH`| `/exchangeRate/USDEUR` | Update existing exchange rate |

### Currency Exchange

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/exchange?from=USD&to=EUR&amount=10` | Calculate currency conversion |

## 🗄️ Database Schema

This project uses SQLite as an embedded database.

### Currencies Table

| Column | Type | Comment |
|--------|------|---------|
| ID | INT | Currency ID, auto-increment, primary key |
| Code | VARCHAR | Currency code |
| FullName | VARCHAR | Full currency name |
| Sign | VARCHAR | Currency symbol |

**Indexes:**
- Primary key on ID field
- Unique index on Code field

### ExchangeRates Table

| Column | Type | Comment |
|--------|------|---------|
| ID | INT | Exchange rate ID, auto-increment, primary key |
| BaseCurrencyId | INT | Base currency ID, foreign key to Currencies.ID |
| TargetCurrencyId | INT | Target currency ID, foreign key to Currencies.ID |
| Rate | DECIMAL(6) | Exchange rate of base currency unit to target currency unit |

**Indexes:**
- Primary key on ID field
- Unique index on (BaseCurrencyId, TargetCurrencyId) pair

## 🔄 Exchange Rate Logic

Currency conversion can be obtained through one of three scenarios:

1. **Direct pair exists** - Exchange rate AB exists in the table
2. **Inverse pair exists** - Exchange rate BA exists, calculate inverse
3. **Cross-rate via USD** - Both USD-A and USD-B exist, calculate cross-rate

## 📊 HTTP Response Codes

### Currencies

| Endpoint | Success | Errors |
|----------|---------|--------|
| `GET /currencies` | 200 | 500 |
| `GET /currency/{code}` | 200 | 400, 404, 500 |
| `POST /currencies` | 201 | 400, 409, 500 |

### Exchange Rates

| Endpoint | Success | Errors |
|----------|---------|--------|
| `GET /exchangeRates` | 200 | 500 |
| `GET /exchangeRate/{pair}` | 200 | 400, 404, 500 |
| `POST /exchangeRates` | 201 | 400, 404, 409, 500 |
| `PATCH /exchangeRate/{pair}` | 200 | 400, 404, 500 |

### Currency Exchange

| Endpoint | Success | Errors |
|----------|---------|--------|
| `GET /exchange` | 200 | 400, 404, 500 |

### Error Response Format

All errors return JSON:
```json
{
    "message": "Error description"
}
```

## 🚀 Installation and Deployment

The application is compiled as a `.war` artifact and is designed to run in a servlet container (e.g., Apache Tomcat). Project has the embedded SQLite database, no separate SQL server is required.

### Requirements
- Java 17+
- Maven
- Apache Tomcat 10.x (Jakarta EE 10)

### Running the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/CTY6A/currency-exchange.git
   cd currency-exchange
   ```
2. Build the `.war` artifact with Maven:
   ```bash
   mvn clean package
   ```
3. Copy the built file from `target/currency-exchange-1.0-SNAPSHOT.war` to your Tomcat's `webapps/` folder.

4. Start Tomcat. The application will automatically create the SQLite database.

5. The API will be available at: `http://localhost:8080/`

## 🎯 Implementation Highlights

This project follows SOLID, DRY, and Clean Code principles.

- **Presentation Layer (Servlets):** Servlets remain as "thin" as possible. They only handle HTTP requests, delegate logic to services, and return JSON responses.
- **Service Layer:** Contains all business logic (including cross-rate calculation).
- **Data Access Layer (Repository):** Implements safe JDBC operations with PreparedStatement and try-with-resources.
- **DTO Pattern & MapStruct:** Separation of database models from Data Transfer Objects.

## 🛠️ Technologies

- **Java 17**
- **Maven**
- **Git**
- **Jakarta Servlets 6.0 (Tomcat 10+)**
- **SQLite & JDBC**
- **HikariCP**
- **MapStruct**
- **Jackson**

## 📁 Project Structure

```
├───main
│   ├───java
│   │   └───com.stubedavd
│   │       ├───dto           // Data Transfer Objects
│   │       │   ├───request   // Request DTOs
│   │       │   └───response  // Response DTOs
│   │       ├───exception     // Custom API exceptions
│   │       ├───filter        // Web filters
│   │       ├───listener      // Application initialization
│   │       ├───mapper        // MapStruct interfaces
│   │       ├───model         // Domain entities
│   │       ├───repository    // Data access layer
│   │       │   └───impl      // JDBC implementations
│   │       ├───service       // Business logic layer
│   │       │   └───impl      // Service implementations
│   │       ├───servlet        // Controllers
│   │       │   ├───currency   // Currency endpoints
│   │       │   └───exchange   // Exchange endpoints
│   │       └───utils         // Utilities
│   ├───resources
│   │   └───currencies.db     // SQLite database
│   └───webapp                 // Web context
```

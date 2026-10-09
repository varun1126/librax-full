# 📚 LibraX — Intelligent Library Management Platform

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19.2-61DAFB.svg)](https://react.dev/)
[![Tailwind CSS v4](https://img.shields.io/badge/Tailwind%20CSS-v4.3-38B2AC.svg)](https://tailwindcss.com/)
[![SQLite WAL](https://img.shields.io/badge/Database-SQLite%20WAL-003B57.svg)](https://www.sqlite.org/)
[![Currency](https://img.shields.io/badge/Currency-₹%20INR-orange.svg)](#)

> **LibraX** is a full-stack, enterprise-grade Library Management Platform engineered for colleges and universities. Featuring dual role-based student and administrator portals, live Google Books API integration, automated fine calculation in Indian Rupees (**₹**), an interactive AI Librarian Assistant, and a zero-configuration SQLite WAL relational engine.

---

## ⚡ Fast Start (Single VS Code Terminal)

You only need **ONE** command in your VS Code terminal to run the full application (backend + frontend together):

```powershell
mvn spring-boot:run
```

Then open your browser to:
👉 **[http://localhost:8085](http://localhost:8085)**

*(The pre-built React frontend and Spring Boot REST API are served together on port 8085. No separate frontend terminal is needed.)*

For all commands and troubleshooting, see **[`COMMANDS.md`](file:///c:/Users/harsh/Downloads/librax-full/COMMANDS.md)**.

---

## 🔑 Default Credentials

| Portal / Role | Identifier | Password | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **Administrator / Librarian** | `admin` | `Admin@123` | Catalog curation, Google Books live API search & import, Student Directory, Loan checkout/return/renewal, Cashiering in ₹ (UPI/Cash/Card/Online), Administrative fine waivers, and Audit Logs. |
| **Student Portal** | `student01` | `Student@123` | Book search, Active Loans tracking, Overdue Dues ledger (₹ INR), Book Reservations & Queue, and AI Librarian Assistant widget. |
| **New Student Self-Registration** | *(Self-Service)* | *(Custom)* | Click **"Register here"** on the Login screen (`/login`). Automatically generates a unique Library Card Number (`LIB-STU-XXXX`) and immediate borrowing access. |

---

## 🌟 Key Platform Features

### 1. 🇮🇳 Indian University Currency Localization (₹ INR)
* Daily overdue fines calculated automatically at **₹1.00 per day** (configurable in circulation policies).
* Cashier settlement supports **UPI**, **Cash**, **Debit/Credit Card**, and **Online NetBanking**.
* Administrative waivers with mandatory reasoning recorded in the immutable audit trail.
* Rupee symbol (**`₹`**) seamlessly rendered across student ledgers, admin KPI tiles, and CSV reports.

### 2. 🌐 Google Books API Live Integration
* Search millions of published books by title, author, or ISBN.
* Live preview of high-definition cover art, publisher, publication date, page count, and book synopsis.
* 1-Click import directly allocates physical copy barcodes and adds the title to the college database.

### 3. 🤖 AI Librarian Assistant
* Real-time conversational widget embedded across Student and Admin portals.
* Answers policy queries, fine rates (₹1/day), checkout limits, and book reservation procedures with live interactive guidance.

### 4. 🛡️ Enterprise Security & RBAC
* Passwords salted and hashed with **BCrypt (cost 12)**.
* Password hashes strictly excluded from JSON API responses (`@JsonIgnore` / `WRITE_ONLY`).
* Stateless **HMAC-SHA256 JWT** authentication with configurable keys (`LIBRAX_JWT_SECRET`).
* Server-side authorization interceptor enforcing granular permissions on all endpoints.
* Protection against Insecure Direct Object References (IDOR) on student renewals, cancellations, and fines.

### 5. ⚡ High-Concurrency SQLite WAL Engine
* Embedded SQLite engine configured in **Write-Ahead Logging (WAL)** mode with busy timeouts.
* Benchmarked across 20 simultaneous threads with **0 lock errors** (`SQLITE_BUSY`) and < 0.6s execution time.

---

## 📖 Project Documentation

| Document | Description |
| :--- | :--- |
| **[`COMMANDS.md`](file:///c:/Users/harsh/Downloads/librax-full/COMMANDS.md)** | **Commands Reference**: Clean terminal command sheet for running, building, testing, and troubleshooting. |
| **[`docs/ARCHITECTURE.md`](file:///c:/Users/harsh/Downloads/librax-full/docs/ARCHITECTURE.md)** | System architecture diagram, request flow, and security layers. |
| **[`docs/DATABASE.md`](file:///c:/Users/harsh/Downloads/librax-full/docs/DATABASE.md)** | Relational database schema, ER diagram, 13 table definitions, and constraints. |
| **[`docs/API.md`](file:///c:/Users/harsh/Downloads/librax-full/docs/API.md)** | REST API endpoints, request/response formats, and status codes. |

---

## 🧪 Testing

Run the automated test suite with Maven:
```powershell
mvn test
```
All automated unit, security audit, and concurrency benchmark tests execute with 100% pass rates.

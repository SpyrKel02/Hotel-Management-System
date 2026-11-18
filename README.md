# Hotel Management System

A desktop hotel management application built with **Java (JavaFX)**, **Hibernate ORM**, and **PostgreSQL**. The system supports full management of customers, reservations, rooms, extra services, dynamic reporting, and automated database-level audit logging.

---

## Technologies & Architecture

* **Programming Language:** Java 17+
* **User Interface (UI):** JavaFX
* **ORM / Persistence:** Hibernate ORM (HQL, `JOIN FETCH`, Transaction Management)
* **Database:** PostgreSQL (Stored Procedures & Database Triggers)
* **Architecture:** Layered Architecture (`UI Views` ➔ `Controllers` ➔ `DAOs` ➔ `Entities / PostgreSQL`)

---

## Key Features

### 1. Make a Reservation (`BookingView`)
* Real-time display of available rooms (`Available`).
* Selection of Check-in / Check-out dates, number of adults/children, and extra services (e.g., *Breakfast Buffet, Spa, Sauna & Jacuzzi*).
* Automatic customer lookup or registration via email.
* **Stored Procedure Cost Calculation:** Invokes the `calculate_reservation_total` database stored procedure to automatically compute the total reservation price.
* **Duplicate Booking Prevention:** Blocks a customer from creating a second active (`Confirmed`) reservation (validated by Email, Full Name, and Customer ID).

### 2. Customer Management (`CustomerView`)
* Full CRUD operations (View, Add, Edit, Delete customers).
* Detailed view of each customer's reservation history and selected services.
* **Deletion Constraints:** Checks for existing dependencies before deletion (prevents deleting customers with active reservations and prompts for confirmation).

### 3. Reservation History & Management (`MyBookingsView`)
* Master table displaying all reservations along with their associated extra services (`LEFT JOIN FETCH` to prevent `LazyInitializationException`).
* Dynamic filtering by customer email and reservation status (`Confirmed` / `Cancelled`).
* **Safe Cancellation:** Updates the reservation status to `Cancelled` and automatically resets the assigned room's status back to `Available` within a single atomic transaction.

### 4. Dynamic Search & Statistics (`SearchQueriesView`)
5 parameterized HQL queries with dynamically adapting result columns:
* **Q1:** Search reservations by date range and status.
* **Q2:** Aggregated statistics and revenue by room type (`GROUP BY`, `COUNT`, `SUM`, `AVG`).
* **Q3:** Filter reservations above a specified minimum amount (€).
* **Q4:** Popularity and revenue analysis of extra services.
* **Q5:** Total spending and reservation count per customer.

### 5. Action Logs (`AuditLogView`)
* Automated tracking of database modifications (`INSERT`, `UPDATE`, `DELETE`) powered by PostgreSQL Triggers.

---

## Project Structure

```text
src/main/java/com/hotel/
├── controller/            # Business logic & transactions (ReservationController)
├── model/
│   ├── dao/               # Data Access Objects (GenericDao, CustomerDao, RoomDao, ReservationDao, etc.)
│   ├── database_connection/ # Hibernate SessionFactory configuration (HibernateUtil)
│   └── tables/            # JPA/Hibernate Entities (Customer, Room, Reservation, Service, etc.)
└── ui/                    # JavaFX Views & Main Application (HotelApp, BookingView, CustomerView, MyBookingsView, SearchQueriesView)

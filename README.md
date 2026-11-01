# Spring Boot Role-Based Authentication & Dashboards

A secure, lightweight Spring Boot backend application implementing **JWT (JSON Web Token)** authentication with role-based access control (`ROLE_USER` vs. `ROLE_ADMIN`). This project is designed for learning how to secure REST APIs, manage user registration/login, and restrict backend endpoints by roles.

---

## 🚀 Features

* **User & Admin Registration:** Register new accounts with custom roles (`USER` or `ADMIN`).
* **Secure Login:** Authenticate credentials via Spring Security and generate a signed JWT token.
* **Role-Based Authorization:** Separate protected endpoints for normal users and administrators.
* **Stateless Security:** Uses JWT filters to validate requests without maintaining server-side sessions.
* **In-Memory Database:** Uses H2 Database for rapid setup and out-of-the-box testing.

---

## 📁 Project Structure

```text
src/main/java/com/authe/Authentication/
│
├── controller/
│   └── AuthController.java         # Exposes public auth & secured dashboard endpoints
├── dto/
│   ├── AuthRequest.java            # Login payload
│   ├── JwtResponse.java            # Token & Role response payload
│   └── RegisterRequest.java        # Registration payload
├── entity/
│   └── User.java                   # User database entity (email, password, role)
├── repository/
│   └── UserRepository.java         # Spring Data JPA repository for User entity
├── security/
│   ├── JwtAuthenticationFilter.java# Intercepts requests to validate JWT headers
│   ├── JwtUtil.java                # Generates and validates JWT tokens
│   └── SecurityConfig.java         # Spring Security filter chain & rules
└── service/
    └── CustomUserDetailsService.java # Bridges database users with Spring Security
```
------------------------------------------------

# 🧠 Project Explanation: How It All Works

Understanding how these components interact is key to mastering Spring Boot security. Here is how your code flows step-by-step:

### 1. Registration (`/api/auth/register`)
* When a user sends a registration request (`RegisterRequest`), the `AuthController` receives it.
* The application takes the plain-text password and hashes it securely using **BCrypt** (`passwordEncoder.encode(...)`).
* It formats the role string (ensuring it starts with `ROLE_` like `ROLE_ADMIN` or `ROLE_USER`) and saves the record into the H2 database via `UserRepository`.

### 2. Login & Token Generation (`/api/auth/login`)
* The user sends their email and password (`AuthRequest`).
* **`AuthenticationManager`** verifies the credentials against the database using your `CustomUserDetailsService`.
* If valid, **`JwtUtil`** generates a cryptographically signed **JWT Token** containing the user's identity.
* The server responds with a `JwtResponse` containing both the **JWT Token** and the user's **Role** (`ROLE_USER` or `ROLE_ADMIN`). 

### 3. Frontend Dashboard Routing
* On your frontend application, once you receive the `JwtResponse`:
  * Save the **Token** in `localStorage` or `sessionStorage`.
  * Inspect the **Role** property (`ROLE_ADMIN` vs `ROLE_USER`).
  * Conditionally render the corresponding dashboard view on your screen.

### 4. Securing Requests with JWT Filter (`JwtAuthenticationFilter`)
* HTTP is stateless. Every time the frontend tries to fetch a protected dashboard (e.g., `/api/admin/dashboard`), it must attach the token in the header: 
  `Authorization: Bearer <TOKEN>`.
* Before the request hits your controller, **`JwtAuthenticationFilter`** intercepts it:
  1. Extracts the token from the header.
  2. Parses and validates the token's signature and expiration using `JwtUtil`.
  3. Loads the user details and loads their authorities (roles) into Spring's **`SecurityContextHolder`**.

### 5. Role-Based Endpoint Enforcement (`SecurityConfig`)
* Inside `SecurityConfig.java`, your security filter chain enforces strict routing rules:
  ```java
  .requestMatchers("/api/user/dashboard").hasRole("USER")
  .requestMatchers("/api/admin/dashboard").hasRole("ADMIN")
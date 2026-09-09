# E-Commerce Backend API

A secure RESTful e-commerce backend built with **Java, Spring Boot, Spring Security, JWT, Spring Data JPA/Hibernate, and PostgreSQL**.

## Features

- User registration and login
- JWT-based stateless authentication
- BCrypt password hashing
- USER and ADMIN role-based authorization
- Product CRUD operations
- Shopping cart management
- Authenticated-user ownership checks
- DTO-based API responses
- Bean Validation
- Centralized exception handling
- Transactional checkout
- Inventory/stock validation and deduction
- Purchase-time price snapshots
- Order history
- Order cancellation with stock restoration
- Return requests for delivered orders
- ADMIN approval of return requests
- Stock restoration after approved returns
- PostgreSQL persistence
- Postman API testing

## Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Application framework |
| Spring Web | REST API development |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| BCrypt | Password hashing |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Relational database |
| Lombok | Boilerplate reduction |
| Maven | Build and dependency management |
| Postman | API testing |

## Architecture

```text
Client / Postman
      |
      v
Controller Layer
      |
      v
Service Layer
      |
      v
Repository Layer
      |
      v
JPA / Hibernate
      |
      v
PostgreSQL
```

## Authentication

The application uses Spring Security with JWT and stateless sessions.

```text
Register / Login
      |
      v
AuthenticationManager
      |
      v
UserDetailsService
      |
      v
BCrypt password verification
      |
      v
JWT generated
      |
      v
Authorization: Bearer <JWT>
      |
      v
JwtAuthenticationFilter
      |
      v
SecurityContext
```

Protected requests use:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Roles

### USER

Can view products, manage their cart, place orders, view their orders, cancel eligible orders, and request returns for delivered orders.

### ADMIN

Can manage products and approve return requests.

## Main Entities

```text
User
 ├── 1 : 1 ── Cart
 │              └── 1 : N ── CartItem ── N : 1 ── Product
 │
 └── 1 : N ── Order
                └── 1 : N ── OrderItem ── N : 1 ── Product
```

## Shopping Cart

Cart operations are associated with the currently authenticated user rather than trusting a client-supplied user ID.

Example:

```http
POST /api/cart/items?productId=3&quantity=2
```

The backend obtains the authenticated user's identity from the security context and verifies ownership when updating or deleting cart items.

## Checkout

Checkout is transactional.

```text
Authenticated User
      |
      v
Find Cart
      |
      v
Check Cart Is Not Empty
      |
      v
Validate Stock
      |
      v
Create Order
      |
      v
Create OrderItems
      |
      v
Snapshot Product Prices
      |
      v
Calculate Total
      |
      v
Deduct Stock
      |
      v
Save Order
      |
      v
Clear Cart
```

The checkout operation uses `@Transactional` so related database changes are handled as one transaction.

## Inventory Management

Stock is validated when adding products to the cart, updating cart quantities, and placing an order.

During checkout:

```text
newStock = currentStock - orderedQuantity
```

When an eligible order is cancelled, stock is restored. When an ADMIN approves a return, the corresponding quantities are restored as well.

## Order Lifecycle

```text
PENDING
   |
   v
CONFIRMED
   |
   +------> CANCELLED --> Stock Restored
   |
   v
SHIPPED
   |
   v
DELIVERED
   |
   v
RETURN_REQUESTED
   |
   v
ADMIN APPROVAL
   |
   v
RETURNED --> Stock Restored
```

Order statuses are represented by an enum:

```text
PENDING
CONFIRMED
SHIPPED
DELIVERED
CANCELLED
RETURN_REQUESTED
RETURNED
```

## Order Ownership

Users cannot access or modify another user's order.

```text
JWT
 |
 v
Authenticated Email
 |
 v
User
 |
 v
Owned Cart / Order
```

The service layer verifies ownership before operations such as viewing or cancelling an order.

## DTOs

The API uses DTOs instead of exposing JPA entities directly.

```text
AuthResponse
LoginRequest
CartResponse
CartItemResponse
OrderResponse
OrderItemResponse
```

This helps avoid circular JSON relationships, limits exposed data, and separates API models from persistence models.

## Validation

The project uses Jakarta Bean Validation annotations including:

```java
@NotBlank
@Email
@NotNull
@Positive
@Min
@Size
```

Business rules are additionally enforced in the service layer, including stock availability, non-empty checkout carts, valid order states, and resource ownership.

## Exception Handling

Centralized exception handling is implemented using `@RestControllerAdvice`.

Custom exceptions include:

```text
ResourceNotFoundException
BadRequestException
```

Example response:

```json
{
  "timestamp": "2026-09-05T12:00:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Product not found"
}
```

## API Endpoints

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register user |
| POST | `/api/auth/login` | Public | Login and receive JWT |

### Products

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/products` | ADMIN | Create product |
| GET | `/api/products` | USER / ADMIN | Get products |
| GET | `/api/products/{id}` | USER / ADMIN | Get product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Delete product |

### Cart

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/cart/items?productId={id}&quantity={qty}` | Authenticated | Add product |
| GET | `/api/cart` | Authenticated | Get current user's cart |
| PUT | `/api/cart/items/{cartItemId}?quantity={qty}` | Cart owner | Update quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Cart owner | Remove item |

### Orders

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/orders` | Authenticated | Place order |
| GET | `/api/orders/my-orders` | Authenticated | Get current user's orders |
| GET | `/api/orders/{orderId}` | Order owner | Get specific order |
| PUT | `/api/orders/{orderId}/cancel` | Order owner | Cancel eligible order |
| PUT | `/api/orders/{orderId}/return` | Order owner | Request return |
| PUT | `/api/orders/{orderId}/approve-return` | ADMIN | Approve return |

> The return-request business logic is implemented; ensure the `/return` mapping is present in `OrderController` before publishing it as an active endpoint.

## Project Structure

```text
src/main/java/com/project
│
├── config
│   └── SecurityConfig.java
│
├── controller
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── CartController.java
│   └── OrderController.java
│
├── dto
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── CartResponse.java
│   ├── CartItemResponse.java
│   ├── OrderResponse.java
│   └── OrderItemResponse.java
│
├── entity
│   ├── User.java
│   ├── Product.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatus.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BadRequestException.java
│
├── repository
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   └── OrderRepository.java
│
├── security
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
│
└── service
    ├── AuthService.java
    ├── ProductService.java
    ├── CartService.java
    └── OrderService.java
```

## Database Configuration

Create a PostgreSQL database:

```sql
CREATE DATABASE ecommerce;
```

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Do not commit real database passwords or JWT secrets to Git.

## Running the Project

### Prerequisites

- Java 23 or compatible JDK
- Maven
- PostgreSQL
- Postman (recommended for API testing)

### Run

```bash
./mvnw spring-boot:run
```

or:

```bash
mvn spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

## Example Authentication Flow

### Register

```http
POST /api/auth/register
```

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "USER"
}
```

### Login

```http
POST /api/auth/login
```

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

Use the returned JWT in protected requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Recommended Postman Test Flow

```text
Register USER
      ↓
Login USER
      ↓
Receive JWT
      ↓
Login / create ADMIN
      ↓
Create products as ADMIN
      ↓
View products as USER
      ↓
Add product to cart
      ↓
View cart
      ↓
Update / remove cart items
      ↓
Place order
      ↓
Verify stock deduction
      ↓
Verify cart is cleared
      ↓
View order history
      ↓
Test order cancellation
      ↓
Verify stock restoration
      ↓
Move an order to DELIVERED
      ↓
Request return
      ↓
Approve return as ADMIN
      ↓
Verify RETURNED status
      ↓
Verify stock restoration
```

## Current Status

### Completed

- [x] Spring Boot REST backend
- [x] PostgreSQL integration
- [x] Spring Data JPA / Hibernate
- [x] User registration
- [x] Login
- [x] BCrypt password hashing
- [x] JWT authentication
- [x] Stateless security
- [x] USER / ADMIN roles
- [x] Role-based authorization
- [x] Product CRUD
- [x] Product stock management
- [x] Cart management
- [x] Cart ownership validation
- [x] DTO responses
- [x] Bean Validation
- [x] Global exception handling
- [x] Transactional checkout
- [x] Stock validation and deduction
- [x] Purchase-time price snapshots
- [x] Order history
- [x] Order ownership verification
- [x] Order cancellation
- [x] Stock restoration after cancellation
- [x] Return request business logic
- [x] ADMIN return approval
- [x] Stock restoration after approved return
- [x] Postman testing

## Future Enhancements

- Swagger / OpenAPI documentation
- JUnit and Mockito unit tests
- MockMvc integration tests
- Pagination and sorting
- Product search and filtering
- Standardized API response format
- Redis caching
- Database indexing
- Optimistic locking for concurrent inventory updates
- Docker / Docker Compose
- GitHub Actions CI/CD
- Spring Boot Actuator
- Payment gateway integration
- Refund tracking
- Delivery timestamps
- Return windows and return reasons
- More complete admin order-management APIs

## Interview-Relevant Concepts

This project demonstrates practical experience with:

- REST API design
- Layered architecture
- Dependency Injection
- Spring Boot
- Spring Security
- JWT authentication
- Authentication vs Authorization
- Role-Based Access Control
- JPA entity relationships
- Hibernate ORM
- PostgreSQL
- DTO design
- Bean Validation
- Global exception handling
- Transaction management
- Inventory consistency
- Order state transitions
- Resource ownership authorization
- Stateless authentication
- API testing

## Author

**Anshuman Gahlot**

B.Tech Computer Science Engineering

## License

This project is intended for educational and portfolio purposes.

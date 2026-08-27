# E-Commerce Backend API

A RESTful e-commerce backend built using **Java, Spring Boot, Spring Data JPA, Spring Security, JWT, and PostgreSQL**. The project follows a layered **Controller → Service → Repository** architecture and implements user authentication, product management, shopping cart functionality, and order processing.

## 🚀 Features

### 🔐 Authentication & Security
- User registration and login
- JWT-based stateless authentication
- Spring Security integration
- BCrypt password hashing
- Role-based users (`USER`, `ADMIN`)
- Protected REST endpoints
- JWT validation through a custom authentication filter
- Configurable token expiration

### 📦 Product Management
- Create and retrieve products
- Product price and stock management
- Product description support
- Automatic creation and update timestamps
- Database-level stock validation

### 🛒 Shopping Cart
- Add products to cart
- View user's cart
- Update cart item quantities
- Remove items from cart
- Automatic cart creation
- Product existence and quantity validation

### 🧾 Order Processing
- Place orders directly from the shopping cart
- Create orders and order items
- Calculate order totals
- Store product price at the time of purchase
- Update product inventory after purchase
- Automatically clear the cart after successful checkout
- Transactional order processing using `@Transactional`

## 🏗️ Architecture

```text
Client
  │
  ▼
Controller → Service → Repository → Hibernate/JPA → PostgreSQL
```

### Security Flow

```text
Client
   │ Authorization: Bearer <JWT>
   ▼
JwtAuthenticationFilter
   ▼
JwtService
   ├── Validate Signature
   ├── Check Expiration
   └── Extract User Email
   ▼
CustomUserDetailsService
   ▼
UserRepository
   ▼
SecurityContext
   ▼
Protected Controller
```

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java 23** | Programming language |
| **Spring Boot 4** | Backend framework |
| **Spring MVC** | REST API development |
| **Spring Data JPA** | Database persistence |
| **Hibernate** | ORM |
| **Spring Security** | Authentication & authorization |
| **JWT** | Stateless authentication |
| **BCrypt** | Password hashing |
| **PostgreSQL** | Relational database |
| **Maven** | Dependency management |
| **Lombok** | Boilerplate reduction |
| **Postman** | API testing |

## 📂 Project Structure

```text
src/main/java/com/project/
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── CartController.java
│   └── OrderController.java
├── entity/
│   ├── User.java
│   ├── Product.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   └── OrderItem.java
├── repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   └── OrderRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── ProductService.java
    ├── CartService.java
    └── OrderService.java
```

## 🗄️ Database Design

```text
User
 │
 ├──► Cart
 │      └──► CartItem ──► Product
 │
 └──► Order
        └──► OrderItem ──► Product
```

### Main Tables

#### `users`

```text
id, name, email, password, role, created_at
```

#### `products`

```text
id, name, description, price, stock, created_at, updated_at
```

#### `carts`

```text
id, user_id, created_at, updated_at
```

#### `cart_items`

```text
id, cart_id, product_id, quantity
```

#### `orders`

```text
id, user_id, order_date, status, total_amount
```

#### `order_items`

```text
id, order_id, product_id, quantity, price
```

## 🔄 Order Workflow

```text
User
  │
  ▼
View Cart
  │
  ▼
Place Order
  │
  ▼
Validate Cart
  │
  ▼
Create Order
  │
  ▼
Create Order Items
  │
  ▼
Calculate Total
  │
  ▼
Validate & Update Stock
  │
  ▼
Clear Cart
  │
  ▼
Order Created
```

The checkout operation is transactional so related database changes are handled atomically.

```text
Create Order
      ↓
Create Order Items
      ↓
Update Product Stock
      ↓
Clear Cart
      ↓
     COMMIT
```

## 🔑 Authentication Workflow

### Registration

```text
POST /api/auth/register
        ↓
Validate User
        ↓
Hash Password using BCrypt
        ↓
Save User
        ↓
PostgreSQL
```

### Login

```text
POST /api/auth/login
        ↓
Authenticate Credentials
        ↓
UserDetailsService
        ↓
BCrypt Password Verification
        ↓
Generate JWT
        ↓
Return Token
```

### Protected API Request

The client sends:

```http
Authorization: Bearer <JWT>
```

The JWT filter extracts and validates the token, retrieves the user, and establishes authentication in Spring Security's `SecurityContext`.

## 🌐 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST   | `/api/auth/register` | Register a user | ❌ |
| POST   | `/api/auth/login` | Login and receive JWT | ❌ |

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | Retrieve products |
| GET | `/api/products/{id}` | Retrieve product by ID |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

### Cart

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/cart/items` | Add product to cart |
| GET | `/api/cart/{userId}` | Get user's cart |
| PUT | `/api/cart/items/{cartItemId}` | Update cart quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Remove cart item |

Example:

```http
POST /api/cart/items?userId=1&productId=2&quantity=2
```

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders/{userId}` | Place an order |
| GET | `/api/orders/{id}` | Retrieve an order |

## 🔒 Security

The application implements:

- JWT-based stateless authentication
- BCrypt password hashing
- Spring Security authentication
- Role-based users
- Protected REST endpoints
- Token signature and expiration validation
- Primary and foreign key constraints
- NOT NULL and validation constraints
- Transaction management

## 🧪 Testing

The APIs were tested using **Postman**.

Testing covered:

- User registration
- User login
- JWT generation
- JWT-protected endpoints
- Product retrieval
- Cart operations
- Order creation
- Invalid product handling
- Empty cart handling
- Authentication failures
- Database state verification

PostgreSQL `psql` was also used to inspect and verify database records during development.

## ⚙️ Setup & Installation

### 1. Clone the repository

```bash
git clone https://github.com/anshumangahlot/Ecommerce-Project.git
cd Ecommerce-Project
```

### 2. Configure PostgreSQL

```sql
CREATE DATABASE ecommerce;
```

### 3. Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Do not commit real database credentials or JWT secrets to GitHub.**

### 4. Build

```bash
./mvnw clean install
```

or:

```bash
mvn clean install
```

### 5. Run

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## 📮 Postman Example

### Register

```http
POST http://localhost:8080/api/auth/register
```

```json
{
  "name": "Alex",
  "email": "alex@gmail.com",
  "password": "password123",
  "role": "USER"
}
```

### Login

```http
POST http://localhost:8080/api/auth/login
```

```json
{
  "email": "alex@gmail.com",
  "password": "password123"
}
```

Use the returned JWT for protected APIs:

```http
Authorization: Bearer <your-token>
```

## 🧠 Key Concepts Demonstrated

- Object-Oriented Programming
- RESTful API design
- Layered architecture
- Dependency Injection
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- Entity relationships
- CRUD operations
- Transactions
- Authentication and authorization
- JWT
- BCrypt
- Role-Based Access Control
- API testing
- Inventory management

## 🔮 Future Improvements

- DTO-based API responses
- Global exception handling with `@ControllerAdvice`
- More granular role-based authorization
- Resource ownership validation
- Pagination and sorting
- Product search and filtering
- Payment integration
- Order history and status tracking
- Refresh tokens
- Redis caching
- API rate limiting
- Unit and integration testing
- Audit logging
- Docker containerization
- CI/CD pipeline
- Production-grade secrets management
- Inventory concurrency control

## 📌 Current Status

**Core backend functionality implemented and tested.**

```text
✅ User Authentication
✅ JWT Security
✅ BCrypt Password Hashing
✅ Product Management
✅ Shopping Cart
✅ Order Processing
✅ PostgreSQL Persistence
✅ Spring Data JPA
✅ REST APIs
✅ API Testing
```

## 👨‍💻 Author

**Anshuman Gahlot**

B.Tech Computer Science Engineering  
Symbiosis Institute of Technology, Pune

**Technologies:** Java • Spring Boot • Spring Security • JWT • PostgreSQL • JPA • Hibernate • REST APIs

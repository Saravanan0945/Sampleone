# E-Commerce API - ASP.NET Core Web API

A secure and scalable RESTful API for an e-commerce application built with ASP.NET Core 8.0, featuring JWT authentication, role-based authorization, and shopping cart functionality.

## Features

- **User Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (Admin, Customer)
  - Secure password hashing with BCrypt
  - Login with username or email
  - User registration
  - Forgot password functionality
  - Password reset

- **Product Management**
  - CRUD operations for products
  - Product categorization
  - Stock management
  - Admin-only product management

- **Shopping Cart**
  - Add products to cart
  - Update cart item quantities
  - Remove items from cart
  - Clear entire cart
  - Automatic cart creation for new users
  - Real-time price tracking

## Technology Stack

- **Framework**: ASP.NET Core 8.0
- **Database**: SQLite with Entity Framework Core
- **Authentication**: JWT Bearer tokens
- **Password Hashing**: BCrypt.Net
- **API Documentation**: Swagger/OpenAPI

## Project Structure

```
ECommerceAPI/
├── Controllers/          # API endpoints
│   ├── AuthController.cs
│   ├── CartController.cs
│   └── ProductController.cs
├── Models/              # Entity models
│   ├── User.cs
│   ├── Role.cs
│   ├── Product.cs
│   ├── Cart.cs
│   └── CartItem.cs
├── DTOs/                # Data transfer objects
│   ├── LoginDto.cs
│   ├── RegisterDto.cs
│   ├── UserDto.cs
│   ├── ProductDto.cs
│   ├── CartDto.cs
│   └── PasswordDto.cs
├── Data/                # Database context
│   └── ApplicationDbContext.cs
├── Services/            # Business logic
│   ├── AuthService.cs
│   ├── ProductService.cs
│   └── CartService.cs
├── Middleware/          # Custom middleware
│   └── ErrorHandlingMiddleware.cs
└── Program.cs           # Application entry point
```

## Prerequisites

- [.NET 8.0 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- SQLite (included with .NET)

## Getting Started

### 1. Installation

Navigate to the backend directory:
```bash
cd backend/ECommerceAPI
```

Restore NuGet packages:
```bash
dotnet restore
```

### 2. Database Setup

The database will be automatically created when you run the application for the first time. It uses SQLite and creates a file named `ecommerce.db` in the project root.

To manually create the database:
```bash
dotnet ef database update
```

### 3. Configuration

Update `appsettings.json` with your configuration:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Data Source=ecommerce.db"
  },
  "JwtSettings": {
    "SecretKey": "YourSuperSecretKeyForJWTTokenGeneration123456789",
    "Issuer": "ECommerceAPI",
    "Audience": "ECommerceClient",
    "ExpirationHours": "24"
  }
}
```

**Important**: Change the `SecretKey` to a strong, unique value in production.

### 4. Run the Application

```bash
dotnet run
```

The API will be available at:
- HTTP: `http://localhost:5000`
- HTTPS: `https://localhost:5001`
- Swagger UI: `http://localhost:5000/swagger`

## API Endpoints

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "SecurePassword123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "Customer"
  }
}
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "john@example.com",
  "token": "reset-token",
  "newPassword": "NewSecurePassword123"
}
```

### Products

#### Get All Products
```http
GET /api/product
```

#### Get Product by ID
```http
GET /api/product/{id}
```

#### Get Products by Category
```http
GET /api/product/category/{category}
```

#### Create Product (Admin only)
```http
POST /api/product
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Product Name",
  "description": "Product Description",
  "price": 29.99,
  "imageUrl": "https://example.com/image.jpg",
  "stockQuantity": 100,
  "category": "Electronics"
}
```

#### Update Product (Admin only)
```http
PUT /api/product/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Product Name",
  "price": 39.99,
  "stockQuantity": 150
}
```

#### Delete Product (Admin only)
```http
DELETE /api/product/{id}
Authorization: Bearer {token}
```

### Shopping Cart

#### Get User Cart
```http
GET /api/cart
Authorization: Bearer {token}
```

#### Add to Cart
```http
POST /api/cart/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Update Cart Item
```http
PUT /api/cart/items/{cartItemId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove from Cart
```http
DELETE /api/cart/items/{cartItemId}
Authorization: Bearer {token}
```

#### Clear Cart
```http
DELETE /api/cart
Authorization: Bearer {token}
```

## Authentication

The API uses JWT Bearer token authentication. Include the token in the Authorization header:

```
Authorization: Bearer {your-jwt-token}
```

## Role-Based Authorization

- **Customer**: Can view products, manage their own cart
- **Admin**: Full access to product management, plus all customer permissions

## Error Handling

The API uses a global error handling middleware that returns consistent error responses:

```json
{
  "error": "An error occurred while processing your request",
  "message": "Detailed error message"
}
```

## CORS Configuration

The API is configured to accept requests from:
- `http://localhost:3000` (React default)
- `http://localhost:5173` (Vite default)

Update the CORS policy in `Program.cs` to add additional origins.

## Database Schema

### Users
- Id, Username, Email, PasswordHash, FirstName, LastName
- RoleId (Foreign Key to Roles)
- CreatedAt, LastLoginAt, IsActive

### Roles
- Id, Name, Description
- Seeded with: Admin, Customer

### Products
- Id, Name, Description, Price, ImageUrl
- StockQuantity, Category, IsActive
- CreatedAt, UpdatedAt

### Carts
- Id, UserId (Foreign Key to Users)
- CreatedAt, UpdatedAt, IsActive

### CartItems
- Id, CartId (Foreign Key to Carts)
- ProductId (Foreign Key to Products)
- Quantity, PriceAtAdd, AddedAt

## Development

### Build
```bash
dotnet build
```

### Run Tests
```bash
dotnet test
```

### Create Migration
```bash
dotnet ef migrations add MigrationName
```

### Update Database
```bash
dotnet ef database update
```

## Security Considerations

1. **JWT Secret Key**: Change the default secret key in production
2. **HTTPS**: Always use HTTPS in production
3. **Password Policy**: Implement strong password requirements
4. **Rate Limiting**: Consider adding rate limiting for authentication endpoints
5. **Input Validation**: All inputs are validated at the controller level
6. **SQL Injection**: Protected by Entity Framework Core parameterized queries

## License

This project is part of a multi-technology stack e-commerce application.


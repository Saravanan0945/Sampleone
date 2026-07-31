# E-Commerce Application

A full-stack e-commerce application with user authentication, product browsing, and shopping cart functionality. Built with ASP.NET Core Web API backend and React frontend.

## 📋 Project Overview

This repository contains a complete e-commerce solution featuring:
- **Backend**: RESTful API built with ASP.NET Core 6.0+ providing authentication, product management, and cart operations
- **Frontend**: Modern React 18+ single-page application with responsive design
- **Database**: SQLite for lightweight, file-based data persistence
- **Authentication**: JWT token-based authentication with BCrypt password hashing
- **Authorization**: Role-based access control (Admin, Customer)

> **Note**: This repository also contains an existing Java Maven project that has been preserved alongside the new ASP.NET Core + React implementation.

## 🚀 Technology Stack

### Backend
- **ASP.NET Core 6.0+** - Web API framework
- **Entity Framework Core** - ORM for database operations
- **SQLite** - Lightweight database
- **JWT (JSON Web Tokens)** - Authentication mechanism
- **BCrypt.Net** - Password hashing
- **Swashbuckle (Swagger)** - API documentation

### Frontend
- **React 18+** - UI library
- **React Router v6** - Client-side routing
- **Axios** - HTTP client for API calls
- **Context API** - Global state management
- **jwt-decode** - JWT token decoding
- **CSS3** - Modern styling with responsive design

### Existing
- **Java Maven Project** - Legacy codebase (preserved)

## ✨ Features Implemented

### Authentication & Authorization
- ✅ User registration with validation
- ✅ Login with username OR email support
- ✅ Forgot password functionality
- ✅ Password reset with secure tokens
- ✅ JWT token-based authentication
- ✅ BCrypt password hashing
- ✅ Role-based authorization (Admin, Customer)
- ✅ Automatic cart creation on registration

### Product Management
- ✅ Product listing with details
- ✅ Product viewing by ID
- ✅ Category-based filtering
- ✅ Stock management
- ✅ 15 pre-seeded sample products

### Shopping Cart (Jira ST-2)
- ✅ Add products to cart with quantity selection
- ✅ Update cart item quantities
- ✅ Remove items from cart
- ✅ Clear entire cart
- ✅ **"Go to Cart" button** - visible and clickable from Products page
- ✅ **Cart navigation** - redirects to Cart page
- ✅ **Product retention** - selected products displayed correctly
- ✅ Real-time cart count badge
- ✅ Price locking at time of addition
- ✅ Stock validation

### Security Features
- 🔐 JWT authentication with 24-hour expiration
- 🔐 Password hashing with BCrypt (automatic salt)
- 🔐 Role-based authorization
- 🔐 CORS configuration for frontend
- 🔐 Global error handling middleware
- 🔐 Token expiration validation
- 🔐 Single-use password reset tokens

## 📁 Project Structure

```
/
├── backend/                          # ASP.NET Core Web API
│   └── ECommerceAPI/
│       ├── Controllers/              # API endpoints
│       │   ├── AuthController.cs
│       │   ├── ProductController.cs
│       │   └── CartController.cs
│       ├── Models/                   # Entity models
│       │   ├── User.cs
│       │   ├── Role.cs
│       │   ├── Product.cs
│       │   ├── Cart.cs
│       │   ├── CartItem.cs
│       │   └── PasswordResetToken.cs
│       ├── DTOs/                     # Data transfer objects
│       ├── Services/                 # Business logic
│       │   ├── AuthService.cs
│       │   ├── ProductService.cs
│       │   └── CartService.cs
│       ├── Data/                     # Database context
│       │   ├── ApplicationDbContext.cs
│       │   └── DbInitializer.cs
│       ├── Middleware/               # Custom middleware
│       │   └── ErrorHandlingMiddleware.cs
│       ├── Program.cs                # Application entry point
│       ├── appsettings.json          # Configuration
│       └── ECommerceAPI.csproj       # Project file
│
├── frontend/                         # React Application
│   └── ecommerce-app/
│       ├── public/                   # Static files
│       ├── src/
│       │   ├── components/           # Reusable components
│       │   │   ├── Header.js
│       │   │   ├── ProtectedRoute.js
│       │   │   ├── LoadingSpinner.js
│       │   │   └── ErrorMessage.js
│       │   ├── pages/                # Page components
│       │   │   ├── Login.js
│       │   │   ├── Register.js
│       │   │   ├── ForgotPassword.js
│       │   │   ├── ResetPassword.js
│       │   │   ├── Products.js
│       │   │   └── Cart.js
│       │   ├── services/             # API services
│       │   │   ├── api.js
│       │   │   ├── authService.js
│       │   │   ├── productService.js
│       │   │   └── cartService.js
│       │   ├── context/              # React Context
│       │   │   ├── AuthContext.js
│       │   │   └── CartContext.js
│       │   ├── utils/                # Utility functions
│       │   │   └── auth.js
│       │   ├── App.js                # Main app component
│       │   └── index.js              # Entry point
│       └── package.json              # Dependencies
│
├── src/                              # Java Maven project (legacy)
├── pom.xml                           # Maven configuration
├── .gitignore                        # Git ignore rules
└── README.md                         # This file
```

## 🔧 Prerequisites

Before running this application, ensure you have the following installed:

- **Node.js** 16.0 or higher ([Download](https://nodejs.org/))
- **.NET SDK** 6.0 or higher ([Download](https://dotnet.microsoft.com/download))
- **npm** or **yarn** (comes with Node.js)
- **Git** (for version control)

### Verify Installation

```bash
# Check Node.js version
node --version

# Check npm version
npm --version

# Check .NET SDK version
dotnet --version
```

## 🚀 Backend Setup Instructions

### 1. Navigate to Backend Directory

```bash
cd backend/ECommerceAPI
```

### 2. Restore NuGet Packages

```bash
dotnet restore
```

### 3. Create Database

```bash
# Create initial migration (if not already created)
dotnet ef migrations add InitialCreate

# Apply migrations to create database
dotnet ef database update
```

> **Note**: The database file `ecommerce.db` will be created in the project directory.

### 4. Run the API

```bash
dotnet run
```

The API will start on:
- **HTTP**: http://localhost:5000
- **HTTPS**: https://localhost:5001
- **Swagger UI**: http://localhost:5000/swagger

### 5. Verify API is Running

Open your browser and navigate to http://localhost:5000/swagger to see the API documentation.

### Initial Data Seeding

On first run, the application will automatically seed:
- **2 Roles**: Admin, Customer
- **15 Sample Products** across 5 categories (Electronics, Clothing, Home, Books, Sports)

## 🎨 Frontend Setup Instructions

### 1. Navigate to Frontend Directory

```bash
cd frontend/ecommerce-app
```

### 2. Install Dependencies

```bash
npm install
```

Or if using yarn:

```bash
yarn install
```

### 3. Configure Environment (Optional)

Create a `.env` file in the `frontend/ecommerce-app` directory:

```env
REACT_APP_API_URL=http://localhost:5000/api
```

### 4. Start the React Application

```bash
npm start
```

Or with yarn:

```bash
yarn start
```

The application will start on:
- **URL**: http://localhost:3000

### 5. Verify Frontend is Running

Your browser should automatically open to http://localhost:3000. You should see the Products page.

## 👤 Default Test Users

**No default users are created.** You need to register a new account to use the application.

### To Create a Test User:

1. Navigate to http://localhost:3000/register
2. Fill in the registration form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `Test123!` (minimum 6 characters)
   - Confirm Password: `Test123!`
3. Click "Register"
4. You will be automatically logged in and redirected to the Products page

### To Create an Admin User:

Admin users must be created directly in the database. After registering a normal user:

1. Stop the backend API
2. Open the SQLite database file `backend/ECommerceAPI/ecommerce.db` with a SQLite client
3. Update the user's RoleId to 1 (Admin role):
   ```sql
   UPDATE Users SET RoleId = 1 WHERE Username = 'testuser';
   ```
4. Restart the backend API

## 📡 API Endpoints Documentation

### Base URL
```
http://localhost:5000/api
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login with username/email | No |
| POST | `/auth/forgot-password` | Request password reset | No |
| POST | `/auth/reset-password` | Reset password with token | No |

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "Customer"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "Customer"
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

**Response (200 OK):**
```json
{
  "message": "Password reset token generated successfully"
}
```

> **Note**: In production, this would send an email. For development, the token is stored in the database.

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "newPassword": "NewSecurePass123!"
}
```

### Product Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/products` | Get all active products | No |
| GET | `/products/{id}` | Get product by ID | No |

#### Get All Products
```http
GET /api/products
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "imageUrl": "https://via.placeholder.com/300/4A90E2/FFFFFF?text=Laptop",
    "stock": 50,
    "isActive": true
  }
]
```

#### Get Product by ID
```http
GET /api/products/1
```

### Cart Endpoints (Authentication Required)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/cart` | Get user's cart | Yes |
| POST | `/cart/add` | Add item to cart | Yes |
| PUT | `/cart/update` | Update cart item quantity | Yes |
| DELETE | `/cart/remove/{id}` | Remove item from cart | Yes |
| DELETE | `/cart/clear` | Clear entire cart | Yes |

> **Note**: All cart endpoints require a valid JWT token in the Authorization header.

#### Get Cart
```http
GET /api/cart
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "price": 999.99,
      "quantity": 2,
      "subtotal": 1999.98,
      "imageUrl": "https://via.placeholder.com/300/4A90E2/FFFFFF?text=Laptop"
    }
  ],
  "totalPrice": 1999.98
}
```

#### Add to Cart
```http
POST /api/cart/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Update Cart Item
```http
PUT /api/cart/update
Authorization: Bearer <token>
Content-Type: application/json

{
  "cartItemId": 1,
  "quantity": 3
}
```

#### Remove from Cart
```http
DELETE /api/cart/remove/1
Authorization: Bearer <token>
```

**Response (204 No Content)**

#### Clear Cart
```http
DELETE /api/cart/clear
Authorization: Bearer <token>
```

**Response (204 No Content)**

## 📖 Usage Guide

### 1. Register a New Account

1. Open http://localhost:3000
2. Click "Register" in the header
3. Fill in the registration form
4. Click "Register" button
5. You'll be automatically logged in and redirected to Products page

### 2. Login

1. Navigate to http://localhost:3000/login
2. Enter your username/email and password
3. Click "Login"
4. You'll be redirected to the Products page

### 3. Browse Products

1. The Products page displays all available products
2. Each product shows:
   - Product image
   - Name and description
   - Price
   - Stock availability
   - Quantity selector
   - "Add to Cart" button

### 4. Add Products to Cart

1. On the Products page, select a quantity (default is 1)
2. Click "Add to Cart" button
3. A success message will appear
4. The cart count badge will update

### 5. View Cart (Jira ST-2)

**Option 1: Using "Go to Cart" Button**
- Click the floating "Go to Cart" button at the bottom-right corner
- The button shows the current cart item count

**Option 2: Using Header Navigation**
- Click "Cart" link in the header
- The badge shows the number of items in your cart

### 6. Manage Cart

On the Cart page, you can:
- **Update Quantity**: Use +/- buttons to adjust quantities
- **Remove Items**: Click the "Remove" button on any item
- **Clear Cart**: Click "Clear Cart" to remove all items
- **Continue Shopping**: Return to Products page
- **Checkout**: (Placeholder for future implementation)

### 7. Logout

1. Click your username in the header
2. Click "Logout"
3. You'll be redirected to the login page

### 8. Forgot Password

1. On the Login page, click "Forgot Password?"
2. Enter your email address
3. Click "Send Reset Link"
4. In production, you would receive an email with a reset link
5. For development, retrieve the token from the database and navigate to:
   ```
   http://localhost:3000/reset-password?token=<your-token>
   ```

## 🎫 Jira Ticket ST-2 Implementation

### Ticket: Implement Go to Cart Button Functionality

**Status**: ✅ Completed

### Requirements Met:

✅ **User selects one or more products**
- Products page allows quantity selection (1 to stock limit)
- Multiple products can be added to cart

✅ **User clicks the "Go to Cart" button**
- Fixed position button at bottom-right corner
- Always visible and clickable
- Shows cart item count badge
- Animated pulse effect on badge

✅ **Application navigates to Cart page**
- Button uses React Router navigation
- Smooth transition to /cart route
- No page reload required

✅ **Selected products displayed correctly**
- All cart items shown with details
- Product images, names, prices displayed
- Quantities and subtotals calculated
- Total price updated in real-time

### Acceptance Criteria:

✅ **"Go to Cart" button is visible and clickable**
- Fixed position button always visible
- Responsive design (adapts to mobile)
- Clear visual feedback on hover

✅ **User is redirected to Cart page upon clicking**
- React Router navigation implemented
- URL changes to /cart
- Cart page renders immediately

✅ **Selected products are retained and displayed**
- Cart data persisted in backend database
- Cart state managed via CartContext
- Real-time synchronization with API

✅ **No navigation or data loss issues**
- Cart data fetched from backend on mount
- Authentication state preserved
- No data loss on page refresh

### Implementation Details:

**Frontend Components:**
- `Products.js` - "Go to Cart" button with badge
- `Cart.js` - Complete cart management interface
- `CartContext.js` - Global cart state management
- `cartService.js` - API integration

**Backend Endpoints:**
- `GET /api/cart` - Fetch user's cart
- `POST /api/cart/add` - Add items to cart
- `PUT /api/cart/update` - Update quantities
- `DELETE /api/cart/remove/{id}` - Remove items
- `DELETE /api/cart/clear` - Clear cart

## 🔒 Security Features

### Authentication
- **JWT Tokens**: 24-hour expiration, signed with secret key
- **Password Hashing**: BCrypt with automatic salt generation
- **Token Storage**: localStorage with automatic cleanup on expiration
- **Login Support**: Username OR email authentication

### Authorization
- **Role-Based Access**: Admin and Customer roles
- **Protected Routes**: Cart page requires authentication
- **API Authorization**: JWT bearer token validation

### Data Protection
- **CORS Configuration**: Restricted to frontend origin
- **Password Reset**: Single-use tokens with 1-hour expiration
- **Error Handling**: No sensitive information in error messages
- **Input Validation**: Client and server-side validation

### Best Practices
- **HTTPS Ready**: Configured for SSL/TLS
- **SQL Injection Prevention**: Entity Framework parameterized queries
- **XSS Protection**: React's built-in escaping
- **CSRF Protection**: JWT tokens (no cookies)

## 🚧 Future Enhancements

### Planned Features
- [ ] **Checkout Process**: Complete order placement workflow
- [ ] **Payment Integration**: Stripe/PayPal integration
- [ ] **Order History**: View past orders and tracking
- [ ] **Admin Panel**: Product management interface
- [ ] **Email Notifications**: Password reset and order confirmations
- [ ] **Product Search**: Search and filter functionality
- [ ] **Product Reviews**: User ratings and reviews
- [ ] **Wishlist**: Save products for later
- [ ] **Address Management**: Multiple shipping addresses
- [ ] **Order Tracking**: Real-time order status updates

### Technical Improvements
- [ ] **Unit Tests**: Backend and frontend test coverage
- [ ] **Integration Tests**: API endpoint testing
- [ ] **CI/CD Pipeline**: Automated deployment
- [ ] **Docker Support**: Containerization
- [ ] **Database Migration**: PostgreSQL/SQL Server
- [ ] **Caching**: Redis for performance
- [ ] **Logging**: Structured logging with Serilog
- [ ] **Monitoring**: Application insights and metrics

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. CORS Errors

**Problem**: Browser console shows CORS policy errors

**Solution**:
- Ensure backend is running on http://localhost:5000
- Verify CORS configuration in `Program.cs` includes your frontend URL
- Check that frontend is making requests to correct API URL

```csharp
// In Program.cs
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowReactApp", policy =>
    {
        policy.WithOrigins("http://localhost:3000", "http://localhost:5173")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});
```

#### 2. Database Connection Errors

**Problem**: "Unable to open database file" or migration errors

**Solution**:
```bash
# Delete existing database and migrations
rm ecommerce.db
rm -rf Migrations/

# Recreate migrations and database
dotnet ef migrations add InitialCreate
dotnet ef database update
```

#### 3. Port Conflicts

**Problem**: "Address already in use" errors

**Solution**:

**Backend (Port 5000)**:
```bash
# Find process using port 5000
lsof -i :5000  # macOS/Linux
netstat -ano | findstr :5000  # Windows

# Kill the process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

**Frontend (Port 3000)**:
```bash
# Find and kill process using port 3000
lsof -i :3000  # macOS/Linux
netstat -ano | findstr :3000  # Windows
```

Or change the port in `package.json`:
```json
{
  "scripts": {
    "start": "PORT=3001 react-scripts start"
  }
}
```

#### 4. JWT Token Errors

**Problem**: "Unauthorized" or "Invalid token" errors

**Solution**:
- Clear browser localStorage: `localStorage.clear()`
- Logout and login again
- Check token expiration (24 hours)
- Verify JWT secret key matches in `appsettings.json`

#### 5. npm Install Failures

**Problem**: Dependency installation errors

**Solution**:
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall dependencies
npm install
```

#### 6. .NET SDK Not Found

**Problem**: "dotnet command not found"

**Solution**:
- Download and install .NET SDK from https://dotnet.microsoft.com/download
- Verify installation: `dotnet --version`
- Restart terminal after installation

#### 7. React Build Errors

**Problem**: "Module not found" or compilation errors

**Solution**:
```bash
# Ensure all dependencies are installed
npm install

# Clear React cache
rm -rf node_modules/.cache

# Restart development server
npm start
```

#### 8. Database Seeding Issues

**Problem**: Products not appearing or duplicate data

**Solution**:
- Check `DbInitializer.cs` is called in `Program.cs`
- Delete database and recreate:
  ```bash
  rm ecommerce.db
  dotnet ef database update
  ```
- Restart the backend API

## 📝 Note About Java Code

This repository contains an existing **Java Maven project** in the `src/` directory with a `pom.xml` file. This legacy codebase has been **preserved alongside** the new ASP.NET Core + React implementation.

### Java Project Structure:
```
/
├── src/main/java/txt/com/  # Java source files
├── pom.xml                  # Maven configuration
└── target/                  # Java build output (ignored by git)
```

The Java project is independent and does not interfere with the ASP.NET Core + React application. Both can coexist in the same repository.

## 📄 License

This project is for educational and demonstration purposes.

## 👥 Contributors

- **Backend Development**: ASP.NET Core Web API with Entity Framework Core
- **Frontend Development**: React with Context API and React Router
- **Jira Ticket ST-2**: Go to Cart button functionality

## 📞 Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review API documentation at http://localhost:5000/swagger
3. Check browser console for frontend errors
4. Review backend logs in the terminal

---

**Happy Coding! 🚀**


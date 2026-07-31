# API Services Documentation

## Overview

This document describes the API service modules that handle all communication between the React frontend and the ASP.NET Core Web API backend.

## Architecture

```
src/
├── services/
│   ├── api.js              # Axios instance with interceptors
│   ├── authService.js      # Authentication API calls
│   ├── productService.js   # Product API calls
│   └── cartService.js      # Shopping cart API calls
└── utils/
    └── auth.js             # Token management utilities
```

---

## 1. API Client (`src/services/api.js`)

### Configuration

- **Base URL**: `http://localhost:5000/api` (configurable via `REACT_APP_API_URL` environment variable)
- **Content-Type**: `application/json`

### Request Interceptor

Automatically attaches JWT token to all requests:

```javascript
Authorization: Bearer <token>
```

### Response Interceptor

Handles common errors:
- **401 Unauthorized**: Clears token and redirects to `/login`
- Other errors are passed through for component-level handling

### Usage

```javascript
import apiClient from './api';

// The token is automatically attached to requests
const response = await apiClient.get('/endpoint');
```

---

## 2. Authentication Service (`src/services/authService.js`)

### Methods

#### `register(username, email, password)`

Registers a new user account.

**Endpoint**: `POST /auth/register`

**Parameters**:
- `username` (string): Unique username
- `email` (string): Valid email address
- `password` (string): User password

**Returns**: Promise with `AuthResponseDto`
```javascript
{
  token: "jwt-token-string",
  username: "john_doe",
  email: "john@example.com",
  role: "Customer"
}
```

**Example**:
```javascript
import authService from './services/authService';

try {
  const response = await authService.register('john_doe', 'john@example.com', 'SecurePass123!');
  console.log('Registration successful:', response);
} catch (error) {
  console.error('Registration failed:', error);
}
```

---

#### `login(usernameOrEmail, password)`

Authenticates a user with username or email.

**Endpoint**: `POST /auth/login`

**Parameters**:
- `usernameOrEmail` (string): Username or email address
- `password` (string): User password

**Returns**: Promise with `AuthResponseDto`

**Example**:
```javascript
try {
  const response = await authService.login('john_doe', 'SecurePass123!');
  localStorage.setItem('token', response.token);
} catch (error) {
  console.error('Login failed:', error);
}
```

---

#### `forgotPassword(email)`

Initiates password reset process.

**Endpoint**: `POST /auth/forgot-password`

**Parameters**:
- `email` (string): User's email address

**Returns**: Promise with success message

**Example**:
```javascript
try {
  await authService.forgotPassword('john@example.com');
  alert('Password reset email sent!');
} catch (error) {
  console.error('Failed to send reset email:', error);
}
```

---

#### `resetPassword(token, newPassword)`

Resets user password using reset token.

**Endpoint**: `POST /auth/reset-password`

**Parameters**:
- `token` (string): Password reset token from email
- `newPassword` (string): New password

**Returns**: Promise with success message

**Example**:
```javascript
try {
  await authService.resetPassword('reset-token-123', 'NewSecurePass456!');
  alert('Password reset successful!');
} catch (error) {
  console.error('Password reset failed:', error);
}
```

---

## 3. Product Service (`src/services/productService.js`)

### Methods

#### `getAllProducts()`

Retrieves all active products.

**Endpoint**: `GET /products`

**Returns**: Promise with array of `ProductDto`
```javascript
[
  {
    id: 1,
    name: "Laptop",
    description: "High-performance laptop",
    price: 999.99,
    imageUrl: "https://via.placeholder.com/300/0000FF/FFFFFF?text=Laptop",
    stock: 50,
    isActive: true
  },
  // ... more products
]
```

**Example**:
```javascript
import productService from './services/productService';

try {
  const products = await productService.getAllProducts();
  console.log('Products:', products);
} catch (error) {
  console.error('Failed to fetch products:', error);
}
```

---

#### `getProductById(id)`

Retrieves a single product by ID.

**Endpoint**: `GET /products/{id}`

**Parameters**:
- `id` (number): Product ID

**Returns**: Promise with `ProductDto`

**Example**:
```javascript
try {
  const product = await productService.getProductById(1);
  console.log('Product details:', product);
} catch (error) {
  console.error('Product not found:', error);
}
```

---

## 4. Cart Service (`src/services/cartService.js`)

### Methods

#### `getCart()`

Retrieves the current user's shopping cart.

**Endpoint**: `GET /cart`

**Authentication**: Required

**Returns**: Promise with `CartDto`
```javascript
{
  id: 1,
  items: [
    {
      id: 1,
      productId: 1,
      productName: "Laptop",
      price: 999.99,
      quantity: 1,
      subtotal: 999.99,
      imageUrl: "https://via.placeholder.com/300/0000FF/FFFFFF?text=Laptop"
    }
  ],
  totalPrice: 999.99
}
```

**Example**:
```javascript
import cartService from './services/cartService';

try {
  const cart = await cartService.getCart();
  console.log('Cart:', cart);
} catch (error) {
  console.error('Failed to fetch cart:', error);
}
```

---

#### `addToCart(productId, quantity)`

Adds a product to the shopping cart.

**Endpoint**: `POST /cart/add`

**Authentication**: Required

**Parameters**:
- `productId` (number): Product ID to add
- `quantity` (number): Quantity to add (default: 1)

**Returns**: Promise with updated `CartDto`

**Example**:
```javascript
try {
  const updatedCart = await cartService.addToCart(1, 2);
  console.log('Product added to cart:', updatedCart);
} catch (error) {
  console.error('Failed to add to cart:', error);
}
```

---

#### `updateCartItem(cartItemId, quantity)`

Updates the quantity of a cart item.

**Endpoint**: `PUT /cart/update`

**Authentication**: Required

**Parameters**:
- `cartItemId` (number): Cart item ID
- `quantity` (number): New quantity (0 to remove)

**Returns**: Promise with updated `CartDto`

**Example**:
```javascript
try {
  const updatedCart = await cartService.updateCartItem(1, 3);
  console.log('Cart item updated:', updatedCart);
} catch (error) {
  console.error('Failed to update cart item:', error);
}
```

---

#### `removeFromCart(cartItemId)`

Removes an item from the shopping cart.

**Endpoint**: `DELETE /cart/remove/{cartItemId}`

**Authentication**: Required

**Parameters**:
- `cartItemId` (number): Cart item ID to remove

**Returns**: Promise with success response

**Example**:
```javascript
try {
  await cartService.removeFromCart(1);
  console.log('Item removed from cart');
} catch (error) {
  console.error('Failed to remove item:', error);
}
```

---

#### `clearCart()`

Removes all items from the shopping cart.

**Endpoint**: `DELETE /cart/clear`

**Authentication**: Required

**Returns**: Promise with success response

**Example**:
```javascript
try {
  await cartService.clearCart();
  console.log('Cart cleared');
} catch (error) {
  console.error('Failed to clear cart:', error);
}
```

---

## 5. Auth Utilities (`src/utils/auth.js`)

### Functions

#### `getToken()`

Retrieves JWT token from localStorage.

**Returns**: `string | null`

**Example**:
```javascript
import { getToken } from './utils/auth';

const token = getToken();
if (token) {
  console.log('User is authenticated');
}
```

---

#### `setToken(token)`

Saves JWT token to localStorage.

**Parameters**:
- `token` (string): JWT token to save

**Example**:
```javascript
import { setToken } from './utils/auth';

setToken('jwt-token-string');
```

---

#### `removeToken()`

Removes JWT token from localStorage.

**Example**:
```javascript
import { removeToken } from './utils/auth';

removeToken(); // User logged out
```

---

#### `isTokenExpired(token)`

Checks if a JWT token is expired.

**Parameters**:
- `token` (string): JWT token to check

**Returns**: `boolean` - `true` if expired or invalid, `false` if valid

**Example**:
```javascript
import { isTokenExpired, getToken } from './utils/auth';

const token = getToken();
if (isTokenExpired(token)) {
  console.log('Token expired, please login again');
}
```

---

#### `getDecodedToken(token)`

Decodes a JWT token to extract claims.

**Parameters**:
- `token` (string): JWT token to decode

**Returns**: Decoded token object or `null` if invalid

**Example**:
```javascript
import { getDecodedToken, getToken } from './utils/auth';

const token = getToken();
const decoded = getDecodedToken(token);
console.log('Token claims:', decoded);
```

---

#### `getUserFromToken(token)`

Extracts user information from JWT token (ASP.NET Core compatible).

**Parameters**:
- `token` (string): JWT token

**Returns**: User object or `null`
```javascript
{
  id: "user-id",
  username: "john_doe",
  email: "john@example.com",
  role: "Customer"
}
```

**Example**:
```javascript
import { getUserFromToken, getToken } from './utils/auth';

const token = getToken();
const user = getUserFromToken(token);
console.log('Current user:', user);
```

---

## Error Handling

All service methods use try-catch blocks and throw errors with the following structure:

```javascript
{
  message: "Error message",
  status: 400, // HTTP status code
  errors: { ... } // Validation errors (if applicable)
}
```

### Example Error Handling

```javascript
try {
  await authService.login('invalid', 'credentials');
} catch (error) {
  if (error.status === 401) {
    console.error('Invalid credentials');
  } else if (error.status === 400) {
    console.error('Validation error:', error.errors);
  } else {
    console.error('Server error:', error.message);
  }
}
```

---

## Environment Configuration

Create a `.env` file in the frontend root:

```env
REACT_APP_API_URL=http://localhost:5000/api
```

For production, update the URL to your deployed API endpoint.

---

## Testing the Services

### Prerequisites

1. ASP.NET Core Web API running on `http://localhost:5000`
2. React app running on `http://localhost:3000`

### Test Authentication Flow

```javascript
import authService from './services/authService';
import { setToken, getUserFromToken } from './utils/auth';

// 1. Register
const registerResponse = await authService.register('testuser', 'test@example.com', 'Test123!');
setToken(registerResponse.token);

// 2. Login
const loginResponse = await authService.login('testuser', 'Test123!');
setToken(loginResponse.token);

// 3. Get user info
const user = getUserFromToken(loginResponse.token);
console.log('Logged in user:', user);
```

### Test Product Flow

```javascript
import productService from './services/productService';

// Get all products
const products = await productService.getAllProducts();
console.log('Products:', products);

// Get specific product
const product = await productService.getProductById(1);
console.log('Product details:', product);
```

### Test Cart Flow

```javascript
import cartService from './services/cartService';

// Add to cart
await cartService.addToCart(1, 2);

// Get cart
const cart = await cartService.getCart();
console.log('Cart:', cart);

// Update quantity
await cartService.updateCartItem(cart.items[0].id, 3);

// Remove item
await cartService.removeFromCart(cart.items[0].id);

// Clear cart
await cartService.clearCart();
```

---

## Security Considerations

1. **Token Storage**: JWT tokens are stored in localStorage (consider httpOnly cookies for production)
2. **HTTPS**: Always use HTTPS in production
3. **Token Expiration**: Tokens expire after 24 hours (configurable in backend)
4. **CORS**: Backend configured to accept requests from React frontend
5. **Authorization**: Cart operations require authentication

---

## Next Steps

1. Implement React Router with protected routes
2. Create login and registration pages
3. Build product listing page
4. Implement shopping cart page
5. Add navigation and styling

---

## Support

For issues or questions, refer to:
- Backend API documentation: `backend/ECommerceAPI/README.md`
- React app documentation: `frontend/ecommerce-app/README.md`


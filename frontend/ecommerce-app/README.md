# E-Commerce React Frontend

React frontend application for the E-Commerce platform with authentication and shopping cart functionality.

## Features

- **Authentication System**
  - User login with username or email
  - User registration
  - JWT token-based authentication
  - Forgot password functionality
  - Reset password functionality
  - Automatic token refresh and validation

- **Shopping Cart**
  - Add products to cart
  - Update cart item quantities
  - Remove items from cart
  - Clear entire cart
  - Real-time cart count and total price
  - Persistent cart across sessions

- **Context Management**
  - AuthContext for global authentication state
  - CartContext for global cart state
  - Automatic token restoration from localStorage

## Technology Stack

- **React** 18.x - UI library
- **React Router DOM** - Client-side routing
- **Axios** - HTTP client for API calls
- **JWT Decode** - JWT token decoding
- **Context API** - Global state management

## Project Structure

```
src/
├── components/       # Reusable UI components
├── pages/           # Page components
│   ├── Login.js
│   ├── Register.js
│   ├── Products.js
│   ├── Cart.js
│   ├── ForgotPassword.js
│   └── ResetPassword.js
├── services/        # API service modules
│   ├── api.js              # Axios instance with interceptors
│   ├── authService.js      # Authentication API calls
│   ├── productService.js   # Product API calls
│   └── cartService.js      # Cart API calls
├── context/         # React Context providers
│   ├── AuthContext.js      # Authentication state
│   └── CartContext.js      # Cart state
├── utils/           # Utility functions
│   └── helpers.js          # Helper functions
├── styles/          # CSS files
└── App.js           # Main application component
```

## Setup Instructions

### Prerequisites

- Node.js 14.x or higher
- npm or yarn package manager
- ASP.NET Core Web API backend running

### Installation

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend/ecommerce-app
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Configure environment variables:**
   - Copy `.env.example` to `.env`
   - Update `REACT_APP_API_URL` with your backend API URL
   ```bash
   cp .env.example .env
   ```

4. **Start the development server:**
   ```bash
   npm start
   ```

   The application will open at `http://localhost:3000`

### Available Scripts

- `npm start` - Runs the app in development mode
- `npm test` - Launches the test runner
- `npm run build` - Builds the app for production
- `npm run eject` - Ejects from Create React App (one-way operation)

## Environment Variables

Create a `.env` file in the root directory:

```env
REACT_APP_API_URL=http://localhost:5000/api
```

## Context Usage

### AuthContext

```javascript
import { useAuth } from './context/AuthContext';

function MyComponent() {
  const { user, isAuthenticated, login, logout, register } = useAuth();
  
  // Use authentication state and functions
}
```

**Available Properties:**
- `user` - Current user object (id, username, email, role)
- `token` - JWT authentication token
- `isAuthenticated` - Boolean indicating if user is logged in
- `isLoading` - Boolean indicating if auth state is being restored
- `login(token, userData)` - Login function
- `logout()` - Logout function
- `register(token, userData)` - Register function

### CartContext

```javascript
import { useCart } from './context/CartContext';

function MyComponent() {
  const { 
    cartItems, 
    cartCount, 
    totalPrice, 
    addToCart, 
    updateCartItem, 
    removeFromCart, 
    clearCart 
  } = useCart();
  
  // Use cart state and functions
}
```

**Available Properties:**
- `cartItems` - Array of cart items
- `cartCount` - Total number of items in cart
- `totalPrice` - Total price of all items
- `isLoading` - Boolean indicating if cart operation is in progress
- `error` - Error message if cart operation failed
- `fetchCart()` - Fetch cart from backend
- `addToCart(productId, quantity)` - Add item to cart
- `updateCartItem(cartItemId, quantity)` - Update cart item quantity
- `removeFromCart(cartItemId)` - Remove item from cart
- `clearCart()` - Clear entire cart

## API Services

### Authentication Service

```javascript
import authService from './services/authService';

// Login
const response = await authService.login(usernameOrEmail, password);

// Register
const response = await authService.register(username, email, password);

// Forgot Password
await authService.forgotPassword(email);

// Reset Password
await authService.resetPassword(token, newPassword);
```

### Product Service

```javascript
import productService from './services/productService';

// Get all products
const products = await productService.getAllProducts();

// Get product by ID
const product = await productService.getProductById(id);

// Get products by category
const products = await productService.getProductsByCategory(category);
```

### Cart Service

```javascript
import cartService from './services/cartService';

// Get cart
const cart = await cartService.getCart();

// Add to cart
const cart = await cartService.addToCart(productId, quantity);

// Update cart item
const cart = await cartService.updateCartItem(cartItemId, quantity);

// Remove from cart
await cartService.removeFromCart(cartItemId);

// Clear cart
await cartService.clearCart();
```

## Utility Functions

```javascript
import { 
  formatCurrency, 
  validateEmail, 
  validatePassword, 
  getErrorMessage,
  truncateText,
  debounce
} from './utils/helpers';

// Format currency
const formatted = formatCurrency(99.99); // "$99.99"

// Validate email
const isValid = validateEmail('user@example.com'); // true

// Validate password
const isValid = validatePassword('password123'); // true (>= 6 chars)

// Get error message
const message = getErrorMessage(error);

// Truncate text
const short = truncateText('Long text...', 50);

// Debounce function
const debouncedSearch = debounce(searchFunction, 300);
```

## Authentication Flow

1. User enters credentials on login page
2. `authService.login()` sends request to backend
3. Backend returns JWT token and user data
4. `AuthContext.login()` stores token in localStorage
5. Token is decoded to extract user information
6. User state is updated and `isAuthenticated` becomes true
7. Axios interceptor automatically adds token to all API requests
8. On page refresh, token is restored from localStorage

## Cart Flow

1. User adds product to cart
2. `CartContext.addToCart()` sends request to backend
3. Backend validates stock and updates database
4. Cart state is updated with new data
5. Cart count and total price are recalculated
6. UI reflects updated cart state

## Error Handling

- API errors are caught and displayed to users
- 401 errors automatically redirect to login page
- Token expiration is handled gracefully
- Network errors show user-friendly messages

## Security Features

- JWT tokens stored in localStorage
- Automatic token validation on mount
- Expired tokens are removed automatically
- Protected routes require authentication
- Axios interceptors handle token injection
- CORS configured for backend communication

## Next Steps

The following features will be implemented in subsequent tasks:

1. **Routing** - React Router setup with protected routes
2. **Login Page** - Complete login form with validation
3. **Register Page** - User registration form
4. **Products Page** - Product listing with add to cart
5. **Cart Page** - Shopping cart with checkout
6. **Forgot/Reset Password** - Password recovery flow
7. **Navigation** - Header with cart count and user menu
8. **Styling** - Professional UI with CSS

## Development Notes

- The application uses Create React App as the base
- Context API is used for state management (no Redux needed)
- Axios interceptors handle authentication automatically
- All API calls go through centralized service modules
- Error handling is consistent across all services

## Troubleshooting

**Issue: API calls fail with CORS errors**
- Ensure backend CORS is configured to allow `http://localhost:3000`
- Check that `REACT_APP_API_URL` is set correctly in `.env`

**Issue: Token not persisting**
- Check browser localStorage for `token` key
- Ensure token is not expired
- Verify JWT secret matches between frontend and backend

**Issue: Cart not updating**
- Ensure user is authenticated
- Check that backend API is running
- Verify cart endpoints are accessible

## Contributing

This is part of a larger e-commerce application. Follow the coding standards and patterns established in the existing codebase.

## License

This project is part of the E-Commerce application suite.


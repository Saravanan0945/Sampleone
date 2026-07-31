# Cart Service Implementation

## Overview
The Cart Service provides complete shopping cart functionality for the e-commerce application, including product selection, quantity management, and cart persistence. It implements comprehensive validation, stock checking, and error handling.

## Components

### DTOs (Data Transfer Objects)

#### CartDto
Represents the complete shopping cart with all items and calculated totals.

**Properties:**
- `Id` (int) - Cart identifier
- `UserId` (int) - Owner of the cart
- `Items` (List<CartItemDto>) - List of items in the cart
- `TotalPrice` (decimal) - Sum of all item subtotals
- `CreatedAt` (DateTime) - Cart creation timestamp
- `UpdatedAt` (DateTime?) - Last update timestamp

#### CartItemDto
Represents a single item in the shopping cart.

**Properties:**
- `Id` (int) - Cart item identifier
- `ProductId` (int) - Product identifier
- `ProductName` (string) - Product name
- `ImageUrl` (string) - Product image URL
- `Quantity` (int) - Quantity of the product
- `Price` (decimal) - Price at the time of adding to cart
- `Subtotal` (decimal) - Calculated as Quantity × Price

#### AddToCartDto
Request DTO for adding products to the cart.

**Properties:**
- `ProductId` (int, required, min: 1) - Product to add
- `Quantity` (int, required, min: 1) - Quantity to add

**Validation:**
- ProductId must be greater than 0
- Quantity must be at least 1

#### UpdateCartItemDto
Request DTO for updating cart item quantities.

**Properties:**
- `CartItemId` (int, required, min: 1) - Cart item to update
- `Quantity` (int, required, min: 0) - New quantity (0 to remove)

**Validation:**
- CartItemId must be greater than 0
- Quantity must be 0 or greater (0 removes the item)

### Service Interface (ICartService)

```csharp
public interface ICartService
{
    Task<CartDto> GetCartAsync(int userId);
    Task<CartDto> AddToCartAsync(int userId, AddToCartDto dto);
    Task<CartDto> UpdateCartItemAsync(int userId, UpdateCartItemDto dto);
    Task<bool> RemoveFromCartAsync(int userId, int cartItemId);
    Task<bool> ClearCartAsync(int userId);
}
```

### Service Implementation (CartService)

#### GetCartAsync(int userId)
Retrieves the user's shopping cart or creates a new one if it doesn't exist.

**Process:**
1. Validates user exists and is active
2. Retrieves cart with all items and product details
3. Creates new cart if none exists
4. Returns CartDto with calculated totals

**Exceptions:**
- `NotFoundException` - User not found

#### AddToCartAsync(int userId, AddToCartDto dto)
Adds a product to the shopping cart or updates quantity if already present.

**Process:**
1. Validates user exists and is active
2. Validates product exists and is active
3. Validates sufficient stock is available
4. Gets or creates user's cart
5. If product already in cart:
   - Updates quantity (adds to existing)
   - Validates total quantity against stock
   - Updates price to current price
6. If product not in cart:
   - Creates new cart item
   - Sets current price
7. Updates cart timestamp
8. Returns updated CartDto

**Stock Validation:**
- Checks product stock before adding
- Validates total quantity if product already in cart
- Prevents adding more than available stock

**Exceptions:**
- `NotFoundException` - User or product not found
- `ValidationException` - Product not available or insufficient stock

#### UpdateCartItemAsync(int userId, UpdateCartItemDto dto)
Updates the quantity of a cart item or removes it if quantity is 0.

**Process:**
1. Validates user exists and is active
2. Retrieves user's cart
3. Validates cart item belongs to user
4. If quantity is 0:
   - Removes cart item
5. If quantity > 0:
   - Validates product still exists and is active
   - Validates new quantity against stock
   - Updates quantity
6. Updates cart timestamp
7. Returns updated CartDto

**Exceptions:**
- `NotFoundException` - User, cart, or cart item not found
- `ValidationException` - Product not available or insufficient stock

#### RemoveFromCartAsync(int userId, int cartItemId)
Removes a specific item from the shopping cart.

**Process:**
1. Validates user exists and is active
2. Retrieves user's cart
3. Validates cart item belongs to user
4. Removes cart item
5. Updates cart timestamp
6. Returns true on success

**Exceptions:**
- `NotFoundException` - User, cart, or cart item not found

#### ClearCartAsync(int userId)
Removes all items from the shopping cart.

**Process:**
1. Validates user exists and is active
2. Retrieves user's cart
3. Clears all cart items
4. Updates cart timestamp
5. Returns true on success

**Exceptions:**
- `NotFoundException` - User or cart not found

## API Endpoints

### GET /api/cart
Get the current user's shopping cart.

**Authentication:** Required (JWT)

**Response:** 200 OK
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Product Name",
      "imageUrl": "https://example.com/image.jpg",
      "quantity": 2,
      "price": 29.99,
      "subtotal": 59.98
    }
  ],
  "totalPrice": 59.98,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

### POST /api/cart/items
Add a product to the shopping cart.

**Authentication:** Required (JWT)

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response:** 200 OK (returns updated CartDto)

**Errors:**
- 400 Bad Request - Invalid input or insufficient stock
- 401 Unauthorized - Not authenticated
- 404 Not Found - Product not found

### PUT /api/cart/items
Update the quantity of a cart item.

**Authentication:** Required (JWT)

**Request Body:**
```json
{
  "cartItemId": 1,
  "quantity": 3
}
```

**Response:** 200 OK (returns updated CartDto)

**Errors:**
- 400 Bad Request - Invalid input or insufficient stock
- 401 Unauthorized - Not authenticated
- 404 Not Found - Cart item not found

### DELETE /api/cart/items/{cartItemId}
Remove an item from the shopping cart.

**Authentication:** Required (JWT)

**Response:** 200 OK
```json
{
  "message": "Item removed from cart successfully",
  "success": true
}
```

**Errors:**
- 401 Unauthorized - Not authenticated
- 404 Not Found - Cart item not found

### DELETE /api/cart
Clear all items from the shopping cart.

**Authentication:** Required (JWT)

**Response:** 200 OK
```json
{
  "message": "Cart cleared successfully",
  "success": true
}
```

**Errors:**
- 401 Unauthorized - Not authenticated
- 404 Not Found - Cart not found

## Features

### ✅ Product Selection
- Add products to cart with specified quantities
- Automatic cart creation for new users
- Product validation (exists, active)

### ✅ Stock Validation
- Validates stock availability before adding
- Checks total quantity when updating existing items
- Prevents adding more than available stock
- Provides clear error messages with stock information

### ✅ Cart Persistence
- Carts are stored in the database
- Cart items maintain price at time of addition
- Automatic timestamp tracking (created, updated)
- Soft delete support (IsActive flag)

### ✅ Quantity Management
- Add new items to cart
- Update quantities of existing items
- Remove items (quantity 0 or explicit delete)
- Clear entire cart

### ✅ Price Tracking
- Stores price at time of adding to cart
- Updates price when quantity is modified
- Calculates subtotals and total price
- Protects against price changes after adding

### ✅ Security
- JWT authentication required for all endpoints
- User validation on every operation
- Cart item ownership verification
- Prevents unauthorized access to other users' carts

### ✅ Error Handling
- Custom exceptions for different error scenarios
- Comprehensive validation messages
- Proper HTTP status codes
- User-friendly error messages

## Database Schema

### Cart Table
- Id (PK)
- UserId (FK to Users)
- CreatedAt
- UpdatedAt
- IsActive

### CartItem Table
- Id (PK)
- CartId (FK to Carts)
- ProductId (FK to Products)
- Quantity
- PriceAtAdd
- AddedAt

## Business Rules

1. **One Active Cart Per User**: Each user can have only one active cart at a time
2. **Automatic Cart Creation**: Cart is created automatically when user adds first item
3. **Stock Validation**: Cannot add more items than available in stock
4. **Price Locking**: Price is locked when item is added to cart
5. **Quantity Zero Removal**: Setting quantity to 0 removes the item from cart
6. **User Ownership**: Users can only access and modify their own carts
7. **Product Availability**: Only active products can be added to cart
8. **User Status**: Only active users can perform cart operations

## Testing Scenarios

### Happy Path
1. User adds product to empty cart → Cart created with item
2. User adds same product again → Quantity updated
3. User adds different product → New item added to cart
4. User updates item quantity → Quantity changed
5. User removes item → Item deleted from cart
6. User clears cart → All items removed

### Error Scenarios
1. Add non-existent product → 404 Not Found
2. Add inactive product → 400 Validation Error
3. Add more than stock → 400 Validation Error
4. Update non-existent cart item → 404 Not Found
5. Update with insufficient stock → 400 Validation Error
6. Remove item from another user's cart → 404 Not Found
7. Unauthenticated request → 401 Unauthorized

## Integration

The Cart Service integrates with:
- **ApplicationDbContext** - Database operations
- **ErrorHandlingMiddleware** - Exception handling
- **JWT Authentication** - User identification
- **Product Service** - Product validation (indirect via database)

## Future Enhancements

Potential improvements for future versions:
1. Cart expiration (auto-clear after X days)
2. Save for later functionality
3. Cart sharing/guest carts
4. Wishlist integration
5. Price change notifications
6. Stock reservation during checkout
7. Cart analytics and recommendations
8. Bulk operations (add multiple items at once)


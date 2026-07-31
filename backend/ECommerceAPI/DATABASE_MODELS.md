# Database Models Summary

## Overview
All database models have been successfully implemented with Entity Framework Core configuration. The models support the e-commerce application's authentication, product management, and shopping cart functionality.

## Models Implemented

### 1. User Model (`Models/User.cs`)
**Purpose**: Stores user account information with authentication details

**Properties**:
- `Id` (int, Primary Key) - Unique user identifier
- `Username` (string, Unique, Required, Max 50) - User's login username
- `Email` (string, Unique, Required, Max 100) - User's email address
- `PasswordHash` (string, Required) - BCrypt hashed password
- `FirstName` (string, Max 50) - User's first name
- `LastName` (string, Max 50) - User's last name
- `RoleId` (int, Foreign Key) - Reference to user's role
- `CreatedAt` (DateTime) - Account creation timestamp
- `LastLoginAt` (DateTime?) - Last login timestamp
- `IsActive` (bool) - Account active status

**Relationships**:
- One-to-Many with Role (via RoleId)
- One-to-Many with Cart (user can have multiple carts)

**Indexes**:
- Unique index on Username
- Unique index on Email

---

### 2. Role Model (`Models/Role.cs`)
**Purpose**: Defines user roles for authorization

**Properties**:
- `Id` (int, Primary Key) - Unique role identifier
- `Name` (string, Unique, Required, Max 50) - Role name
- `Description` (string) - Role description

**Relationships**:
- One-to-Many with User

**Seeded Data**:
- Admin (Id: 1) - Administrator with full access
- Customer (Id: 2) - Regular customer

**Indexes**:
- Unique index on Name

---

### 3. Product Model (`Models/Product.cs`)
**Purpose**: Stores product catalog information

**Properties**:
- `Id` (int, Primary Key) - Unique product identifier
- `Name` (string, Required, Max 200) - Product name
- `Description` (string, Max 1000) - Product description
- `Price` (decimal, Precision 18,2) - Product price
- `ImageUrl` (string) - Product image URL
- `StockQuantity` (int) - Available stock quantity
- `Category` (string, Max 100) - Product category
- `IsActive` (bool) - Product active status
- `CreatedAt` (DateTime) - Product creation timestamp
- `UpdatedAt` (DateTime?) - Last update timestamp

**Relationships**:
- One-to-Many with CartItem

---

### 4. Cart Model (`Models/Cart.cs`)
**Purpose**: Represents a user's shopping cart

**Properties**:
- `Id` (int, Primary Key) - Unique cart identifier
- `UserId` (int, Foreign Key) - Reference to cart owner
- `CreatedAt` (DateTime) - Cart creation timestamp
- `UpdatedAt` (DateTime?) - Last update timestamp
- `IsActive` (bool) - Cart active status

**Relationships**:
- Many-to-One with User (via UserId)
- One-to-Many with CartItem

**Delete Behavior**:
- Cascade delete when user is deleted

---

### 5. CartItem Model (`Models/CartItem.cs`)
**Purpose**: Represents individual items in a shopping cart

**Properties**:
- `Id` (int, Primary Key) - Unique cart item identifier
- `CartId` (int, Foreign Key) - Reference to parent cart
- `ProductId` (int, Foreign Key) - Reference to product
- `Quantity` (int) - Quantity of product in cart
- `PriceAtAdd` (decimal, Precision 18,2) - Product price when added to cart
- `AddedAt` (DateTime) - Timestamp when item was added

**Relationships**:
- Many-to-One with Cart (via CartId)
- Many-to-One with Product (via ProductId)

**Delete Behavior**:
- Cascade delete when cart is deleted
- Restrict delete when product is deleted (prevents accidental product deletion)

---

### 6. PasswordResetToken Model (`Models/PasswordResetToken.cs`)
**Purpose**: Manages password reset tokens for forgot password functionality

**Properties**:
- `Id` (int, Primary Key) - Unique token identifier
- `UserId` (int, Foreign Key) - Reference to user
- `Token` (string, Required, Max 500) - Reset token string
- `ExpiresAt` (DateTime) - Token expiration timestamp
- `IsUsed` (bool) - Whether token has been used
- `CreatedAt` (DateTime) - Token creation timestamp

**Relationships**:
- Many-to-One with User (via UserId)

**Delete Behavior**:
- Cascade delete when user is deleted

**Indexes**:
- Index on Token for fast lookup

---

## Database Context Configuration

### ApplicationDbContext (`Data/ApplicationDbContext.cs`)

**DbSet Properties**:
- `Users` - User entities
- `Roles` - Role entities
- `Products` - Product entities
- `Carts` - Cart entities
- `CartItems` - CartItem entities
- `PasswordResetTokens` - PasswordResetToken entities

**Key Configurations**:
1. **Unique Constraints**: Username and Email must be unique
2. **Cascade Deletes**: 
   - Deleting a user cascades to their carts and password reset tokens
   - Deleting a cart cascades to its cart items
3. **Restrict Deletes**:
   - Products cannot be deleted if referenced in cart items
   - Roles cannot be deleted if assigned to users
4. **Indexes**: Optimized for common queries (username, email, token lookups)
5. **Precision**: Decimal fields use 18,2 precision for currency values
6. **Seeded Data**: Admin and Customer roles are automatically created

---

## Entity Relationships Diagram

```
User (1) ----< (M) Cart (1) ----< (M) CartItem (M) >---- (1) Product
  |                                                              
  |                                                              
  v                                                              
Role (1) ----< (M) User                                         
  |                                                              
  v                                                              
PasswordResetToken (M) >---- (1) User                           
```

---

## Migration Status

✅ **Models Created**: All 6 models are implemented
✅ **DbContext Configured**: ApplicationDbContext is fully configured
✅ **Relationships Defined**: All foreign keys and navigation properties set up
✅ **Constraints Applied**: Unique indexes, required fields, and delete behaviors configured
✅ **Seeded Data**: Admin and Customer roles ready for seeding

⏳ **Pending**: Database migration needs to be run when .NET SDK is available
- Run: `dotnet ef migrations add InitialCreate`
- Run: `dotnet ef database update`

See `MIGRATION_INSTRUCTIONS.md` for detailed migration steps.

---

## Notes

1. **Password Security**: User passwords are stored as BCrypt hashes, never in plain text
2. **Soft Deletes**: Products and Users use `IsActive` flag for soft deletion
3. **Price Tracking**: CartItem stores `PriceAtAdd` to preserve historical pricing
4. **Token Security**: Password reset tokens have expiration and single-use enforcement
5. **Role-Based Access**: User roles enable authorization for admin-only operations
6. **Data Integrity**: Foreign key constraints and cascade behaviors maintain referential integrity


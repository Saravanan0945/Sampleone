# Database Migration Instructions

## Prerequisites
- .NET SDK 8.0 or later must be installed
- Entity Framework Core tools must be installed globally

## Install EF Core Tools (if not already installed)
```bash
dotnet tool install --global dotnet-ef
```

## Create and Apply Initial Migration

### Step 1: Navigate to the project directory
```bash
cd backend/ECommerceAPI
```

### Step 2: Create the initial migration
```bash
dotnet ef migrations add InitialCreate
```

This will create a `Migrations` folder with the initial database schema based on your models.

### Step 3: Apply the migration to create the database
```bash
dotnet ef database update
```

This will create the SQLite database file (`ecommerce.db`) in the project directory with all tables:
- Users
- Roles (with seeded Admin and Customer roles)
- Products
- Carts
- CartItems
- PasswordResetTokens

## Verify Database Creation

After running the migrations, you should see:
- A `Migrations` folder in the project directory
- A `ecommerce.db` file in the project directory

## Future Migrations

When you make changes to your models, create a new migration:
```bash
dotnet ef migrations add <MigrationName>
dotnet ef database update
```

## Troubleshooting

If you encounter issues:

1. **Remove existing migrations** (if needed):
   ```bash
   dotnet ef migrations remove
   ```

2. **Drop the database** (if needed):
   ```bash
   dotnet ef database drop
   ```

3. **Rebuild and retry**:
   ```bash
   dotnet build
   dotnet ef migrations add InitialCreate
   dotnet ef database update
   ```

## Database Schema Overview

### Users Table
- Id (Primary Key)
- Username (Unique)
- Email (Unique)
- PasswordHash
- FirstName
- LastName
- RoleId (Foreign Key to Roles)
- CreatedAt
- LastLoginAt
- IsActive

### Roles Table
- Id (Primary Key)
- Name (Unique)
- Description
- Seeded with: Admin, Customer

### Products Table
- Id (Primary Key)
- Name
- Description
- Price (Decimal 18,2)
- ImageUrl
- StockQuantity
- Category
- IsActive
- CreatedAt
- UpdatedAt

### Carts Table
- Id (Primary Key)
- UserId (Foreign Key to Users)
- CreatedAt
- UpdatedAt
- IsActive

### CartItems Table
- Id (Primary Key)
- CartId (Foreign Key to Carts)
- ProductId (Foreign Key to Products)
- Quantity
- PriceAtAdd (Decimal 18,2)
- AddedAt

### PasswordResetTokens Table
- Id (Primary Key)
- UserId (Foreign Key to Users)
- Token (Indexed)
- ExpiresAt
- IsUsed
- CreatedAt


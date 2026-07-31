using Microsoft.EntityFrameworkCore;
using ECommerceAPI.Data;
using ECommerceAPI.Models;
using ECommerceAPI.DTOs;
using ECommerceAPI.Exceptions;

namespace ECommerceAPI.Services;

public interface ICartService
{
    Task<CartDto> GetCartAsync(int userId);
    Task<CartDto> AddToCartAsync(int userId, AddToCartDto dto);
    Task<CartDto> UpdateCartItemAsync(int userId, UpdateCartItemDto dto);
    Task<bool> RemoveFromCartAsync(int userId, int cartItemId);
    Task<bool> ClearCartAsync(int userId);
}

public class CartService : ICartService
{
    private readonly ApplicationDbContext _context;

    public CartService(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<CartDto> GetCartAsync(int userId)
    {
        // Validate user exists
        var userExists = await _context.Users.AnyAsync(u => u.Id == userId && u.IsActive);
        if (!userExists)
        {
            throw new NotFoundException($"User with ID {userId} not found");
        }

        // Retrieve or create cart for user
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
        {
            // Create new cart for user
            cart = new Cart
            {
                UserId = userId,
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            };
            _context.Carts.Add(cart);
            await _context.SaveChangesAsync();
        }

        return MapToCartDto(cart);
    }

    public async Task<CartDto> AddToCartAsync(int userId, AddToCartDto dto)
    {
        // Validate user exists
        var userExists = await _context.Users.AnyAsync(u => u.Id == userId && u.IsActive);
        if (!userExists)
        {
            throw new NotFoundException($"User with ID {userId} not found");
        }

        // Validate product exists and is active
        var product = await _context.Products.FindAsync(dto.ProductId);
        if (product == null)
        {
            throw new NotFoundException($"Product with ID {dto.ProductId} not found");
        }

        if (!product.IsActive)
        {
            throw new ValidationException($"Product '{product.Name}' is not available");
        }

        // Validate sufficient stock
        if (product.StockQuantity < dto.Quantity)
        {
            throw new ValidationException($"Insufficient stock for product '{product.Name}'. Available: {product.StockQuantity}, Requested: {dto.Quantity}");
        }

        // Get or create cart
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
        {
            cart = new Cart
            {
                UserId = userId,
                CreatedAt = DateTime.UtcNow,
                IsActive = true
            };
            _context.Carts.Add(cart);
            await _context.SaveChangesAsync();
        }

        // Check if product already in cart
        var existingCartItem = cart.CartItems.FirstOrDefault(ci => ci.ProductId == dto.ProductId);

        if (existingCartItem != null)
        {
            // Update existing cart item quantity
            var newQuantity = existingCartItem.Quantity + dto.Quantity;
            
            // Validate total quantity against stock
            if (product.StockQuantity < newQuantity)
            {
                throw new ValidationException($"Insufficient stock for product '{product.Name}'. Available: {product.StockQuantity}, Total requested: {newQuantity}");
            }

            existingCartItem.Quantity = newQuantity;
            existingCartItem.PriceAtAdd = product.Price; // Update price to current price
        }
        else
        {
            // Add new cart item
            var cartItem = new CartItem
            {
                CartId = cart.Id,
                ProductId = dto.ProductId,
                Quantity = dto.Quantity,
                PriceAtAdd = product.Price,
                AddedAt = DateTime.UtcNow
            };
            cart.CartItems.Add(cartItem);
        }

        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        // Reload cart with updated data
        await _context.Entry(cart).ReloadAsync();
        cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.Id == cart.Id);

        return MapToCartDto(cart!);
    }

    public async Task<CartDto> UpdateCartItemAsync(int userId, UpdateCartItemDto dto)
    {
        // Validate user exists
        var userExists = await _context.Users.AnyAsync(u => u.Id == userId && u.IsActive);
        if (!userExists)
        {
            throw new NotFoundException($"User with ID {userId} not found");
        }

        // Get user's cart
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
        {
            throw new NotFoundException($"Cart not found for user with ID {userId}");
        }

        // Validate cart item belongs to user
        var cartItem = cart.CartItems.FirstOrDefault(ci => ci.Id == dto.CartItemId);
        if (cartItem == null)
        {
            throw new NotFoundException($"Cart item with ID {dto.CartItemId} not found in user's cart");
        }

        // If quantity is 0, remove the item
        if (dto.Quantity == 0)
        {
            cart.CartItems.Remove(cartItem);
        }
        else
        {
            // Validate product still exists and is active
            var product = await _context.Products.FindAsync(cartItem.ProductId);
            if (product == null)
            {
                throw new NotFoundException($"Product with ID {cartItem.ProductId} not found");
            }

            if (!product.IsActive)
            {
                throw new ValidationException($"Product '{product.Name}' is no longer available");
            }

            // Validate new quantity against stock
            if (product.StockQuantity < dto.Quantity)
            {
                throw new ValidationException($"Insufficient stock for product '{product.Name}'. Available: {product.StockQuantity}, Requested: {dto.Quantity}");
            }

            cartItem.Quantity = dto.Quantity;
        }

        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        // Reload cart with updated data
        await _context.Entry(cart).ReloadAsync();
        cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.Id == cart.Id);

        return MapToCartDto(cart!);
    }

    public async Task<bool> RemoveFromCartAsync(int userId, int cartItemId)
    {
        // Validate user exists
        var userExists = await _context.Users.AnyAsync(u => u.Id == userId && u.IsActive);
        if (!userExists)
        {
            throw new NotFoundException($"User with ID {userId} not found");
        }

        // Get user's cart
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
        {
            throw new NotFoundException($"Cart not found for user with ID {userId}");
        }

        // Validate cart item belongs to user
        var cartItem = cart.CartItems.FirstOrDefault(ci => ci.Id == cartItemId);
        if (cartItem == null)
        {
            throw new NotFoundException($"Cart item with ID {cartItemId} not found in user's cart");
        }

        cart.CartItems.Remove(cartItem);
        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return true;
    }

    public async Task<bool> ClearCartAsync(int userId)
    {
        // Validate user exists
        var userExists = await _context.Users.AnyAsync(u => u.Id == userId && u.IsActive);
        if (!userExists)
        {
            throw new NotFoundException($"User with ID {userId} not found");
        }

        // Get user's cart
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
        {
            throw new NotFoundException($"Cart not found for user with ID {userId}");
        }

        cart.CartItems.Clear();
        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return true;
    }

    private static CartDto MapToCartDto(Cart cart)
    {
        var items = cart.CartItems.Select(ci => new CartItemDto
        {
            Id = ci.Id,
            ProductId = ci.ProductId,
            ProductName = ci.Product.Name,
            ImageUrl = ci.Product.ImageUrl,
            Quantity = ci.Quantity,
            Price = ci.PriceAtAdd,
            Subtotal = ci.Quantity * ci.PriceAtAdd
        }).ToList();

        return new CartDto
        {
            Id = cart.Id,
            UserId = cart.UserId,
            Items = items,
            TotalPrice = items.Sum(i => i.Subtotal),
            CreatedAt = cart.CreatedAt,
            UpdatedAt = cart.UpdatedAt
        };
    }
}


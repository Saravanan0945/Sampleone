using Microsoft.EntityFrameworkCore;
using ECommerceAPI.Data;
using ECommerceAPI.Models;
using ECommerceAPI.DTOs;

namespace ECommerceAPI.Services;

public interface ICartService
{
    Task<CartDto?> GetUserCartAsync(int userId);
    Task<CartDto?> AddToCartAsync(int userId, AddToCartDto addToCartDto);
    Task<CartDto?> UpdateCartItemAsync(int userId, int cartItemId, UpdateCartItemDto updateCartItemDto);
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

    public async Task<CartDto?> GetUserCartAsync(int userId)
    {
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

        return MapToCartDto(cart);
    }

    public async Task<CartDto?> AddToCartAsync(int userId, AddToCartDto addToCartDto)
    {
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

        var product = await _context.Products.FindAsync(addToCartDto.ProductId);
        if (product == null || !product.IsActive)
            return null;

        if (product.StockQuantity < addToCartDto.Quantity)
            return null;

        var existingCartItem = cart.CartItems.FirstOrDefault(ci => ci.ProductId == addToCartDto.ProductId);

        if (existingCartItem != null)
        {
            existingCartItem.Quantity += addToCartDto.Quantity;
            existingCartItem.PriceAtAdd = product.Price;
        }
        else
        {
            var cartItem = new CartItem
            {
                CartId = cart.Id,
                ProductId = addToCartDto.ProductId,
                Quantity = addToCartDto.Quantity,
                PriceAtAdd = product.Price,
                AddedAt = DateTime.UtcNow
            };
            cart.CartItems.Add(cartItem);
        }

        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        await _context.Entry(cart).ReloadAsync();
        cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.Id == cart.Id);

        return MapToCartDto(cart!);
    }

    public async Task<CartDto?> UpdateCartItemAsync(int userId, int cartItemId, UpdateCartItemDto updateCartItemDto)
    {
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
            return null;

        var cartItem = cart.CartItems.FirstOrDefault(ci => ci.Id == cartItemId);
        if (cartItem == null)
            return null;

        if (updateCartItemDto.Quantity <= 0)
        {
            cart.CartItems.Remove(cartItem);
        }
        else
        {
            var product = await _context.Products.FindAsync(cartItem.ProductId);
            if (product == null || product.StockQuantity < updateCartItemDto.Quantity)
                return null;

            cartItem.Quantity = updateCartItemDto.Quantity;
        }

        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        await _context.Entry(cart).ReloadAsync();
        cart = await _context.Carts
            .Include(c => c.CartItems)
            .ThenInclude(ci => ci.Product)
            .FirstOrDefaultAsync(c => c.Id == cart.Id);

        return MapToCartDto(cart!);
    }

    public async Task<bool> RemoveFromCartAsync(int userId, int cartItemId)
    {
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
            return false;

        var cartItem = cart.CartItems.FirstOrDefault(ci => ci.Id == cartItemId);
        if (cartItem == null)
            return false;

        cart.CartItems.Remove(cartItem);
        cart.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return true;
    }

    public async Task<bool> ClearCartAsync(int userId)
    {
        var cart = await _context.Carts
            .Include(c => c.CartItems)
            .FirstOrDefaultAsync(c => c.UserId == userId && c.IsActive);

        if (cart == null)
            return false;

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
            ProductImageUrl = ci.Product.ImageUrl,
            Quantity = ci.Quantity,
            Price = ci.PriceAtAdd,
            Subtotal = ci.Quantity * ci.PriceAtAdd
        }).ToList();

        return new CartDto
        {
            Id = cart.Id,
            UserId = cart.UserId,
            Items = items,
            TotalAmount = items.Sum(i => i.Subtotal),
            CreatedAt = cart.CreatedAt,
            UpdatedAt = cart.UpdatedAt
        };
    }
}


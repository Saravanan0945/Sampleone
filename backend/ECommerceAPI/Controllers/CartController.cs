using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ECommerceAPI.DTOs;
using ECommerceAPI.Services;

namespace ECommerceAPI.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
public class CartController : ControllerBase
{
    private readonly ICartService _cartService;

    public CartController(ICartService cartService)
    {
        _cartService = cartService;
    }

    /// <summary>
    /// Get the current user's shopping cart
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetCart()
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var cart = await _cartService.GetCartAsync(userId.Value);
        return Ok(cart);
    }

    /// <summary>
    /// Add a product to the shopping cart
    /// </summary>
    [HttpPost("items")]
    public async Task<IActionResult> AddToCart([FromBody] AddToCartDto addToCartDto)
    {
        if (!ModelState.IsValid)
            return BadRequest(ModelState);

        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var cart = await _cartService.AddToCartAsync(userId.Value, addToCartDto);
        return Ok(cart);
    }

    /// <summary>
    /// Update the quantity of a cart item
    /// </summary>
    [HttpPut("items")]
    public async Task<IActionResult> UpdateCartItem([FromBody] UpdateCartItemDto updateCartItemDto)
    {
        if (!ModelState.IsValid)
            return BadRequest(ModelState);

        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var cart = await _cartService.UpdateCartItemAsync(userId.Value, updateCartItemDto);
        return Ok(cart);
    }

    /// <summary>
    /// Remove an item from the shopping cart
    /// </summary>
    [HttpDelete("items/{cartItemId}")]
    public async Task<IActionResult> RemoveFromCart(int cartItemId)
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var result = await _cartService.RemoveFromCartAsync(userId.Value, cartItemId);
        return Ok(new { message = "Item removed from cart successfully", success = result });
    }

    /// <summary>
    /// Clear all items from the shopping cart
    /// </summary>
    [HttpDelete]
    public async Task<IActionResult> ClearCart()
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var result = await _cartService.ClearCartAsync(userId.Value);
        return Ok(new { message = "Cart cleared successfully", success = result });
    }

    private int? GetUserId()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim))
            return null;

        return int.TryParse(userIdClaim, out var userId) ? userId : null;
    }
}


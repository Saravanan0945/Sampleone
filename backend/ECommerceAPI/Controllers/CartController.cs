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

    [HttpGet]
    public async Task<IActionResult> GetCart()
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var cart = await _cartService.GetUserCartAsync(userId.Value);
        if (cart == null)
            return NotFound(new { message = "Cart not found" });

        return Ok(cart);
    }

    [HttpPost("items")]
    public async Task<IActionResult> AddToCart([FromBody] AddToCartDto addToCartDto)
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        if (addToCartDto.ProductId <= 0 || addToCartDto.Quantity <= 0)
            return BadRequest(new { message = "Invalid product or quantity" });

        var cart = await _cartService.AddToCartAsync(userId.Value, addToCartDto);
        if (cart == null)
            return BadRequest(new { message = "Failed to add item to cart. Product may not exist or insufficient stock." });

        return Ok(cart);
    }

    [HttpPut("items/{cartItemId}")]
    public async Task<IActionResult> UpdateCartItem(int cartItemId, [FromBody] UpdateCartItemDto updateCartItemDto)
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        if (updateCartItemDto.Quantity < 0)
            return BadRequest(new { message = "Invalid quantity" });

        var cart = await _cartService.UpdateCartItemAsync(userId.Value, cartItemId, updateCartItemDto);
        if (cart == null)
            return BadRequest(new { message = "Failed to update cart item. Item may not exist or insufficient stock." });

        return Ok(cart);
    }

    [HttpDelete("items/{cartItemId}")]
    public async Task<IActionResult> RemoveFromCart(int cartItemId)
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var result = await _cartService.RemoveFromCartAsync(userId.Value, cartItemId);
        if (!result)
            return NotFound(new { message = "Cart item not found" });

        return Ok(new { message = "Item removed from cart successfully" });
    }

    [HttpDelete]
    public async Task<IActionResult> ClearCart()
    {
        var userId = GetUserId();
        if (userId == null)
            return Unauthorized(new { message = "User not authenticated" });

        var result = await _cartService.ClearCartAsync(userId.Value);
        if (!result)
            return NotFound(new { message = "Cart not found" });

        return Ok(new { message = "Cart cleared successfully" });
    }

    private int? GetUserId()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim))
            return null;

        return int.TryParse(userIdClaim, out var userId) ? userId : null;
    }
}


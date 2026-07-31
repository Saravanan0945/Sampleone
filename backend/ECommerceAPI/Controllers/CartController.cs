using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ECommerceAPI.DTOs;
using ECommerceAPI.Services;
using ECommerceAPI.Exceptions;

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
        try
        {
            var userId = GetUserId();
            if (userId == null)
                return Unauthorized(new { error = "User not authenticated" });

            var cart = await _cartService.GetCartAsync(userId.Value);
            return Ok(cart);
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { error = "An error occurred while retrieving the cart" });
        }
    }

    /// <summary>
    /// Add a product to the shopping cart
    /// </summary>
    [HttpPost("add")]
    public async Task<IActionResult> AddToCart([FromBody] AddToCartDto addToCartDto)
    {
        try
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var userId = GetUserId();
            if (userId == null)
                return Unauthorized(new { error = "User not authenticated" });

            var cart = await _cartService.AddToCartAsync(userId.Value, addToCartDto);
            return Ok(cart);
        }
        catch (ValidationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { error = "An error occurred while adding to cart" });
        }
    }

    /// <summary>
    /// Update the quantity of a cart item
    /// </summary>
    [HttpPut("update")]
    public async Task<IActionResult> UpdateCartItem([FromBody] UpdateCartItemDto updateCartItemDto)
    {
        try
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var userId = GetUserId();
            if (userId == null)
                return Unauthorized(new { error = "User not authenticated" });

            var cart = await _cartService.UpdateCartItemAsync(userId.Value, updateCartItemDto);
            return Ok(cart);
        }
        catch (ValidationException ex)
        {
            return BadRequest(new { error = ex.Message });
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return StatusCode(403, new { error = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { error = "An error occurred while updating the cart" });
        }
    }

    /// <summary>
    /// Remove an item from the shopping cart
    /// </summary>
    [HttpDelete("remove/{cartItemId}")]
    public async Task<IActionResult> RemoveFromCart(int cartItemId)
    {
        try
        {
            var userId = GetUserId();
            if (userId == null)
                return Unauthorized(new { error = "User not authenticated" });

            await _cartService.RemoveFromCartAsync(userId.Value, cartItemId);
            return NoContent();
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return StatusCode(403, new { error = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { error = "An error occurred while removing from cart" });
        }
    }

    /// <summary>
    /// Clear all items from the shopping cart
    /// </summary>
    [HttpDelete("clear")]
    public async Task<IActionResult> ClearCart()
    {
        try
        {
            var userId = GetUserId();
            if (userId == null)
                return Unauthorized(new { error = "User not authenticated" });

            await _cartService.ClearCartAsync(userId.Value);
            return NoContent();
        }
        catch (NotFoundException ex)
        {
            return NotFound(new { error = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { error = "An error occurred while clearing the cart" });
        }
    }

    /// <summary>
    /// Extract user ID from JWT claims
    /// </summary>
    private int? GetUserId()
    {
        var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userIdClaim))
            return null;

        return int.TryParse(userIdClaim, out var userId) ? userId : null;
    }
}


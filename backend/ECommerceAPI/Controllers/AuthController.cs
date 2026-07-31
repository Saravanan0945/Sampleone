using Microsoft.AspNetCore.Mvc;
using ECommerceAPI.DTOs;
using ECommerceAPI.Services;

namespace ECommerceAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginDto loginDto)
    {
        if (string.IsNullOrWhiteSpace(loginDto.UsernameOrEmail) || string.IsNullOrWhiteSpace(loginDto.Password))
            return BadRequest(new { message = "Username/Email and password are required" });

        var result = await _authService.LoginAsync(loginDto);
        if (result == null)
            return Unauthorized(new { message = "Invalid credentials" });

        return Ok(result);
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterDto registerDto)
    {
        if (string.IsNullOrWhiteSpace(registerDto.Username) || 
            string.IsNullOrWhiteSpace(registerDto.Email) || 
            string.IsNullOrWhiteSpace(registerDto.Password))
            return BadRequest(new { message = "Username, email, and password are required" });

        var result = await _authService.RegisterAsync(registerDto);
        if (result == null)
            return BadRequest(new { message = "Username or email already exists" });

        return CreatedAtAction(nameof(Register), new { id = result.Id }, result);
    }

    [HttpPost("forgot-password")]
    public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordDto forgotPasswordDto)
    {
        if (string.IsNullOrWhiteSpace(forgotPasswordDto.Email))
            return BadRequest(new { message = "Email is required" });

        var result = await _authService.ForgotPasswordAsync(forgotPasswordDto);
        if (!result)
            return NotFound(new { message = "User not found" });

        return Ok(new { message = "Password reset instructions sent to email" });
    }

    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDto resetPasswordDto)
    {
        if (string.IsNullOrWhiteSpace(resetPasswordDto.Email) || 
            string.IsNullOrWhiteSpace(resetPasswordDto.Token) || 
            string.IsNullOrWhiteSpace(resetPasswordDto.NewPassword))
            return BadRequest(new { message = "Email, token, and new password are required" });

        var result = await _authService.ResetPasswordAsync(resetPasswordDto);
        if (!result)
            return BadRequest(new { message = "Invalid reset token or user not found" });

        return Ok(new { message = "Password reset successfully" });
    }
}


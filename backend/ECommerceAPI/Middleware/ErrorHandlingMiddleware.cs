using System.Net;
using System.Text.Json;
using ECommerceAPI.Exceptions;

namespace ECommerceAPI.Middleware;

public class ErrorHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ErrorHandlingMiddleware> _logger;

    public ErrorHandlingMiddleware(RequestDelegate next, ILogger<ErrorHandlingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An exception occurred: {Message}", ex.Message);
            await HandleExceptionAsync(context, ex);
        }
    }

    private static Task HandleExceptionAsync(HttpContext context, Exception exception)
    {
        var code = HttpStatusCode.InternalServerError;
        var message = "An error occurred while processing your request";

        switch (exception)
        {
            case AuthenticationException:
                code = HttpStatusCode.Unauthorized;
                message = exception.Message;
                break;
            case ValidationException:
                code = HttpStatusCode.BadRequest;
                message = exception.Message;
                break;
            case NotFoundException:
                code = HttpStatusCode.NotFound;
                message = exception.Message;
                break;
            case InvalidOperationException:
                code = HttpStatusCode.BadRequest;
                message = exception.Message;
                break;
            case UnauthorizedAccessException:
                code = HttpStatusCode.Forbidden;
                message = exception.Message;
                break;
        }

        var result = JsonSerializer.Serialize(new
        {
            error = message,
            statusCode = (int)code
        });

        context.Response.ContentType = "application/json";
        context.Response.StatusCode = (int)code;

        return context.Response.WriteAsync(result);
    }
}


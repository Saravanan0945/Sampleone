using Microsoft.EntityFrameworkCore;
using ECommerceAPI.Data;
using ECommerceAPI.Models;
using ECommerceAPI.DTOs;
using ECommerceAPI.Exceptions;

namespace ECommerceAPI.Services;

public interface IProductService
{
    Task<List<ProductDto>> GetAllProductsAsync();
    Task<ProductDto> GetProductByIdAsync(int id);
}

public class ProductService : IProductService
{
    private readonly ApplicationDbContext _context;

    public ProductService(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<List<ProductDto>> GetAllProductsAsync()
    {
        return await _context.Products
            .Where(p => p.IsActive)
            .Select(p => new ProductDto
            {
                Id = p.Id,
                Name = p.Name,
                Description = p.Description,
                Price = p.Price,
                ImageUrl = p.ImageUrl,
                Stock = p.StockQuantity,
                IsActive = p.IsActive
            })
            .ToListAsync();
    }

    public async Task<ProductDto> GetProductByIdAsync(int id)
    {
        var product = await _context.Products.FindAsync(id);
        
        if (product == null || !product.IsActive)
        {
            throw new NotFoundException($"Product with ID {id} not found.");
        }

        return new ProductDto
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            ImageUrl = product.ImageUrl,
            Stock = product.StockQuantity,
            IsActive = product.IsActive
        };
    }
}


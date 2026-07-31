using Microsoft.EntityFrameworkCore;
using ECommerceAPI.Data;
using ECommerceAPI.Models;
using ECommerceAPI.DTOs;

namespace ECommerceAPI.Services;

public interface IProductService
{
    Task<List<ProductDto>> GetAllProductsAsync();
    Task<ProductDto?> GetProductByIdAsync(int id);
    Task<ProductDto?> CreateProductAsync(CreateProductDto createProductDto);
    Task<ProductDto?> UpdateProductAsync(int id, UpdateProductDto updateProductDto);
    Task<bool> DeleteProductAsync(int id);
    Task<List<ProductDto>> GetProductsByCategoryAsync(string category);
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
                StockQuantity = p.StockQuantity,
                Category = p.Category,
                IsActive = p.IsActive
            })
            .ToListAsync();
    }

    public async Task<ProductDto?> GetProductByIdAsync(int id)
    {
        var product = await _context.Products.FindAsync(id);
        if (product == null || !product.IsActive)
            return null;

        return new ProductDto
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            ImageUrl = product.ImageUrl,
            StockQuantity = product.StockQuantity,
            Category = product.Category,
            IsActive = product.IsActive
        };
    }

    public async Task<ProductDto?> CreateProductAsync(CreateProductDto createProductDto)
    {
        var product = new Product
        {
            Name = createProductDto.Name,
            Description = createProductDto.Description,
            Price = createProductDto.Price,
            ImageUrl = createProductDto.ImageUrl,
            StockQuantity = createProductDto.StockQuantity,
            Category = createProductDto.Category,
            IsActive = true,
            CreatedAt = DateTime.UtcNow
        };

        _context.Products.Add(product);
        await _context.SaveChangesAsync();

        return new ProductDto
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            ImageUrl = product.ImageUrl,
            StockQuantity = product.StockQuantity,
            Category = product.Category,
            IsActive = product.IsActive
        };
    }

    public async Task<ProductDto?> UpdateProductAsync(int id, UpdateProductDto updateProductDto)
    {
        var product = await _context.Products.FindAsync(id);
        if (product == null)
            return null;

        if (updateProductDto.Name != null)
            product.Name = updateProductDto.Name;
        if (updateProductDto.Description != null)
            product.Description = updateProductDto.Description;
        if (updateProductDto.Price.HasValue)
            product.Price = updateProductDto.Price.Value;
        if (updateProductDto.ImageUrl != null)
            product.ImageUrl = updateProductDto.ImageUrl;
        if (updateProductDto.StockQuantity.HasValue)
            product.StockQuantity = updateProductDto.StockQuantity.Value;
        if (updateProductDto.Category != null)
            product.Category = updateProductDto.Category;
        if (updateProductDto.IsActive.HasValue)
            product.IsActive = updateProductDto.IsActive.Value;

        product.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return new ProductDto
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            ImageUrl = product.ImageUrl,
            StockQuantity = product.StockQuantity,
            Category = product.Category,
            IsActive = product.IsActive
        };
    }

    public async Task<bool> DeleteProductAsync(int id)
    {
        var product = await _context.Products.FindAsync(id);
        if (product == null)
            return false;

        product.IsActive = false;
        product.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return true;
    }

    public async Task<List<ProductDto>> GetProductsByCategoryAsync(string category)
    {
        return await _context.Products
            .Where(p => p.IsActive && p.Category == category)
            .Select(p => new ProductDto
            {
                Id = p.Id,
                Name = p.Name,
                Description = p.Description,
                Price = p.Price,
                ImageUrl = p.ImageUrl,
                StockQuantity = p.StockQuantity,
                Category = p.Category,
                IsActive = p.IsActive
            })
            .ToListAsync();
    }
}


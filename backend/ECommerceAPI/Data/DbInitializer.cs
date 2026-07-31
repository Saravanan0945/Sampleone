using ECommerceAPI.Models;
using Microsoft.EntityFrameworkCore;

namespace ECommerceAPI.Data;

public static class DbInitializer
{
    public static async Task SeedData(ApplicationDbContext context)
    {
        // Check if products already exist
        if (await context.Products.AnyAsync())
        {
            return; // Database has been seeded
        }

        var products = new List<Product>
        {
            // Electronics
            new Product
            {
                Name = "Premium Laptop",
                Description = "High-performance laptop with 16GB RAM, 512GB SSD, and Intel Core i7 processor. Perfect for work and gaming.",
                Price = 999.99m,
                ImageUrl = "https://via.placeholder.com/400x300/4A90E2/FFFFFF?text=Laptop",
                StockQuantity = 25,
                Category = "Electronics",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Smartphone Pro",
                Description = "Latest smartphone with 128GB storage, 5G connectivity, and advanced camera system.",
                Price = 699.99m,
                ImageUrl = "https://via.placeholder.com/400x300/50C878/FFFFFF?text=Smartphone",
                StockQuantity = 40,
                Category = "Electronics",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Wireless Headphones",
                Description = "Premium noise-cancelling wireless headphones with 30-hour battery life.",
                Price = 149.99m,
                ImageUrl = "https://via.placeholder.com/400x300/9B59B6/FFFFFF?text=Headphones",
                StockQuantity = 50,
                Category = "Electronics",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            
            // Clothing
            new Product
            {
                Name = "Classic Cotton T-Shirt",
                Description = "Comfortable 100% cotton t-shirt available in multiple colors. Perfect for everyday wear.",
                Price = 29.99m,
                ImageUrl = "https://via.placeholder.com/400x300/E74C3C/FFFFFF?text=T-Shirt",
                StockQuantity = 100,
                Category = "Clothing",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Designer Jeans",
                Description = "Premium denim jeans with modern fit and durable construction.",
                Price = 59.99m,
                ImageUrl = "https://via.placeholder.com/400x300/3498DB/FFFFFF?text=Jeans",
                StockQuantity = 60,
                Category = "Clothing",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Running Sneakers",
                Description = "Lightweight running shoes with excellent cushioning and support.",
                Price = 89.99m,
                ImageUrl = "https://via.placeholder.com/400x300/F39C12/FFFFFF?text=Sneakers",
                StockQuantity = 45,
                Category = "Clothing",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            
            // Home
            new Product
            {
                Name = "Automatic Coffee Maker",
                Description = "Programmable coffee maker with 12-cup capacity and auto-shutoff feature.",
                Price = 79.99m,
                ImageUrl = "https://via.placeholder.com/400x300/8E44AD/FFFFFF?text=Coffee+Maker",
                StockQuantity = 30,
                Category = "Home",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "High-Speed Blender",
                Description = "Powerful 1000W blender perfect for smoothies, soups, and more.",
                Price = 49.99m,
                ImageUrl = "https://via.placeholder.com/400x300/16A085/FFFFFF?text=Blender",
                StockQuantity = 35,
                Category = "Home",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "LED Desk Lamp",
                Description = "Adjustable LED desk lamp with multiple brightness levels and USB charging port.",
                Price = 39.99m,
                ImageUrl = "https://via.placeholder.com/400x300/D35400/FFFFFF?text=Desk+Lamp",
                StockQuantity = 50,
                Category = "Home",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Smart Thermostat",
                Description = "WiFi-enabled smart thermostat with energy-saving features and mobile app control.",
                Price = 129.99m,
                ImageUrl = "https://via.placeholder.com/400x300/2C3E50/FFFFFF?text=Thermostat",
                StockQuantity = 20,
                Category = "Home",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            
            // Books
            new Product
            {
                Name = "Clean Code: A Handbook of Agile Software Craftsmanship",
                Description = "Essential programming book teaching best practices for writing clean, maintainable code.",
                Price = 45.99m,
                ImageUrl = "https://via.placeholder.com/400x300/27AE60/FFFFFF?text=Programming+Book",
                StockQuantity = 40,
                Category = "Books",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "The Midnight Library",
                Description = "Bestselling novel about life, regret, and the infinite possibilities of existence.",
                Price = 15.99m,
                ImageUrl = "https://via.placeholder.com/400x300/C0392B/FFFFFF?text=Novel",
                StockQuantity = 50,
                Category = "Books",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Design Patterns: Elements of Reusable Object-Oriented Software",
                Description = "Classic software engineering book covering essential design patterns.",
                Price = 49.99m,
                ImageUrl = "https://via.placeholder.com/400x300/2980B9/FFFFFF?text=Design+Patterns",
                StockQuantity = 30,
                Category = "Books",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            
            // Additional Products
            new Product
            {
                Name = "Wireless Mouse",
                Description = "Ergonomic wireless mouse with precision tracking and long battery life.",
                Price = 24.99m,
                ImageUrl = "https://via.placeholder.com/400x300/7F8C8D/FFFFFF?text=Mouse",
                StockQuantity = 75,
                Category = "Electronics",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            },
            new Product
            {
                Name = "Yoga Mat",
                Description = "Non-slip yoga mat with extra cushioning for comfortable workouts.",
                Price = 34.99m,
                ImageUrl = "https://via.placeholder.com/400x300/1ABC9C/FFFFFF?text=Yoga+Mat",
                StockQuantity = 40,
                Category = "Sports",
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            }
        };

        await context.Products.AddRangeAsync(products);
        await context.SaveChangesAsync();
    }
}


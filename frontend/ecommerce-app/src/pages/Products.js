import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import { getAllProducts } from '../services/productService';
import './Products.css';

const Products = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);
  const { addToCart } = useContext(CartContext);

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantities, setQuantities] = useState({});
  const [successMessage, setSuccessMessage] = useState('');
  const [addingToCart, setAddingToCart] = useState({});

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await getAllProducts();
      setProducts(data);
      
      // Initialize quantities for all products
      const initialQuantities = {};
      data.forEach(product => {
        initialQuantities[product.id] = 1;
      });
      setQuantities(initialQuantities);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load products. Please try again later.');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (productId, value) => {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    const quantity = parseInt(value) || 1;
    const validQuantity = Math.max(1, Math.min(quantity, product.stock));
    
    setQuantities(prev => ({
      ...prev,
      [productId]: validQuantity
    }));
  };

  const handleAddToCart = async (product) => {
    if (!isAuthenticated) {
      alert('Please login to add items to your cart');
      navigate('/login');
      return;
    }

    const quantity = quantities[product.id] || 1;

    try {
      setAddingToCart(prev => ({ ...prev, [product.id]: true }));
      setError('');
      
      await addToCart(product.id, quantity);
      
      setSuccessMessage(`${product.name} added to cart!`);
      setTimeout(() => setSuccessMessage(''), 3000);
      
      // Reset quantity to 1 after successful add
      setQuantities(prev => ({
        ...prev,
        [product.id]: 1
      }));
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to add item to cart';
      setError(errorMessage);
      setTimeout(() => setError(''), 5000);
      console.error('Error adding to cart:', err);
    } finally {
      setAddingToCart(prev => ({ ...prev, [product.id]: false }));
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(price);
  };

  if (loading) {
    return (
      <div className="products-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading products...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="products-container">
      <div className="products-header">
        <h1>Our Products</h1>
        <p>Browse our collection of quality products</p>
      </div>

      {successMessage && (
        <div className="success-toast">
          <span className="success-icon">✓</span>
          {successMessage}
        </div>
      )}

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠</span>
          {error}
        </div>
      )}

      {products.length === 0 ? (
        <div className="no-products">
          <p>No products available at the moment.</p>
        </div>
      ) : (
        <div className="products-grid">
          {products.map(product => (
            <div key={product.id} className="product-card">
              <div className="product-image-container">
                <img 
                  src={product.imageUrl} 
                  alt={product.name}
                  className="product-image"
                  onError={(e) => {
                    e.target.src = 'https://via.placeholder.com/300x200?text=No+Image';
                  }}
                />
                {product.stock < 10 && product.stock > 0 && (
                  <span className="low-stock-badge">Only {product.stock} left!</span>
                )}
                {product.stock === 0 && (
                  <span className="out-of-stock-badge">Out of Stock</span>
                )}
              </div>

              <div className="product-info">
                <h3 className="product-name">{product.name}</h3>
                <p className="product-description">{product.description}</p>
                
                <div className="product-footer">
                  <div className="product-price-section">
                    <span className="product-price">{formatPrice(product.price)}</span>
                    <span className="product-stock">
                      {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
                    </span>
                  </div>

                  {product.stock > 0 && (
                    <div className="product-actions">
                      <div className="quantity-selector">
                        <label htmlFor={`quantity-${product.id}`}>Qty:</label>
                        <input
                          id={`quantity-${product.id}`}
                          type="number"
                          min="1"
                          max={product.stock}
                          value={quantities[product.id] || 1}
                          onChange={(e) => handleQuantityChange(product.id, e.target.value)}
                          className="quantity-input"
                        />
                      </div>

                      <button
                        onClick={() => handleAddToCart(product)}
                        disabled={addingToCart[product.id]}
                        className="add-to-cart-btn"
                      >
                        {addingToCart[product.id] ? (
                          <>
                            <span className="btn-spinner"></span>
                            Adding...
                          </>
                        ) : (
                          <>
                            <span className="cart-icon">🛒</span>
                            Add to Cart
                          </>
                        )}
                      </button>
                    </div>
                  )}

                  {product.stock === 0 && (
                    <button className="add-to-cart-btn disabled" disabled>
                      Out of Stock
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Products;


import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import './Cart.css';

const Cart = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);
  const { 
    cartItems, 
    cartCount, 
    totalPrice, 
    fetchCart, 
    updateCartItem, 
    removeFromCart, 
    clearCart 
  } = useContext(CartContext);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingItems, setUpdatingItems] = useState({});

  useEffect(() => {
    // Redirect to login if not authenticated
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    // Fetch cart data on component mount
    const loadCart = async () => {
      try {
        setLoading(true);
        setError('');
        await fetchCart();
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load cart. Please try again.');
        console.error('Error loading cart:', err);
      } finally {
        setLoading(false);
      }
    };

    loadCart();
  }, [isAuthenticated, navigate, fetchCart]);

  const handleQuantityChange = async (cartItemId, currentQuantity, change) => {
    const newQuantity = currentQuantity + change;
    
    if (newQuantity < 1) {
      return; // Don't allow quantity less than 1
    }

    try {
      setUpdatingItems(prev => ({ ...prev, [cartItemId]: true }));
      setError('');
      await updateCartItem(cartItemId, newQuantity);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update quantity. Please try again.');
      console.error('Error updating cart item:', err);
    } finally {
      setUpdatingItems(prev => ({ ...prev, [cartItemId]: false }));
    }
  };

  const handleRemoveItem = async (cartItemId) => {
    if (!window.confirm('Are you sure you want to remove this item from your cart?')) {
      return;
    }

    try {
      setError('');
      await removeFromCart(cartItemId);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item. Please try again.');
      console.error('Error removing cart item:', err);
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm('Are you sure you want to clear your entire cart?')) {
      return;
    }

    try {
      setError('');
      await clearCart();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to clear cart. Please try again.');
      console.error('Error clearing cart:', err);
    }
  };

  const handleContinueShopping = () => {
    navigate('/products');
  };

  const handleCheckout = () => {
    alert('Checkout functionality will be implemented in a future update.');
  };

  if (loading) {
    return (
      <div className="cart-container">
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading your cart...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-container">
      <div className="cart-header">
        <h1>Shopping Cart</h1>
        <p className="cart-count">{cartCount} {cartCount === 1 ? 'item' : 'items'}</p>
      </div>

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          {error}
        </div>
      )}

      {cartItems.length === 0 ? (
        <div className="empty-cart">
          <div className="empty-cart-icon">🛒</div>
          <h2>Your cart is empty</h2>
          <p>Add some products to get started!</p>
          <button className="continue-shopping-btn" onClick={handleContinueShopping}>
            Browse Products
          </button>
        </div>
      ) : (
        <>
          <div className="cart-content">
            <div className="cart-items">
              <div className="cart-items-header">
                <span className="header-product">Product</span>
                <span className="header-price">Price</span>
                <span className="header-quantity">Quantity</span>
                <span className="header-subtotal">Subtotal</span>
                <span className="header-remove"></span>
              </div>

              {cartItems.map((item) => (
                <div key={item.id} className="cart-item">
                  <div className="item-product">
                    <img 
                      src={item.imageUrl} 
                      alt={item.productName}
                      className="item-image"
                    />
                    <div className="item-details">
                      <h3>{item.productName}</h3>
                    </div>
                  </div>

                  <div className="item-price">
                    ${item.price.toFixed(2)}
                  </div>

                  <div className="item-quantity">
                    <button
                      className="quantity-btn"
                      onClick={() => handleQuantityChange(item.id, item.quantity, -1)}
                      disabled={updatingItems[item.id] || item.quantity <= 1}
                    >
                      -
                    </button>
                    <span className="quantity-value">{item.quantity}</span>
                    <button
                      className="quantity-btn"
                      onClick={() => handleQuantityChange(item.id, item.quantity, 1)}
                      disabled={updatingItems[item.id]}
                    >
                      +
                    </button>
                  </div>

                  <div className="item-subtotal">
                    ${item.subtotal.toFixed(2)}
                  </div>

                  <div className="item-remove">
                    <button
                      className="remove-btn"
                      onClick={() => handleRemoveItem(item.id)}
                      disabled={updatingItems[item.id]}
                      title="Remove item"
                    >
                      ✕
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="cart-summary">
              <h2>Order Summary</h2>
              
              <div className="summary-row">
                <span>Items ({cartCount}):</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>

              <div className="summary-row">
                <span>Shipping:</span>
                <span>Calculated at checkout</span>
              </div>

              <div className="summary-divider"></div>

              <div className="summary-row summary-total">
                <span>Total:</span>
                <span>${totalPrice.toFixed(2)}</span>
              </div>

              <button className="checkout-btn" onClick={handleCheckout}>
                Proceed to Checkout
              </button>

              <button className="continue-shopping-btn" onClick={handleContinueShopping}>
                Continue Shopping
              </button>

              <button className="clear-cart-btn" onClick={handleClearCart}>
                Clear Cart
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Cart;


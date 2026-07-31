import React, { createContext, useState, useContext, useCallback } from 'react';
import axios from 'axios';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api';

export const CartProvider = ({ children }) => {
  const { token, isAuthenticated } = useAuth();
  const [cartItems, setCartItems] = useState([]);
  const [cartCount, setCartCount] = useState(0);
  const [totalPrice, setTotalPrice] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const getAuthHeaders = useCallback(() => {
    return {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    };
  }, [token]);

  const updateCartState = useCallback((cartData) => {
    if (cartData && cartData.items) {
      setCartItems(cartData.items);
      setCartCount(cartData.items.reduce((sum, item) => sum + item.quantity, 0));
      setTotalPrice(cartData.totalPrice || 0);
    } else {
      setCartItems([]);
      setCartCount(0);
      setTotalPrice(0);
    }
  }, []);

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated || !token) {
      setCartItems([]);
      setCartCount(0);
      setTotalPrice(0);
      return;
    }

    setIsLoading(true);
    setError(null);
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cart`, getAuthHeaders());
      updateCartState(response.data);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to fetch cart';
      setError(errorMessage);
      console.error('Error fetching cart:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, token, getAuthHeaders, updateCartState]);

  const addToCart = useCallback(async (productId, quantity = 1) => {
    if (!isAuthenticated || !token) {
      throw new Error('You must be logged in to add items to cart');
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await axios.post(
        `${API_BASE_URL}/cart/add`,
        { productId, quantity },
        getAuthHeaders()
      );
      updateCartState(response.data);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to add item to cart';
      setError(errorMessage);
      console.error('Error adding to cart:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, token, getAuthHeaders, updateCartState]);

  const updateCartItem = useCallback(async (cartItemId, quantity) => {
    if (!isAuthenticated || !token) {
      throw new Error('You must be logged in to update cart items');
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await axios.put(
        `${API_BASE_URL}/cart/update`,
        { cartItemId, quantity },
        getAuthHeaders()
      );
      updateCartState(response.data);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to update cart item';
      setError(errorMessage);
      console.error('Error updating cart item:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, token, getAuthHeaders, updateCartState]);

  const removeFromCart = useCallback(async (cartItemId) => {
    if (!isAuthenticated || !token) {
      throw new Error('You must be logged in to remove items from cart');
    }

    setIsLoading(true);
    setError(null);

    try {
      await axios.delete(
        `${API_BASE_URL}/cart/remove/${cartItemId}`,
        getAuthHeaders()
      );
      await fetchCart();
      return true;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to remove item from cart';
      setError(errorMessage);
      console.error('Error removing from cart:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, token, getAuthHeaders, fetchCart]);

  const clearCart = useCallback(async () => {
    if (!isAuthenticated || !token) {
      throw new Error('You must be logged in to clear cart');
    }

    setIsLoading(true);
    setError(null);

    try {
      await axios.delete(`${API_BASE_URL}/cart/clear`, getAuthHeaders());
      setCartItems([]);
      setCartCount(0);
      setTotalPrice(0);
      return true;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to clear cart';
      setError(errorMessage);
      console.error('Error clearing cart:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, token, getAuthHeaders]);

  const value = {
    cartItems,
    cartCount,
    totalPrice,
    isLoading,
    error,
    fetchCart,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export { CartContext };
export default CartContext;


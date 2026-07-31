import apiClient from './api';

const cartService = {
  getCart: async () => {
    try {
      const response = await apiClient.get('/cart');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  addToCart: async (productId, quantity = 1) => {
    try {
      const response = await apiClient.post('/cart/add', {
        productId,
        quantity
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  updateCartItem: async (cartItemId, quantity) => {
    try {
      const response = await apiClient.put('/cart/update', {
        cartItemId,
        quantity
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  removeFromCart: async (cartItemId) => {
    try {
      const response = await apiClient.delete(`/cart/remove/${cartItemId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  clearCart: async () => {
    try {
      const response = await apiClient.delete('/cart/clear');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};

export default cartService;


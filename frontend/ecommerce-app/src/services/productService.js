import apiClient from './api';

const productService = {
  getAllProducts: async () => {
    try {
      const response = await apiClient.get('/products');
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  getProductById: async (id) => {
    try {
      const response = await apiClient.get(`/products/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  },

  getProductsByCategory: async (category) => {
    try {
      const response = await apiClient.get(`/products/category/${category}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error;
    }
  }
};

export default productService;


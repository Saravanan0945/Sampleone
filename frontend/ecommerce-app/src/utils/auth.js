import { jwtDecode } from 'jwt-decode';

export const getToken = () => {
  return localStorage.getItem('token');
};

export const setToken = (token) => {
  localStorage.setItem('token', token);
};

export const removeToken = () => {
  localStorage.removeItem('token');
};

export const isTokenExpired = (token) => {
  if (!token) {
    return true;
  }

  try {
    const decoded = jwtDecode(token);
    const currentTime = Date.now() / 1000;
    
    return decoded.exp < currentTime;
  } catch (error) {
    console.error('Error decoding token:', error);
    return true;
  }
};

export const getDecodedToken = (token) => {
  if (!token) {
    return null;
  }

  try {
    return jwtDecode(token);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
};

export const getUserFromToken = (token) => {
  const decoded = getDecodedToken(token);
  if (!decoded) {
    return null;
  }

  return {
    id: decoded['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier'] || decoded.sub,
    username: decoded['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name'] || decoded.username,
    email: decoded['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress'] || decoded.email,
    role: decoded['http://schemas.microsoft.com/ws/2008/06/identity/claims/role'] || decoded.role
  };
};


import React, { useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { CartContext } from '../context/CartContext';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useContext(AuthContext);
  const { cartCount } = useContext(CartContext);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="header">
      <div className="header-container">
        {/* Logo/Brand */}
        <div className="header-logo">
          <Link to="/products" className="logo-link">
            <span className="logo-icon">🛒</span>
            <span className="logo-text">E-Shop</span>
          </Link>
        </div>

        {/* Navigation Links */}
        <nav className="header-nav">
          <Link to="/products" className="nav-link">
            Products
          </Link>
          <Link to="/cart" className="nav-link cart-link">
            Cart
            {cartCount > 0 && (
              <span className="cart-badge">{cartCount}</span>
            )}
          </Link>
        </nav>

        {/* Authentication Section */}
        <div className="header-auth">
          {isAuthenticated ? (
            <>
              <span className="user-greeting">
                Hello, <strong>{user?.username}</strong>
              </span>
              <button onClick={handleLogout} className="btn-logout">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-login">
                Login
              </Link>
              <Link to="/register" className="btn-register">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;


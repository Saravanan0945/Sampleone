import React from 'react';
import './ErrorMessage.css';

const ErrorMessage = ({ message, type = 'error', onClose }) => {
  if (!message) return null;

  const getIcon = () => {
    switch (type) {
      case 'success':
        return '✓';
      case 'warning':
        return '⚠';
      case 'info':
        return 'ℹ';
      case 'error':
      default:
        return '✕';
    }
  };

  return (
    <div className={`error-message ${type}`}>
      <div className="error-content">
        <span className="error-icon">{getIcon()}</span>
        <span className="error-text">{message}</span>
      </div>
      {onClose && (
        <button className="error-close" onClick={onClose} aria-label="Close">
          ✕
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;


import React from 'react';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <div className="App">
          <header className="App-header">
            <h1>E-Commerce Application</h1>
            <p>React + ASP.NET Core Web API</p>
          </header>
          <main>
            <p>Application is ready. Routing and pages will be added in the next steps.</p>
          </main>
        </div>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;


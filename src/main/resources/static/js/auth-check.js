// Authentication utility functions
const AuthCheck = {
    // API base URL
    API_BASE_URL: '/api/auth',

    // Session timeout duration (30 minutes in milliseconds)
    SESSION_TIMEOUT: 30 * 60 * 1000,

    // Activity timeout handler
    activityTimeout: null,

    /**
     * Check if user is authenticated
     * @returns {boolean} True if authenticated, false otherwise
     */
    isAuthenticated() {
        const token = this.getToken();
        if (!token) {
            return false;
        }

        // Check if token is expired
        if (this.isTokenExpired(token)) {
            this.clearAuth();
            return false;
        }

        return true;
    },

    /**
     * Get authentication token from storage
     * @returns {string|null} Token or null if not found
     */
    getToken() {
        return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    },

    /**
     * Get refresh token from storage
     * @returns {string|null} Refresh token or null if not found
     */
    getRefreshToken() {
        return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken');
    },

    /**
     * Store authentication tokens
     * @param {string} accessToken - JWT access token
     * @param {string} refreshToken - JWT refresh token
     * @param {boolean} rememberMe - Whether to use localStorage (true) or sessionStorage (false)
     */
    setTokens(accessToken, refreshToken, rememberMe = false) {
        const storage = rememberMe ? localStorage : sessionStorage;
        storage.setItem('authToken', accessToken);
        storage.setItem('refreshToken', refreshToken);
        storage.setItem('tokenTimestamp', Date.now().toString());
    },

    /**
     * Store user information
     * @param {object} user - User object
     * @param {boolean} rememberMe - Whether to use localStorage
     */
    setUser(user, rememberMe = false) {
        const storage = rememberMe ? localStorage : sessionStorage;
        storage.setItem('user', JSON.stringify(user));
    },

    /**
     * Get stored user information
     * @returns {object|null} User object or null
     */
    getUser() {
        const userStr = localStorage.getItem('user') || sessionStorage.getItem('user');
        if (!userStr) return null;
        
        try {
            return JSON.parse(userStr);
        } catch (e) {
            console.error('Error parsing user data:', e);
            return null;
        }
    },

    /**
     * Clear all authentication data
     */
    clearAuth() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        localStorage.removeItem('tokenTimestamp');
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('refreshToken');
        sessionStorage.removeItem('user');
        sessionStorage.removeItem('tokenTimestamp');
        
        if (this.activityTimeout) {
            clearTimeout(this.activityTimeout);
        }
    },

    /**
     * Check if token is expired (basic check based on timestamp)
     * @param {string} token - JWT token
     * @returns {boolean} True if expired
     */
    isTokenExpired(token) {
        try {
            const payload = this.parseJwt(token);
            if (!payload || !payload.exp) {
                return true;
            }
            
            const currentTime = Math.floor(Date.now() / 1000);
            return payload.exp < currentTime;
        } catch (e) {
            console.error('Error checking token expiration:', e);
            return true;
        }
    },

    /**
     * Parse JWT token to extract payload
     * @param {string} token - JWT token
     * @returns {object|null} Decoded payload or null
     */
    parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error('Error parsing JWT:', e);
            return null;
        }
    },

    /**
     * Make authenticated API request
     * @param {string} url - API endpoint URL
     * @param {object} options - Fetch options
     * @returns {Promise<Response>} Fetch response
     */
    async authenticatedFetch(url, options = {}) {
        const token = this.getToken();
        
        if (!token) {
            throw new Error('No authentication token found');
        }

        // Add authorization header
        const headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        try {
            const response = await fetch(url, { ...options, headers });

            // Handle token expiration
            if (response.status === 401) {
                // Try to refresh token
                const refreshed = await this.refreshToken();
                if (refreshed) {
                    // Retry the request with new token
                    headers['Authorization'] = `Bearer ${this.getToken()}`;
                    return await fetch(url, { ...options, headers });
                } else {
                    // Refresh failed, redirect to login
                    this.redirectToLogin();
                    throw new Error('Session expired. Please login again.');
                }
            }

            return response;
        } catch (error) {
            console.error('Authenticated fetch error:', error);
            throw error;
        }
    },

    /**
     * Refresh access token using refresh token
     * @returns {Promise<boolean>} True if refresh successful
     */
    async refreshToken() {
        const refreshToken = this.getRefreshToken();
        
        if (!refreshToken) {
            return false;
        }

        try {
            const response = await fetch(`${this.API_BASE_URL}/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken })
            });

            if (response.ok) {
                const data = await response.json();
                const rememberMe = !!localStorage.getItem('authToken');
                this.setTokens(data.accessToken, data.refreshToken, rememberMe);
                return true;
            }

            return false;
        } catch (error) {
            console.error('Token refresh error:', error);
            return false;
        }
    },

    /**
     * Redirect to login page
     * @param {string} returnUrl - URL to return to after login
     */
    redirectToLogin(returnUrl = null) {
        this.clearAuth();
        const url = returnUrl ? `/login.html?returnUrl=${encodeURIComponent(returnUrl)}` : '/login.html';
        window.location.href = url;
    },

    /**
     * Redirect to profile/dashboard page
     */
    redirectToProfile() {
        window.location.href = '/profile.html';
    },

    /**
     * Initialize session timeout tracking
     */
    initSessionTimeout() {
        // Clear existing timeout
        if (this.activityTimeout) {
            clearTimeout(this.activityTimeout);
        }

        // Set new timeout
        this.activityTimeout = setTimeout(() => {
            alert('Your session has expired due to inactivity. Please login again.');
            this.redirectToLogin();
        }, this.SESSION_TIMEOUT);

        // Reset timeout on user activity
        const resetTimeout = () => {
            if (this.activityTimeout) {
                clearTimeout(this.activityTimeout);
            }
            this.initSessionTimeout();
        };

        // Listen for user activity
        ['mousedown', 'keydown', 'scroll', 'touchstart'].forEach(event => {
            document.addEventListener(event, resetTimeout, { once: true, passive: true });
        });
    },

    /**
     * Protect page - redirect to login if not authenticated
     */
    protectPage() {
        if (!this.isAuthenticated()) {
            const currentUrl = window.location.pathname + window.location.search;
            this.redirectToLogin(currentUrl);
            return false;
        }
        
        // Initialize session timeout for authenticated users
        this.initSessionTimeout();
        return true;
    },

    /**
     * Sanitize HTML to prevent XSS
     * @param {string} str - String to sanitize
     * @returns {string} Sanitized string
     */
    sanitizeHTML(str) {
        const temp = document.createElement('div');
        temp.textContent = str;
        return temp.innerHTML;
    },

    /**
     * Escape HTML special characters
     * @param {string} str - String to escape
     * @returns {string} Escaped string
     */
    escapeHTML(str) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#x27;',
            '/': '&#x2F;'
        };
        return str.replace(/[&<>"'/]/g, (char) => map[char]);
    }
};

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthCheck;
}


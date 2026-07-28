// Profile page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Protect page - redirect to login if not authenticated
    if (!AuthCheck.protectPage()) {
        return;
    }

    // Get elements
    const logoutBtn = document.getElementById('logoutBtn');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const refreshTokenBtn = document.getElementById('refreshTokenBtn');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');
    const loadingOverlay = document.getElementById('loadingOverlay');

    // Profile field elements
    const userNameElement = document.getElementById('userName');
    const userEmailElement = document.getElementById('userEmail');
    const userIdElement = document.getElementById('userId');
    const usernameElement = document.getElementById('username');
    const emailElement = document.getElementById('email');
    const firstNameElement = document.getElementById('firstName');
    const lastNameElement = document.getElementById('lastName');
    const lastLoginElement = document.getElementById('lastLogin');
    const createdAtElement = document.getElementById('createdAt');
    const tokenExpiryElement = document.getElementById('tokenExpiry');

    /**
     * Show error message
     * @param {string} message - Error message to display
     */
    function showError(message) {
        errorText.textContent = AuthCheck.escapeHTML(message);
        errorMessage.style.display = 'flex';
        successMessage.style.display = 'none';
        
        setTimeout(() => {
            errorMessage.style.display = 'none';
        }, 5000);
    }

    /**
     * Show success message
     * @param {string} message - Success message to display
     */
    function showSuccess(message) {
        successText.textContent = AuthCheck.escapeHTML(message);
        successMessage.style.display = 'flex';
        errorMessage.style.display = 'none';
        
        setTimeout(() => {
            successMessage.style.display = 'none';
        }, 5000);
    }

    /**
     * Show loading overlay
     * @param {boolean} show - Whether to show overlay
     */
    function showLoading(show) {
        loadingOverlay.style.display = show ? 'flex' : 'none';
    }

    /**
     * Format date string
     * @param {string} dateString - ISO date string
     * @returns {string} Formatted date
     */
    function formatDate(dateString) {
        if (!dateString) return '-';
        
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (e) {
            return dateString;
        }
    }

    /**
     * Calculate token expiry time
     * @returns {string} Formatted expiry time
     */
    function getTokenExpiry() {
        const token = AuthCheck.getToken();
        if (!token) return '-';

        try {
            const payload = AuthCheck.parseJwt(token);
            if (!payload || !payload.exp) return '-';

            const expiryDate = new Date(payload.exp * 1000);
            const now = new Date();
            const diffMs = expiryDate - now;
            
            if (diffMs <= 0) {
                return 'Expired';
            }

            const diffMins = Math.floor(diffMs / 60000);
            const diffHours = Math.floor(diffMins / 60);
            const remainingMins = diffMins % 60;

            if (diffHours > 0) {
                return `${diffHours}h ${remainingMins}m remaining`;
            } else {
                return `${diffMins}m remaining`;
            }
        } catch (e) {
            console.error('Error calculating token expiry:', e);
            return '-';
        }
    }

    /**
     * Load user profile from stored data
     */
    function loadStoredProfile() {
        const user = AuthCheck.getUser();
        
        if (user) {
            userNameElement.textContent = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username;
            userEmailElement.textContent = user.email || '-';
            userIdElement.textContent = user.id || '-';
            usernameElement.textContent = user.username || '-';
            emailElement.textContent = user.email || '-';
            firstNameElement.textContent = user.firstName || '-';
            lastNameElement.textContent = user.lastName || '-';
        }

        // Update token expiry
        tokenExpiryElement.textContent = getTokenExpiry();
    }

    /**
     * Fetch user profile from API
     */
    async function fetchUserProfile() {
        showLoading(true);

        try {
            const response = await AuthCheck.authenticatedFetch('/api/auth/profile', {
                method: 'GET'
            });

            if (response.ok) {
                const data = await response.json();
                
                // Update UI with profile data
                const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim() || data.username;
                userNameElement.textContent = fullName;
                userEmailElement.textContent = data.email || '-';
                userIdElement.textContent = data.id || '-';
                usernameElement.textContent = data.username || '-';
                emailElement.textContent = data.email || '-';
                firstNameElement.textContent = data.firstName || '-';
                lastNameElement.textContent = data.lastName || '-';
                lastLoginElement.textContent = formatDate(data.lastLogin);
                createdAtElement.textContent = formatDate(data.createdAt);
                tokenExpiryElement.textContent = getTokenExpiry();

                // Update stored user data
                const rememberMe = !!localStorage.getItem('authToken');
                AuthCheck.setUser(data, rememberMe);
            } else {
                showError('Failed to load profile data');
                // Fall back to stored data
                loadStoredProfile();
            }
        } catch (error) {
            console.error('Error fetching profile:', error);
            showError('Error loading profile. Using cached data.');
            loadStoredProfile();
        } finally {
            showLoading(false);
        }
    }

    /**
     * Handle logout
     */
    async function handleLogout() {
        if (!confirm('Are you sure you want to logout?')) {
            return;
        }

        showLoading(true);

        try {
            const user = AuthCheck.getUser();
            const username = user ? user.username : null;

            if (username) {
                // Call logout API
                await AuthCheck.authenticatedFetch('/api/auth/logout', {
                    method: 'POST',
                    body: JSON.stringify({ username })
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // Clear auth data and redirect regardless of API result
            AuthCheck.clearAuth();
            window.location.href = '/login.html?message=' + 
                encodeURIComponent('You have been logged out successfully.');
        }
    }

    /**
     * Handle change password
     */
    function handleChangePassword() {
        // In a real application, this would open a modal or navigate to a change password page
        alert('Change password functionality would be implemented here.\n\n' +
              'This would typically:\n' +
              '1. Show a modal with current password and new password fields\n' +
              '2. Validate the new password\n' +
              '3. Call the change password API\n' +
              '4. Show success/error message');
    }

    /**
     * Handle refresh token
     */
    async function handleRefreshToken() {
        showLoading(true);

        try {
            const refreshed = await AuthCheck.refreshToken();
            
            if (refreshed) {
                showSuccess('Session refreshed successfully!');
                tokenExpiryElement.textContent = getTokenExpiry();
            } else {
                showError('Failed to refresh session. Please login again.');
                setTimeout(() => {
                    AuthCheck.redirectToLogin();
                }, 2000);
            }
        } catch (error) {
            console.error('Refresh token error:', error);
            showError('Error refreshing session. Please login again.');
            setTimeout(() => {
                AuthCheck.redirectToLogin();
            }, 2000);
        } finally {
            showLoading(false);
        }
    }

    // Event listeners
    logoutBtn.addEventListener('click', handleLogout);
    changePasswordBtn.addEventListener('click', handleChangePassword);
    refreshTokenBtn.addEventListener('click', handleRefreshToken);

    // Load profile on page load
    loadStoredProfile(); // Load cached data first for instant display
    fetchUserProfile();  // Then fetch fresh data from API

    // Update token expiry every minute
    setInterval(() => {
        tokenExpiryElement.textContent = getTokenExpiry();
    }, 60000);

    // Check for success message in URL
    const urlParams = new URLSearchParams(window.location.search);
    const message = urlParams.get('message');
    if (message) {
        showSuccess(decodeURIComponent(message));
        // Clean URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});


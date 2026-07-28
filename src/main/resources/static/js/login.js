// Login page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Check if already authenticated
    if (AuthCheck.isAuthenticated()) {
        AuthCheck.redirectToProfile();
        return;
    }

    // Get form elements
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const toggleIcon = document.getElementById('toggleIcon');
    const loginBtn = document.getElementById('loginBtn');
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    const rememberMeCheckbox = document.getElementById('rememberMe');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');
    const rateLimitWarning = document.getElementById('rateLimitWarning');
    const rateLimitText = document.getElementById('rateLimitText');

    // Field error elements
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');

    // Login attempt tracking
    let loginAttempts = 0;
    const MAX_CLIENT_ATTEMPTS = 10;

    /**
     * Show error message
     * @param {string} message - Error message to display
     */
    function showError(message) {
        errorText.textContent = AuthCheck.escapeHTML(message);
        errorMessage.style.display = 'flex';
        successMessage.style.display = 'none';
        
        // Auto-hide after 5 seconds
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
    }

    /**
     * Show field error
     * @param {HTMLElement} errorElement - Error element to show
     * @param {string} message - Error message
     */
    function showFieldError(errorElement, message) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }

    /**
     * Hide field error
     * @param {HTMLElement} errorElement - Error element to hide
     */
    function hideFieldError(errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('show');
    }

    /**
     * Hide all messages
     */
    function hideAllMessages() {
        errorMessage.style.display = 'none';
        successMessage.style.display = 'none';
        rateLimitWarning.style.display = 'none';
        hideFieldError(usernameError);
        hideFieldError(passwordError);
    }

    /**
     * Validate email format
     * @param {string} email - Email to validate
     * @returns {boolean} True if valid
     */
    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    /**
     * Validate username format
     * @param {string} username - Username to validate
     * @returns {boolean} True if valid
     */
    function isValidUsername(username) {
        // Username: 3-50 characters, alphanumeric, underscore, hyphen
        const usernameRegex = /^[a-zA-Z0-9_-]{3,50}$/;
        return usernameRegex.test(username);
    }

    /**
     * Validate form inputs
     * @returns {boolean} True if all inputs are valid
     */
    function validateForm() {
        let isValid = true;
        hideAllMessages();

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        // Validate username/email
        if (!username) {
            showFieldError(usernameError, 'Username or email is required');
            usernameInput.classList.add('error');
            isValid = false;
        } else if (username.length < 3) {
            showFieldError(usernameError, 'Username must be at least 3 characters');
            usernameInput.classList.add('error');
            isValid = false;
        } else if (username.length > 50) {
            showFieldError(usernameError, 'Username must not exceed 50 characters');
            usernameInput.classList.add('error');
            isValid = false;
        } else {
            usernameInput.classList.remove('error');
        }

        // Validate password
        if (!password) {
            showFieldError(passwordError, 'Password is required');
            passwordInput.classList.add('error');
            isValid = false;
        } else if (password.length < 8) {
            showFieldError(passwordError, 'Password must be at least 8 characters');
            passwordInput.classList.add('error');
            isValid = false;
        } else {
            passwordInput.classList.remove('error');
        }

        return isValid;
    }

    /**
     * Sanitize input to prevent XSS
     * @param {string} input - Input to sanitize
     * @returns {string} Sanitized input
     */
    function sanitizeInput(input) {
        return AuthCheck.escapeHTML(input.trim());
    }

    /**
     * Set loading state
     * @param {boolean} loading - Whether to show loading state
     */
    function setLoading(loading) {
        if (loading) {
            loginBtn.disabled = true;
            btnText.style.display = 'none';
            btnSpinner.style.display = 'inline-block';
            usernameInput.disabled = true;
            passwordInput.disabled = true;
        } else {
            loginBtn.disabled = false;
            btnText.style.display = 'inline';
            btnSpinner.style.display = 'none';
            usernameInput.disabled = false;
            passwordInput.disabled = false;
        }
    }

    /**
     * Handle login response
     * @param {object} data - Response data
     */
    function handleLoginSuccess(data) {
        const rememberMe = rememberMeCheckbox.checked;
        
        // Store tokens
        AuthCheck.setTokens(data.accessToken, data.refreshToken, rememberMe);
        
        // Store user info
        const user = {
            id: data.id,
            username: data.username,
            email: data.email,
            firstName: data.firstName,
            lastName: data.lastName
        };
        AuthCheck.setUser(user, rememberMe);

        // Show success message
        showSuccess('Login successful! Redirecting...');

        // Redirect after short delay
        setTimeout(() => {
            // Check for return URL
            const urlParams = new URLSearchParams(window.location.search);
            const returnUrl = urlParams.get('returnUrl');
            
            if (returnUrl) {
                window.location.href = decodeURIComponent(returnUrl);
            } else {
                AuthCheck.redirectToProfile();
            }
        }, 1000);
    }

    /**
     * Handle login error
     * @param {number} status - HTTP status code
     * @param {object} error - Error data
     */
    function handleLoginError(status, error) {
        loginAttempts++;

        let errorMsg = 'Login failed. Please try again.';

        switch (status) {
            case 401:
                errorMsg = error.message || 'Invalid username or password';
                break;
            case 423:
                errorMsg = error.message || 'Account is locked due to multiple failed login attempts. Please try again later or reset your password.';
                break;
            case 429:
                errorMsg = 'Too many login attempts. Please try again later.';
                if (error.remainingAttempts !== undefined) {
                    rateLimitText.textContent = `You have ${error.remainingAttempts} login attempts remaining.`;
                    rateLimitWarning.style.display = 'flex';
                }
                break;
            case 500:
                errorMsg = 'Server error. Please try again later.';
                break;
            default:
                errorMsg = error.message || 'An unexpected error occurred';
        }

        showError(errorMsg);

        // Show warning if approaching client-side limit
        if (loginAttempts >= MAX_CLIENT_ATTEMPTS - 3 && loginAttempts < MAX_CLIENT_ATTEMPTS) {
            rateLimitText.textContent = `Warning: ${MAX_CLIENT_ATTEMPTS - loginAttempts} attempts remaining before temporary lockout.`;
            rateLimitWarning.style.display = 'flex';
        }

        // Client-side rate limiting
        if (loginAttempts >= MAX_CLIENT_ATTEMPTS) {
            showError('Too many failed attempts. Please wait 5 minutes before trying again.');
            loginBtn.disabled = true;
            
            setTimeout(() => {
                loginAttempts = 0;
                loginBtn.disabled = false;
                rateLimitWarning.style.display = 'none';
            }, 5 * 60 * 1000); // 5 minutes
        }
    }

    /**
     * Perform login
     */
    async function performLogin() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        // Get and sanitize inputs
        const username = sanitizeInput(usernameInput.value);
        const password = passwordInput.value; // Don't sanitize password

        // Set loading state
        setLoading(true);
        hideAllMessages();

        try {
            // Get client IP (for rate limiting tracking)
            const ipResponse = await fetch('https://api.ipify.org?format=json').catch(() => ({ json: () => ({ ip: 'unknown' }) }));
            const ipData = await ipResponse.json();
            const clientIp = ipData.ip || 'unknown';

            // Make login request
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Forwarded-For': clientIp
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            const data = await response.json();

            if (response.ok) {
                handleLoginSuccess(data);
            } else {
                handleLoginError(response.status, data);
            }
        } catch (error) {
            console.error('Login error:', error);
            showError('Network error. Please check your connection and try again.');
        } finally {
            setLoading(false);
        }
    }

    // Toggle password visibility
    togglePasswordBtn.addEventListener('click', function() {
        const type = passwordInput.type === 'password' ? 'text' : 'password';
        passwordInput.type = type;
        
        // Toggle icon
        if (type === 'text') {
            toggleIcon.classList.remove('fa-eye');
            toggleIcon.classList.add('fa-eye-slash');
            togglePasswordBtn.setAttribute('aria-label', 'Hide password');
        } else {
            toggleIcon.classList.remove('fa-eye-slash');
            toggleIcon.classList.add('fa-eye');
            togglePasswordBtn.setAttribute('aria-label', 'Show password');
        }
    });

    // Form submit handler
    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        performLogin();
    });

    // Clear field errors on input
    usernameInput.addEventListener('input', function() {
        hideFieldError(usernameError);
        usernameInput.classList.remove('error');
    });

    passwordInput.addEventListener('input', function() {
        hideFieldError(passwordError);
        passwordInput.classList.remove('error');
    });

    // Handle Enter key in password field
    passwordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            performLogin();
        }
    });

    // Focus username input on load
    usernameInput.focus();

    // Check for messages in URL (e.g., from password reset)
    const urlParams = new URLSearchParams(window.location.search);
    const message = urlParams.get('message');
    if (message) {
        showSuccess(decodeURIComponent(message));
        // Clean URL
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});


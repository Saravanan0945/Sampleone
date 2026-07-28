// Registration page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Redirect if already authenticated
    if (AuthCheck.isAuthenticated()) {
        AuthCheck.redirectToProfile();
        return;
    }

    // Get form elements
    const registerForm = document.getElementById('registerForm');
    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const firstNameInput = document.getElementById('firstName');
    const lastNameInput = document.getElementById('lastName');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const toggleIcon = document.getElementById('toggleIcon');
    const registerBtn = document.getElementById('registerBtn');
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');

    // Field error elements
    const usernameError = document.getElementById('usernameError');
    const emailError = document.getElementById('emailError');
    const firstNameError = document.getElementById('firstNameError');
    const lastNameError = document.getElementById('lastNameError');
    const passwordError = document.getElementById('passwordError');

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
    }

    /**
     * Show field error
     * @param {HTMLElement} errorElement - Error element
     * @param {HTMLElement} inputElement - Input element
     * @param {string} message - Error message
     */
    function showFieldError(errorElement, inputElement, message) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
        inputElement.classList.add('error');
    }

    /**
     * Hide field error
     * @param {HTMLElement} errorElement - Error element
     * @param {HTMLElement} inputElement - Input element
     */
    function hideFieldError(errorElement, inputElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('show');
        inputElement.classList.remove('error');
    }

    /**
     * Hide all messages
     */
    function hideAllMessages() {
        errorMessage.style.display = 'none';
        successMessage.style.display = 'none';
        hideFieldError(usernameError, usernameInput);
        hideFieldError(emailError, emailInput);
        hideFieldError(firstNameError, firstNameInput);
        hideFieldError(lastNameError, lastNameInput);
        hideFieldError(passwordError, passwordInput);
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
        const usernameRegex = /^[a-zA-Z0-9_-]{3,50}$/;
        return usernameRegex.test(username);
    }

    /**
     * Validate password strength
     * @param {string} password - Password to validate
     * @returns {object} Validation result
     */
    function validatePassword(password) {
        const errors = [];

        if (!password) {
            return { valid: false, errors: ['Password is required'] };
        }

        if (password.length < 8) {
            errors.push('Password must be at least 8 characters');
        }

        if (!/[a-z]/.test(password)) {
            errors.push('Password must contain lowercase letter');
        }

        if (!/[A-Z]/.test(password)) {
            errors.push('Password must contain uppercase letter');
        }

        if (!/[0-9]/.test(password)) {
            errors.push('Password must contain number');
        }

        return {
            valid: errors.length === 0,
            errors: errors
        };
    }

    /**
     * Validate form
     * @returns {boolean} True if valid
     */
    function validateForm() {
        hideAllMessages();
        let isValid = true;

        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const firstName = firstNameInput.value.trim();
        const lastName = lastNameInput.value.trim();
        const password = passwordInput.value;

        // Validate username
        if (!username) {
            showFieldError(usernameError, usernameInput, 'Username is required');
            isValid = false;
        } else if (!isValidUsername(username)) {
            showFieldError(usernameError, usernameInput, 'Username must be 3-50 characters (letters, numbers, _, -)');
            isValid = false;
        }

        // Validate email
        if (!email) {
            showFieldError(emailError, emailInput, 'Email is required');
            isValid = false;
        } else if (!isValidEmail(email)) {
            showFieldError(emailError, emailInput, 'Please enter a valid email address');
            isValid = false;
        }

        // Validate first name
        if (!firstName) {
            showFieldError(firstNameError, firstNameInput, 'First name is required');
            isValid = false;
        } else if (firstName.length < 2) {
            showFieldError(firstNameError, firstNameInput, 'First name must be at least 2 characters');
            isValid = false;
        }

        // Validate last name
        if (!lastName) {
            showFieldError(lastNameError, lastNameInput, 'Last name is required');
            isValid = false;
        } else if (lastName.length < 2) {
            showFieldError(lastNameError, lastNameInput, 'Last name must be at least 2 characters');
            isValid = false;
        }

        // Validate password
        const passwordValidation = validatePassword(password);
        if (!passwordValidation.valid) {
            showFieldError(passwordError, passwordInput, passwordValidation.errors[0]);
            isValid = false;
        }

        return isValid;
    }

    /**
     * Set loading state
     * @param {boolean} loading - Whether to show loading state
     */
    function setLoading(loading) {
        if (loading) {
            registerBtn.disabled = true;
            btnText.style.display = 'none';
            btnSpinner.style.display = 'inline-block';
            [usernameInput, emailInput, firstNameInput, lastNameInput, passwordInput].forEach(input => {
                input.disabled = true;
            });
        } else {
            registerBtn.disabled = false;
            btnText.style.display = 'inline';
            btnSpinner.style.display = 'none';
            [usernameInput, emailInput, firstNameInput, lastNameInput, passwordInput].forEach(input => {
                input.disabled = false;
            });
        }
    }

    /**
     * Submit registration
     */
    async function submitRegistration() {
        if (!validateForm()) {
            return;
        }

        const username = AuthCheck.escapeHTML(usernameInput.value.trim());
        const email = AuthCheck.escapeHTML(emailInput.value.trim());
        const firstName = AuthCheck.escapeHTML(firstNameInput.value.trim());
        const lastName = AuthCheck.escapeHTML(lastNameInput.value.trim());
        const password = passwordInput.value;

        setLoading(true);
        hideAllMessages();

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    email,
                    firstName,
                    lastName,
                    password
                })
            });

            const data = await response.json();

            if (response.ok) {
                showSuccess('Registration successful! Redirecting to login...');
                
                // Clear form
                registerForm.reset();

                // Redirect to login after delay
                setTimeout(() => {
                    window.location.href = '/login.html?message=' + 
                        encodeURIComponent('Registration successful! Please login with your credentials.');
                }, 2000);
            } else {
                const errorMsg = data.message || 'Registration failed. Please try again.';
                showError(errorMsg);
            }
        } catch (error) {
            console.error('Registration error:', error);
            showError('Network error. Please check your connection and try again.');
        } finally {
            setLoading(false);
        }
    }

    // Toggle password visibility
    togglePasswordBtn.addEventListener('click', function() {
        const type = passwordInput.type === 'password' ? 'text' : 'password';
        passwordInput.type = type;
        
        if (type === 'text') {
            toggleIcon.classList.remove('fa-eye');
            toggleIcon.classList.add('fa-eye-slash');
        } else {
            toggleIcon.classList.remove('fa-eye-slash');
            toggleIcon.classList.add('fa-eye');
        }
    });

    // Form submit handler
    registerForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitRegistration();
    });

    // Clear field errors on input
    usernameInput.addEventListener('input', () => hideFieldError(usernameError, usernameInput));
    emailInput.addEventListener('input', () => hideFieldError(emailError, emailInput));
    firstNameInput.addEventListener('input', () => hideFieldError(firstNameError, firstNameInput));
    lastNameInput.addEventListener('input', () => hideFieldError(lastNameError, lastNameInput));
    passwordInput.addEventListener('input', () => hideFieldError(passwordError, passwordInput));

    // Focus username input on load
    usernameInput.focus();
});


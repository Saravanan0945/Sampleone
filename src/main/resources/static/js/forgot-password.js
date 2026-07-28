// Forgot password page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Redirect if already authenticated
    if (AuthCheck.isAuthenticated()) {
        AuthCheck.redirectToProfile();
        return;
    }

    // Get form elements
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    const emailInput = document.getElementById('email');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');
    const emailError = document.getElementById('emailError');

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
     * @param {string} message - Error message
     */
    function showFieldError(message) {
        emailError.textContent = message;
        emailError.classList.add('show');
        emailInput.classList.add('error');
    }

    /**
     * Hide field error
     */
    function hideFieldError() {
        emailError.textContent = '';
        emailError.classList.remove('show');
        emailInput.classList.remove('error');
    }

    /**
     * Hide all messages
     */
    function hideAllMessages() {
        errorMessage.style.display = 'none';
        successMessage.style.display = 'none';
        hideFieldError();
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
     * Validate form
     * @returns {boolean} True if valid
     */
    function validateForm() {
        hideAllMessages();

        const email = emailInput.value.trim();

        if (!email) {
            showFieldError('Email address is required');
            return false;
        }

        if (!isValidEmail(email)) {
            showFieldError('Please enter a valid email address');
            return false;
        }

        return true;
    }

    /**
     * Set loading state
     * @param {boolean} loading - Whether to show loading state
     */
    function setLoading(loading) {
        if (loading) {
            submitBtn.disabled = true;
            btnText.style.display = 'none';
            btnSpinner.style.display = 'inline-block';
            emailInput.disabled = true;
        } else {
            submitBtn.disabled = false;
            btnText.style.display = 'inline';
            btnSpinner.style.display = 'none';
            emailInput.disabled = false;
        }
    }

    /**
     * Submit forgot password request
     */
    async function submitForgotPassword() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        const email = AuthCheck.escapeHTML(emailInput.value.trim());

        // Set loading state
        setLoading(true);
        hideAllMessages();

        try {
            const response = await fetch('/api/auth/forgot-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            });

            const data = await response.json();

            if (response.ok) {
                // Show success message
                showSuccess(
                    'Password reset instructions have been sent to your email address. ' +
                    'Please check your inbox and follow the instructions to reset your password. ' +
                    'The reset link will expire in 1 hour.'
                );

                // Clear form
                emailInput.value = '';

                // Optionally redirect to login after delay
                setTimeout(() => {
                    window.location.href = '/login.html?message=' + 
                        encodeURIComponent('Password reset email sent. Please check your inbox.');
                }, 5000);
            } else {
                // Show error message
                const errorMsg = data.message || 'Failed to send reset email. Please try again.';
                showError(errorMsg);
            }
        } catch (error) {
            console.error('Forgot password error:', error);
            showError('Network error. Please check your connection and try again.');
        } finally {
            setLoading(false);
        }
    }

    // Form submit handler
    forgotPasswordForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitForgotPassword();
    });

    // Clear field error on input
    emailInput.addEventListener('input', function() {
        hideFieldError();
    });

    // Handle Enter key
    emailInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            submitForgotPassword();
        }
    });

    // Focus email input on load
    emailInput.focus();
});


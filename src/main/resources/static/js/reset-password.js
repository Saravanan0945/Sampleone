// Reset password page functionality
document.addEventListener('DOMContentLoaded', function() {
    // Redirect if already authenticated
    if (AuthCheck.isAuthenticated()) {
        AuthCheck.redirectToProfile();
        return;
    }

    // Get reset token from URL
    const urlParams = new URLSearchParams(window.location.search);
    const resetToken = urlParams.get('token');

    // Check if token exists
    if (!resetToken) {
        showError('Invalid or missing reset token. Please request a new password reset link.');
        document.getElementById('resetPasswordForm').style.display = 'none';
        return;
    }

    // Get form elements
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const toggleNewPasswordBtn = document.getElementById('toggleNewPassword');
    const toggleNewIcon = document.getElementById('toggleNewIcon');
    const toggleConfirmPasswordBtn = document.getElementById('toggleConfirmPassword');
    const toggleConfirmIcon = document.getElementById('toggleConfirmIcon');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnSpinner = document.getElementById('btnSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const successMessage = document.getElementById('successMessage');
    const successText = document.getElementById('successText');
    const newPasswordError = document.getElementById('newPasswordError');
    const confirmPasswordError = document.getElementById('confirmPasswordError');
    const passwordStrength = document.getElementById('passwordStrength');
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');

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
        hideFieldError(newPasswordError, newPasswordInput);
        hideFieldError(confirmPasswordError, confirmPasswordInput);
    }

    /**
     * Calculate password strength
     * @param {string} password - Password to check
     * @returns {object} Strength score and text
     */
    function calculatePasswordStrength(password) {
        let score = 0;
        
        if (!password) return { score: 0, text: '', width: 0, className: '' };

        // Length
        if (password.length >= 8) score += 1;
        if (password.length >= 12) score += 1;
        if (password.length >= 16) score += 1;

        // Complexity
        if (/[a-z]/.test(password)) score += 1; // lowercase
        if (/[A-Z]/.test(password)) score += 1; // uppercase
        if (/[0-9]/.test(password)) score += 1; // numbers
        if (/[^a-zA-Z0-9]/.test(password)) score += 1; // special chars

        // Determine strength
        let strength = { score: 0, text: '', width: 0, className: '' };

        if (score <= 2) {
            strength = { score, text: 'Weak', width: 25, className: 'strength-weak' };
        } else if (score <= 4) {
            strength = { score, text: 'Fair', width: 50, className: 'strength-fair' };
        } else if (score <= 6) {
            strength = { score, text: 'Good', width: 75, className: 'strength-good' };
        } else {
            strength = { score, text: 'Strong', width: 100, className: 'strength-strong' };
        }

        return strength;
    }

    /**
     * Update password strength indicator
     * @param {string} password - Password to check
     */
    function updatePasswordStrength(password) {
        if (!password) {
            passwordStrength.style.display = 'none';
            return;
        }

        const strength = calculatePasswordStrength(password);
        
        passwordStrength.style.display = 'block';
        strengthBar.style.width = strength.width + '%';
        strengthBar.className = 'strength-bar-fill ' + strength.className;
        strengthText.textContent = 'Password strength: ' + strength.text;
        strengthText.className = 'strength-text ' + strength.className;
    }

    /**
     * Validate password
     * @param {string} password - Password to validate
     * @returns {object} Validation result
     */
    function validatePassword(password) {
        const errors = [];

        if (!password) {
            return { valid: false, errors: ['Password is required'] };
        }

        if (password.length < 8) {
            errors.push('Password must be at least 8 characters long');
        }

        if (!/[a-z]/.test(password)) {
            errors.push('Password must contain at least one lowercase letter');
        }

        if (!/[A-Z]/.test(password)) {
            errors.push('Password must contain at least one uppercase letter');
        }

        if (!/[0-9]/.test(password)) {
            errors.push('Password must contain at least one number');
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

        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        let isValid = true;

        // Validate new password
        const passwordValidation = validatePassword(newPassword);
        if (!passwordValidation.valid) {
            showFieldError(newPasswordError, newPasswordInput, passwordValidation.errors[0]);
            isValid = false;
        }

        // Validate confirm password
        if (!confirmPassword) {
            showFieldError(confirmPasswordError, confirmPasswordInput, 'Please confirm your password');
            isValid = false;
        } else if (newPassword !== confirmPassword) {
            showFieldError(confirmPasswordError, confirmPasswordInput, 'Passwords do not match');
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
            submitBtn.disabled = true;
            btnText.style.display = 'none';
            btnSpinner.style.display = 'inline-block';
            newPasswordInput.disabled = true;
            confirmPasswordInput.disabled = true;
        } else {
            submitBtn.disabled = false;
            btnText.style.display = 'inline';
            btnSpinner.style.display = 'none';
            newPasswordInput.disabled = false;
            confirmPasswordInput.disabled = false;
        }
    }

    /**
     * Submit password reset
     */
    async function submitPasswordReset() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        const newPassword = newPasswordInput.value;

        // Set loading state
        setLoading(true);
        hideAllMessages();

        try {
            const response = await fetch('/api/auth/reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    token: resetToken,
                    newPassword: newPassword
                })
            });

            const data = await response.json();

            if (response.ok) {
                // Show success message
                showSuccess('Password reset successful! Redirecting to login page...');

                // Clear form
                newPasswordInput.value = '';
                confirmPasswordInput.value = '';
                passwordStrength.style.display = 'none';

                // Redirect to login after delay
                setTimeout(() => {
                    window.location.href = '/login.html?message=' + 
                        encodeURIComponent('Password reset successful. Please login with your new password.');
                }, 2000);
            } else {
                // Show error message
                let errorMsg = data.message || 'Failed to reset password. Please try again.';
                
                if (response.status === 400) {
                    errorMsg = 'Invalid or expired reset token. Please request a new password reset link.';
                }
                
                showError(errorMsg);
            }
        } catch (error) {
            console.error('Reset password error:', error);
            showError('Network error. Please check your connection and try again.');
        } finally {
            setLoading(false);
        }
    }

    // Toggle new password visibility
    toggleNewPasswordBtn.addEventListener('click', function() {
        const type = newPasswordInput.type === 'password' ? 'text' : 'password';
        newPasswordInput.type = type;
        
        if (type === 'text') {
            toggleNewIcon.classList.remove('fa-eye');
            toggleNewIcon.classList.add('fa-eye-slash');
        } else {
            toggleNewIcon.classList.remove('fa-eye-slash');
            toggleNewIcon.classList.add('fa-eye');
        }
    });

    // Toggle confirm password visibility
    toggleConfirmPasswordBtn.addEventListener('click', function() {
        const type = confirmPasswordInput.type === 'password' ? 'text' : 'password';
        confirmPasswordInput.type = type;
        
        if (type === 'text') {
            toggleConfirmIcon.classList.remove('fa-eye');
            toggleConfirmIcon.classList.add('fa-eye-slash');
        } else {
            toggleConfirmIcon.classList.remove('fa-eye-slash');
            toggleConfirmIcon.classList.add('fa-eye');
        }
    });

    // Update password strength on input
    newPasswordInput.addEventListener('input', function() {
        hideFieldError(newPasswordError, newPasswordInput);
        updatePasswordStrength(this.value);
    });

    // Clear confirm password error on input
    confirmPasswordInput.addEventListener('input', function() {
        hideFieldError(confirmPasswordError, confirmPasswordInput);
    });

    // Form submit handler
    resetPasswordForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitPasswordReset();
    });

    // Focus new password input on load
    newPasswordInput.focus();
});


/**
 * Displays a toast notification.
 * @param {string} message - The message to display.
 * @param {string} type - The type of toast (e.g., 'success', 'error').
 * @param {number} duration - How long the toast should be visible in milliseconds.
 */
function showToast(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toast-container');
    if (!container) {
        console.error('Toast container not found!');
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    // Animate in
    setTimeout(() => {
        toast.classList.add('show');
    }, 100); // Short delay to allow CSS transition

    // Animate out and remove
    setTimeout(() => {
        toast.classList.remove('show');
        // Remove the element after the transition is complete
        toast.addEventListener('transitionend', () => toast.remove());
    }, duration);
}

// app.js - RIMBestStay

document.addEventListener('DOMContentLoaded', function() {
    console.log('RIMBestStay application loaded');
    
    // Initialiser les tooltips Bootstrap
    initTooltips();
    
    // Initialiser les popovers
    initPopovers();
    
    // Gérer les messages flash
    handleFlashMessages();
    
    // Améliorer l'expérience utilisateur
    enhanceUX();
});

// Initialiser les tooltips
function initTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    console.log('Tooltips initialisés');
}

// Initialiser les popovers
function initPopovers() {
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}

// Gérer les messages flash
function handleFlashMessages() {
    const alerts = document.querySelectorAll('.alert');
    
    alerts.forEach(alert => {
        // Auto-dismiss après 5 secondes
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
        
        // Bouton de fermeture manuel
        const closeBtn = alert.querySelector('.btn-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', function() {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }
    });
}

// Améliorations UX
function enhanceUX() {
    // Smooth scroll pour les ancres
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
    
    // Confirmation pour les actions critiques
    const criticalActions = document.querySelectorAll('[data-confirm]');
    criticalActions.forEach(action => {
        action.addEventListener('click', function(e) {
            const message = this.getAttribute('data-confirm') || 'Êtes-vous sûr de vouloir effectuer cette action ?';
            if (!confirm(message)) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });
    
    // Ajouter des classes de chargement aux boutons de soumission
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Chargement...';
                submitBtn.disabled = true;
            }
        });
    });
}

// Fonctions utilitaires
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container') || createToastContainer();
    
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        delay: 3000
    });
    
    toast.show();
    
    // Nettoyer après la fermeture
    toastElement.addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'position-fixed bottom-0 end-0 p-3';
    container.style.zIndex = '1055';
    document.body.appendChild(container);
    return container;
}

// API helpers
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    };
    
    const finalOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, finalOptions);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        
        return await response.text();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// Form validation helpers
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePassword(password) {
    return password.length >= 6;
}

function validatePhone(phone) {
    const re = /^[\d\s\-\+\(\)]{10,}$/;
    return re.test(phone);
}

// Export pour utilisation dans la console (débogage)
window.RIMBestStay = {
    showToast,
    apiRequest,
    validateEmail,
    validatePassword,
    validatePhone
};

console.log('RIMBestStay JS module loaded successfully');
/**
 * Módulo de Claude Assistant (Stub básico)
 */

class ClaudeModule {
    constructor() {
        this.modalOpen = false;
        this.configured = false;
        
        console.log('🤖 Módulo Claude Assistant inicializado (modo stub)');
        this.initialize();
    }
    
    /**
     * Inicialización del módulo
     */
    initialize() {
        this.setupEventListeners();
        this.checkStatus();
    }
    
    /**
     * Configurar event listeners
     */
    setupEventListeners() {
        // Placeholder para event listeners
    }
    
    /**
     * Verificar estado de Claude
     */
    async checkStatus() {
        // Simulamos que Claude no está configurado por ahora
        this.configured = false;
        this.updateStatus(false, 'No configurado');
    }
    
    /**
     * Abrir/cerrar modal
     */
    toggleModal() {
        if (this.modalOpen) {
            this.closeModal();
        } else {
            this.openModal();
        }
    }
    
    /**
     * Abrir modal
     */
    openModal() {
        const modal = document.getElementById('claudeModal');
        if (modal) {
            modal.classList.add('active');
            this.modalOpen = true;
            
            // Mostrar mensaje de no configurado
            this.showNotConfiguredMessage();
        }
    }
    
    /**
     * Cerrar modal
     */
    closeModal() {
        const modal = document.getElementById('claudeModal');
        if (modal) {
            modal.classList.remove('active');
            this.modalOpen = false;
        }
    }
    
    /**
     * Actualizar estado
     */
    updateStatus(configured, message) {
        const statusElement = document.getElementById('claudeStatus');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = configured ? 'status-connected' : 'status-disconnected';
        }
    }
    
    /**
     * Mostrar mensaje de no configurado
     */
    showNotConfiguredMessage() {
        const conversation = document.getElementById('claudeConversation');
        if (conversation) {
            conversation.innerHTML = `
                <div class="claude-welcome">
                    <div class="welcome-icon">🤖</div>
                    <h3>Claude Assistant</h3>
                    <p>El asistente IA aún no está configurado en el backend.</p>
                    <p>Esta funcionalidad estará disponible próximamente.</p>
                    <div style="margin-top: 20px; padding: 15px; background: #fff3cd; border-radius: 8px; border: 1px solid #ffeaa7;">
                        <strong>Para desarrolladores:</strong><br>
                        Implementa los endpoints de Claude en tu backend para habilitar esta funcionalidad.
                    </div>
                </div>
            `;
        }
    }
    
    /**
     * Usar sugerencia (placeholder)
     */
    useSuggestion(type) {
        this.showNotification('Funcionalidad de Claude pendiente de implementación', 'info');
    }
}

// Registrar módulo globalmente
window.ClaudeModule = ClaudeModule;

// Funciones globales para compatibilidad
window.toggleClaudeModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.claude) {
        window.Dashboard.modules.claude.toggleModal();
    }
};

window.closeClaudeModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.claude) {
        window.Dashboard.modules.claude.closeModal();
    }
};

window.useSuggestion = function(type) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.claude) {
        window.Dashboard.modules.claude.useSuggestion(type);
    }
};
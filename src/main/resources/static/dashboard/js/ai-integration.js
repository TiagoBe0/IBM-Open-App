// ai-integration.js - Integración frontend con IA local

class LocalAIIntegration {
    constructor() {
        this.apiUrl = 'http://localhost:8080/api/local-ai';
        this.isProcessing = false;
        this.initializeAIFeatures();
    }

    /**
     * Inicializar características de IA en el dashboard
     */
    initializeAIFeatures() {
        this.checkModelStatus();
        this.addAIButtons();
        this.setupEventListeners();
    }

    /**
     * Verificar estado del modelo local
     */
    async checkModelStatus() {
        try {
            const response = await fetch(`${this.apiUrl}/status`);
            const status = await response.json();
            
            this.updateStatusIndicator(status);
            
            if (!status.available) {
                console.warn('Modelo de IA local no disponible:', status.message);
            }
        } catch (error) {
            console.error('Error verificando estado de IA:', error);
            this.updateStatusIndicator({ available: false, message: 'Error de conexión' });
        }
    }

    /**
     * Actualizar indicador visual del estado
     */
    updateStatusIndicator(status) {
        // Agregar indicador en el header si no existe
        if (!document.getElementById('ai-status')) {
            const userInfo = document.querySelector('.user-info');
            if (userInfo) {
                const statusDiv = document.createElement('div');
                statusDiv.id = 'ai-status';
                statusDiv.style.marginLeft = '20px';
                userInfo.appendChild(statusDiv);
            }
        }

        const statusElement = document.getElementById('ai-status');
        if (statusElement) {
            const statusIcon = status.available ? '🤖✅' : '🤖❌';
            const statusText = status.available ? 'IA Local Activa' : 'IA Local Inactiva';
            statusElement.innerHTML = `
                <span style="color: ${status.available ? '#38ef7d' : '#f45c43'};">
                    ${statusIcon} ${statusText}
                </span>
            `;
        }
    }

    /**
     * Agregar botones de IA al dashboard
     */
    addAIButtons() {
        // Botón de asistente IA en cada sección
        const sections = ['arboles', 'ramas', 'hojas'];
        
        sections.forEach(section => {
            const toolbar = document.querySelector(`#${section}-section .toolbar`);
            if (toolbar) {
                const aiButton = document.createElement('button');
                aiButton.className = 'btn btn-primary';
                aiButton.innerHTML = '🤖 Asistente IA';
                aiButton.onclick = () => this.openAIAssistant(section);
                toolbar.appendChild(aiButton);
            }
        });

        // Botón de análisis de datos
        this.addDataAnalysisButton();
    }

    /**
     * Agregar botón de análisis de datos
     */
    addDataAnalysisButton() {
        const statsSection = document.getElementById('stats');
        if (statsSection) {
            const analysisCard = document.createElement('div');
            analysisCard.className = 'stat-card';
            analysisCard.style.cursor = 'pointer';
            analysisCard.onclick = () => this.analyzeData();
            analysisCard.innerHTML = `
                <div class="number">🧠</div>
                <div class="label">Análisis IA</div>
            `;
            statsSection.appendChild(analysisCard);
        }
    }

    /**
     * Configurar event listeners
     */
    setupEventListeners() {
        // Agregar modal de IA si no existe
        this.createAIModal();
        
        // Listener para tecla de acceso rápido (Ctrl+I)
        document.addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.key === 'i') {
                e.preventDefault();
                this.openAIAssistant('general');
            }
        });
    }

    /**
     * Crear modal de IA
     */
    createAIModal() {
        const modalHTML = `
            <div class="modal" id="ai-modal">
                <div class="modal-content" style="max-width: 800px;">
                    <div class="modal-header">
                        <h2>🤖 Asistente IA Local</h2>
                        <button class="close-btn" onclick="closeAIModal()">×</button>
                    </div>
                    <div class="ai-chat-container">
                        <div id="ai-chat-messages" style="height: 300px; overflow-y: auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 15px; margin-bottom: 15px; background: #f8f9fa;">
                            <div class="ai-message">
                                <strong>🤖 Asistente:</strong> ¡Hola! Soy tu asistente de IA local. Puedo ayudarte con:
                                <ul>
                                    <li>Análisis de tus datos</li>
                                    <li>Sugerencias de optimización</li>
                                    <li>Resolución de problemas técnicos</li>
                                    <li>Consultas generales sobre el sistema</li>
                                </ul>
                            </div>
                        </div>
                        <div class="ai-input-container">
                            <div style="display: flex; gap: 10px;">
                                <input type="text" id="ai-message-input" placeholder="Escribe tu pregunta aquí..." style="flex: 1; padding: 10px; border: 2px solid #e0e0e0; border-radius: 8px;">
                                <button class="btn btn-primary" onclick="sendAIMessage()">Enviar</button>
                            </div>
                            <div style="margin-top: 10px;">
                                <button class="btn btn-success" onclick="analyzeCurrentData()">📊 Analizar Datos Actuales</button>
                                <button class="btn btn-warning" onclick="getSystemHelp()">❓ Ayuda del Sistema</button>
                            </div>
                        </div>
                    </div>
                    <div id="ai-loading" style="display: none; text-align: center; padding: 20px;">
                        <div style="animation: spin 1s linear infinite; display: inline-block;">🤖</div>
                        <div>Procesando con IA local...</div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // Agregar estilos de animación
        const style = document.createElement('style');
        style.textContent = `
            @keyframes spin {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
            }
            .ai-message {
                margin-bottom: 15px;
                padding: 10px;
                border-radius: 8px;
                background: white;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .ai-message.user {
                background: #e3f2fd;
                margin-left: 20px;
            }
            .ai-message.assistant {
                background: #f1f8e9;
                margin-right: 20px;
            }
        `;
        document.head.appendChild(style);
    }

    /**
     * Abrir asistente IA
     */
    openAIAssistant(context) {
        const modal = document.getElementById('ai-modal');
        if (modal) {
            modal.classList.add('active');
            
            // Enfocar input
            setTimeout(() => {
                const input = document.getElementById('ai-message-input');
                if (input) input.focus();
            }, 100);
        }
    }

    /**
     * Enviar mensaje a IA
     */
    async sendAIMessage() {
        const input = document.getElementById('ai-message-input');
        const message = input.value.trim();
        
        if (!message || this.isProcessing) return;

        this.isProcessing = true;
        this.showLoading(true);
        
        // Agregar mensaje del usuario al chat
        this.addMessageToChat(message, 'user');
        input.value = '';

        try {
            const response = await fetch(`${this.apiUrl}/ask`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    mensaje: message,
                    usuarioNombre: USUARIO_NOMBRE,
                    tipo: 'general'
                })
            });

            const result = await response.json();
            
            if (result.success) {
                this.addMessageToChat(result.content, 'assistant');
                this.showTokenUsage(result.totalTokens);
            } else {
                this.addMessageToChat(`Error: ${result.error}`, 'error');
            }

        } catch (error) {
            console.error('Error enviando mensaje:', error);
            this.addMessageToChat('Error de conexión con la IA local', 'error');
        }

        this.showLoading(false);
        this.isProcessing = false;
    }

    /**
     * Analizar datos actuales
     */
    async analyzeCurrentData() {
        if (this.isProcessing) return;

        this.isProcessing = true;
        this.showLoading(true);

        try {
            // Recopilar estadísticas actuales
            const stats = {
                usuarios: parseInt(document.getElementById('totalUsuarios').textContent) || 0,
                arboles: parseInt(document.getElementById('totalArboles').textContent) || 0,
                ramas: parseInt(document.getElementById('totalRamas').textContent) || 0,
                hojas: parseInt(document.getElementById('totalHojas').textContent) || 0
            };

            const analysisPrompt = `
                Analiza los siguientes datos de mi sistema:
                - Total de usuarios: ${stats.usuarios}
                - Total de árboles: ${stats.arboles}
                - Total de ramas: ${stats.ramas}
                - Total de hojas: ${stats.hojas}
                
                Proporciona un análisis detallado con insights y recomendaciones.
            `;

            const response = await fetch(`${this.apiUrl}/ask`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    mensaje: analysisPrompt,
                    usuarioNombre: USUARIO_NOMBRE,
                    tipo: 'analysis'
                })
            });

            const result = await response.json();
            
            if (result.success) {
                this.addMessageToChat('📊 Análisis de datos completado:', 'assistant');
                this.addMessageToChat(result.content, 'assistant');
            } else {
                this.addMessageToChat(`Error en análisis: ${result.error}`, 'error');
            }

        } catch (error) {
            console.error('Error analizando datos:', error);
            this.addMessageToChat('Error realizando análisis de datos', 'error');
        }

        this.showLoading(false);
        this.isProcessing = false;
    }

    /**
     * Obtener ayuda del sistema
     */
    async getSystemHelp() {
        if (this.isProcessing) return;

        this.isProcessing = true;
        this.showLoading(true);

        try {
            const helpPrompt = `
                Explica cómo funciona este sistema de gestión jerárquica de Árboles/Ramas/Hojas.
                Incluye las principales funcionalidades y cómo navegar por el dashboard.
            `;

            const response = await fetch(`${this.apiUrl}/ask`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    mensaje: helpPrompt,
                    usuarioNombre: USUARIO_NOMBRE,
                    tipo: 'help'
                })
            });

            const result = await response.json();
            
            if (result.success) {
                this.addMessageToChat('❓ Ayuda del sistema:', 'assistant');
                this.addMessageToChat(result.content, 'assistant');
            } else {
                this.addMessageToChat(`Error obteniendo ayuda: ${result.error}`, 'error');
            }

        } catch (error) {
            console.error('Error obteniendo ayuda:', error);
            this.addMessageToChat('Error obteniendo ayuda del sistema', 'error');
        }

        this.showLoading(false);
        this.isProcessing = false;
    }

    /**
     * Agregar mensaje al chat
     */
    addMessageToChat(message, type) {
        const chatContainer = document.getElementById('ai-chat-messages');
        if (!chatContainer) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = `ai-message ${type}`;
        
        const timestamp = new Date().toLocaleTimeString();
        const icon = type === 'user' ? '👤' : type === 'error' ? '❌' : '🤖';
        
        messageDiv.innerHTML = `
            <strong>${icon} ${type === 'user' ? 'Tú' : 'Asistente'}:</strong> ${message}
            <div style="font-size: 0.8em; color: #666; margin-top: 5px;">${timestamp}</div>
        `;

        chatContainer.appendChild(messageDiv);
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    /**
     * Mostrar información de tokens usados
     */
    showTokenUsage(tokens) {
        if (tokens) {
            this.addMessageToChat(`📊 Tokens utilizados: ${tokens}`, 'info');
        }
    }

    /**
     * Mostrar/ocultar loading
     */
    showLoading(show) {
        const loading = document.getElementById('ai-loading');
        const chatContainer = document.querySelector('.ai-chat-container');
        
        if (loading && chatContainer) {
            loading.style.display = show ? 'block' : 'none';
            chatContainer.style.display = show ? 'none' : 'block';
        }
    }

    /**
     * Analizar código específico
     */
    async analyzeCode(code, language = 'javascript') {
        try {
            const response = await fetch(`${this.apiUrl}/analyze-code`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    code: code,
                    language: language,
                    focus: 'optimización y mejores prácticas'
                })
            });

            const result = await response.json();
            return result;

        } catch (error) {
            console.error('Error analizando código:', error);
            return { success: false, error: error.message };
        }
    }

    /**
     * Obtener sugerencias de código
     */
    async getCodeSuggestions(description, language = 'javascript', context = '') {
        try {
            const response = await fetch(`${this.apiUrl}/code-suggestions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    description: description,
                    language: language,
                    context: context
                })
            });

            const result = await response.json();
            return result;

        } catch (error) {
            console.error('Error obteniendo sugerencias:', error);
            return { success: false, error: error.message };
        }
    }
}

// Funciones globales para el modal
function closeAIModal() {
    const modal = document.getElementById('ai-modal');
    if (modal) {
        modal.classList.remove('active');
    }
}

function sendAIMessage() {
    if (window.aiIntegration) {
        window.aiIntegration.sendAIMessage();
    }
}

function analyzeCurrentData() {
    if (window.aiIntegration) {
        window.aiIntegration.analyzeCurrentData();
    }
}

function getSystemHelp() {
    if (window.aiIntegration) {
        window.aiIntegration.getSystemHelp();
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    window.aiIntegration = new LocalAIIntegration();
    
    // Agregar listener para Enter en el input
    setTimeout(() => {
        const input = document.getElementById('ai-message-input');
        if (input) {
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    sendAIMessage();
                }
            });
        }
    }, 1000);
});

// Exportar para uso en otros módulos
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LocalAIIntegration;
}
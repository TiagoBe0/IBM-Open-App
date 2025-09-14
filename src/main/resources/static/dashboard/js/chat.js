/**
 * Módulo de Chat en Tiempo Real
 */

class ChatModule {
    constructor() {
        this.stompClient = null;
        this.isConnected = false;
        this.modalOpen = false;
        this.unreadMessages = 0;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 5000;
        
        this.currentUser = {
            id: window.APP_CONFIG.USUARIO_ID,
            nombre: window.APP_CONFIG.USUARIO_NOMBRE,
            username: window.APP_CONFIG.USUARIO_USERNAME
        };
        
        this.elements = {};
        
        console.log('💬 Módulo Chat inicializado para usuario:', this.currentUser.nombre);
        this.initialize();
    }
    
    /**
     * Inicialización del módulo
     */
    initialize() {
        this.initializeElements();
        this.setupEventListeners();
    }
    
    /**
     * Inicializar elementos del DOM
     */
    initializeElements() {
        this.elements = {
            modal: document.getElementById('chatModal'),
            messages: document.getElementById('chatMessagesModal'),
            messageInput: document.getElementById('chatMessageInput'),
            sendButton: document.getElementById('chatSendButton'),
            emojiButton: document.getElementById('chatEmojiButton'),
            emojiPicker: document.getElementById('chatEmojiPicker'),
            connectionStatus: document.getElementById('chatConnectionStatus'),
            statusText: document.getElementById('chatStatusText'),
            usersOnline: document.getElementById('chatUsersOnline'),
            typingIndicator: document.getElementById('chatTypingIndicator'),
            unreadBadge: document.getElementById('unread-count'),
            toggle: document.getElementById('chat-toggle')
        };
    }
    
    /**
     * Configurar event listeners
     */
    setupEventListeners() {
        // Input de mensaje
        if (this.elements.messageInput) {
            this.elements.messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
            
            // Indicador de escritura
            let typingTimer;
            this.elements.messageInput.addEventListener('input', () => {
                if (this.isConnected && this.stompClient) {
                    this.sendTypingIndicator(true);
                    
                    clearTimeout(typingTimer);
                    typingTimer = setTimeout(() => {
                        this.sendTypingIndicator(false);
                    }, 1000);
                }
            });
        }
        
        // Botón enviar
        if (this.elements.sendButton) {
            this.elements.sendButton.addEventListener('click', () => this.sendMessage());
        }
        
        // Botón emoji
        if (this.elements.emojiButton) {
            this.elements.emojiButton.addEventListener('click', () => {
                this.toggleEmojiPicker();
            });
        }
        
        // Cerrar emoji picker al hacer click fuera
        document.addEventListener('click', (e) => {
            if (this.elements.emojiPicker && 
                !this.elements.emojiPicker.contains(e.target) && 
                !this.elements.emojiButton.contains(e.target)) {
                this.elements.emojiPicker.classList.remove('show');
            }
        });
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
        if (!this.elements.modal) return;
        
        this.elements.modal.classList.add('active');
        this.modalOpen = true;
        
        // Marcar mensajes como leídos
        this.unreadMessages = 0;
        this.updateUnreadBadge();
        
        // Conectar si no está conectado
        if (!this.isConnected) {
            this.connect();
        }
        
        // Cargar historial
        this.loadChatHistory();
        
        // Focus en input
        if (this.elements.messageInput) {
            setTimeout(() => this.elements.messageInput.focus(), 100);
        }
    }
    
    /**
     * Cerrar modal
     */
    closeModal() {
        if (this.elements.modal) {
            this.elements.modal.classList.remove('active');
        }
        this.modalOpen = false;
    }
    
    /**
     * Conectar al WebSocket
     */
    connect() {
        if (this.isConnected) return;
        
        try {
            console.log('🔌 Conectando al chat WebSocket...');
            
            const socket = new SockJS('/ws-chat');
            this.stompClient = Stomp.over(socket);
            
            // Configurar debug
            this.stompClient.debug = (str) => {
                console.log('WebSocket Debug:', str);
            };
            
            this.stompClient.connect({}, 
                (frame) => this.onConnected(frame),
                (error) => this.onError(error)
            );
            
        } catch (error) {
            console.error('Error iniciando conexión WebSocket:', error);
            this.onError(error);
        }
    }
    
    /**
     * Callback de conexión exitosa
     */
    onConnected(frame) {
        console.log('✅ Conectado al chat:', frame);
        this.isConnected = true;
        this.reconnectAttempts = 0;
        this.updateConnectionStatus(true);
        
        try {
            // Suscribirse al canal público
            this.stompClient.subscribe('/topic/public', (message) => {
                this.onMessageReceived(JSON.parse(message.body));
            });
            
            // Suscribirse a notificaciones de typing
            this.stompClient.subscribe('/topic/typing', (message) => {
                this.onTypingReceived(JSON.parse(message.body));
            });
            
            // Notificar que el usuario se unió
            this.sendJoinMessage();
            
            // Habilitar controles
            this.enableChatControls();
            
        } catch (error) {
            console.error('Error configurando suscripciones:', error);
        }
    }
    
    /**
     * Callback de error
     */
    onError(error) {
        console.error('❌ Error de conexión WebSocket:', error);
        this.isConnected = false;
        this.updateConnectionStatus(false);
        
        // Intentar reconexión
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Reintentando conexión (${this.reconnectAttempts}/${this.maxReconnectAttempts}) en ${this.reconnectDelay}ms`);
            
            setTimeout(() => {
                if (!this.isConnected) {
                    this.connect();
                }
            }, this.reconnectDelay);
            
            // Incrementar delay exponencialmente
            this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30000);
        } else {
            this.showSystemMessage('Error de conexión. Por favor, recarga la página.');
        }
    }
    
    /**
     * Enviar mensaje de unión
     */
    sendJoinMessage() {
        if (!this.stompClient || !this.isConnected) return;
        
        const joinMessage = {
            usuarioId: this.currentUser.id,
            usuarioNombre: this.currentUser.nombre,
            tipo: 'JOIN',
            salaChat: 'general'
        };
        
        try {
            this.stompClient.send('/app/chat.addUser', {}, JSON.stringify(joinMessage));
        } catch (error) {
            console.error('Error enviando mensaje de unión:', error);
        }
    }
    
    /**
     * Enviar mensaje
     */
    sendMessage() {
        if (!this.elements.messageInput || !this.isConnected) return;
        
        const messageContent = this.elements.messageInput.value.trim();
        if (!messageContent) return;
        
        const chatMessage = {
            usuarioId: this.currentUser.id,
            usuarioNombre: this.currentUser.nombre,
            contenido: messageContent,
            tipo: 'CHAT',
            salaChat: 'general'
        };
        
        try {
            this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage));
            this.elements.messageInput.value = '';
            this.sendTypingIndicator(false);
        } catch (error) {
            console.error('Error enviando mensaje:', error);
            this.showError('Error al enviar mensaje');
        }
    }
    
    /**
     * Enviar indicador de escritura
     */
    sendTypingIndicator(isTyping) {
        if (!this.stompClient || !this.isConnected) return;
        
        const typingMessage = {
            usuarioId: this.currentUser.id,
            usuarioNombre: this.currentUser.nombre,
            isTyping: isTyping,
            salaChat: 'general'
        };
        
        try {
            this.stompClient.send('/app/chat.typing', {}, JSON.stringify(typingMessage));
        } catch (error) {
            console.warn('Error enviando indicador de escritura:', error);
        }
    }
    
    /**
     * Manejar mensaje recibido
     */
    onMessageReceived(message) {
        this.displayMessage(message);
        
        // Mostrar notificación si el modal está cerrado
        if (!this.modalOpen && !this.isOwnMessage(message) && !this.isSystemMessage(message)) {
            this.showChatNotification(message);
        }
    }
    
    /**
     * Manejar indicador de escritura recibido
     */
    onTypingReceived(typingData) {
        if (this.isOwnMessage(typingData)) return;
        
        if (typingData.isTyping) {
            this.showTypingIndicator(typingData.usuarioNombre);
        } else {
            this.hideTypingIndicator();
        }
    }
    
    /**
     * Mostrar mensaje en el chat
     */
    displayMessage(message) {
        if (!this.elements.messages) return;
        
        const messageElement = document.createElement('div');
        const isOwnMessage = this.isOwnMessage(message);
        const isSystemMessage = this.isSystemMessage(message);
        
        if (isSystemMessage) {
            messageElement.className = 'message system';
            messageElement.innerHTML = `
                <div class="message-content">${this.escapeHtml(message.contenido)}</div>
                <div class="message-time">${this.formatTime(message.fechaEnvio)}</div>
            `;
        } else {
            messageElement.className = `message ${isOwnMessage ? 'own' : 'other'}`;
            messageElement.innerHTML = `
                ${!isOwnMessage ? `<div class="message-header">${this.escapeHtml(message.usuarioNombre)}</div>` : ''}
                <div class="message-content">${this.escapeHtml(message.contenido)}</div>
                <div class="message-time">${this.formatTime(message.fechaEnvio)}</div>
            `;
        }
        
        this.elements.messages.appendChild(messageElement);
        this.scrollToBottom();
        
        // Incrementar contador de no leídos si el modal está cerrado
        if (!this.modalOpen && !isOwnMessage && !isSystemMessage) {
            this.unreadMessages++;
            this.updateUnreadBadge();
        }
    }
    
    /**
     * Mostrar mensaje del sistema
     */
    showSystemMessage(content) {
        this.displayMessage({
            contenido: content,
            tipo: 'SYSTEM',
            fechaEnvio: new Date().toISOString(),
            usuarioId: null,
            usuarioNombre: 'Sistema'
        });
    }
    
    /**
     * Mostrar indicador de escritura
     */
    showTypingIndicator(userName) {
        if (!this.elements.typingIndicator) return;
        
        this.elements.typingIndicator.innerHTML = `
            <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
            <span style="margin-left: 10px;">${this.escapeHtml(userName)} está escribiendo...</span>
        `;
        this.elements.typingIndicator.style.display = 'block';
        this.scrollToBottom();
    }
    
    /**
     * Ocultar indicador de escritura
     */
    hideTypingIndicator() {
        if (this.elements.typingIndicator) {
            this.elements.typingIndicator.style.display = 'none';
        }
    }
    
    /**
     * Cargar historial del chat
     */
    async loadChatHistory() {
        if (!this.elements.messages) return;
        
        try {
            const response = await fetch('/api/chat/messages/general?limite=20');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const messages = await response.json();
            
            // Limpiar mensajes existentes (excepto mensajes del sistema)
            const systemMessages = this.elements.messages.querySelectorAll('.message.system');
            this.elements.messages.innerHTML = '';
            
            // Reestablecer mensajes del sistema
            systemMessages.forEach(msg => this.elements.messages.appendChild(msg));
            
            // Mostrar historial
            messages.reverse().forEach(message => {
                this.displayMessage(message);
            });
            
        } catch (error) {
            console.error('Error cargando historial del chat:', error);
            this.showSystemMessage('Error al cargar el historial del chat');
        }
    }
    
    /**
     * Actualizar estado de conexión
     */
    updateConnectionStatus(connected) {
        if (this.elements.connectionStatus && this.elements.statusText) {
            if (connected) {
                this.elements.connectionStatus.classList.add('connected');
                this.elements.statusText.textContent = 'Conectado';
            } else {
                this.elements.connectionStatus.classList.remove('connected');
                this.elements.statusText.textContent = 'Desconectado';
            }
        }
    }
    
    /**
     * Habilitar controles del chat
     */
    enableChatControls() {
        if (this.elements.messageInput && this.elements.sendButton && this.elements.emojiButton) {
            this.elements.messageInput.disabled = false;
            this.elements.sendButton.disabled = false;
            this.elements.emojiButton.disabled = false;
            this.elements.messageInput.placeholder = 'Escribe tu mensaje...';
        }
    }
    
    /**
     * Actualizar badge de mensajes no leídos
     */
    updateUnreadBadge() {
        if (this.elements.unreadBadge) {
            if (this.unreadMessages > 0) {
                this.elements.unreadBadge.textContent = this.unreadMessages > 99 ? '99+' : this.unreadMessages;
                this.elements.unreadBadge.style.display = 'flex';
            } else {
                this.elements.unreadBadge.style.display = 'none';
            }
        }
    }
    
    /**
     * Mostrar notificación de mensaje nuevo
     */
    showChatNotification(message) {
        const notification = document.createElement('div');
        notification.className = 'chat-notification';
        
        notification.innerHTML = `
            <strong>${this.escapeHtml(message.usuarioNombre)}</strong><br>
            ${this.escapeHtml(this.truncateText(message.contenido, 50))}
        `;
        
        notification.onclick = () => {
            this.openModal();
            document.body.removeChild(notification);
        };
        
        document.body.appendChild(notification);
        
        // Remover después de 5 segundos
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
    }
    
    /**
     * Toggle emoji picker
     */
    toggleEmojiPicker() {
        if (this.elements.emojiPicker) {
            this.elements.emojiPicker.classList.toggle('show');
        }
    }
    
    /**
     * Agregar emoji al mensaje
     */
    addEmoji(emoji) {
        if (this.elements.messageInput) {
            const cursorPos = this.elements.messageInput.selectionStart;
            const textBefore = this.elements.messageInput.value.substring(0, cursorPos);
            const textAfter = this.elements.messageInput.value.substring(cursorPos);
            
            this.elements.messageInput.value = textBefore + emoji + textAfter;
            this.elements.messageInput.focus();
            
            // Posicionar cursor después del emoji
            const newPos = cursorPos + emoji.length;
            this.elements.messageInput.setSelectionRange(newPos, newPos);
            
            this.elements.emojiPicker.classList.remove('show');
        }
    }
    
    /**
     * Hacer scroll al final
     */
    scrollToBottom() {
        if (this.elements.messages) {
            this.elements.messages.scrollTop = this.elements.messages.scrollHeight;
        }
    }
    
    /**
     * Verificar si el mensaje es propio
     */
    isOwnMessage(message) {
        return message.usuarioId === this.currentUser.id;
    }
    
    /**
     * Verificar si es mensaje del sistema
     */
    isSystemMessage(message) {
        return message.tipo === 'SYSTEM' || message.tipo === 'JOIN' || message.tipo === 'LEAVE';
    }
    
    /**
     * Formatear tiempo
     */
    formatTime(dateString) {
        if (!dateString) return '';
        
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        
        if (diffMins < 1) return 'ahora';
        if (diffMins < 60) return `hace ${diffMins}m`;
        if (diffMins < 1440) return `hace ${Math.floor(diffMins / 60)}h`;
        
        return date.toLocaleString('es-ES', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    /**
     * Escapar HTML
     */
    escapeHtml(text) {
        if (window.escapeHtml) {
            return window.escapeHtml(text);
        }
        
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    /**
     * Truncar texto
     */
    truncateText(text, maxLength) {
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }
    
    /**
     * Mostrar error
     */
    showError(message) {
        if (window.showNotification) {
            window.showNotification(message, 'error');
        } else {
            console.error(message);
        }
    }
    
    /**
     * Funciones del ciclo de vida
     */
    onPageHidden() {
        // Pausar indicadores de escritura
        this.sendTypingIndicator(false);
    }
    
    onPageVisible() {
        // Verificar conexión
        if (!this.isConnected && this.modalOpen) {
            this.connect();
        }
    }
    
    /**
     * Limpiar recursos
     */
    cleanup() {
        if (this.stompClient && this.isConnected) {
            try {
                // Enviar mensaje de salida
                const leaveMessage = {
                    usuarioId: this.currentUser.id,
                    usuarioNombre: this.currentUser.nombre,
                    tipo: 'LEAVE',
                    salaChat: 'general'
                };
                
                this.stompClient.send('/app/chat.removeUser', {}, JSON.stringify(leaveMessage));
                this.stompClient.disconnect();
            } catch (error) {
                console.warn('Error al desconectar chat:', error);
            }
        }
        
        this.isConnected = false;
        this.stompClient = null;
    }
}

// Registrar módulo globalmente
window.ChatModule = ChatModule;

// Funciones globales para compatibilidad
window.toggleChatModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.chat) {
        window.Dashboard.modules.chat.toggleModal();
    }
};

window.closeChatModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.chat) {
        window.Dashboard.modules.chat.closeModal();
    }
};

window.addChatEmoji = function(emoji) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.chat) {
        window.Dashboard.modules.chat.addEmoji(emoji);
    }
};
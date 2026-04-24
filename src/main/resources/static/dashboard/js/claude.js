/**
 * Módulo de Sistema de Mensajería
 */

class MensajesModule {
    constructor() {
        this.data = {
            conversaciones: [],
            mensajesActuales: [],
            amigoActual: null,
            estadisticas: {},
            paginaActual: 0
        };
        
        this.activeSubTab = 'conversaciones';
        
        console.log('💬 Módulo de Mensajes inicializado');
        this.initialize();
    }
    
    /**
     * Inicialización del módulo
     */
    initialize() {
        this.setupEventListeners();
    }
    
    /**
     * Configurar event listeners
     */
    setupEventListeners() {
        // Formulario de envío de mensaje
        const enviarForm = document.getElementById('enviarMensajeForm');
        if (enviarForm) {
            enviarForm.addEventListener('submit', (e) => this.handleEnviarMensaje(e));
        }
        
        // Auto-resize del textarea
        const textarea = document.getElementById('messageTextarea');
        if (textarea) {
            textarea.addEventListener('input', () => {
                this.autoResizeTextarea(textarea);
                this.updateCharCounter();
            });
        }
        
        // Búsqueda de mensajes con debounce
        const searchInput = document.getElementById('searchMensajes');
        if (searchInput) {
            let timeout;
            searchInput.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.buscarMensajes(), 300);
            });
        }
    }
    
    /**
     * Activar módulo cuando se selecciona la pestaña
     */
    onActivate() {
        this.switchMensajesTab('conversaciones');
        this.loadConversaciones();
        this.loadEstadisticasMensajes();
    }
    
    /**
     * Cambiar sub-pestaña de mensajes
     */
    switchMensajesTab(tabName) {
        // Actualizar UI
        document.querySelectorAll('#mensajes-section .tabs .tab').forEach(tab => {
            tab.classList.remove('active');
        });
        
        document.querySelectorAll('.mensajes-subsection').forEach(section => {
            section.classList.remove('active');
        });
        
        // Activar nueva pestaña
        const activeTab = document.querySelector(`#mensajes-section .tabs .tab[onclick*="${tabName}"]`);
        const activeSection = document.getElementById(`${tabName}-mensajes`);
        
        if (activeTab) activeTab.classList.add('active');
        if (activeSection) activeSection.classList.add('active');
        
        this.activeSubTab = tabName;
        
        // Cargar datos específicos
        switch(tabName) {
            case 'conversaciones':
                this.loadConversaciones();
                break;
            case 'estadisticas':
                this.loadEstadisticasMensajes();
                break;
        }
    }
    
    /**
     * Cargar lista de conversaciones
     */
    async loadConversaciones() {
        try {
            const response = await this.apiCall(`/mensajes/conversaciones/${window.APP_CONFIG.USUARIO_ID}`);
            this.data.conversaciones = response || [];
            this.renderConversaciones(this.data.conversaciones);
        } catch (error) {
            console.error('Error cargando conversaciones:', error);
            this.renderError('conversacionesLista', 'Error al cargar conversaciones');
        }
    }
    
    /**
     * Renderizar lista de conversaciones
     */
    renderConversaciones(conversaciones) {
        const container = document.getElementById('conversacionesLista');
        if (!container) return;
        
        if (!conversaciones || conversaciones.length === 0) {
            container.innerHTML = '<div class="empty-state">No tienes conversaciones aún. ¡Envía tu primer mensaje a un amigo!</div>';
            return;
        }
        
        let html = '';
        conversaciones.forEach(conversacion => {
            const iniciales = `${conversacion.amigoNombre} ${conversacion.amigoApellido}`.split(' ')
                .map(n => n[0]).join('').toUpperCase();
            const tieneNoLeidos = conversacion.mensajesNoLeidos > 0;
            const ultimoMensaje = conversacion.ultimoMensaje;
            
            html += `
                <div class="conversacion-card ${tieneNoLeidos ? 'no-leidos' : ''}" 
                     onclick="window.Dashboard.modules.mensajes.abrirChat(${conversacion.amigoId}, '${this.escapeHtml(conversacion.amigoNombre)}', '${this.escapeHtml(conversacion.amigoApellido)}')">
                    <div class="amigo-avatar">${iniciales}</div>
                    <div class="conversacion-info">
                        <h4>${this.escapeHtml(conversacion.amigoNombre)} ${this.escapeHtml(conversacion.amigoApellido)}</h4>
                        <p class="ultimo-mensaje">
                            ${ultimoMensaje ? 
                                (ultimoMensaje.esPropio ? 'Tú: ' : '') + this.escapeHtml(ultimoMensaje.contenido) :
                                'No hay mensajes aún'
                            }
                        </p>
                    </div>
                    <div class="conversacion-meta">
                        <span class="fecha-ultimo-mensaje">
                            ${ultimoMensaje ? this.formatDate(ultimoMensaje.fechaEnvio) : ''}
                        </span>
                        ${tieneNoLeidos ? `<span class="badge-no-leidos">${conversacion.mensajesNoLeidos}</span>` : ''}
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Abrir chat con un amigo
     */
    async abrirChat(amigoId, amigoNombre, amigoApellido) {
        this.data.amigoActual = {
            id: amigoId,
            nombre: amigoNombre,
            apellido: amigoApellido
        };
        
        // Actualizar header del chat
        const iniciales = `${amigoNombre} ${amigoApellido}`.split(' ').map(n => n[0]).join('').toUpperCase();
        document.getElementById('chatAmigoAvatar').textContent = iniciales;
        document.getElementById('chatAmigoNombre').textContent = `${amigoNombre} ${amigoApellido}`;
        document.getElementById('chatAmigoEstado').textContent = 'En línea';
        document.getElementById('chatDestinatarioId').value = amigoId;
        
        // Mostrar input de mensaje
        document.getElementById('chatInputContainer').style.display = 'block';
        
        // Cambiar a la pestaña de chat
        this.switchMensajesTab('chat');
        
        // Cargar mensajes
        await this.loadMensajesConversacion(amigoId);
        
        // Marcar mensajes como leídos
        await this.marcarMensajesComoLeidos(amigoId);
    }
    
    /**
     * Cargar mensajes de una conversación
     */
    async loadMensajesConversacion(amigoId, pagina = 0) {
        try {
            document.getElementById('chatMessages').innerHTML = '<div class="loading">Cargando mensajes...</div>';
            
            const response = await this.apiCall(`/mensajes/conversacion/${amigoId}?usuarioId=${window.APP_CONFIG.USUARIO_ID}&pagina=${pagina}&tamaño=20`);
            this.data.mensajesActuales = response || [];
            this.renderMensajes(this.data.mensajesActuales);
            
        } catch (error) {
            console.error('Error cargando mensajes:', error);
            this.renderError('chatMessages', 'Error al cargar mensajes');
        }
    }
    
    /**
     * Renderizar mensajes en el chat
     */
    renderMensajes(mensajes) {
        const container = document.getElementById('chatMessages');
        if (!container) return;
        
        if (!mensajes || mensajes.length === 0) {
            container.innerHTML = '<div class="empty-state">No hay mensajes aún. ¡Escribe el primero!</div>';
            return;
        }
        
        let html = '';
        mensajes.reverse().forEach(mensaje => {
            const esPropio = mensaje.esPropio;
            const tipoClase = esPropio ? 'sent' : 'received';
            const tiempoTranscurrido = this.formatTiempoTranscurrido(mensaje.minutosTranscurridos);
            
            html += `
                <div class="message-bubble ${tipoClase}">
                    <div class="message-content">${this.escapeHtml(mensaje.contenido)}</div>
                    <div class="message-info">
                        <span class="message-time">${tiempoTranscurrido}</span>
                        <div class="message-actions">
                            ${esPropio ? 
                                `<button class="btn-message-action" onclick="window.Dashboard.modules.mensajes.eliminarMensaje(${mensaje.id})" title="Eliminar">🗑️</button>` : 
                                ''
                            }
                            ${mensaje.leido ? '<span class="message-status read" title="Leído">✓✓</span>' : 
                                             '<span class="message-status unread" title="Enviado">✓</span>'}
                        </div>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
        this.scrollToBottom();
    }
    
    /**
     * Manejar envío de mensaje
     */
    async handleEnviarMensaje(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const contenido = formData.get('contenido').trim();
        const destinatarioId = document.getElementById('chatDestinatarioId').value;
        
        if (!contenido || !destinatarioId) {
            this.showNotification('Por favor escribe un mensaje', 'warning');
            return;
        }
        
        try {
            await this.apiCall('/mensajes/enviar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    remitenteId: window.APP_CONFIG.USUARIO_ID,
                    destinatarioId: parseInt(destinatarioId),
                    contenido: contenido,
                    tipoMensaje: 'TEXTO'
                })
            });
            
            // Limpiar formulario
            document.getElementById('messageTextarea').value = '';
            this.updateCharCounter();
            
            // Recargar mensajes
            if (this.data.amigoActual) {
                await this.loadMensajesConversacion(this.data.amigoActual.id);
            }
            
            this.showNotification('Mensaje enviado', 'success');
            
        } catch (error) {
            console.error('Error enviando mensaje:', error);
            this.showError('Error al enviar mensaje');
        }
    }
    
    /**
     * Marcar mensajes como leídos
     */
    async marcarMensajesComoLeidos(remitenteId) {
        try {
            await this.apiCall('/mensajes/marcar-leidos', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    destinatarioId: window.APP_CONFIG.USUARIO_ID,
                    remitenteId: parseInt(remitenteId)
                })
            });
        } catch (error) {
            console.warn('Error marcando mensajes como leídos:', error);
        }
    }
    
    /**
     * Eliminar mensaje
     */
    async eliminarMensaje(mensajeId) {
        if (!confirm('¿Estás seguro de que quieres eliminar este mensaje?')) {
            return;
        }
        
        try {
            await this.apiCall(`/mensajes/${mensajeId}?usuarioId=${window.APP_CONFIG.USUARIO_ID}`, {
                method: 'DELETE'
            });
            
            this.showNotification('Mensaje eliminado', 'info');
            
            // Recargar mensajes
            if (this.data.amigoActual) {
                await this.loadMensajesConversacion(this.data.amigoActual.id);
            }
            
        } catch (error) {
            console.error('Error eliminando mensaje:', error);
            this.showError('Error al eliminar mensaje');
        }
    }
    
    /**
     * Buscar mensajes
     */
    async buscarMensajes() {
        const termino = document.getElementById('searchMensajes')?.value?.trim() || '';
        
        if (termino.length < 2) {
            document.getElementById('mensajesBusqueda').innerHTML = 
                '<div class="empty-state">Ingresa al menos 2 caracteres para buscar</div>';
            return;
        }
        
        try {
            document.getElementById('mensajesBusqueda').innerHTML = '<div class="loading">Buscando mensajes...</div>';
            
            const response = await this.apiCall(`/mensajes/buscar?usuarioId=${window.APP_CONFIG.USUARIO_ID}&q=${encodeURIComponent(termino)}&pagina=0&tamaño=20`);
            const mensajes = response || [];
            this.renderMensajesBusqueda(mensajes);
            
        } catch (error) {
            console.error('Error buscando mensajes:', error);
            this.renderError('mensajesBusqueda', 'Error al buscar mensajes');
        }
    }
    
    /**
     * Renderizar resultados de búsqueda
     */
    renderMensajesBusqueda(mensajes) {
        const container = document.getElementById('mensajesBusqueda');
        if (!container) return;
        
        if (!mensajes || mensajes.length === 0) {
            container.innerHTML = '<div class="empty-state">No se encontraron mensajes</div>';
            return;
        }
        
        let html = '';
        mensajes.forEach(mensaje => {
            const contacto = mensaje.esPropio ? 
                `${mensaje.destinatarioNombre} ${mensaje.destinatarioApellido}` :
                `${mensaje.remitenteNombre} ${mensaje.remitenteApellido}`;
            
            html += `
                <div class="mensaje-busqueda-card">
                    <div class="mensaje-busqueda-header">
                        <h4>${this.escapeHtml(contacto)}</h4>
                        <span class="badge ${mensaje.esPropio ? 'badge-true' : 'badge-false'}">
                            ${mensaje.esPropio ? 'Enviado' : 'Recibido'}
                        </span>
                    </div>
                    <div class="mensaje-busqueda-content">
                        ${this.escapeHtml(mensaje.contenido)}
                    </div>
                    <div class="mensaje-busqueda-meta">
                        <span>${this.formatDate(mensaje.fechaEnvio)}</span>
                        <button class="btn btn-primary btn-sm" onclick="window.Dashboard.modules.mensajes.abrirChatDesde(${mensaje.esPropio ? mensaje.destinatarioId : mensaje.remitenteId})">
                            Ver conversación
                        </button>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Abrir chat desde búsqueda
     */
    async abrirChatDesde(amigoId) {
        try {
            // Buscar en conversaciones existentes
            const amigo = this.data.conversaciones.find(c => c.amigoId === amigoId);
            
            if (amigo) {
                await this.abrirChat(amigo.amigoId, amigo.amigoNombre, amigo.amigoApellido);
            } else {
                this.showNotification('No se pudo abrir la conversación', 'error');
            }
        } catch (error) {
            console.error('Error abriendo conversación:', error);
            this.showError('Error al abrir conversación');
        }
    }
    
    /**
     * Cargar estadísticas de mensajes
     */
    async loadEstadisticasMensajes() {
        try {
            const response = await this.apiCall(`/mensajes/estadisticas/${window.APP_CONFIG.USUARIO_ID}`);
            this.data.estadisticas = response || {};
            
            document.getElementById('statMensajesEnviados').textContent = this.data.estadisticas.totalMensajesEnviados || 0;
            document.getElementById('statMensajesRecibidos').textContent = this.data.estadisticas.totalMensajesRecibidos || 0;
            document.getElementById('statMensajesNoLeidos').textContent = this.data.estadisticas.mensajesNoLeidos || 0;
            document.getElementById('statConversacionesActivas').textContent = this.data.estadisticas.conversacionesActivas || 0;
            
        } catch (error) {
            console.error('Error cargando estadísticas:', error);
            this.showError('Error al cargar estadísticas');
        }
    }
    
    /**
     * Abrir selector de amigo
     */
    async abrirSelectorAmigo() {
        try {
            // Obtener lista de amigos desde el módulo de amigos
            if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.amigos) {
                await window.Dashboard.modules.amigos.loadAmigos();
                const amigos = window.Dashboard.modules.amigos.data.amigos;
                this.renderAmigosDisponibles(amigos);
            } else {
                // Fallback: cargar amigos directamente
                const response = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/amigos`);
                const amigos = response || [];
                this.renderAmigosDisponibles(amigos);
            }
            
            document.getElementById('modalSelectorAmigo').classList.add('active');
            
        } catch (error) {
            console.error('Error cargando amigos:', error);
            this.showError('Error al cargar amigos');
        }
    }
    
    /**
     * Renderizar amigos disponibles para chat
     */
    renderAmigosDisponibles(amigos) {
        const container = document.getElementById('amigosDisponiblesChat');
        if (!container) return;
        
        if (!amigos || amigos.length === 0) {
            container.innerHTML = '<div class="empty-state">No tienes amigos para chatear</div>';
            return;
        }
        
        let html = '';
        amigos.forEach(amistad => {
            const esUsuarioAmigo = amistad.usuarioId === window.APP_CONFIG.USUARIO_ID;
            const nombreAmigo = esUsuarioAmigo ? 
                `${amistad.amigoNombre} ${amistad.amigoApellido}` : 
                `${amistad.usuarioNombre} ${amistad.usuarioApellido}`;
            const amigoId = esUsuarioAmigo ? amistad.amigoId : amistad.usuarioId;
            const iniciales = nombreAmigo.split(' ').map(n => n[0]).join('').toUpperCase();
            
            html += `
                <div class="amigo-selector-card" onclick="window.Dashboard.modules.mensajes.seleccionarAmigoChat(${amigoId}, '${this.escapeHtml(nombreAmigo)}')">
                    <div class="amigo-avatar">${iniciales}</div>
                    <div class="amigo-details">
                        <h4>${this.escapeHtml(nombreAmigo)}</h4>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Seleccionar amigo para chat
     */
    async seleccionarAmigoChat(amigoId, nombreCompleto) {
        const [nombre, apellido] = nombreCompleto.split(' ');
        await this.abrirChat(amigoId, nombre, apellido || '');
        this.closeSelectorAmigoModal();
    }
    
    /**
     * Cerrar modal selector de amigo
     */
    closeSelectorAmigoModal() {
        document.getElementById('modalSelectorAmigo').classList.remove('active');
    }
    
    /**
     * Auto-resize del textarea
     */
    autoResizeTextarea(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 100) + 'px';
    }
    
    /**
     * Actualizar contador de caracteres
     */
    updateCharCounter() {
        const textarea = document.getElementById('messageTextarea');
        const counter = document.getElementById('charCounter');
        if (textarea && counter) {
            counter.textContent = `${textarea.value.length}/2000`;
        }
    }
    
    /**
     * Hacer scroll al final
     */
    scrollToBottom() {
        const chatMessages = document.getElementById('chatMessages');
        if (chatMessages) {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }
    }
    
    /**
     * Formatear tiempo transcurrido
     */
    formatTiempoTranscurrido(minutos) {
        if (minutos < 1) return 'Ahora';
        if (minutos < 60) return `${Math.floor(minutos)}m`;
        if (minutos < 1440) return `${Math.floor(minutos / 60)}h`;
        return `${Math.floor(minutos / 1440)}d`;
    }
    
    /**
     * Utilidades
     */
    async apiCall(endpoint, options = {}) {
        const url = `${window.APP_CONFIG.API_URL}${endpoint}`;
        
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error ${response.status}: ${errorText}`);
            }
            
            if (options.method === 'DELETE') {
                return true;
            }
            
            return await response.json();
            
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    }
    
    renderError(containerId, message) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `<div class="empty-state">${message}</div>`;
        }
    }
    
    showError(message) {
        this.showNotification(message, 'error');
    }
    
    showNotification(message, type = 'info') {
        if (window.showNotification) {
            window.showNotification(message, type);
        } else {
            console.log(`${type.toUpperCase()}: ${message}`);
        }
    }
    
    escapeHtml(text) {
        if (window.escapeHtml) {
            return window.escapeHtml(text);
        }
        
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    formatDate(dateString) {
        if (window.formatDate) {
            return window.formatDate(dateString);
        }
        
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES') + ' ' + 
               date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    }
}

// Registrar módulo globalmente
window.MensajesModule = MensajesModule;

// Funciones globales para compatibilidad
window.switchMensajesTab = function(tabName) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.mensajes) {
        window.Dashboard.modules.mensajes.switchMensajesTab(tabName);
    }
};

window.loadConversaciones = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.mensajes) {
        window.Dashboard.modules.mensajes.loadConversaciones();
    }
};

window.buscarMensajes = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.mensajes) {
        window.Dashboard.modules.mensajes.buscarMensajes();
    }
};

window.abrirSelectorAmigo = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.mensajes) {
        window.Dashboard.modules.mensajes.abrirSelectorAmigo();
    }
};

window.closeSelectorAmigoModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.mensajes) {
        window.Dashboard.modules.mensajes.closeSelectorAmigoModal();
    }
};

window.toggleEmojis = function() {
    // Placeholder para funcionalidad de emojis
    console.log('Toggle emojis - función pendiente de implementar');
};
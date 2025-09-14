/**
 * Módulo de Sistema de Amistades
 */

class AmigosModule {
    constructor() {
        this.data = {
            amigos: [],
            solicitudesRecibidas: [],
            solicitudesEnviadas: [],
            estadisticas: {}
        };
        
        this.activeSubTab = 'lista';
        this.searchCache = new Map();
        
        console.log('👥 Módulo de Amigos inicializado');
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
        // Formulario de solicitud de amistad
        const solicitudForm = document.getElementById('solicitudAmistadForm');
        if (solicitudForm) {
            solicitudForm.addEventListener('submit', (e) => this.handleSolicitudSubmit(e));
        }
        
        // Búsqueda de usuarios con debounce
        const searchInput = document.getElementById('searchUsuarios');
        if (searchInput) {
            let timeout;
            searchInput.addEventListener('input', () => {
                clearTimeout(timeout);
                timeout = setTimeout(() => this.buscarUsuarios(), 300);
            });
        }
    }
    
    /**
     * Activar módulo cuando se selecciona la pestaña
     */
    onActivate() {
        this.switchAmigosTab('lista');
        this.loadAmigos();
        this.loadEstadisticasAmistad();
    }
    
    /**
     * Cambiar sub-pestaña de amigos
     */
    switchAmigosTab(tabName) {
        // Actualizar UI
        document.querySelectorAll('#amigos-section .tabs .tab').forEach(tab => {
            tab.classList.remove('active');
        });
        
        document.querySelectorAll('.amigos-subsection').forEach(section => {
            section.classList.remove('active');
        });
        
        // Activar nueva pestaña
        const activeTab = document.querySelector(`#amigos-section .tabs .tab[onclick*="${tabName}"]`);
        const activeSection = document.getElementById(`${tabName}-amigos`);
        
        if (activeTab) activeTab.classList.add('active');
        if (activeSection) activeSection.classList.add('active');
        
        this.activeSubTab = tabName;
        
        // Cargar datos específicos
        switch(tabName) {
            case 'lista':
                this.loadAmigos();
                break;
            case 'solicitudes':
                this.loadSolicitudes();
                break;
            case 'estadisticas':
                this.loadEstadisticasAmistad();
                break;
        }
    }
    
    /**
     * Cargar lista de amigos
     */
    async loadAmigos() {
        try {
            const response = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/amigos`);
            this.data.amigos = response || [];
            this.renderAmigos(this.data.amigos);
            this.updateAmigosCount();
        } catch (error) {
            console.error('Error cargando amigos:', error);
            this.renderError('amigosLista', 'Error al cargar amigos');
        }
    }
    
    /**
     * Cargar solicitudes de amistad
     */
    async loadSolicitudes() {
        try {
            // Cargar solicitudes recibidas
            const recibidas = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/solicitudes/recibidas`);
            this.data.solicitudesRecibidas = recibidas || [];
            
            // Cargar solicitudes enviadas
            const enviadas = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/solicitudes/enviadas`);
            this.data.solicitudesEnviadas = enviadas || [];
            
            this.renderSolicitudesRecibidas(this.data.solicitudesRecibidas);
            this.renderSolicitudesEnviadas(this.data.solicitudesEnviadas);
            this.updateSolicitudesCount();
            
        } catch (error) {
            console.error('Error cargando solicitudes:', error);
            this.renderError('solicitudesRecibidas', 'Error al cargar solicitudes');
            this.renderError('solicitudesEnviadas', 'Error al cargar solicitudes');
        }
    }
    
    /**
     * Buscar usuarios
     */
    async buscarUsuarios() {
        const termino = document.getElementById('searchUsuarios')?.value?.trim() || '';
        
        if (termino.length < 2) {
            document.getElementById('usuariosBusqueda').innerHTML = 
                '<div class="empty-state">Ingresa al menos 2 caracteres para buscar</div>';
            return;
        }
        
        try {
            document.getElementById('usuariosBusqueda').innerHTML = '<div class="loading">Buscando usuarios...</div>';
            
            const response = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/buscar?q=${encodeURIComponent(termino)}`);
            const usuarios = response || [];
            this.renderUsuariosBusqueda(usuarios);
            
        } catch (error) {
            console.error('Error buscando usuarios:', error);
            this.renderError('usuariosBusqueda', 'Error al buscar usuarios');
        }
    }
    
    /**
     * Renderizar lista de amigos
     */
    renderAmigos(amigos) {
        const container = document.getElementById('amigosLista');
        if (!container) return;
        
        if (!amigos || amigos.length === 0) {
            container.innerHTML = '<div class="empty-state">No tienes amigos agregados aún</div>';
            return;
        }
        
        let html = '';
        amigos.forEach(amistad => {
            const esUsuarioAmigo = amistad.usuarioId === window.APP_CONFIG.USUARIO_ID;
            const nombreAmigo = esUsuarioAmigo ? 
                `${amistad.amigoNombre} ${amistad.amigoApellido}` : 
                `${amistad.usuarioNombre} ${amistad.usuarioApellido}`;
            const usernameAmigo = esUsuarioAmigo ? amistad.amigoUsername : amistad.usuarioUsername;
            const emailAmigo = esUsuarioAmigo ? amistad.amigoEmail : amistad.usuarioEmail;
            const iniciales = nombreAmigo.split(' ').map(n => n[0]).join('').toUpperCase();
            const amigoId = esUsuarioAmigo ? amistad.amigoId : amistad.usuarioId;
            
            html += `
                <div class="amigo-card">
                    <div class="amigo-info">
                        <div class="amigo-avatar">${iniciales}</div>
                        <div class="amigo-details">
                            <h4>${this.escapeHtml(nombreAmigo)}</h4>
                            <p>@${this.escapeHtml(usernameAmigo)}</p>
                            ${emailAmigo ? `<p>${this.escapeHtml(emailAmigo)}</p>` : ''}
                            <p class="solicitud-fecha">Amigos desde: ${this.formatDate(amistad.fechaAceptacion)}</p>
                        </div>
                    </div>
                    <div class="actions">
                        <button class="btn btn-danger" onclick="window.Dashboard.modules.amigos.eliminarAmistad(${amigoId}, '${this.escapeHtml(nombreAmigo)}')">
                            🗑️ Eliminar
                        </button>
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Renderizar solicitudes recibidas
     */
    renderSolicitudesRecibidas(solicitudes) {
        const container = document.getElementById('solicitudesRecibidas');
        if (!container) return;
        
        if (!solicitudes || solicitudes.length === 0) {
            container.innerHTML = '<div class="empty-state">No tienes solicitudes pendientes</div>';
            return;
        }
        
        let html = '';
        solicitudes.forEach(solicitud => {
            html += `
                <div class="solicitud-card">
                    <div class="solicitud-header">
                        <h4>${this.escapeHtml(solicitud.usuarioNombre)} ${this.escapeHtml(solicitud.usuarioApellido)} (@${this.escapeHtml(solicitud.usuarioUsername)})</h4>
                        <div class="actions">
                            <button class="btn btn-success" onclick="window.Dashboard.modules.amigos.aceptarSolicitud(${solicitud.id})">✅ Aceptar</button>
                            <button class="btn btn-danger" onclick="window.Dashboard.modules.amigos.rechazarSolicitud(${solicitud.id})">❌ Rechazar</button>
                        </div>
                    </div>
                    ${solicitud.mensajeSolicitud ? `<div class="solicitud-mensaje">"${this.escapeHtml(solicitud.mensajeSolicitud)}"</div>` : ''}
                    <div class="solicitud-fecha">Enviada: ${this.formatDate(solicitud.fechaSolicitud)}</div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Renderizar solicitudes enviadas
     */
    renderSolicitudesEnviadas(solicitudes) {
        const container = document.getElementById('solicitudesEnviadas');
        if (!container) return;
        
        if (!solicitudes || solicitudes.length === 0) {
            container.innerHTML = '<div class="empty-state">No has enviado solicitudes</div>';
            return;
        }
        
        let html = '';
        solicitudes.forEach(solicitud => {
            html += `
                <div class="solicitud-card">
                    <div class="solicitud-header">
                        <h4>${this.escapeHtml(solicitud.amigoNombre)} ${this.escapeHtml(solicitud.amigoApellido)} (@${this.escapeHtml(solicitud.amigoUsername)})</h4>
                        <span class="estado-badge estado-pendiente">Pendiente</span>
                    </div>
                    ${solicitud.mensajeSolicitud ? `<div class="solicitud-mensaje">"${this.escapeHtml(solicitud.mensajeSolicitud)}"</div>` : ''}
                    <div class="solicitud-fecha">Enviada: ${this.formatDate(solicitud.fechaSolicitud)}</div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Renderizar resultados de búsqueda
     */
    renderUsuariosBusqueda(usuarios) {
        const container = document.getElementById('usuariosBusqueda');
        if (!container) return;
        
        if (!usuarios || usuarios.length === 0) {
            container.innerHTML = '<div class="empty-state">No se encontraron usuarios</div>';
            return;
        }
        
        let html = '';
        usuarios.forEach(usuario => {
            const iniciales = `${usuario.nombre} ${usuario.apellido}`.split(' ').map(n => n[0]).join('').toUpperCase();
            
            let accionHtml = '';
            switch(usuario.estadoAmistad) {
                case null:
                case 'NINGUNA':
                    accionHtml = `<button class="btn btn-success" onclick="window.Dashboard.modules.amigos.abrirSolicitudModal(${usuario.id}, '${this.escapeHtml(usuario.nombre)} ${this.escapeHtml(usuario.apellido)}')">➕ Agregar</button>`;
                    break;
                case 'PENDIENTE':
                    if (usuario.solicitudEnviada) {
                        accionHtml = `<span class="estado-badge estado-pendiente">Solicitud enviada</span>`;
                    } else {
                        accionHtml = `<span class="estado-badge estado-pendiente">Te envió solicitud</span>`;
                    }
                    break;
                case 'ACEPTADA':
                    accionHtml = `<span class="estado-badge estado-aceptada">Ya son amigos</span>`;
                    break;
                case 'RECHAZADA':
                    accionHtml = `<button class="btn btn-warning" onclick="window.Dashboard.modules.amigos.abrirSolicitudModal(${usuario.id}, '${this.escapeHtml(usuario.nombre)} ${this.escapeHtml(usuario.apellido)}')">🔄 Reintentar</button>`;
                    break;
            }
            
            html += `
                <div class="usuario-busqueda-card">
                    <div class="usuario-busqueda-info">
                        <div class="amigo-avatar">${iniciales}</div>
                        <div class="amigo-details">
                            <h4>${this.escapeHtml(usuario.nombre)} ${this.escapeHtml(usuario.apellido)}</h4>
                            <p>@${this.escapeHtml(usuario.username)}</p>
                            <p>${this.escapeHtml(usuario.email)}</p>
                        </div>
                    </div>
                    <div class="actions">
                        ${accionHtml}
                    </div>
                </div>
            `;
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Abrir modal para enviar solicitud
     */
    abrirSolicitudModal(amigoId, nombreAmigo) {
        document.getElementById('solicitudAmigoId').value = amigoId;
        document.getElementById('solicitudAmigoNombre').value = nombreAmigo;
        document.getElementById('modalSolicitudAmistad').classList.add('active');
    }
    
    /**
     * Cerrar modal de solicitud
     */
    closeSolicitudModal() {
        document.getElementById('modalSolicitudAmistad').classList.remove('active');
        document.getElementById('solicitudAmistadForm').reset();
    }
    
    /**
     * Manejar envío de solicitud
     */
    async handleSolicitudSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const amigoId = document.getElementById('solicitudAmigoId').value;
        const mensaje = formData.get('mensaje') || '';
        
        try {
            await this.apiCall('/amistades/solicitud', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    usuarioId: window.APP_CONFIG.USUARIO_ID,
                    amigoId: parseInt(amigoId),
                    mensaje: mensaje
                })
            });
            
            this.showNotification('Solicitud enviada exitosamente', 'success');
            this.closeSolicitudModal();
            this.buscarUsuarios(); // Actualizar resultados
            
        } catch (error) {
            console.error('Error enviando solicitud:', error);
            this.showError('Error al enviar solicitud');
        }
    }
    
    /**
     * Aceptar solicitud
     */
    async aceptarSolicitud(solicitudId) {
        try {
            await this.apiCall(`/amistades/${solicitudId}/aceptar?usuarioId=${window.APP_CONFIG.USUARIO_ID}`, {
                method: 'PUT'
            });
            
            this.showNotification('Solicitud aceptada exitosamente', 'success');
            this.loadSolicitudes();
            this.loadAmigos();
            
        } catch (error) {
            console.error('Error aceptando solicitud:', error);
            this.showError('Error al aceptar solicitud');
        }
    }
    
    /**
     * Rechazar solicitud
     */
    async rechazarSolicitud(solicitudId) {
        if (!confirm('¿Estás seguro de que quieres rechazar esta solicitud?')) {
            return;
        }
        
        try {
            await this.apiCall(`/amistades/${solicitudId}/rechazar?usuarioId=${window.APP_CONFIG.USUARIO_ID}`, {
                method: 'PUT'
            });
            
            this.showNotification('Solicitud rechazada', 'info');
            this.loadSolicitudes();
            
        } catch (error) {
            console.error('Error rechazando solicitud:', error);
            this.showError('Error al rechazar solicitud');
        }
    }
    
    /**
     * Eliminar amistad
     */
    async eliminarAmistad(amigoId, nombreAmigo) {
        if (!confirm(`¿Estás seguro de que quieres eliminar a ${nombreAmigo} de tu lista de amigos?`)) {
            return;
        }
        
        try {
            await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/amigo/${amigoId}`, {
                method: 'DELETE'
            });
            
            this.showNotification('Amistad eliminada', 'info');
            this.loadAmigos();
            
        } catch (error) {
            console.error('Error eliminando amistad:', error);
            this.showError('Error al eliminar amistad');
        }
    }
    
    /**
     * Cargar estadísticas
     */
    async loadEstadisticasAmistad() {
        try {
            const response = await this.apiCall(`/amistades/usuario/${window.APP_CONFIG.USUARIO_ID}/estadisticas`);
            this.data.estadisticas = response || {};
            
            document.getElementById('statTotalAmigos').textContent = this.data.estadisticas.totalAmigos || 0;
            document.getElementById('statSolicitudesRecibidas').textContent = this.data.estadisticas.solicitudesRecibidas || 0;
            document.getElementById('statSolicitudesEnviadas').textContent = this.data.estadisticas.solicitudesEnviadas || 0;
            
        } catch (error) {
            console.error('Error cargando estadísticas:', error);
            this.showError('Error al cargar estadísticas');
        }
    }
    
    /**
     * Actualizar contadores
     */
    updateAmigosCount() {
        const element = document.getElementById('totalAmigosActivos');
        if (element) {
            element.textContent = this.data.amigos.length;
        }
    }
    
    updateSolicitudesCount() {
        const recibidas = document.getElementById('totalSolicitudesRecibidas');
        const enviadas = document.getElementById('totalSolicitudesEnviadas');
        
        if (recibidas) recibidas.textContent = this.data.solicitudesRecibidas.length;
        if (enviadas) enviadas.textContent = this.data.solicitudesEnviadas.length;
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
        return date.toLocaleDateString('es-ES');
    }
}

// Registrar módulo globalmente
window.AmigosModule = AmigosModule;

// Funciones globales para compatibilidad
window.switchAmigosTab = function(tabName) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.amigos) {
        window.Dashboard.modules.amigos.switchAmigosTab(tabName);
    }
};

window.loadAmigos = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.amigos) {
        window.Dashboard.modules.amigos.loadAmigos();
    }
};

window.buscarUsuarios = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.amigos) {
        window.Dashboard.modules.amigos.buscarUsuarios();
    }
};

window.closeSolicitudModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.amigos) {
        window.Dashboard.modules.amigos.closeSolicitudModal();
    }
};
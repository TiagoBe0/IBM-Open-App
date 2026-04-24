/**
 * Dashboard Principal - Controlador de aplicación
 * Versión corregida para Spring Boot
 */

// Configuración global de la aplicación
window.Dashboard = {
    config: window.APP_CONFIG,
    modules: {},
    activeTab: 'arboles',
    initialized: false
};

// Estado global de la aplicación
window.AppState = {
    user: {
        id: window.APP_CONFIG?.USUARIO_ID || 1,
        nombre: window.APP_CONFIG?.USUARIO_NOMBRE || 'Usuario',
        apellido: window.APP_CONFIG?.USUARIO_APELLIDO || '',
        username: window.APP_CONFIG?.USUARIO_USERNAME || 'usuario'
    },
    data: {
        arboles: [],
        ramas: [],
        hojas: []
    },
    ui: {
        modalsOpen: 0,
        loading: false
    }
};

/**
 * Inicialización de la aplicación
 */
document.addEventListener('DOMContentLoaded', async function() {
    try {
        console.log('🌳 Inicializando Dashboard para usuario:', AppState.user.nombre);
        
        // Verificar configuración
        if (!window.APP_CONFIG) {
            console.warn('APP_CONFIG no disponible, usando valores por defecto');
        }
        
        // Cargar componentes HTML
        await loadComponents();
        
        // Esperar un poco para que los componentes se carguen
        setTimeout(async () => {
            // Inicializar módulos
            initializeModules();
            
            // Configurar eventos globales
            setupGlobalEvents();
            
            // Cargar datos iniciales
            await loadInitialData();
            
            Dashboard.initialized = true;
            console.log('✅ Dashboard inicializado correctamente');
            
            // Debug de módulos
            console.log('Módulos cargados:', Object.keys(Dashboard.modules));
        }, 500);
        
    } catch (error) {
        console.error('❌ Error inicializando Dashboard:', error);
        showNotification('Error al cargar el dashboard', 'error');
    }
});

/**
 * Cargar componentes HTML desde Spring Boot static folder
 */
async function loadComponents() {
    const baseUrl = window.DASHBOARD_CONFIG?.COMPONENTS_BASE_URL || '/dashboard/components';
    
    const components = [
        { id: 'header-container', file: `${baseUrl}/header.html` },
        { id: 'stats-container', file: `${baseUrl}/stats-cards.html` },
        { id: 'content-container', file: `${baseUrl}/content-sections.html` },
        { id: 'modal-container', file: `${baseUrl}/modals.html` }
    ];
    
    for (const component of components) {
        try {
            console.log(`Cargando componente: ${component.file}`);
            const response = await fetch(component.file);
            if (response.ok) {
                const html = await response.text();
                const container = document.getElementById(component.id);
                if (container) {
                    container.innerHTML = html;
                    console.log(`✅ Componente ${component.id} cargado`);
                } else {
                    console.warn(`Container ${component.id} no encontrado`);
                }
            } else {
                console.warn(`No se pudo cargar ${component.file}: ${response.status}`);
            }
        } catch (error) {
            console.warn(`Error cargando ${component.file}:`, error);
        }
    }
}

/**
 * Inicializar módulos de la aplicación
 */
function initializeModules() {
    console.log('Inicializando módulos...');
    
    // Inicializar módulos si están disponibles
    if (window.ArbolesRamasHojas) {
        try {
            Dashboard.modules.arbolesRamasHojas = new ArbolesRamasHojas();
            console.log('✅ Módulo ArbolesRamasHojas inicializado');
        } catch (error) {
            console.error('Error inicializando ArbolesRamasHojas:', error);
        }
    }
    
    if (window.ChatModule) {
        try {
            Dashboard.modules.chat = new ChatModule();
            console.log('✅ Módulo Chat inicializado');
        } catch (error) {
            console.error('Error inicializando ChatModule:', error);
        }
    }
    
    if (window.ClaudeModule) {
        try {
            Dashboard.modules.claude = new ClaudeModule();
            console.log('✅ Módulo Claude inicializado');
        } catch (error) {
            console.error('Error inicializando ClaudeModule:', error);
        }
    }
    
    if (window.AmigosModule) {
        try {
            Dashboard.modules.amigos = new AmigosModule();
            console.log('✅ Módulo Amigos inicializado');
        } catch (error) {
            console.error('Error inicializando AmigosModule:', error);
        }
    }
    
    if (window.MensajesModule) {
        try {
            Dashboard.modules.mensajes = new MensajesModule();
            console.log('✅ Módulo Mensajes inicializado');
        } catch (error) {
            console.error('Error inicializando MensajesModule:', error);
        }
    }
    
    console.log('Módulos inicializados:', Object.keys(Dashboard.modules));
}

/**
 * Configurar eventos globales
 */
function setupGlobalEvents() {
    // Eventos de pestañas
    setupTabEvents();
    
    // Eventos de botones flotantes
    setupFloatingButtons();
    
    // Eventos de teclado globales
    setupKeyboardEvents();
    
    // Eventos de ventana
    setupWindowEvents();
}

/**
 * Configurar eventos de pestañas
 */
function setupTabEvents() {
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.dataset.tab;
            if (tabName) {
                switchTab(tabName);
            }
        });
    });
}

/**
 * Configurar botones flotantes
 */
function setupFloatingButtons() {
    // Botón de chat
    const chatToggle = document.getElementById('chat-toggle');
    if (chatToggle) {
        chatToggle.addEventListener('click', function() {
            if (Dashboard.modules.chat) {
                Dashboard.modules.chat.toggleModal();
            }
        });
    }
    
    // Botón de Claude
    const claudeToggle = document.getElementById('claude-toggle');
    if (claudeToggle) {
        claudeToggle.addEventListener('click', function() {
            if (Dashboard.modules.claude) {
                Dashboard.modules.claude.toggleModal();
            }
        });
    }
}

/**
 * Configurar eventos de teclado
 */
function setupKeyboardEvents() {
    document.addEventListener('keydown', function(e) {
        // ESC para cerrar modales
        if (e.key === 'Escape') {
            closeTopModal();
        }
        
        // Ctrl+/ para abrir Claude
        if (e.ctrlKey && e.key === '/') {
            e.preventDefault();
            if (Dashboard.modules.claude) {
                Dashboard.modules.claude.toggleModal();
            }
        }
        
        // Alt+C para abrir chat
        if (e.altKey && e.key === 'c') {
            e.preventDefault();
            if (Dashboard.modules.chat) {
                Dashboard.modules.chat.toggleModal();
            }
        }
    });
}

/**
 * Configurar eventos de ventana
 */
function setupWindowEvents() {
    // Verificar sesión cada 5 minutos
    setInterval(checkSession, 5 * 60 * 1000);
    
    // Limpiar recursos al cerrar
    window.addEventListener('beforeunload', function() {
        cleanup();
    });
    
    // Manejar cambios de visibilidad
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            onPageHidden();
        } else {
            onPageVisible();
        }
    });
}

/**
 * Cambiar pestaña activa
 */
function switchTab(tabName) {
    if (Dashboard.activeTab === tabName) return;
    
    console.log('Cambiando a pestaña:', tabName);
    
    // Actualizar UI
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Activar nueva pestaña
    const activeTab = document.querySelector(`[data-tab="${tabName}"]`);
    const activeSection = document.getElementById(`${tabName}-section`);
    
    if (activeTab) activeTab.classList.add('active');
    if (activeSection) activeSection.classList.add('active');
    
    // Notificar módulos del cambio
    Dashboard.activeTab = tabName;
    onTabChange(tabName);
}

/**
 * Manejar cambio de pestaña
 */
function onTabChange(tabName) {
    switch(tabName) {
        case 'arboles':
        case 'ramas':
        case 'hojas':
            if (Dashboard.modules.arbolesRamasHojas) {
                Dashboard.modules.arbolesRamasHojas.onTabChange(tabName);
            }
            break;
            
        case 'tree':
            if (Dashboard.modules.arbolesRamasHojas) {
                Dashboard.modules.arbolesRamasHojas.loadTreeView();
            }
            break;
            
        case 'amigos':
            if (Dashboard.modules.amigos) {
                Dashboard.modules.amigos.onActivate();
            }
            break;
            
        case 'mensajes':
            if (Dashboard.modules.mensajes) {
                Dashboard.modules.mensajes.onActivate();
            }
            break;
            
        case 'config':
            loadUserConfig();
            break;
    }
}

/**
 * Cargar datos iniciales
 */
async function loadInitialData() {
    try {
        AppState.ui.loading = true;
        updateLoadingState(true);
        
        // Cargar datos del módulo principal
        if (Dashboard.modules.arbolesRamasHojas) {
            await Dashboard.modules.arbolesRamasHojas.loadAllData();
        }
        
        // Actualizar estadísticas
        updateGlobalStats();
        
    } catch (error) {
        console.error('Error cargando datos iniciales:', error);
        showNotification('Error al cargar los datos', 'error');
    } finally {
        AppState.ui.loading = false;
        updateLoadingState(false);
    }
}

/**
 * Actualizar estadísticas globales
 */
function updateGlobalStats() {
    const stats = {
        usuarios: 1, // Solo el usuario actual
        arboles: AppState.data.arboles.length,
        ramas: AppState.data.ramas.length,
        hojas: AppState.data.hojas.length
    };
    
    // Actualizar elementos del DOM
    const elements = {
        'totalUsuarios': stats.usuarios,
        'totalArboles': stats.arboles,
        'totalRamas': stats.ramas,
        'totalHojas': stats.hojas
    };
    
    Object.entries(elements).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element) {
            animateNumber(element, value);
        }
    });
}

/**
 * Animar números en estadísticas
 */
function animateNumber(element, targetValue) {
    const currentValue = parseInt(element.textContent) || 0;
    const increment = Math.ceil((targetValue - currentValue) / 10);
    
    if (currentValue === targetValue) return;
    
    const timer = setInterval(() => {
        const current = parseInt(element.textContent) || 0;
        if (current < targetValue) {
            element.textContent = Math.min(current + increment, targetValue);
        } else {
            clearInterval(timer);
        }
    }, 50);
}

/**
 * Actualizar estado de carga
 */
function updateLoadingState(loading) {
    const loadingElements = document.querySelectorAll('.loading-indicator');
    loadingElements.forEach(element => {
        element.style.display = loading ? 'block' : 'none';
    });
}

/**
 * Cerrar modal superior
 */
function closeTopModal() {
    const activeModals = document.querySelectorAll('.modal.active');
    if (activeModals.length > 0) {
        const topModal = activeModals[activeModals.length - 1];
        topModal.classList.remove('active');
        AppState.ui.modalsOpen = Math.max(0, AppState.ui.modalsOpen - 1);
    }
}

/**
 * Verificar sesión de usuario
 */
async function checkSession() {
    try {
        const response = await fetch(`${Dashboard.config.API_URL}/auth/check`);
        if (!response.ok) {
            sessionExpired();
        }
    } catch (error) {
        console.warn('Error verificando sesión:', error);
    }
}

/**
 * Manejar expiración de sesión
 */
function sessionExpired() {
    showNotification('Su sesión ha expirado. Redirigiendo...', 'warning');
    setTimeout(() => {
        window.location.href = '/login';
    }, 3000);
}

/**
 * Cargar configuración de usuario
 */
function loadUserConfig() {
    const configSection = document.getElementById('config-section');
    if (!configSection) return;
    
    const user = AppState.user;
    configSection.innerHTML = `
        <h2>Mi Perfil</h2>
        <div class="user-profile">
            <div class="form-group">
                <label>ID de Usuario:</label>
                <input type="text" value="${user.id}" readonly>
            </div>
            <div class="form-group">
                <label>Nombre de Usuario:</label>
                <input type="text" value="${user.username}" readonly>
            </div>
            <div class="form-group">
                <label>Nombre Completo:</label>
                <input type="text" value="${user.nombre} ${user.apellido}" readonly>
            </div>
        </div>
        <hr>
        <div class="config-actions">
            <button class="btn btn-warning" onclick="loadTestData()">
                📥 Cargar Datos de Prueba
            </button>
            <button class="btn btn-danger" onclick="clearAllData()">
                🗑️ Limpiar Mis Datos
            </button>
        </div>
    `;
}

/**
 * Página oculta
 */
function onPageHidden() {
    // Pausar actualizaciones innecesarias
    Object.values(Dashboard.modules).forEach(module => {
        if (module.onPageHidden) {
            module.onPageHidden();
        }
    });
}

/**
 * Página visible
 */
function onPageVisible() {
    // Reanudar actualizaciones
    Object.values(Dashboard.modules).forEach(module => {
        if (module.onPageVisible) {
            module.onPageVisible();
        }
    });
}

/**
 * Limpiar recursos
 */
function cleanup() {
    Object.values(Dashboard.modules).forEach(module => {
        if (module.cleanup) {
            module.cleanup();
        }
    });
}

/**
 * Cargar datos de prueba
 */
async function loadTestData() {
    if (!confirm('¿Cargar datos de prueba? Esto creará varios registros de ejemplo.')) {
        return;
    }
    
    if (Dashboard.modules.arbolesRamasHojas) {
        await Dashboard.modules.arbolesRamasHojas.loadTestData();
    }
}

/**
 * Limpiar todos los datos
 */
async function clearAllData() {
    if (!confirm('¿Está seguro de eliminar TODOS sus datos? Esta acción no se puede deshacer.')) {
        return;
    }
    
    if (!confirm('¿Está REALMENTE seguro? Se eliminarán todos los árboles, ramas y hojas.')) {
        return;
    }
    
    if (Dashboard.modules.arbolesRamasHojas) {
        await Dashboard.modules.arbolesRamasHojas.clearAllData();
    }
}

/**
 * Utilidades globales
 */

// Función para mostrar notificaciones
window.showNotification = function(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // Aplicar estilos inline para asegurar visibilidad
    Object.assign(notification.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '15px 20px',
        borderRadius: '10px',
        color: 'white',
        fontWeight: '500',
        zIndex: '2000',
        animation: 'slideInRight 0.3s ease'
    });
    
    // Colores por tipo
    const colors = {
        success: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
        error: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)',
        warning: 'linear-gradient(135deg, #f2994a 0%, #f2c94c 100%)',
        info: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    };
    
    notification.style.background = colors[type] || colors.info;
    
    document.body.appendChild(notification);
    
    // Auto-remover después de 3 segundos
    setTimeout(() => {
        notification.style.animation = 'slideInRight 0.3s reverse';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
};

// Función para formatear fechas
window.formatDate = function(dateString) {
    if (!dateString) return '-';
    
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '-';
    
    return date.toLocaleDateString('es-ES') + ' ' + 
           date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
};

// Función para escapar HTML
window.escapeHtml = function(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
};

// ===== FUNCIONES GLOBALES PARA MÓDULOS =====

// Funciones de amigos
window.switchAmigosTab = function(tabName) {
    if (Dashboard.modules.amigos) {
        Dashboard.modules.amigos.switchAmigosTab(tabName);
    } else {
        console.warn('Módulo de amigos no disponible');
    }
};

window.loadAmigos = function() {
    if (Dashboard.modules.amigos) {
        Dashboard.modules.amigos.loadAmigos();
    }
};

window.buscarUsuarios = function() {
    if (Dashboard.modules.amigos) {
        Dashboard.modules.amigos.buscarUsuarios();
    }
};

window.closeSolicitudModal = function() {
    if (Dashboard.modules.amigos) {
        Dashboard.modules.amigos.closeSolicitudModal();
    }
};

// Funciones de mensajes
window.switchMensajesTab = function(tabName) {
    console.log('switchMensajesTab llamado con:', tabName);
    if (Dashboard.modules.mensajes) {
        Dashboard.modules.mensajes.switchMensajesTab(tabName);
    } else {
        console.warn('Módulo de mensajes no disponible, usando fallback');
        
        // Fallback manual
        document.querySelectorAll('#mensajes-section .tabs .tab').forEach(tab => {
            tab.classList.remove('active');
        });
        
        document.querySelectorAll('.mensajes-subsection').forEach(section => {
            section.classList.remove('active');
        });
        
        // Activar nueva pestaña
        const activeTab = document.querySelector(`#mensajes-section .tabs .tab[onclick*="${tabName}"]`);
        const activeSection = document.getElementById(`${tabName}-mensajes`);
        
        if (activeTab) {
            activeTab.classList.add('active');
            console.log('Tab activado:', activeTab);
        }
        if (activeSection) {
            activeSection.classList.add('active');
            console.log('Sección activada:', activeSection);
        }
    }
};

window.loadConversaciones = function() {
    if (Dashboard.modules.mensajes) {
        Dashboard.modules.mensajes.loadConversaciones();
    }
};

window.buscarMensajes = function() {
    if (Dashboard.modules.mensajes) {
        Dashboard.modules.mensajes.buscarMensajes();
    }
};

window.abrirSelectorAmigo = function() {
    if (Dashboard.modules.mensajes) {
        Dashboard.modules.mensajes.abrirSelectorAmigo();
    }
};

window.closeSelectorAmigoModal = function() {
    if (Dashboard.modules.mensajes) {
        Dashboard.modules.mensajes.closeSelectorAmigoModal();
    }
};

window.toggleEmojis = function() {
    console.log('Función de emojis pendiente de implementar');
};

// Funciones de Claude
window.toggleClaudeModal = function() {
    if (Dashboard.modules.claude) {
        Dashboard.modules.claude.toggleModal();
    }
};

window.closeClaudeModal = function() {
    if (Dashboard.modules.claude) {
        Dashboard.modules.claude.closeModal();
    }
};

window.useSuggestion = function(type) {
    if (Dashboard.modules.claude) {
        Dashboard.modules.claude.useSuggestion(type);
    }
};

// Exportar funciones principales para uso global
window.Dashboard.switchTab = switchTab;
window.Dashboard.updateStats = updateGlobalStats;
window.Dashboard.showNotification = window.showNotification;

// Debug: Verificar carga de módulos
window.debugModules = function() {
    console.log('=== DEBUG DE MÓDULOS ===');
    console.log('Dashboard:', Dashboard);
    console.log('Módulos disponibles:', Object.keys(Dashboard?.modules || {}));
    
    if (Dashboard?.modules) {
        Object.entries(Dashboard.modules).forEach(([name, module]) => {
            console.log(`${name}:`, module);
        });
    }
    
    // Verificar clases disponibles
    console.log('Clases disponibles:');
    console.log('ArbolesRamasHojas:', window.ArbolesRamasHojas);
    console.log('ChatModule:', window.ChatModule);
    console.log('AmigosModule:', window.AmigosModule);
    console.log('MensajesModule:', window.MensajesModule);
    console.log('ClaudeModule:', window.ClaudeModule);
};
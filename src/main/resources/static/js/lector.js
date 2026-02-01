// Variables globales
let libroCompleto = null;
let seccionActual = null;
let comentariosActuales = [];
let parrafoSeleccionado = null;

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Lector inicializado');
    cargarLibro();
});

/**
 * Carga el libro completo desde el servidor
 */
async function cargarLibro() {
    try {
        const response = await fetch(`${API_URL}/libro`);
        if (!response.ok) {
            throw new Error('Error al cargar el libro');
        }

        libroCompleto = await response.json();
        console.log('📚 Libro cargado:', libroCompleto);

        renderizarNavegacion();

        // Cargar primera sección por defecto
        if (libroCompleto.documentos && libroCompleto.documentos.length > 0) {
            const primerDoc = libroCompleto.documentos[0];
            if (primerDoc.secciones && primerDoc.secciones.length > 0) {
                const primeraSec = primerDoc.secciones[0];
                cargarSeccion(primeraSec.documento, primeraSec.seccion);
            }
        }
    } catch (error) {
        console.error('Error al cargar el libro:', error);
        document.getElementById('reading-content').innerHTML = `
            <div class="empty-state">
                <h3>❌ Error al cargar el libro</h3>
                <p>${error.message}</p>
                <p style="margin-top: 20px;">Asegúrate de que el archivo <strong>libro.txt</strong> está en <strong>src/main/resources/</strong></p>
            </div>
        `;
    }
}

/**
 * Renderiza la navegación del libro
 */
function renderizarNavegacion() {
    const navDiv = document.getElementById('navigation');

    if (!libroCompleto || !libroCompleto.documentos || libroCompleto.documentos.length === 0) {
        navDiv.innerHTML = '<div class="empty-state">No hay documentos disponibles</div>';
        return;
    }

    let html = '';

    libroCompleto.documentos.forEach(doc => {
        html += `
            <div style="margin-bottom: 15px;">
                <div style="font-weight: 600; color: #667eea; margin-bottom: 8px;">
                    Documento ${doc.numero}
                </div>
        `;

        if (doc.secciones && doc.secciones.length > 0) {
            doc.secciones.forEach(sec => {
                html += `
                    <div class="nav-item" onclick="cargarSeccion(${sec.documento}, ${sec.seccion})">
                        Sección ${sec.documento}:${sec.seccion}
                    </div>
                `;
            });
        }

        html += '</div>';
    });

    navDiv.innerHTML = html;
}

/**
 * Carga una sección específica
 */
async function cargarSeccion(documento, seccion) {
    try {
        const response = await fetch(`${API_URL}/seccion/${documento}/${seccion}`);
        if (!response.ok) {
            throw new Error('Sección no encontrada');
        }

        seccionActual = await response.json();
        console.log('📄 Sección cargada:', seccionActual);

        renderizarSeccion();
        cargarComentariosSeccion(documento, seccion);
        actualizarNavegacionActiva(documento, seccion);
    } catch (error) {
        console.error('Error al cargar sección:', error);
        showNotification('Error al cargar la sección', 'error');
    }
}

/**
 * Renderiza la sección actual en el área de lectura
 */
function renderizarSeccion() {
    const readingDiv = document.getElementById('reading-content');

    if (!seccionActual) {
        readingDiv.innerHTML = '<div class="empty-state">Selecciona una sección para leer</div>';
        return;
    }

    let html = `
        <div class="reading-header">
            <h2>El Libro de Urantia</h2>
            <div class="reference">${seccionActual.referencia || `${seccionActual.documento}:${seccionActual.seccion}`}</div>
        </div>
    `;

    if (seccionActual.parrafos && seccionActual.parrafos.length > 0) {
        seccionActual.parrafos.forEach((parrafo, index) => {
            const numeroParrafo = index;
            const comentariosCount = contarComentariosParrafo(numeroParrafo);

            html += `
                <div class="parrafo" id="parrafo-${numeroParrafo}" data-parrafo="${numeroParrafo}">
                    <span class="parrafo-numero">${seccionActual.documento}:${seccionActual.seccion}.${numeroParrafo}</span>
                    <span class="parrafo-texto">${parrafo}</span>
                    <div class="parrafo-acciones">
                        <button class="btn-icon" onclick="abrirModalComentario(${numeroParrafo})" title="Agregar comentario">
                            💬
                            ${comentariosCount > 0 ? `<span class="comentarios-count">${comentariosCount}</span>` : ''}
                        </button>
                        <button class="btn-icon" onclick="verComentariosParrafo(${numeroParrafo})" title="Ver comentarios">
                            👁️
                        </button>
                    </div>
                </div>
            `;
        });
    } else {
        html += '<div class="empty-state">Esta sección no tiene contenido</div>';
    }

    readingDiv.innerHTML = html;
}

/**
 * Actualiza la navegación para mostrar la sección activa
 */
function actualizarNavegacionActiva(documento, seccion) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    const itemActivo = Array.from(document.querySelectorAll('.nav-item')).find(item =>
        item.textContent.includes(`${documento}:${seccion}`)
    );

    if (itemActivo) {
        itemActivo.classList.add('active');
        itemActivo.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

/**
 * Carga los comentarios de una sección
 */
async function cargarComentariosSeccion(documento, seccion) {
    try {
        const response = await fetch(`${API_URL}/comentarios/${documento}/${seccion}`);
        if (!response.ok) {
            throw new Error('Error al cargar comentarios');
        }

        comentariosActuales = await response.json();
        console.log('💬 Comentarios cargados:', comentariosActuales.length);

        renderizarComentarios();
    } catch (error) {
        console.error('Error al cargar comentarios:', error);
    }
}

/**
 * Renderiza todos los comentarios de la sección
 */
function renderizarComentarios() {
    const commentsDiv = document.getElementById('comments-area');

    if (!comentariosActuales || comentariosActuales.length === 0) {
        commentsDiv.innerHTML = '<div class="empty-state">No hay comentarios en esta sección</div>';
        return;
    }

    let html = '';

    comentariosActuales.forEach(comentario => {
        const fecha = new Date(comentario.fechaCreacion).toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });

        const esPropio = USUARIO_AUTENTICADO && comentario.usuarioId === USUARIO_ID;

        html += `
            <div class="comment-item" data-comment-id="${comentario.id}">
                <div class="comment-header">
                    <span class="comment-author">${comentario.usuarioNombre}</span>
                    <span class="comment-date">${fecha}</span>
                </div>
                <div class="comment-reference">
                    📍 ${comentario.numeroDocumento}:${comentario.numeroSeccion}.${comentario.numeroParrafo}
                </div>
                <div class="comment-text">${comentario.contenido}</div>
                ${esPropio ? `
                    <div class="comment-actions">
                        <button onclick="editarComentario(${comentario.id})">✏️ Editar</button>
                        <button onclick="eliminarComentario(${comentario.id})">🗑️ Eliminar</button>
                    </div>
                ` : ''}
            </div>
        `;
    });

    commentsDiv.innerHTML = html;
}

/**
 * Cuenta los comentarios de un párrafo específico
 */
function contarComentariosParrafo(numeroParrafo) {
    if (!comentariosActuales) return 0;
    return comentariosActuales.filter(c => c.numeroParrafo === numeroParrafo).length;
}

/**
 * Muestra los comentarios de un párrafo específico
 */
function verComentariosParrafo(numeroParrafo) {
    const comentarios = comentariosActuales.filter(c => c.numeroParrafo === numeroParrafo);

    const commentsDiv = document.getElementById('comments-area');

    if (comentarios.length === 0) {
        commentsDiv.innerHTML = `
            <div class="empty-state">
                No hay comentarios en este párrafo
                <button class="btn btn-primary" style="margin-top: 15px;" onclick="abrirModalComentario(${numeroParrafo})">
                    Agregar comentario
                </button>
            </div>
        `;
        return;
    }

    let html = `<div style="margin-bottom: 15px; padding-bottom: 15px; border-bottom: 2px solid #f0f0f0;">
        <strong>Párrafo ${seccionActual.documento}:${seccionActual.seccion}.${numeroParrafo}</strong>
    </div>`;

    comentarios.forEach(comentario => {
        const fecha = new Date(comentario.fechaCreacion).toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });

        const esPropio = USUARIO_AUTENTICADO && comentario.usuarioId === USUARIO_ID;

        html += `
            <div class="comment-item" data-comment-id="${comentario.id}">
                <div class="comment-header">
                    <span class="comment-author">${comentario.usuarioNombre}</span>
                    <span class="comment-date">${fecha}</span>
                </div>
                <div class="comment-text">${comentario.contenido}</div>
                ${esPropio ? `
                    <div class="comment-actions">
                        <button onclick="editarComentario(${comentario.id})">✏️ Editar</button>
                        <button onclick="eliminarComentario(${comentario.id})">🗑️ Eliminar</button>
                    </div>
                ` : ''}
            </div>
        `;
    });

    commentsDiv.innerHTML = html;

    // Scroll a los comentarios en móvil
    if (window.innerWidth < 1200) {
        document.getElementById('comments-area').scrollIntoView({ behavior: 'smooth' });
    }
}

/**
 * Abre el modal para agregar comentario
 */
function abrirModalComentario(numeroParrafo) {
    if (!USUARIO_AUTENTICADO) {
        showNotification('Debes iniciar sesión para comentar', 'error');
        setTimeout(() => {
            window.location.href = '/login';
        }, 2000);
        return;
    }

    parrafoSeleccionado = numeroParrafo;

    document.getElementById('commentReference').value =
        `${seccionActual.documento}:${seccionActual.seccion}.${numeroParrafo}`;
    document.getElementById('commentText').value = '';
    document.getElementById('commentPublic').checked = true;

    document.getElementById('commentModal').classList.add('active');
}

/**
 * Cierra el modal de comentario
 */
function closeCommentModal() {
    document.getElementById('commentModal').classList.remove('active');
    parrafoSeleccionado = null;
}

/**
 * Maneja el envío del formulario de comentario
 */
document.getElementById('commentForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!USUARIO_AUTENTICADO) {
        showNotification('Debes iniciar sesión para comentar', 'error');
        return;
    }

    const contenido = document.getElementById('commentText').value.trim();
    const esPublico = document.getElementById('commentPublic').checked;

    if (!contenido) {
        showNotification('El comentario no puede estar vacío', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/comentario`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                numeroDocumento: seccionActual.documento,
                numeroSeccion: seccionActual.seccion,
                numeroParrafo: parrafoSeleccionado,
                contenido: contenido,
                esPublico: esPublico
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Error al guardar comentario');
        }

        const comentarioGuardado = await response.json();
        console.log('✅ Comentario guardado:', comentarioGuardado);

        showNotification('Comentario agregado exitosamente', 'success');
        closeCommentModal();

        // Recargar comentarios y actualizar vista
        await cargarComentariosSeccion(seccionActual.documento, seccionActual.seccion);
        renderizarSeccion();

    } catch (error) {
        console.error('Error al guardar comentario:', error);
        showNotification(error.message, 'error');
    }
});

/**
 * Edita un comentario existente
 */
async function editarComentario(comentarioId) {
    const comentario = comentariosActuales.find(c => c.id === comentarioId);
    if (!comentario) return;

    const nuevoContenido = prompt('Editar comentario:', comentario.contenido);
    if (!nuevoContenido || nuevoContenido.trim() === '') return;

    try {
        const response = await fetch(`${API_URL}/comentario/${comentarioId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ contenido: nuevoContenido })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Error al actualizar comentario');
        }

        showNotification('Comentario actualizado exitosamente', 'success');
        await cargarComentariosSeccion(seccionActual.documento, seccionActual.seccion);

    } catch (error) {
        console.error('Error al editar comentario:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * Elimina un comentario
 */
async function eliminarComentario(comentarioId) {
    if (!confirm('¿Estás seguro de eliminar este comentario?')) return;

    try {
        const response = await fetch(`${API_URL}/comentario/${comentarioId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Error al eliminar comentario');
        }

        showNotification('Comentario eliminado exitosamente', 'success');
        await cargarComentariosSeccion(seccionActual.documento, seccionActual.seccion);
        renderizarSeccion();

    } catch (error) {
        console.error('Error al eliminar comentario:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * Muestra una notificación
 */
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#2ecc71' : type === 'error' ? '#e74c3c' : '#3498db'};
        color: white;
        padding: 15px 25px;
        border-radius: 10px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
        font-family: 'Segoe UI', sans-serif;
    `;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Cerrar modal al hacer clic fuera
document.getElementById('commentModal').addEventListener('click', (e) => {
    if (e.target.id === 'commentModal') {
        closeCommentModal();
    }
});

// Animaciones CSS
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);

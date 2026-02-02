// Módulo de Gestión de Proyectos y Tareas
const ProyectosTareas = {
    usuarioId: null,
    proyectoSeleccionado: null,

    init(usuarioId) {
        this.usuarioId = usuarioId;
        console.log('Módulo Proyectos y Tareas inicializado para usuario:', usuarioId);
        this.cargarProyectos();
        this.inicializarEventListeners();
    },

    inicializarEventListeners() {
        // Event listener para el botón de nuevo proyecto
        const btnNuevoProyecto = document.getElementById('btnNuevoProyecto');
        if (btnNuevoProyecto) {
            btnNuevoProyecto.addEventListener('click', () => this.mostrarModalNuevoProyecto());
        }

        // Event listener para guardar proyecto
        const btnGuardarProyecto = document.getElementById('btnGuardarProyecto');
        if (btnGuardarProyecto) {
            btnGuardarProyecto.addEventListener('click', () => this.guardarProyecto());
        }

        // Event listener para nueva tarea
        const btnNuevaTarea = document.getElementById('btnNuevaTarea');
        if (btnNuevaTarea) {
            btnNuevaTarea.addEventListener('click', () => this.mostrarModalNuevaTarea());
        }

        // Event listener para guardar tarea
        const btnGuardarTarea = document.getElementById('btnGuardarTarea');
        if (btnGuardarTarea) {
            btnGuardarTarea.addEventListener('click', () => this.guardarTarea());
        }
    },

    async cargarProyectos() {
        try {
            const response = await fetch(`/api/proyecto/usuario/${this.usuarioId}`);
            if (!response.ok) throw new Error('Error al cargar proyectos');

            const proyectos = await response.json();
            console.log('Proyectos cargados:', proyectos);

            this.renderizarProyectos(proyectos);
            this.actualizarEstadisticas(proyectos);
        } catch (error) {
            console.error('Error cargando proyectos:', error);
            this.mostrarError('No se pudieron cargar los proyectos');
        }
    },

    renderizarProyectos(proyectos) {
        const container = document.getElementById('proyectosContainer');
        if (!container) return;

        if (proyectos.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-folder-open fa-3x mb-3"></i>
                    <p>No tienes proyectos todavía</p>
                    <button class="btn btn-primary" onclick="ProyectosTareas.mostrarModalNuevoProyecto()">
                        Crear tu primer proyecto
                    </button>
                </div>
            `;
            return;
        }

        container.innerHTML = proyectos.map(proyecto => `
            <div class="proyecto-card" data-proyecto-id="${proyecto.id}">
                <div class="proyecto-header" style="border-left: 4px solid ${proyecto.color || '#007bff'}">
                    <div class="proyecto-info">
                        <h4>${this.escaparHTML(proyecto.nombre)}</h4>
                        <p class="proyecto-descripcion">${this.escaparHTML(proyecto.descripcion || '')}</p>
                    </div>
                    <div class="proyecto-badges">
                        <span class="badge badge-${this.obtenerColorEstado(proyecto.estado)}">${proyecto.estado || 'Sin estado'}</span>
                        <span class="badge badge-${this.obtenerColorPrioridad(proyecto.prioridad)}">${proyecto.prioridad || 'Media'}</span>
                    </div>
                </div>
                <div class="proyecto-meta">
                    <span><i class="far fa-calendar"></i> ${this.formatearFecha(proyecto.fechaInicio)} - ${this.formatearFecha(proyecto.fechaFin)}</span>
                </div>
                <div class="proyecto-actions">
                    <button class="btn btn-sm btn-outline-primary" onclick="ProyectosTareas.verProyecto(${proyecto.id})">
                        <i class="fas fa-eye"></i> Ver Tareas
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="ProyectosTareas.editarProyecto(${proyecto.id})">
                        <i class="fas fa-edit"></i> Editar
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="ProyectosTareas.eliminarProyecto(${proyecto.id})">
                        <i class="fas fa-trash"></i> Eliminar
                    </button>
                </div>
            </div>
        `).join('');
    },

    async verProyecto(proyectoId) {
        this.proyectoSeleccionado = proyectoId;

        // Cambiar a la pestaña de tareas
        const tareaTab = document.querySelector('[data-tab="tareas"]');
        if (tareaTab) {
            tareaTab.click();
        }

        await this.cargarTareas(proyectoId);
    },

    async cargarTareas(proyectoId) {
        try {
            const response = await fetch(`/api/tarea/proyecto/${proyectoId}`);
            if (!response.ok) throw new Error('Error al cargar tareas');

            const tareas = await response.json();
            console.log('Tareas cargadas:', tareas);

            this.renderizarTareas(tareas);
        } catch (error) {
            console.error('Error cargando tareas:', error);
            this.mostrarError('No se pudieron cargar las tareas');
        }
    },

    renderizarTareas(tareas) {
        const container = document.getElementById('tareasContainer');
        if (!container) return;

        if (tareas.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-tasks fa-3x mb-3"></i>
                    <p>No hay tareas en este proyecto</p>
                    <button class="btn btn-primary" onclick="ProyectosTareas.mostrarModalNuevaTarea()">
                        Crear primera tarea
                    </button>
                </div>
            `;
            return;
        }

        // Agrupar tareas por estado
        const tareasPendientes = tareas.filter(t => t.estado === 'Pendiente');
        const tareasEnProgreso = tareas.filter(t => t.estado === 'En progreso');
        const tareasCompletadas = tareas.filter(t => t.completada);

        container.innerHTML = `
            <div class="tareas-board">
                <div class="tareas-column">
                    <h5 class="column-title">Pendientes (${tareasPendientes.length})</h5>
                    ${tareasPendientes.map(t => this.renderizarTarjeTarea(t)).join('')}
                </div>
                <div class="tareas-column">
                    <h5 class="column-title">En Progreso (${tareasEnProgreso.length})</h5>
                    ${tareasEnProgreso.map(t => this.renderizarTarjeTarea(t)).join('')}
                </div>
                <div class="tareas-column">
                    <h5 class="column-title">Completadas (${tareasCompletadas.length})</h5>
                    ${tareasCompletadas.map(t => this.renderizarTarjeTarea(t)).join('')}
                </div>
            </div>
        `;
    },

    renderizarTarjeTarea(tarea) {
        return `
            <div class="tarea-card ${tarea.completada ? 'completada' : ''}" data-tarea-id="${tarea.id}">
                <div class="tarea-header">
                    <h6>${this.escaparHTML(tarea.titulo)}</h6>
                    <span class="badge badge-${this.obtenerColorPrioridad(tarea.prioridad)}">${tarea.prioridad}</span>
                </div>
                <p class="tarea-descripcion">${this.escaparHTML(tarea.descripcion || '')}</p>
                <div class="tarea-meta">
                    ${tarea.fechaVencimiento ? `<span><i class="far fa-calendar"></i> ${this.formatearFecha(tarea.fechaVencimiento)}</span>` : ''}
                    ${tarea.asignadoA ? `<span><i class="far fa-user"></i> ${this.escaparHTML(tarea.asignadoA)}</span>` : ''}
                </div>
                <div class="tarea-actions">
                    <button class="btn btn-sm ${tarea.completada ? 'btn-outline-secondary' : 'btn-outline-success'}"
                            onclick="ProyectosTareas.toggleCompletarTarea(${tarea.id}, ${!tarea.completada})">
                        <i class="fas fa-${tarea.completada ? 'undo' : 'check'}"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-primary" onclick="ProyectosTareas.editarTarea(${tarea.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="ProyectosTareas.eliminarTarea(${tarea.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    },

    mostrarModalNuevoProyecto() {
        const modal = document.getElementById('modalProyecto');
        if (modal) {
            document.getElementById('formProyecto').reset();
            document.getElementById('proyectoId').value = '';
            document.getElementById('modalProyectoTitle').textContent = 'Nuevo Proyecto';
            $(modal).modal('show');
        }
    },

    async guardarProyecto() {
        const id = document.getElementById('proyectoId').value;
        const proyecto = {
            nombre: document.getElementById('proyectoNombre').value,
            descripcion: document.getElementById('proyectoDescripcion').value,
            fechaInicio: document.getElementById('proyectoFechaInicio').value,
            fechaFin: document.getElementById('proyectoFechaFin').value,
            estado: document.getElementById('proyectoEstado').value,
            prioridad: document.getElementById('proyectoPrioridad').value,
            color: document.getElementById('proyectoColor').value,
            usuarioId: this.usuarioId
        };

        try {
            const url = id ? `/api/proyecto/${id}` : '/api/proyecto/registrar';
            const method = id ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(proyecto)
            });

            if (!response.ok) throw new Error('Error al guardar proyecto');

            $('#modalProyecto').modal('hide');
            this.mostrarExito(id ? 'Proyecto actualizado' : 'Proyecto creado');
            this.cargarProyectos();
        } catch (error) {
            console.error('Error guardando proyecto:', error);
            this.mostrarError('No se pudo guardar el proyecto');
        }
    },

    async editarProyecto(proyectoId) {
        try {
            const response = await fetch(`/api/proyecto/${proyectoId}`);
            if (!response.ok) throw new Error('Error al cargar proyecto');

            const proyecto = await response.json();

            document.getElementById('proyectoId').value = proyecto.id;
            document.getElementById('proyectoNombre').value = proyecto.nombre;
            document.getElementById('proyectoDescripcion').value = proyecto.descripcion || '';
            document.getElementById('proyectoFechaInicio').value = proyecto.fechaInicio || '';
            document.getElementById('proyectoFechaFin').value = proyecto.fechaFin || '';
            document.getElementById('proyectoEstado').value = proyecto.estado || 'En progreso';
            document.getElementById('proyectoPrioridad').value = proyecto.prioridad || 'Media';
            document.getElementById('proyectoColor').value = proyecto.color || '#007bff';

            document.getElementById('modalProyectoTitle').textContent = 'Editar Proyecto';
            $('#modalProyecto').modal('show');
        } catch (error) {
            console.error('Error cargando proyecto:', error);
            this.mostrarError('No se pudo cargar el proyecto');
        }
    },

    async eliminarProyecto(proyectoId) {
        if (!confirm('¿Estás seguro de eliminar este proyecto? Se eliminarán todas sus tareas.')) return;

        try {
            const response = await fetch(`/api/proyecto/${proyectoId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error('Error al eliminar proyecto');

            this.mostrarExito('Proyecto eliminado');
            this.cargarProyectos();
        } catch (error) {
            console.error('Error eliminando proyecto:', error);
            this.mostrarError('No se pudo eliminar el proyecto');
        }
    },

    mostrarModalNuevaTarea() {
        if (!this.proyectoSeleccionado) {
            this.mostrarError('Por favor, selecciona un proyecto primero');
            return;
        }

        const modal = document.getElementById('modalTarea');
        if (modal) {
            document.getElementById('formTarea').reset();
            document.getElementById('tareaId').value = '';
            document.getElementById('modalTareaTitle').textContent = 'Nueva Tarea';
            $(modal).modal('show');
        }
    },

    async guardarTarea() {
        const id = document.getElementById('tareaId').value;
        const tarea = {
            titulo: document.getElementById('tareaTitulo').value,
            descripcion: document.getElementById('tareaDescripcion').value,
            fechaVencimiento: document.getElementById('tareaFechaVencimiento').value,
            estado: document.getElementById('tareaEstado').value,
            prioridad: document.getElementById('tareaPrioridad').value,
            asignadoA: document.getElementById('tareaAsignadoA').value,
            etiquetas: document.getElementById('tareaEtiquetas').value,
            completada: document.getElementById('tareaCompletada').checked,
            proyectoId: this.proyectoSeleccionado
        };

        try {
            const url = id ? `/api/tarea/${id}` : '/api/tarea/registrar';
            const method = id ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(tarea)
            });

            if (!response.ok) throw new Error('Error al guardar tarea');

            $('#modalTarea').modal('hide');
            this.mostrarExito(id ? 'Tarea actualizada' : 'Tarea creada');
            this.cargarTareas(this.proyectoSeleccionado);
        } catch (error) {
            console.error('Error guardando tarea:', error);
            this.mostrarError('No se pudo guardar la tarea');
        }
    },

    async editarTarea(tareaId) {
        try {
            const response = await fetch(`/api/tarea/${tareaId}`);
            if (!response.ok) throw new Error('Error al cargar tarea');

            const tarea = await response.json();

            document.getElementById('tareaId').value = tarea.id;
            document.getElementById('tareaTitulo').value = tarea.titulo;
            document.getElementById('tareaDescripcion').value = tarea.descripcion || '';
            document.getElementById('tareaFechaVencimiento').value = tarea.fechaVencimiento || '';
            document.getElementById('tareaEstado').value = tarea.estado || 'Pendiente';
            document.getElementById('tareaPrioridad').value = tarea.prioridad || 'Media';
            document.getElementById('tareaAsignadoA').value = tarea.asignadoA || '';
            document.getElementById('tareaEtiquetas').value = tarea.etiquetas || '';
            document.getElementById('tareaCompletada').checked = tarea.completada;

            document.getElementById('modalTareaTitle').textContent = 'Editar Tarea';
            $('#modalTarea').modal('show');
        } catch (error) {
            console.error('Error cargando tarea:', error);
            this.mostrarError('No se pudo cargar la tarea');
        }
    },

    async toggleCompletarTarea(tareaId, completada) {
        try {
            const response = await fetch(`/api/tarea/${tareaId}`);
            if (!response.ok) throw new Error('Error al cargar tarea');

            const tarea = await response.json();
            tarea.completada = completada;
            tarea.estado = completada ? 'Completada' : 'Pendiente';

            const updateResponse = await fetch(`/api/tarea/${tareaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(tarea)
            });

            if (!updateResponse.ok) throw new Error('Error al actualizar tarea');

            this.cargarTareas(this.proyectoSeleccionado);
        } catch (error) {
            console.error('Error actualizando tarea:', error);
            this.mostrarError('No se pudo actualizar la tarea');
        }
    },

    async eliminarTarea(tareaId) {
        if (!confirm('¿Estás seguro de eliminar esta tarea?')) return;

        try {
            const response = await fetch(`/api/tarea/${tareaId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error('Error al eliminar tarea');

            this.mostrarExito('Tarea eliminada');
            this.cargarTareas(this.proyectoSeleccionado);
        } catch (error) {
            console.error('Error eliminando tarea:', error);
            this.mostrarError('No se pudo eliminar la tarea');
        }
    },

    actualizarEstadisticas(proyectos) {
        const totalProyectos = proyectos.length;
        const proyectosActivos = proyectos.filter(p => p.estado === 'En progreso').length;
        const proyectosCompletados = proyectos.filter(p => p.estado === 'Completado').length;

        // Actualizar cards de estadísticas
        const statProyectos = document.getElementById('statTotalProyectos');
        if (statProyectos) statProyectos.textContent = totalProyectos;

        const statActivos = document.getElementById('statProyectosActivos');
        if (statActivos) statActivos.textContent = proyectosActivos;

        const statCompletados = document.getElementById('statProyectosCompletados');
        if (statCompletados) statCompletados.textContent = proyectosCompletados;
    },

    // Utilidades
    escaparHTML(texto) {
        if (!texto) return '';
        const div = document.createElement('div');
        div.textContent = texto;
        return div.innerHTML;
    },

    formatearFecha(fecha) {
        if (!fecha) return 'Sin fecha';
        const d = new Date(fecha);
        return d.toLocaleDateString('es-ES', { year: 'numeric', month: 'short', day: 'numeric' });
    },

    obtenerColorEstado(estado) {
        const colores = {
            'En progreso': 'primary',
            'Completado': 'success',
            'Pausado': 'warning',
            'Cancelado': 'danger',
            'Pendiente': 'secondary'
        };
        return colores[estado] || 'secondary';
    },

    obtenerColorPrioridad(prioridad) {
        const colores = {
            'Alta': 'danger',
            'Media': 'warning',
            'Baja': 'info'
        };
        return colores[prioridad] || 'secondary';
    },

    mostrarExito(mensaje) {
        // Implementar notificación de éxito
        console.log('Éxito:', mensaje);
        alert(mensaje);
    },

    mostrarError(mensaje) {
        // Implementar notificación de error
        console.error('Error:', mensaje);
        alert(mensaje);
    }
};

// Exportar para uso global
window.ProyectosTareas = ProyectosTareas;

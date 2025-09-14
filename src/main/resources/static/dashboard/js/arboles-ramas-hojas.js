/**
 * Módulo para gestión de Árboles, Ramas y Hojas
 */

class ArbolesRamasHojas {
    constructor() {
        this.data = {
            arboles: [],
            ramas: [],
            hojas: []
        };
        
        this.currentModal = {
            type: null,
            editId: null,
            photoFile: null,
            currentPhotoId: null
        };
        
        this.searchCache = new Map();
        this.loadingStates = new Set();
        
        console.log('📊 Módulo ArbolesRamasHojas inicializado');
        this.initialize();
    }
    
    /**
     * Inicialización del módulo
     */
    initialize() {
        this.setupEventListeners();
        this.loadAllData();
    }
    
    /**
     * Configurar event listeners
     */
    setupEventListeners() {
        // Formulario principal
        const mainForm = document.getElementById('modalForm');
        if (mainForm) {
            mainForm.addEventListener('submit', (e) => this.handleFormSubmit(e));
        }
        
        // Búsquedas
        this.setupSearchListeners();
        
        // Eventos de fotos
        this.setupPhotoListeners();
    }
    
    /**
     * Configurar listeners de búsqueda
     */
    setupSearchListeners() {
        const searchInputs = [
            { id: 'searchArboles', handler: () => this.searchArboles() },
            { id: 'searchRamas', handler: () => this.searchRamas() },
            { id: 'searchHojas', handler: () => this.searchHojas() }
        ];
        
        searchInputs.forEach(({ id, handler }) => {
            const input = document.getElementById(id);
            if (input) {
                // Búsqueda en tiempo real con debounce
                let timeout;
                input.addEventListener('input', () => {
                    clearTimeout(timeout);
                    timeout = setTimeout(handler, 300);
                });
                
                // Búsqueda al presionar Enter
                input.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        handler();
                    }
                });
            }
        });
    }
    
    /**
     * Configurar listeners de fotos
     */
    setupPhotoListeners() {
        const photoInput = document.getElementById('photoInput');
        if (photoInput) {
            photoInput.addEventListener('change', (e) => this.handlePhotoPreview(e));
        }
    }
    
    /**
     * Manejar cambio de pestaña
     */
    onTabChange(tabName) {
        switch(tabName) {
            case 'arboles':
                this.renderArbolesTable(this.data.arboles);
                break;
            case 'ramas':
                this.renderRamasTable(this.data.ramas);
                break;
            case 'hojas':
                this.renderHojasTable(this.data.hojas);
                break;
        }
    }
    
    /**
     * Cargar todos los datos
     */
    async loadAllData() {
        try {
            this.setLoadingState('all', true);
            
            await this.loadArboles();
            await this.loadRamas();
            await this.loadHojas();
            
            // Actualizar estado global
            if (window.AppState) {
                window.AppState.data = { ...this.data };
            }
            
            // Actualizar estadísticas
            if (window.Dashboard && window.Dashboard.updateStats) {
                window.Dashboard.updateStats();
            }
            
        } catch (error) {
            console.error('Error cargando datos:', error);
            this.showError('Error al cargar los datos');
        } finally {
            this.setLoadingState('all', false);
        }
    }
    
    /**
     * Cargar árboles del usuario actual
     */
    async loadArboles() {
        try {
            this.setLoadingState('arboles', true);
            
            const response = await this.apiCall(`/arbol/usuario/${window.APP_CONFIG.USUARIO_ID}`);
            this.data.arboles = response || [];
            
            this.renderArbolesTable(this.data.arboles);
            this.clearSearchCache('arboles');
            
        } catch (error) {
            console.error('Error cargando árboles:', error);
            this.renderError('arbolesTable', 'No se pudieron cargar los árboles');
        } finally {
            this.setLoadingState('arboles', false);
        }
    }
    
    /**
     * Cargar ramas
     */
    async loadRamas() {
        try {
            this.setLoadingState('ramas', true);
            
            const ramas = [];
            for (const arbol of this.data.arboles) {
                try {
                    const arbolRamas = await this.apiCall(`/rama/arbol/${arbol.id}`);
                    if (arbolRamas) {
                        ramas.push(...arbolRamas);
                    }
                } catch (error) {
                    console.warn(`Error cargando ramas del árbol ${arbol.id}:`, error);
                }
            }
            
            this.data.ramas = ramas;
            this.renderRamasTable(this.data.ramas);
            this.clearSearchCache('ramas');
            
        } catch (error) {
            console.error('Error cargando ramas:', error);
            this.renderError('ramasTable', 'No se pudieron cargar las ramas');
        } finally {
            this.setLoadingState('ramas', false);
        }
    }
    
    /**
     * Cargar hojas
     */
    async loadHojas() {
        try {
            this.setLoadingState('hojas', true);
            
            const hojas = [];
            for (const rama of this.data.ramas) {
                try {
                    const ramaHojas = await this.apiCall(`/hoja/rama/${rama.id}`);
                    if (ramaHojas) {
                        hojas.push(...ramaHojas);
                    }
                } catch (error) {
                    console.warn(`Error cargando hojas de la rama ${rama.id}:`, error);
                }
            }
            
            this.data.hojas = hojas;
            this.renderHojasTable(this.data.hojas);
            this.clearSearchCache('hojas');
            
        } catch (error) {
            console.error('Error cargando hojas:', error);
            this.renderError('hojasTable', 'No se pudieron cargar las hojas');
        } finally {
            this.setLoadingState('hojas', false);
        }
    }
    
    /**
     * Renderizar tabla de árboles
     */
    renderArbolesTable(arboles) {
        const container = document.getElementById('arbolesTable');
        if (!container) return;
        
        if (!arboles || arboles.length === 0) {
            container.innerHTML = this.getEmptyStateHtml('árboles');
            return;
        }
        
        const html = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Foto</th>
                        <th>Campo A</th>
                        <th>Campo B</th>
                        <th>Campo C</th>
                        <th>Valores</th>
                        <th>Flags</th>
                        <th>Calendario</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    ${arboles.map(arbol => this.getArbolRowHtml(arbol)).join('')}
                </tbody>
            </table>
        `;
        
        container.innerHTML = html;
    }
    
    /**
     * Renderizar tabla de ramas
     */
    renderRamasTable(ramas) {
        const container = document.getElementById('ramasTable');
        if (!container) return;
        
        if (!ramas || ramas.length === 0) {
            container.innerHTML = this.getEmptyStateHtml('ramas');
            return;
        }
        
        const html = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Foto</th>
                        <th>Campo A</th>
                        <th>Campo B</th>
                        <th>Campo C</th>
                        <th>Valores</th>
                        <th>Flags</th>
                        <th>Árbol ID</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    ${ramas.map(rama => this.getRamaRowHtml(rama)).join('')}
                </tbody>
            </table>
        `;
        
        container.innerHTML = html;
    }
    
    /**
     * Renderizar tabla de hojas
     */
    renderHojasTable(hojas) {
        const container = document.getElementById('hojasTable');
        if (!container) return;
        
        if (!hojas || hojas.length === 0) {
            container.innerHTML = this.getEmptyStateHtml('hojas');
            return;
        }
        
        const html = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Foto</th>
                        <th>Campo A</th>
                        <th>Campo B</th>
                        <th>Campo C</th>
                        <th>Valores</th>
                        <th>Flags</th>
                        <th>Rama ID</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    ${hojas.map(hoja => this.getHojaRowHtml(hoja)).join('')}
                </tbody>
            </table>
        `;
        
        container.innerHTML = html;
    }
    
    /**
     * Generar HTML para fila de árbol
     */
    getArbolRowHtml(arbol) {
        return `
            <tr>
                <td>${arbol.id}</td>
                <td class="text-center">${this.getPhotoHtml(arbol.fotoId, 'árbol')}</td>
                <td><strong>${arbol.a || '-'}</strong></td>
                <td>${arbol.b || '-'}</td>
                <td>${arbol.c || '-'}</td>
                <td>AF: ${arbol.af || 0}, BF: ${arbol.bf || 0}, CF: ${arbol.cf || 0}</td>
                <td>
                    ${this.getBadgeHtml('BA', arbol.ba)}
                    ${this.getBadgeHtml('BB', arbol.bb)}
                    ${this.getBadgeHtml('BC', arbol.bc)}
                </td>
                <td>${this.formatDate(arbol.calendario)}</td>
                <td class="actions">
                    <button class="btn btn-warning btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.editRecord('arbol', ${arbol.id})" title="Editar">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.deleteRecord('arbol', ${arbol.id})" title="Eliminar">🗑️</button>
                </td>
            </tr>
        `;
    }
    
    /**
     * Generar HTML para fila de rama
     */
    getRamaRowHtml(rama) {
        return `
            <tr>
                <td>${rama.id}</td>
                <td class="text-center">${this.getPhotoHtml(rama.fotoId, 'rama')}</td>
                <td><strong>${rama.a || '-'}</strong></td>
                <td>${rama.b || '-'}</td>
                <td>${rama.c || '-'}</td>
                <td>AF: ${rama.af || 0}, BF: ${rama.bf || 0}, CF: ${rama.cf || 0}</td>
                <td>
                    ${this.getBadgeHtml('BA', rama.ba)}
                    ${this.getBadgeHtml('BB', rama.bb)}
                    ${this.getBadgeHtml('BC', rama.bc)}
                </td>
                <td>${rama.arbolId}</td>
                <td class="actions">
                    <button class="btn btn-warning btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.editRecord('rama', ${rama.id})" title="Editar">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.deleteRecord('rama', ${rama.id})" title="Eliminar">🗑️</button>
                </td>
            </tr>
        `;
    }
    
    /**
     * Generar HTML para fila de hoja
     */
    getHojaRowHtml(hoja) {
        return `
            <tr>
                <td>${hoja.id}</td>
                <td class="text-center">${this.getPhotoHtml(hoja.fotoId, 'hoja')}</td>
                <td><strong>${hoja.a || '-'}</strong></td>
                <td>${hoja.b || '-'}</td>
                <td>${hoja.c || '-'}</td>
                <td>AF: ${hoja.af || 0}, BF: ${hoja.bf || 0}, CF: ${hoja.cf || 0}</td>
                <td>
                    ${this.getBadgeHtml('BA', hoja.ba)}
                    ${this.getBadgeHtml('BB', hoja.bb)}
                    ${this.getBadgeHtml('BC', hoja.bc)}
                </td>
                <td>${hoja.ramaId}</td>
                <td class="actions">
                    <button class="btn btn-warning btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.editRecord('hoja', ${hoja.id})" title="Editar">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="window.Dashboard.modules.arbolesRamasHojas.deleteRecord('hoja', ${hoja.id})" title="Eliminar">🗑️</button>
                </td>
            </tr>
        `;
    }
    
    /**
     * Generar HTML para foto
     */
    getPhotoHtml(fotoId, tipo = 'elemento') {
        if (!fotoId) {
            return '<span class="text-muted">📷 Sin foto</span>';
        }
        
        return `
            <img src="${window.APP_CONFIG.API_URL}/foto/${fotoId}" 
                 alt="Foto del ${tipo}" 
                 style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px; cursor: pointer; border: 2px solid #e0e0e0;"
                 onclick="window.Dashboard.modules.arbolesRamasHojas.viewFullImage(${fotoId})"
                 onerror="this.style.display='none'; this.nextElementSibling.style.display='inline';"
                 title="Click para ver en tamaño completo">
            <span style="display: none; color: #999; font-size: 0.8em;">⚠ Error</span>
        `;
    }
    
    /**
     * Generar HTML para badge
     */
    getBadgeHtml(label, value) {
        const className = value ? 'badge-true' : 'badge-false';
        return `<span class="badge ${className}">${label}</span>`;
    }
    
    /**
     * Generar HTML para estado vacío
     */
    getEmptyStateHtml(tipo) {
        return `<div class="empty-state">No hay ${tipo} disponibles</div>`;
    }
    
    /**
     * Cargar vista jerárquica
     */
    loadTreeView() {
        const container = document.getElementById('treeView');
        if (!container) return;
        
        if (this.data.arboles.length === 0) {
            container.innerHTML = '<div class="empty-state">No hay datos para mostrar en la vista jerárquica</div>';
            return;
        }
        
        let html = '';
        
        this.data.arboles.forEach(arbol => {
            const arbolRamas = this.data.ramas.filter(r => r.arbolId === arbol.id);
            
            html += this.getTreeItemHtml(arbol, 'arbol', arbolRamas.length);
            
            arbolRamas.forEach(rama => {
                const ramaHojas = this.data.hojas.filter(h => h.ramaId === rama.id);
                html += this.getTreeItemHtml(rama, 'rama', ramaHojas.length);
                
                ramaHojas.forEach(hoja => {
                    html += this.getTreeItemHtml(hoja, 'hoja', 0);
                });
            });
        });
        
        container.innerHTML = html;
    }
    
    /**
     * Generar HTML para item de árbol jerárquico
     */
    getTreeItemHtml(item, tipo, childCount) {
        const icons = { arbol: '🌳', rama: '🌿', hoja: '🍃' };
        const classes = { arbol: '', rama: 'rama', hoja: 'hoja' };
        
        const photoHtml = item.fotoId ? 
            `<img src="${window.APP_CONFIG.API_URL}/foto/${item.fotoId}" 
                  style="width: 30px; height: 30px; object-fit: cover; border-radius: 50%; margin-right: 10px; vertical-align: middle;"
                  onclick="window.Dashboard.modules.arbolesRamasHojas.viewFullImage(${item.fotoId})"
                  title="Click para ver completa">` : 
            `<span style="margin-right: 10px;">${icons[tipo]}</span>`;
        
        return `
            <div class="tree-item ${classes[tipo]}">
                <div class="tree-header">
                    <div class="tree-title">
                        ${photoHtml}
                        ${icons[tipo]} ${item.a || `${tipo.charAt(0).toUpperCase() + tipo.slice(1)} sin nombre`} (ID: ${item.id})
                    </div>
                    ${childCount > 0 ? `<div style="font-size: 0.9em; color: #666;">${childCount} ${tipo === 'arbol' ? 'ramas' : 'hojas'}</div>` : ''}
                </div>
                <div class="tree-details">
                    <div><strong>Descripción:</strong> ${item.b || 'Sin descripción'}</div>
                    <div><strong>Categoría:</strong> ${item.c || 'Sin categoría'}</div>
                    <div><strong>Valores:</strong> AF: ${item.af || 0}, BF: ${item.bf || 0}, CF: ${item.cf || 0}</div>
                    <div><strong>Fecha:</strong> ${this.formatDate(item.calendario)}</div>
                </div>
            </div>
        `;
    }
    
    /**
     * Funciones de búsqueda
     */
    searchArboles() {
        const searchTerm = document.getElementById('searchArboles')?.value?.toLowerCase() || '';
        this.performSearch('arboles', searchTerm, this.data.arboles);
    }
    
    searchRamas() {
        const searchTerm = document.getElementById('searchRamas')?.value?.toLowerCase() || '';
        this.performSearch('ramas', searchTerm, this.data.ramas);
    }
    
    searchHojas() {
        const searchTerm = document.getElementById('searchHojas')?.value?.toLowerCase() || '';
        this.performSearch('hojas', searchTerm, this.data.hojas);
    }
    
    /**
     * Realizar búsqueda
     */
    performSearch(type, searchTerm, data) {
        if (!searchTerm) {
            this[`render${type.charAt(0).toUpperCase() + type.slice(1)}Table`](data);
            return;
        }
        
        // Verificar cache
        const cacheKey = `${type}:${searchTerm}`;
        if (this.searchCache.has(cacheKey)) {
            const cachedResults = this.searchCache.get(cacheKey);
            this[`render${type.charAt(0).toUpperCase() + type.slice(1)}Table`](cachedResults);
            return;
        }
        
        // Filtrar datos
        const filtered = data.filter(item => 
            (item.a && item.a.toLowerCase().includes(searchTerm)) ||
            (item.b && item.b.toLowerCase().includes(searchTerm)) ||
            (item.c && item.c.toLowerCase().includes(searchTerm))
        );
        
        // Guardar en cache
        this.searchCache.set(cacheKey, filtered);
        
        // Renderizar resultados
        this[`render${type.charAt(0).toUpperCase() + type.slice(1)}Table`](filtered);
    }
    
    /**
     * Manejar envío de formulario
     */
    async handleFormSubmit(e) {
        e.preventDefault();
        
        try {
            const formData = new FormData(e.target);
            const data = this.processFormData(formData);
            
            // Subir foto si existe
            if (this.currentModal.photoFile) {
                const photoId = await this.uploadPhoto();
                if (photoId) {
                    data.fotoId = photoId;
                }
            } else if (this.currentModal.currentPhotoId) {
                data.fotoId = this.currentModal.currentPhotoId;
            }
            
            // Guardar registro
            await this.saveRecord(data);
            
            this.closeModal();
            await this.loadAllData();
            
        } catch (error) {
            console.error('Error enviando formulario:', error);
            this.showError(`Error al ${this.currentModal.editId ? 'actualizar' : 'crear'} ${this.currentModal.type}`);
        }
    }
    
    /**
     * Procesar datos del formulario
     */
    processFormData(formData) {
        const data = Object.fromEntries(formData.entries());
        
        // Convertir checkboxes a booleanos
        data.ba = formData.has('ba');
        data.bb = formData.has('bb');
        data.bc = formData.has('bc');
        
        // Convertir valores numéricos
        data.af = parseFloat(data.af) || 0;
        data.bf = parseFloat(data.bf) || 0;
        data.cf = parseFloat(data.cf) || 0;
        data.usuarioId = parseInt(data.usuarioId);
        
        // Agregar ID del padre según el tipo
        if (this.currentModal.type === 'rama' && data.parentId) {
            data.arbolId = parseInt(data.parentId);
            delete data.parentId;
        } else if (this.currentModal.type === 'hoja' && data.parentId) {
            data.ramaId = parseInt(data.parentId);
            delete data.parentId;
        }
        
        return data;
    }
    
    /**
     * Guardar registro
     */
    async saveRecord(data) {
        const isEdit = this.currentModal.editId;
        const type = this.currentModal.type;
        
        let url = `/${type}/${isEdit ? this.currentModal.editId : 'registrar'}`;
        const method = isEdit ? 'PUT' : 'POST';
        
        return await this.apiCall(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
    }
    
    /**
     * Funciones de modal
     */
    openModal(type, editId = null) {
        this.currentModal = {
            type,
            editId,
            photoFile: null,
            currentPhotoId: null
        };
        
        const modal = document.getElementById('modal');
        const form = document.getElementById('modalForm');
        const title = document.getElementById('modalTitle');
        
        if (!modal || !form || !title) return;
        
        // Limpiar formulario
        form.reset();
        this.clearPhotoState();
        
        // Configurar título y opciones
        this.configureModal(type, editId);
        
        // Cargar datos para edición
        if (editId) {
            this.loadRecordForEdit(type, editId);
        } else {
            this.setDefaultValues();
        }
        
        modal.classList.add('active');
    }
    
    closeModal() {
        const modal = document.getElementById('modal');
        if (modal) {
            modal.classList.remove('active');
        }
        
        this.currentModal = {
            type: null,
            editId: null,
            photoFile: null,
            currentPhotoId: null
        };
    }
    
    /**
     * Configurar modal según tipo
     */
    configureModal(type, editId) {
        const title = document.getElementById('modalTitle');
        const parentSelect = document.getElementById('parentSelect');
        const parentLabel = document.getElementById('parentLabel');
        
        const types = {
            arbol: { title: 'Árbol', showParent: false },
            rama: { title: 'Rama', showParent: true, parentType: 'arbol', parentLabel: 'Seleccionar Árbol:' },
            hoja: { title: 'Hoja', showParent: true, parentType: 'rama', parentLabel: 'Seleccionar Rama:' }
        };
        
        const config = types[type];
        if (!config) return;
        
        title.textContent = `${editId ? 'Editar' : 'Nuevo'} ${config.title}`;
        
        if (config.showParent && parentSelect && parentLabel) {
            parentSelect.style.display = 'block';
            parentLabel.textContent = config.parentLabel;
            this.loadParentOptions(config.parentType);
        } else if (parentSelect) {
            parentSelect.style.display = 'none';
        }
    }
    
    /**
     * Cargar opciones de padre
     */
    loadParentOptions(parentType) {
        const select = document.getElementById('parentId');
        if (!select) return;
        
        select.innerHTML = '<option value="">Seleccione...</option>';
        
        const options = parentType === 'arbol' ? this.data.arboles : this.data.ramas;
        
        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.id;
            optionElement.textContent = `${option.id} - ${option.a || 'Sin nombre'}`;
            select.appendChild(optionElement);
        });
    }
    
    /**
     * Establecer valores por defecto
     */
    setDefaultValues() {
        const usuarioInput = document.getElementById('modalUsuarioId');
        if (usuarioInput) {
            usuarioInput.value = window.APP_CONFIG.USUARIO_ID;
        }
        
        const calendarioInput = document.querySelector('[name="calendario"]');
        if (calendarioInput) {
            const now = new Date();
            now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
            calendarioInput.value = now.toISOString().slice(0, 16);
        }
    }
    
    /**
     * Cargar registro para edición
     */
    async loadRecordForEdit(type, id) {
        try {
            const data = await this.apiCall(`/${type}/${id}`);
            if (!data) return;
            
            this.populateForm(data);
            
            if (data.fotoId) {
                await this.loadPhotoForEdit(data.fotoId);
            }
            
            // Establecer padre si corresponde
            this.setParentValue(type, data);
            
        } catch (error) {
            console.error('Error cargando datos para edición:', error);
            this.showError('Error al cargar datos para edición');
        }
    }
    
    /**
     * Poblar formulario con datos
     */
    populateForm(data) {
        const form = document.getElementById('modalForm');
        if (!form) return;
        
        Object.keys(data).forEach(key => {
            const input = form.querySelector(`[name="${key}"]`);
            if (!input) return;
            
            if (input.type === 'checkbox') {
                input.checked = data[key];
            } else if (input.type === 'datetime-local' && data[key]) {
                const date = new Date(data[key]);
                input.value = date.toISOString().slice(0, 16);
            } else {
                input.value = data[key] || '';
            }
        });
    }
    
    /**
     * Establecer valor de padre
     */
    setParentValue(type, data) {
        setTimeout(() => {
            const parentSelect = document.getElementById('parentId');
            if (!parentSelect) return;
            
            if (type === 'rama' && data.arbolId) {
                parentSelect.value = data.arbolId;
            } else if (type === 'hoja' && data.ramaId) {
                parentSelect.value = data.ramaId;
            }
        }, 100);
    }
    
    /**
     * Funciones de foto
     */
    handlePhotoPreview(event) {
        const file = event.target.files[0];
        if (!file) return;
        
        if (!this.validatePhoto(file)) {
            event.target.value = '';
            return;
        }
        
        this.currentModal.photoFile = file;
        this.showPhotoPreview(file);
    }
    
    validatePhoto(file) {
        if (file.size > 5 * 1024 * 1024) {
            this.showError('La imagen es demasiado grande. Máximo 5MB.');
            return false;
        }
        
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            this.showError('Formato no válido. Solo JPG, PNG, GIF y WebP.');
            return false;
        }
        
        return true;
    }
    
    showPhotoPreview(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const previewImg = document.getElementById('previewImage');
            const previewDiv = document.getElementById('photoPreview');
            const currentDiv = document.getElementById('currentPhoto');
            
            if (previewImg && previewDiv) {
                previewImg.src = e.target.result;
                previewDiv.style.display = 'block';
            }
            
            if (currentDiv) {
                currentDiv.style.display = 'none';
            }
        };
        
        reader.onerror = () => {
            this.showError('Error al leer la imagen');
            this.clearPhotoState();
        };
        
        reader.readAsDataURL(file);
    }
    
    async uploadPhoto() {
        if (!this.currentModal.photoFile) return null;
        
        const formData = new FormData();
        formData.append('file', this.currentModal.photoFile);
        
        try {
            const response = await fetch(`${window.APP_CONFIG.API_URL}/foto/upload`, {
                method: 'POST',
                body: formData
            });
            
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            window.showNotification('Foto subida correctamente', 'success');
            return data.id;
            
        } catch (error) {
            console.error('Error subiendo foto:', error);
            this.showError('Error al subir la foto');
            return null;
        }
    }
    
    async loadPhotoForEdit(fotoId) {
        if (!fotoId) {
            this.clearPhotoState();
            return;
        }
        
        try {
            this.currentModal.currentPhotoId = fotoId;
            
            const currentImg = document.getElementById('currentImage');
            const currentDiv = document.getElementById('currentPhoto');
            const previewDiv = document.getElementById('photoPreview');
            
            if (currentImg && currentDiv) {
                currentImg.src = `${window.APP_CONFIG.API_URL}/foto/${fotoId}`;
                currentDiv.style.display = 'block';
            }
            
            if (previewDiv) {
                previewDiv.style.display = 'none';
            }
            
        } catch (error) {
            console.error('Error cargando foto:', error);
            this.clearPhotoState();
        }
    }
    
    clearPhotoState() {
        this.currentModal.photoFile = null;
        this.currentModal.currentPhotoId = null;
        
        const elements = ['photoInput', 'photoPreview', 'currentPhoto'];
        elements.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                if (id === 'photoInput') {
                    element.value = '';
                } else {
                    element.style.display = 'none';
                }
            }
        });
    }
    
    /**
     * Ver imagen completa
     */
    viewFullImage(fotoId) {
        if (!fotoId) return;
        
        const modal = document.createElement('div');
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            cursor: pointer;
        `;
        
        const img = document.createElement('img');
        img.src = `${window.APP_CONFIG.API_URL}/foto/${fotoId}`;
        img.style.cssText = `
            max-width: 90%;
            max-height: 90%;
            object-fit: contain;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
        `;
        
        const loading = document.createElement('div');
        loading.textContent = 'Cargando imagen...';
        loading.style.cssText = 'color: white; font-size: 1.2em;';
        modal.appendChild(loading);
        
        img.onload = () => {
            modal.removeChild(loading);
            modal.appendChild(img);
        };
        
        img.onerror = () => {
            loading.textContent = 'Error al cargar la imagen';
            loading.style.color = '#ff6b6b';
        };
        
        document.body.appendChild(modal);
        
        // Cerrar eventos
        modal.onclick = () => document.body.removeChild(modal);
        
        const closeHandler = (e) => {
            if (e.key === 'Escape' && document.body.contains(modal)) {
                document.body.removeChild(modal);
                document.removeEventListener('keydown', closeHandler);
            }
        };
        document.addEventListener('keydown', closeHandler);
    }
    
    /**
     * Funciones de CRUD
     */
    editRecord(type, id) {
        this.openModal(type, id);
    }
    
    async deleteRecord(type, id) {
        if (!confirm(`¿Estás seguro de que quieres eliminar este ${type}?`)) {
            return;
        }
        
        try {
            await this.apiCall(`/${type}/${id}`, { method: 'DELETE' });
            
            window.showNotification(`${type.charAt(0).toUpperCase() + type.slice(1)} eliminado correctamente`, 'success');
            await this.loadAllData();
            
        } catch (error) {
            console.error('Error eliminando registro:', error);
            this.showError(`Error al eliminar ${type}`);
        }
    }
    
    /**
     * Funciones de datos de prueba
     */
    async loadTestData() {
        try {
            const testArboles = [
                { 
                    a: 'Proyecto Alpha', 
                    b: 'Desarrollo', 
                    c: 'Activo', 
                    d: 'Web', 
                    e: 'React', 
                    f: 'Frontend', 
                    af: 85.5, 
                    bf: 92.3, 
                    cf: 78.9, 
                    ba: true, 
                    bb: false, 
                    bc: true 
                },
                { 
                    a: 'Proyecto Beta', 
                    b: 'Testing', 
                    c: 'Completado', 
                    d: 'API', 
                    e: 'Node.js', 
                    f: 'Backend', 
                    af: 95.0, 
                    bf: 88.7, 
                    cf: 91.2, 
                    ba: true, 
                    bb: true, 
                    bc: false 
                }
            ];
            
            for (const arbol of testArboles) {
                arbol.usuarioId = window.APP_CONFIG.USUARIO_ID;
                arbol.calendario = new Date().toISOString();
                
                await this.apiCall('/arbol/registrar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(arbol)
                });
            }
            
            window.showNotification('Datos de prueba cargados exitosamente', 'success');
            await this.loadAllData();
            
        } catch (error) {
            console.error('Error:', error);
            this.showError('Error al cargar datos de prueba');
        }
    }
    
    async clearAllData() {
        try {
            // Eliminar en orden: hojas, ramas, árboles
            for (const hoja of this.data.hojas) {
                await this.apiCall(`/hoja/${hoja.id}`, { method: 'DELETE' });
            }
            
            for (const rama of this.data.ramas) {
                await this.apiCall(`/rama/${rama.id}`, { method: 'DELETE' });
            }
            
            for (const arbol of this.data.arboles) {
                await this.apiCall(`/arbol/${arbol.id}`, { method: 'DELETE' });
            }
            
            window.showNotification('Todos los datos han sido eliminados', 'success');
            await this.loadAllData();
            
        } catch (error) {
            console.error('Error:', error);
            this.showError('Error al eliminar los datos');
        }
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
    
    setLoadingState(type, loading) {
        if (loading) {
            this.loadingStates.add(type);
        } else {
            this.loadingStates.delete(type);
        }
        
        // Actualizar UI si es necesario
        const container = document.getElementById(`${type}Table`);
        if (container && loading) {
            container.innerHTML = '<div class="loading">Cargando...</div>';
        }
    }
    
    clearSearchCache(type = null) {
        if (type) {
            for (const key of this.searchCache.keys()) {
                if (key.startsWith(`${type}:`)) {
                    this.searchCache.delete(key);
                }
            }
        } else {
            this.searchCache.clear();
        }
    }
    
    renderError(containerId, message) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `<div class="empty-state">${message}</div>`;
        }
    }
    
    showError(message) {
        if (window.showNotification) {
            window.showNotification(message, 'error');
        } else {
            console.error(message);
        }
    }
    
    formatDate(dateString) {
        if (window.formatDate) {
            return window.formatDate(dateString);
        }
        
        if (!dateString) return '-';
        
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '-';
        
        return date.toLocaleDateString('es-ES') + ' ' + 
               date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    }
    
    /**
     * Funciones del ciclo de vida
     */
    onPageHidden() {
        // Pausar actualizaciones o procesos costosos
    }
    
    onPageVisible() {
        // Reanudar si es necesario
    }
    
    cleanup() {
        this.searchCache.clear();
        this.loadingStates.clear();
    }
}

// Registrar módulo globalmente
window.ArbolesRamasHojas = ArbolesRamasHojas;

// Funciones globales para compatibilidad con HTML existente
window.openModal = function(type, editId = null) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.openModal(type, editId);
    }
};

window.closeModal = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.closeModal();
    }
};

window.editRecord = function(type, id) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.editRecord(type, id);
    }
};

window.deleteRecord = function(type, id) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.deleteRecord(type, id);
    }
};

window.searchArboles = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.searchArboles();
    }
};

window.searchRamas = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.searchRamas();
    }
};

window.searchHojas = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.searchHojas();
    }
};

window.loadTreeView = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.loadTreeView();
    }
};

// Funciones de fotos
window.previewPhoto = function(input) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        const event = { target: input };
        window.Dashboard.modules.arbolesRamasHojas.handlePhotoPreview(event);
    }
};

window.removePhoto = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.clearPhotoState();
    }
};

window.changePhoto = function() {
    const photoInput = document.getElementById('photoInput');
    if (photoInput) {
        photoInput.click();
    }
};

window.deleteCurrentPhoto = function() {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.currentModal.currentPhotoId = null;
        window.Dashboard.modules.arbolesRamasHojas.clearPhotoState();
    }
};

window.viewFullImage = function(fotoId) {
    if (window.Dashboard && window.Dashboard.modules && window.Dashboard.modules.arbolesRamasHojas) {
        window.Dashboard.modules.arbolesRamasHojas.viewFullImage(fotoId);
    }
};
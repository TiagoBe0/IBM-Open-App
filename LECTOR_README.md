# 📖 Lector de Libros Interactivo

## Descripción

Sistema completo de lectura interactiva con soporte para comentarios de usuarios. Diseñado específicamente para el Libro de Urantia, pero adaptable a cualquier libro estructurado.

## 🎯 Características

### Para Lectores
- ✅ **Navegación intuitiva** por documentos y secciones
- ✅ **Diseño moderno** estilo aplicaciones de lectura (Kindle, Medium)
- ✅ **Sistema de comentarios** por párrafo
- ✅ **Comentarios públicos o privados**
- ✅ **Edición y eliminación** de comentarios propios
- ✅ **Vista de comentarios** en tiempo real
- ✅ **Interfaz responsiva** para móviles y tablets
- ✅ **Acceso público** para lectura (sin necesidad de login)
- ✅ **Autenticación requerida** solo para comentar

### Para Desarrolladores
- ✅ **API REST completa** para gestión de contenido y comentarios
- ✅ **Base de datos PostgreSQL** para persistencia
- ✅ **Arquitectura limpia** (Controller-Service-Repository)
- ✅ **DTOs** para transferencia de datos
- ✅ **Seguridad integrada** con Spring Security
- ✅ **Manejo de errores** robusto

## 📁 Estructura del Proyecto

### Backend (Java/Spring Boot)

```
src/main/java/com/sbs/open_app/
├── entidades/
│   └── ComentarioLibro.java          # Entidad JPA para comentarios
├── repositorios/
│   └── ComentarioLibroRepositorio.java # Repositorio con queries personalizados
├── servicios/
│   └── LectorLibroService.java        # Lógica de negocio (lectura y comentarios)
├── controllers/
│   └── LectorLibroController.java     # API REST endpoints
├── dto/
│   └── ComentarioLibroDTO.java        # Data Transfer Object
└── config/
    └── SecurityConfig.java             # Configuración de seguridad (actualizada)
```

### Frontend

```
src/main/resources/
├── templates/
│   └── lector.html                    # Vista principal del lector
├── static/
│   └── js/
│       └── lector.js                  # JavaScript para interactividad
└── libro.txt                          # Archivo del libro (ejemplo incluido)
```

## 🚀 Instalación y Uso

### 1. Preparar el Archivo del Libro

El lector espera un archivo `libro.txt` en `src/main/resources/` con el siguiente formato:

```
DOCUMENTO 0

0:0.1
Texto del primer párrafo de la sección 0:0

0:0.2
Texto del segundo párrafo de la sección 0:0

0:1.1
Texto del primer párrafo de la sección 0:1

DOCUMENTO 1

1:0.1
Texto del primer párrafo del documento 1
```

**Formato:**
- `DOCUMENTO N` → Inicio de un nuevo documento
- `D:S.P` → Referencia: Documento:Sección.Párrafo
- Líneas de texto → Contenido del párrafo

### 2. Base de Datos

La tabla `comentarios_libro` se crea automáticamente con JPA:

```sql
CREATE TABLE comentarios_libro (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    numero_documento INTEGER NOT NULL,
    numero_seccion INTEGER NOT NULL,
    numero_parrafo INTEGER NOT NULL,
    contenido TEXT NOT NULL,
    es_publico BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
```

### 3. Configuración de Seguridad

El `SecurityConfig.java` ha sido actualizado para permitir:

**Acceso Público:**
- `/lector` → Página del lector
- `/api/lector/libro` → Obtener libro completo
- `/api/lector/seccion/**` → Obtener secciones
- `/api/lector/comentarios/**` → Ver comentarios

**Requiere Autenticación:**
- `POST /api/lector/comentario` → Crear comentario
- `PUT /api/lector/comentario/{id}` → Editar comentario
- `DELETE /api/lector/comentario/{id}` → Eliminar comentario

### 4. Acceder al Lector

Una vez que la aplicación esté corriendo:

1. Visita: `http://localhost:8080/lector`
2. Navega por documentos y secciones usando la barra lateral izquierda
3. Lee el contenido en el área central
4. Haz clic en 💬 en cualquier párrafo para agregar un comentario (requiere login)
5. Los comentarios aparecen en la barra lateral derecha

## 📡 API Endpoints

### Lectura (Público)

```http
GET /api/lector/libro
→ Obtiene el libro completo estructurado

GET /api/lector/seccion/{documento}/{seccion}
→ Obtiene una sección específica

GET /api/lector/comentarios/{documento}/{seccion}
→ Obtiene comentarios de una sección

GET /api/lector/comentarios/{documento}/{seccion}/{parrafo}
→ Obtiene comentarios de un párrafo específico
```

### Comentarios (Autenticado)

```http
POST /api/lector/comentario
Body: {
  "numeroDocumento": 0,
  "numeroSeccion": 0,
  "numeroParrafo": 1,
  "contenido": "Mi comentario",
  "esPublico": true
}
→ Crea un nuevo comentario

PUT /api/lector/comentario/{id}
Body: {
  "contenido": "Comentario actualizado"
}
→ Actualiza un comentario existente

DELETE /api/lector/comentario/{id}
→ Elimina un comentario

GET /api/lector/mis-comentarios
→ Obtiene todos los comentarios del usuario autenticado
```

## 🎨 Personalización

### Cambiar Estilos

Edita el `<style>` en `lector.html`:
- Colores del tema: Variables de gradiente (`#667eea`, `#764ba2`)
- Fuente de lectura: `font-family: 'Georgia'` (línea 14)
- Tamaño de letra: `.parrafo-texto { font-size: 1.1em }` (línea 160)

### Adaptar a Otro Libro

1. Cambia el archivo `libro.txt` con tu contenido
2. Asegúrate de mantener el formato `D:S.P` para las referencias
3. Actualiza el título en `lector.html` (línea 6 y 227)

### Agregar Funcionalidades

El código está estructurado para fácil extensión:
- **Búsqueda:** Agrega un endpoint de búsqueda en `LectorLibroService`
- **Marcadores:** Crea una nueva entidad `MarcadorLibro`
- **Notas:** Extiende `ComentarioLibro` con campo `tipo`
- **Compartir:** Agrega endpoints de exportación

## 🔧 Troubleshooting

### El libro no carga
- ✅ Verifica que `libro.txt` esté en `src/main/resources/`
- ✅ Verifica que el formato sea correcto (referencias `D:S.P`)
- ✅ Revisa los logs del servidor para errores de parsing

### Los comentarios no aparecen
- ✅ Verifica que estés autenticado
- ✅ Verifica que los comentarios sean públicos (`esPublico = true`)
- ✅ Revisa la consola del navegador (F12) para errores JavaScript

### Error 403 al comentar
- ✅ Asegúrate de haber iniciado sesión
- ✅ Verifica que el `SecurityConfig` permita los endpoints

### Error de base de datos
- ✅ Verifica que PostgreSQL esté corriendo
- ✅ Verifica las credenciales en `application.properties`
- ✅ Asegúrate de que la tabla `usuarios` exista (requerida por FK)

## 📝 Ejemplo de Uso

### Desde el Dashboard

```javascript
// Los usuarios pueden acceder desde el dashboard
// Botón "Leer Libro" en la sección de perfil
```

### Navegación Programática

```javascript
// JavaScript
cargarSeccion(0, 0);  // Carga documento 0, sección 0
verComentariosParrafo(1);  // Ver comentarios del párrafo 1
```

### API desde otro cliente

```bash
# Obtener el libro
curl http://localhost:8080/api/lector/libro

# Obtener una sección
curl http://localhost:8080/api/lector/seccion/0/0

# Ver comentarios (público)
curl http://localhost:8080/api/lector/comentarios/0/0

# Agregar comentario (requiere sesión)
curl -X POST http://localhost:8080/api/lector/comentario \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '{
    "numeroDocumento": 0,
    "numeroSeccion": 0,
    "numeroParrafo": 1,
    "contenido": "Excelente explicación",
    "esPublico": true
  }'
```

## 🌟 Mejoras Futuras

- [ ] Búsqueda de texto completo
- [ ] Marcadores y favoritos
- [ ] Compartir secciones por enlace
- [ ] Sistema de votación para comentarios
- [ ] Resaltado de texto
- [ ] Notas privadas
- [ ] Exportación a PDF
- [ ] Modo lectura nocturna
- [ ] Cambio de tamaño de fuente
- [ ] Audio-libro sincronizado
- [ ] Traducción de comentarios

## 📄 Licencia

Este código es parte del proyecto IBM-Open-App.

## 👥 Contribuciones

Para contribuir:
1. Crea un fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

---

**Desarrollado con ❤️ para la Comunidad Urantia**

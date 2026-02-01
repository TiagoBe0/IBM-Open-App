# 📚 Instrucciones para Agregar el Libro de Urantia

## Pasos para Copiar los Archivos HTML

1. **Descarga los archivos HTML del Libro de Urantia** desde tu fuente (web, disco local, etc.)

2. **Copia TODOS los archivos HTML** a la siguiente carpeta del proyecto:
   ```
   src/main/resources/templates/libro/
   ```

3. **Los archivos deben incluir:**
   - `00-Página-preliminar.html` (opcional)
   - `01-Partes.html`
   - `02-Indice.html`
   - `03-Contenido.html`
   - `04-Documento000.html` (Prólogo)
   - `05-Parte1.html`
   - `06-Documento001.html` hasta `06-Documento031.html` (Parte I)
   - `07-Parte2.html`
   - `08-Documento032.html` hasta `08-Documento056.html` (Parte II)
   - `09-Parte3.html`
   - `10-Documento057.html` hasta `10-Documento119.html` (Parte III)
   - `11-Parte4.html`
   - `12-Documento120.html` hasta `12-Documento196.html` (Parte IV)

4. **También copia el archivo CSS** (si existe):
   ```
   src/main/resources/static/css/ub.css
   ```

## Estructura Esperada

Después de copiar, la estructura debería verse así:

```
src/main/resources/
├── templates/
│   └── libro/
│       ├── 00-Página-preliminar.html
│       ├── 01-Partes.html
│       ├── 02-Indice.html
│       ├── 03-Contenido.html
│       ├── 04-Documento000.html
│       ├── 05-Parte1.html
│       ├── 06-Documento001.html
│       ├── 06-Documento002.html
│       ├── ... (todos los documentos)
│       └── 12-Documento196.html
└── static/
    └── css/
        └── ub.css (opcional)
```

## Comandos para Copiar (Linux/Mac)

Si tienes los archivos en una carpeta local, usa:

```bash
# Desde la carpeta donde descargaste el libro
cp *.html /home/user/IBM-Open-App/src/main/resources/templates/libro/

# Si también tienes el CSS
cp ub.css /home/user/IBM-Open-App/src/main/resources/static/css/
```

## Script de Importación (recomendado)

También puedes usar el script incluido en el repositorio:

```bash
./scripts/import_libro_html.sh /ruta/a/los/html /ruta/a/ub.css
```

- El segundo parámetro (CSS) es opcional. Si existe un `ub.css` en la carpeta de origen, el script lo copiará automáticamente.

## Formato de los Archivos HTML

Cada documento debe tener este formato básico:

```html
<!DOCTYPE html>
<html lang="es-US">
<head>
    <title>Documento N - Título</title>
    <!-- ... -->
</head>
<body>
    <div class="container">
        <p class="ctr">
            <span class="pr">0:0.1</span>
            Texto del párrafo...
        </p>
        <!-- Más párrafos -->
    </div>
</body>
</html>
```

## Verificación

Una vez copiados los archivos, verifica que estén en su lugar:

```bash
ls -la src/main/resources/templates/libro/
```

Deberías ver aproximadamente **200 archivos HTML**.

## ¿Qué Hace el Lector?

El servicio `LectorLibroService` automáticamente:
1. Lee los archivos HTML de la carpeta `libro/`
2. Parsea el contenido usando JSoup
3. Extrae los párrafos con sus referencias (D:S.P)
4. Organiza todo por documentos y secciones
5. Sirve el contenido a través de la API REST

## Próximos Pasos

Después de copiar los archivos:
1. Reinicia la aplicación Spring Boot
2. Navega a `http://localhost:8080/lector`
3. ¡Disfruta leyendo el Libro de Urantia con comentarios!

---

**Nota:** Si no tienes los archivos HTML, puedes descargarlos desde:
- [Urantia Foundation](https://www.urantia.org/es)
- O desde el repositorio oficial del libro en español

#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 /ruta/a/html [ruta/a/ub.css]" >&2
  exit 1
fi

SOURCE_DIR="$1"
CSS_SOURCE="${2:-}"
TARGET_DIR="src/main/resources/templates/libro"
CSS_TARGET="src/main/resources/static/css"

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "No existe el directorio de origen: $SOURCE_DIR" >&2
  exit 1
fi

mkdir -p "$TARGET_DIR"
mkdir -p "$CSS_TARGET"

shopt -s nullglob
HTML_FILES=("$SOURCE_DIR"/*.html)

if [[ ${#HTML_FILES[@]} -eq 0 ]]; then
  echo "No se encontraron archivos .html en $SOURCE_DIR" >&2
  exit 1
fi

cp "${HTML_FILES[@]}" "$TARGET_DIR"/

if [[ -n "$CSS_SOURCE" ]]; then
  if [[ -f "$CSS_SOURCE" ]]; then
    cp "$CSS_SOURCE" "$CSS_TARGET"/ub.css
  else
    echo "No se encontró el archivo CSS indicado: $CSS_SOURCE" >&2
    exit 1
  fi
elif [[ -f "$SOURCE_DIR/ub.css" ]]; then
  cp "$SOURCE_DIR/ub.css" "$CSS_TARGET"/ub.css
fi

COUNT=$(ls -1 "$TARGET_DIR"/*.html | wc -l | tr -d ' ')

cat <<EOF
✅ Copiados $COUNT archivos HTML a $TARGET_DIR
EOF

if [[ -f "$CSS_TARGET/ub.css" ]]; then
  echo "✅ ub.css copiado a $CSS_TARGET/ub.css"
fi

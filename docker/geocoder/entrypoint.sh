#!/bin/sh
set -eu

# Archivo de configuracion del Geocoder
CONFIG_FILE="/usr/local/tomcat/webapps/geocoder/WEB-INF/classes/configuration.properties"

# Variables recibidas desde Docker
: "${ELASTIC_URL:?ERROR: Debes configurar ELASTIC_URL}"
ELASTIC_USER="${ELASTIC_USER:-}"
ELASTIC_PASS="${ELASTIC_PASS:-}"

# Comprobar que existe el archivo
if [ ! -f "$CONFIG_FILE" ]; then
    echo "ERROR: No existe configuration.properties"
    exit 1
fi

# Escapar caracteres especiales para sed
escape_sed() {
    printf '%s' "$1" | sed 's/[\\&|]/\\&/g'
}

# Actualizar las propiedades
sed -i "s|^elastic_url=.*|elastic_url=$(escape_sed "$ELASTIC_URL")|" "$CONFIG_FILE"
sed -i "s|^elastic_user=.*|elastic_user=$(escape_sed "$ELASTIC_USER")|" "$CONFIG_FILE"
sed -i "s|^elastic_pass=.*|elastic_pass=$(escape_sed "$ELASTIC_PASS")|" "$CONFIG_FILE"

echo "Configuracion Elasticsearch preparada."
echo "Servidor: $ELASTIC_URL"

# Arrancar Tomcat
exec catalina.sh run

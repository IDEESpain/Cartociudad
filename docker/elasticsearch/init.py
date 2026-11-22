#!/usr/bin/env python3
"""
Inicialización de Elasticsearch para CartoCiudad.

- Crea los índices con sus mappings originales.
- Carga los datos de ejemplo de Humanes de Madrid.
- No modifica índices que ya contienen documentos.
- Comprueba los errores devueltos por Elasticsearch.
"""

import json
import os
import sys
import time
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

ELASTIC_URL = os.environ.get(
    "ELASTIC_URL",
    "http://elasticsearch:9200"
).rstrip("/")

DATA_DIR = Path("/data")

INDICES = [
    "codigo_postal",
    "division_administrativa",
    "portal_pk",
    "toponimo",
    "vial",
]


def request(method, path, data=None, content_type="application/json"):
    """Realiza una petición HTTP a Elasticsearch."""

    headers = {"Content-Type": content_type}

    req = Request(
        ELASTIC_URL + path,
        data=data,
        headers=headers,
        method=method,
    )

    try:
        with urlopen(req, timeout=120) as response:
            content = response.read()
            return json.loads(content) if content else {}

    except HTTPError as error:
        details = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(
            f"HTTP {error.code} en {method} {path}: {details[:1500]}"
        ) from error


def esperar_elasticsearch():
    """Espera hasta que Elasticsearch responda."""

    print("Esperando a Elasticsearch...", flush=True)

    for intento in range(60):
        try:
            request("GET", "/")
            print("Elasticsearch disponible.", flush=True)
            return
        except (URLError, TimeoutError, RuntimeError):
            time.sleep(2)

    raise RuntimeError("Elasticsearch no responde.")


def existe_indice(indice):
    try:
        request("GET", f"/{indice}")
        return True
    except RuntimeError as error:
        if "HTTP 404" in str(error):
            return False
        raise


def contar_documentos(indice):
    resultado = request("GET", f"/{indice}/_count")
    return resultado["count"]


def crear_indice(indice):
    ruta = (
        DATA_DIR / "mappings" / indice /
        f"{indice}_mapping.json"
    )

    mapping = ruta.read_bytes()

    request("PUT", f"/{indice}", mapping)

    print(f"Índice creado: {indice}", flush=True)


def cargar_datos(indice):
    carpeta = DATA_DIR / "sample_data" / indice

    archivos = sorted(carpeta.glob("bulk_*.json"))

    if not archivos:
        raise RuntimeError(
            f"No hay archivos de datos para {indice}"
        )

    for archivo in archivos:
        print(f"  Cargando {archivo.name}...", flush=True)

        datos = archivo.read_bytes()

        # Elasticsearch Bulk API requiere salto de línea final.
        if not datos.endswith(b"\n"):
            datos += b"\n"

        respuesta = request(
            "POST",
            "/_bulk?refresh=true",
            datos,
            "application/x-ndjson",
        )

        if respuesta.get("errors"):
            errores = [
                item
                for item in respuesta.get("items", [])
                if list(item.values())[0].get("error")
            ]

            raise RuntimeError(
                f"Errores en {archivo.name}: "
                f"{json.dumps(errores[:3], ensure_ascii=False)}"
            )

        print(f"  Archivo cargado: {archivo.name}", flush=True)


def inicializar():
    esperar_elasticsearch()

    for indice in INDICES:
        print(f"\nProcesando índice: {indice}", flush=True)

        if not existe_indice(indice):
            crear_indice(indice)

        documentos = contar_documentos(indice)

        if documentos > 0:
            print(
                f"Índice existente con {documentos} documentos. "
                "Se conserva sin modificaciones.",
                flush=True,
            )
            continue

        cargar_datos(indice)

        documentos = contar_documentos(indice)

        print(
            f"Índice {indice}: {documentos} documentos.",
            flush=True,
        )

    print(
        "\nInicialización de CartoCiudad completada.",
        flush=True,
    )


if __name__ == "__main__":
    try:
        inicializar()
    except Exception as error:
        print(f"\nERROR: {error}", file=sys.stderr, flush=True)
        sys.exit(1)

# Servicio REST Geocoder CartoCiudad
## ✍️ Descripción
Servicio REST Geocoder proporciona funcionalidades de búsqueda y geolocalización sobre el conjunto de datos de entidades poblacionales, unidades administrativas, viales, portales, puntos kilométricos, puntos de interés (POI), topónimos, códigos postales y referencias Catastrales (Servicio SOAP de la Dirección General de Catastro [D.G. Catastro]). Las fuentes de datos de estos elementos, se encuenta [aquí](https://www.cartociudad.es/web/portal/fuentes-oficiales)

Este servicio funciona sobre Elasticsearch, que ejerce de motor de persistencia y búsqueda.
Geocoder sustenta las operaciones de búsqueda de [Cartociudad](https://www.cartociudad.es) y se dispone de documentación oficial sobre los servicios [aquí](https://www.idee.es/resources/documentos/Cartociudad/CartoCiudad_ServiciosWeb.pdf) 


## 🐳 Despliegue con Docker

La dockerización del Servicio Rest Geocoder v1.1.0 se mantiene en la rama [`release/1.x`](https://github.com/IDEESpain/Cartociudad/tree/release/1.x) del repositorio oficial.

Esta rama permite mantener la versión 1 del Geocoder en paralelo al desarrollo de futuras versiones del servicio REST.

El despliegue está disponible en dos modalidades:

- **Local:** Geocoder y Elasticsearch 7.17.25, con creación automática de índices y carga de datos de ejemplo.
- **Externa:** Geocoder conectado a un Elasticsearch existente, con sus índices previamente creados y cargados.

La configuración está orientada a **entornos de desarrollo y pruebas**.

### 1. Requisitos previos

Para ambas modalidades es necesario disponer de:

- [Git](https://git-scm.com/).
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) o Docker Engine con Docker Compose.

Comprobar que Docker está instalado y funcionando:

```bash
docker --version
docker compose version
```

### 2. Descargar el repositorio

Clonar el repositorio oficial de CartoCiudad:

```bash
git clone https://github.com/IDEESpain/Cartociudad.git
cd Cartociudad
```

Seleccionar la rama de mantenimiento de la versión 1, que incorpora el despliegue Docker:

```bash
git switch release/1.x
```

Alternativamente, para utilizar exactamente la distribución Docker publicada, se puede descargar la Release `v1.1.0-docker` desde:

[Releases de CartoCiudad](https://github.com/IDEESpain/Cartociudad/releases)

O seleccionar su etiqueta Git:

```bash
git checkout tags/v1.1.0-docker
```

> **Nota:** la rama `main` contiene actualmente la versión original del Geocoder v1.1.0, mientras que `release/1.x` incorpora la dockerización y permite mantener futuras correcciones compatibles con esta versión.


---

### 🟢 Opción A. Despliegue local con Elasticsearch y datos de ejemplo

Esta es la opción recomendada para probar el proyecto por primera vez.

Docker Compose se encarga de:

1. Construir la aplicación Geocoder utilizando Java 8 y Tomcat 9.
2. Descargar e iniciar Elasticsearch 7.17.25.
3. Crear automáticamente la red Docker y el volumen persistente.
4. Esperar a que Elasticsearch esté disponible.
5. Crear los cinco índices utilizando los mappings originales.
6. Cargar los archivos JSON de ejemplo de Humanes de Madrid.
7. Iniciar el Geocoder cuando haya finalizado correctamente la indexación.

**No es necesario crear manualmente los índices, configurar Elasticsearch ni instalar Python.**

#### A.1. Construir y arrancar

Desde el directorio raíz del repositorio, ejecutar:

```bash
docker compose up -d --build
```

La primera ejecución puede tardar más debido a la descarga de imágenes y dependencias Java.

#### A.2. Comprobar los contenedores

```bash
docker compose ps -a
```

El despliegue está preparado cuando se observa:

| Servicio | Estado esperado |
|---|---|
| `elasticsearch` | Up (healthy) |
| `es-init` | Exited (0) |
| `geocoder` | Up |

El contenedor `es-init` ejecuta automáticamente la carga de datos y finaliza. **Su estado `Exited (0)` es correcto y no representa un error.**

Para revisar la inicialización:

```bash
docker compose logs es-init
```

#### A.3. Acceder al Geocoder

Una vez iniciado el servicio:

**Aplicación web:**

http://localhost:8080/geocoder/

**Ejemplo de petición REST Candidates:**

http://localhost:8080/geocoder/api/geocoder/candidates?q=Humanes%20de%20Madrid

También puede probarse desde un terminal:

```bash
curl "http://localhost:8080/geocoder/api/geocoder/candidates?q=Humanes%20de%20Madrid"
```

El servicio devuelve un JSON con los candidatos encontrados.

#### A.4. Consultar Elasticsearch

Elasticsearch está disponible localmente en:

http://localhost:9201

Para consultar los índices:

```bash
curl "http://localhost:9201/_cat/indices?v"
```

El despliegue local crea los siguientes índices con los datos de ejemplo:

| Índice | Documentos |
|---|---:|
| `codigo_postal` | 1 |
| `division_administrativa` | 3 |
| `portal_pk` | 5.809 |
| `toponimo` | 119 |
| `vial` | 289 |
| **Total** | **6.221** |

Los datos proceden de los archivos incluidos en `src/main/resources/elasticsearch/sample_data/`.

Los mappings y analizadores se obtienen de `src/main/resources/elasticsearch/mappings/` y `src/main/resources/elasticsearch/configuration/`.

#### A.5. Detener y reiniciar

Para detener los servicios:

```bash
docker compose stop
```

Para volver a iniciarlos:

```bash
docker compose start
```

Para consultar los registros:

```bash
docker compose logs -f geocoder
```

Para eliminar los contenedores y la red del proyecto:

```bash
docker compose down
```

**Persistencia:** los índices se almacenan en el volumen Docker `cartociudad_es_data` y se conservan entre reinicios y recreaciones de contenedores.

No utilizar `docker compose down -v` si se desean conservar los datos.

---

### 🔵 Opción B. Despliegue con Elasticsearch externo

Esta modalidad está destinada a organizaciones que ya disponen de una instalación Elasticsearch con los índices de CartoCiudad configurados y cargados.

En este caso, **Docker únicamente construye y ejecuta el Geocoder**.

No se arranca Elasticsearch local, no se crea ningún índice y no se ejecuta el inicializador `es-init`.

Se utiliza el archivo `compose.remote.yaml`.

#### B.1. Requisitos específicos

Antes del despliegue hay que comprobar que:

- Existe un servidor Elasticsearch accesible desde el contenedor Docker.
- Los índices requeridos por el Geocoder ya están creados y contienen datos.
- Los mappings y analizadores son compatibles con el Geocoder.
- La versión y las API de Elasticsearch son compatibles con esta aplicación.
- Se dispone de las credenciales necesarias, si el servidor requiere autenticación.

La configuración local se ha validado con Elasticsearch **7.17.25**. Otras versiones deben comprobarse antes de utilizarlas.

#### B.2. Configurar la conexión

El Geocoder admite las siguientes variables de entorno:

| Variable | Descripción | Obligatoria |
|---|---|---|
| `ELASTIC_URL` | Dirección HTTP o HTTPS del servidor Elasticsearch, incluyendo el puerto. | Sí |
| `ELASTIC_USER` | Usuario de Elasticsearch si se utiliza autenticación básica. | Solo cuando sea necesario |
| `ELASTIC_PASS` | Contraseña del usuario de Elasticsearch. | Solo cuando sea necesario |

En **Windows PowerShell**, configurar la dirección:

```powershell
$env:ELASTIC_URL = "http://servidor-elasticsearch:9200"
```

Si el servidor requiere autenticación básica, configurar también:

```powershell
$env:ELASTIC_USER = "usuario"
$env:ELASTIC_PASS = "contraseña"
```

En **Linux o macOS**, las variables pueden definirse mediante:

```bash
export ELASTIC_URL="http://servidor-elasticsearch:9200"
```

Y, si se necesita autenticación:

```bash
export ELASTIC_USER="usuario"
export ELASTIC_PASS="contraseña"
```

Las direcciones y credenciales anteriores son ejemplos y deben sustituirse por las correspondientes a la instalación.

**Seguridad:** no incluir contraseñas, tokens ni direcciones internas sensibles en archivos versionados o publicados en GitHub. Para entornos corporativos, utilizar preferentemente mecanismos de gestión de secretos. Las variables de entorno pueden resultar visibles en la configuración del contenedor.

#### B.3. Construir y arrancar únicamente el Geocoder

Ejecutar:

```bash
docker compose -p cartociudad-remoto -f compose.remote.yaml up -d --build
```

Se utiliza el nombre de proyecto `cartociudad-remoto` para mantener este despliegue independiente del local.

No se iniciará ningún contenedor Elasticsearch.

#### B.4. Comprobar el funcionamiento

Consultar el estado:

```bash
docker compose -p cartociudad-remoto -f compose.remote.yaml ps
```

Consultar los registros:

```bash
docker compose -p cartociudad-remoto -f compose.remote.yaml logs -f geocoder
```

El Geocoder remoto se publica, por defecto, en el puerto **8082**:

**Aplicación web:**

http://localhost:8082/geocoder/

**Servicio REST Candidates:**

http://localhost:8082/geocoder/api/geocoder/candidates?q=Humanes%20de%20Madrid

Los resultados dependerán de los datos disponibles en el Elasticsearch externo.

> **Nota:** el modo externo dispone de una configuración Docker Compose validada, pero debe comprobarse su funcionamiento con el servidor Elasticsearch de destino, incluyendo conectividad, autenticación y compatibilidad.

#### B.5. Detener el servicio remoto

```bash
docker compose -p cartociudad-remoto -f compose.remote.yaml down
```

Este comando elimina el contenedor y la red Docker de esta modalidad. **No elimina ni modifica los índices almacenados en el Elasticsearch externo.**

---

### 📁 Archivos utilizados en el despliegue

| Archivo | Descripción |
|---|---|
| `compose.yaml` | Despliegue local completo: Elasticsearch, inicializador y Geocoder. |
| `compose.remote.yaml` | Despliegue del Geocoder conectado a Elasticsearch externo. |
| `docker/geocoder/Dockerfile` | Construcción de la imagen Docker del Geocoder. |
| `docker/geocoder/entrypoint.sh` | Configuración de la conexión a Elasticsearch al iniciar el contenedor. |
| `docker/elasticsearch/init.py` | Creación automática de índices y carga de datos de ejemplo. |
| `WebContent/WEB-INF/web.xml` | Configuración de Jersey y de la aplicación web. |
| `src/main/resources/elasticsearch/mappings/` | Definiciones de los índices Elasticsearch. |
| `src/main/resources/elasticsearch/configuration/` | Analizadores, palabras vacías y sinónimos. |
| `src/main/resources/elasticsearch/sample_data/` | Datos de ejemplo de Humanes de Madrid. |

### Consideraciones

- El modo local está pensado para pruebas con datos de ejemplo, no para representar la cobertura nacional completa de CartoCiudad.
- Los puertos se publican en `localhost`, de forma que los servicios no quedan expuestos directamente a la red.
- Elasticsearch local tiene desactivada la autenticación y está destinado exclusivamente a desarrollo y pruebas.
- El inicializador conserva los índices existentes que ya contienen documentos. No sustituye los procesos de actualización y gestión de datos de una instalación real.
- Los despliegues de producción requieren revisar la seguridad, la memoria, la persistencia, la disponibilidad y la compatibilidad de versiones.

---


---

## 📇 Contenidos <a name="contenidos"></a>
* 🐳 [Inicio rápido con Docker](#-inicio-rápido-con-docker)
* 🚂 [Funcionalidades de los servicios](#-funcionalidades-de-los-servicios)
* 🔍📍 [Búsqueda por nombre geográfico](#-búsqueda-por-nombre-geográfico)
* *  🔍 [1. Candidates](#1-candidates)
* *  📍 [2. Find](#-2-find)
* 🌍 [Búsqueda por coordenadas geográficas](#-búsqueda-por-coordenadas-geográficas)
* 📊 [Calculadora unificada de direcciones postales](#-calculadora-unificada-de-direcciones-postales)
* 📁 [Archivos de referencia](#--archivos-de-referencia)
* * 💻 [Configuración del servicio REST Geocoder](#-configuración-del-servicio-rest-geocoder)
* * 🛠️ [GeocodingServiceElasticCore.java - Servicio SOAP de la D.G de Catastro](#️-geocodingserviceelasticcorejava---servicio-soap-de-la-dg-de-catastro)
* * 🗺️ [Index.html - Visualizador propio del Geocoder](#️-indexhtml---visualizador-propio-del-geocoder)
* 🚀 [Despliegue](#-despliegue)


---

## 🚂 Funcionalidades de los servicios
[👆 Volver](#-contenidos)

El servicio REST geocoder tiene dos funcionalidades:
- **Búsqueda por nombre geográfico**: a partir de una búsqueda de un elemento geográfico el servicio devuelve como resultado sus correspondientes coordenadas geográficas.
  - Búsqueda por referencia catastral: en el propio código del geocoder se ha integrado el servicio SOAP de la D. G de Catastro  
- **Búsqueda por coordenadas geográficas**: a partir de unas coordenadas el servicio devuelve la dirección más próxima a dicho punto en un radio de 350 metros.

## 🔍📍 Búsqueda por nombre geográfico
[👆 Volver](#-contenidos)

Para obtener las coordenadas de un elemento geográfico hay que partir de dos métodos, uno seguido de otro:

### 🔍1. Candidates
[👆 Volver](#-contenidos)

El servicio a partir de una petición busca y devuelve un JSON con los resultados con similitud fonética al nombre geográfico buscado, junto con una serie de parámetros de información asociada. 

🔸**PETICIONES HTTP GET**:
- /geocoder/api/geocoder/candidates
- /geocoder/api/geocoder/candidatesJsonp

🔸**PARÁMETROS**: algunos oblitarorios y otros no:

- **q (obligatorio)**: Es el texto sobre el que se quiere buscar candidatos.
- **no_process** (opcional): Permite filtrar la búsqueda, eliminando de los posibles resultados:
    - Municipios: 'no_process=municipio'
    - Provincias: 'no_process=provincia’
    - Comunidades autónomas: 'no_process=comunidad autonoma'
    - Poblaciones: 'no_process=poblacion'
    - Topónimos: 'no_process=toponimo'
    - Expendeduría: 'no_process=expendeduria'
    - Puntos de recarga eléctrica: 'no_process=punto_recarga_electrica'
    - Topónimos orográficos procedentes del Nomenclador Geográfico Básico de España (NGBE): 'no_process=ngbe'
    - Direcciones:
        - Viales (urbana): ‘no_process=callejero’
        - Viales (interurbana): 'no_process=carretera'
        - Portales o puntos kilométricos: 'no_process=portal'
          También se puede hacer la combinación de todas o algunas de ellas.
*Ejemplo: No localizar las direcciones postales, ni municipios y que muestre todo elemento que tengan la mayor similitud fónetica a Salamanca.*
https://www.cartociudad.es/geocoder/api/geocoder/candidates?q=salamanca&limit=6&no_process=callejero,municipio
- **Filtros**: Se pueden hacer búsquedas de elementos que estén en un/unos códigos postales, unidades administrativas y entidades de población en concreto:
    - cod_postal_filter (opcional): permite realizar una búsqueda en un/unos códigos postales. Hay que poner un código postal seguido de otro con comas y sin espacios, ejemplo: 'cod_postal_filter=28003,28022'
    - municipio_filter (opcional): permite realizar una búsqueda en un/unos municipios. Hay que poner un municipio seguido de otro con comas y sin espacios, ejemplo:'municipio_filter=Madrid,Avilés
    - provincia_filter (opcional): permite realizar una búsqueda en una/unas provincias. Hay que poner una provincia seguida de otra con comas y sin espacios, ejemplo: 'provincia_filter=Lugo,Burgos'
    - comunidad_autonoma_filter (opcional): permite realizar una búsqueda en una/unas comunidades autónomas. Hay que poner una comunidad autónoma seguida de otra con comas y sin espacios, ejemplo: 'comunidad_autonoma_filter=Principado de Asturias,Andalucía'
    - poblacion_filter (opcional): permite realizar una búsqueda en una/unas poblaciones. Hay que poner una población seguida de otra con comas y sin espacios, ejemplo: 'poblacion_filter=Madrid'
      También se puede hacer la combinación de todas o algunas de ellas.
*Ejemplo: Buscar un colegio solo en un código postal:*
https://www.cartociudad.es/geocoder/api/geocoder/candidates?q=colegio%20miguel%20hernandez&cod_postal_filter=28100

** **Nota**: para que estos filtros funcionen correctamente hay que escribir los nombres de las unidades administrativas y poblaciones de la forma oficial; es decir, como se tiene en
CartoCiudad. Así, si se tiene duda se puede hacer primero una consulta al candidates del nombre del municipio, por ejemplo, y a continuación hacer la petición con el filtro de municipios.También se tiene esta información pública en el Centro de Descargas del Centro Nacional de Información Geográfica, en [*Contenido Auxiliar*](https://centrodedescargas.cnig.es/CentroDescargas/documentos/Cartociudad_documentacion.zip).

- **countrycodes** (opcional): identificador del país (por defecto 'es').
- **limit** (opcional): Número máximo de coincidencias o resultados próximos a la consulta que se devolverán. Por defecto son 33, si se quieren menos hay que indicar con limit cuantos

🔸**Lógica básica de consulta de candidatos**

Es conveniente aclarar cómo funciona la búsqueda de candidatos en cuanto a la cadena de búsqueda:
- Si la cadena de búsqueda no contiene número, se busca sobre Divisiones Administrativas (Poblaciones, Municipios, Provincias, Comunidades Autónomas), Viales, Carreteras y Topónimos
- Si la cadena de búsqueda contiene algún número, se busca sobre Divisiones Administrativas (Poblaciones, Municipios, Provincias, Comunidades Autónomas), Viales, Carreteras, Topónimos, **Portales y Puntos Kilométricos**
- Si la cadena solo contiene números y son de la longitud adecuada, se buscan Códigos Postales

Es importante comentar el **orden intrínseco de las tipologías, y el número de registros de cada tipología**, siendo lo siguiente:
1. Poblaciones: 2 registros
2. Municipio:  3 registros
3. Callejero (viales urbanos): 7 registros
4. Carretera (viales interurbana): 4 registros
5. Portales y puntos kilométricos: 6 registros
6. Puntos de interés: 4 registros
7. Expendedurías (procedentes de Comisión de Tabacos): 2 registros
8. Puntos de recarga eléctrica (procedentes del Geoportal de Hidrocarburos): 2 registros
9. Topónimos (procedentes del Nomenclátor Geográfico Básico de España): 3 registros
10. Provincias: 1 registro
11. Comunidades autónomas: 1 registro

🔹**RESPUESTA**

El servicio devuelve un fichero JSON con los resultados más parecidos fonéticamente al elemento búscado el parámetro *q* de la petición HTTP GET, con una serie de parámetros de información:
- id: Identificador de la referencia.
- type: Tipo de entidad. Los valores pueden ser 'callejero' (viales urbanos), 'portal' (portal o punto kilométrico), 'carreteras' (viales interurbanos), 'Codpost' (código postal), 'municipio','provincia', 'comunidad autonoma', 'toponimo', 'poblacion', 'expendeduría', 'punto_recarga_electrica', 'ngbe' y 'refcatastral'.
- address: Texto completo del nombre de los resultados.
- tip_via: Especifica el tipo de vía
- portalNumber: Número de portal o punto kilométrico (si se especifica en la consulta).
- noNumber: su valor puede ser “true” cuando el portal encontrado tiene como número S-N, o “false” cuando se esté buscando un número de portal distinto a S-N.
- extension: Extensión del número del portal
- muni: Municipio al que pertenece (si corresponde al tipo de entidad).
- muniCode: Código del municipio
- province: Provincia a la que pertenece (si corresponde).
- provinceCode: Código de la provincia a la que pertenece.
- comunidadAutonoma: Comunidad Autónoma a la que pertenece (si corresponde)
- comunidadAutonomaCode: Código de la Comunidad Autónoma a la que pertenece.
- poblacion: Población a la que pertenece (si corresponde)
- postalCode: Código postal (si corresponde).
- countryCode: Código del país (por defecto '011' para España).
- refCatastral: Referencia catastral (si corresponde).
- lat: Coordenada geográfica (en WGS84; EPSG:4326) que representa la latitud de la entidad de los elementos puntuales(portales, puntos kilométricos, puntos de interés y topónimos).
- lng: Coordenada geográfica (en WGS84; EPSG:4326) que representa la longitud de la entidad de los elementos puntuales (portales, puntos kilométricos, puntos de interés y topónimos).
- geom: no disponible con esta petición.
- state: 0 (este valor con la versión actual del geocoder, se ha suprimido, ya que se empleaelasticsearch y no se puede configurar la salida de candidates según grado de coincidencia).
- stateMsg: Vacío (este valor con la versión actual del geocoder, se ha suprimido, ya que se emplea elasticsearch y no se puede configurar la salida candidates según grado de coincidencia).

coordenadas 
---
### 📍 2. Find
[👆 Volver](#-contenidos)

El método *find* permite geolocalizar el elemento elegido de la petición anterior *candidates* y obtener sus coordenadas geográficas. En este caso además de obtener coordenas de elementos puntuales, como en el método *candidates* también se obtienen coordenadas de los elementos lineales y superficiales.

🔸**PETICIONES HTTP GET**:
- /geocoder/api/geocoder/find
- /geocoder/api/geocoder/findJsonp
  
La petición puede ser invocada de dos formas diferentes, haciendo variar así los parámetros de entrada de esta:

 **A. Petición a través de texto libre, parámetro necesario:**. Los parámetros necesarios son:
- **q (obligatorio)**: Se realizará primero una petición a *candidates* y devolverá la geometría de la primera coincidencia.
- **outputformat** (opcional): Permite escoger el formato de salida de los datos. Por defecto devolverá un JSON, y, si se especifica 'outputformat=geojson', será un GeoJSON.

 *Ejemplo de petición: Calle Iglesia y en concreto el portal 5 y en formato GeoJSON: https://www.cartociudad.es/geocoder/api/geocoder/find?q=calle%20iglesia%205,%20madrid&outputformat=geoJson*

**B. Petición con los datos de una entidad concreta**. Los parámetros necesarios son:
- **id (obligatorio)**: Identificador univoco de la entidad.
- **type (obligatorio)**: Tipo de entidad. Los valores pueden ser 'callejero' (viales urbanos), 'portal' (portal o punto kilométrico), 'carreteras' (viales interurbanos), 'Codpost' (código postal), 'municipio', 'provincia', 'comunidad autonoma',
'toponimo', 'poblacion', 'expendeduría', 'punto_recarga_electrica', 'ngbe' y 'refcatastral'.
- **portal** (opcional): Permite indicar el portal o punto kilométrico del vial referenciado por su id.
- **outputformat** (opcional): Permite escoger el formato de salida de los datos. Por defecto devolverá un JSON, y, si se especifica 'outputformat=geojson', será un GeoJSON.

🔹**RESPUESTA**

El servicio devuelve un fichero JSON o GeoJSON con el resultado geolocalizado y con los mismo parámetros que la respuesta *candidates* pero incluyendo las coordenadas geográficas correspondientes del elemento.

## 🌍 Búsqueda por coordenadas geográficas
[👆 Volver](#-contenidos)

A partir de unas coordenadas geográficas (latitud y longitud en WGS84; EPSG:4326) el servicio devuelve la dirección más próxima a dicho punto en un radio de 350 metros, elemento parametrizable  (reverse_buffer) en el archivo de [*configuration.properties*](#-configuración-del-servicio-rest-geocoder).



🔸**PETICIÓN HTTP GET**:
- /geocoder/api/geocoder/reverseGeocode

🔸**PARÁMETROS**: 
- lon (obligatoria): Coordenada que representa la longitud
- lat (obligatoria): Coordenada que representa la latitud

*Ejemplo: http://www.cartociudad.es/geocoder/api/geocoder/reverseGeocode?lon=-1.371939&lat=41.487733*

🔹**RESPUESTA**
Devuelve un JSON  con los mismo parámetros que en los casos anteriores: *candidates* y en el *find*

** **NOTA**: los campos longitud y latitud que se devuelven no son los que se muestran como parámetros de entrada en la petición, sino los correspondientes a la entidad que se devuelve en el resultado.

## 📊 Calculadora unificada de direcciones postales
[👆 Volver](#-contenidos)

Esta funcionalidad la integra el geocoder, y se encuentra accesible desde [aquí](https://www.cartociudad.es/web/portal/herramientas-calculos/conversor) implementa, en el mismo servicio, la geocodificación por nombre o por coordenadas geográficas de forma masiva y según un fichero CSV de entrada.
Se ha establecido que pueda procesar hasta 60.000 registros, pero esto es configurable (unified_max_rows).

Tanto los requisitos del CSV como su funcionamiento, se encuentran [aquí](https://www.idee.es/resources/documentos/Cartociudad/Instrucciones_conversor.pdf)

🔸**PETICIÓN HTTP POST**:
- /geocoder/api/geocoder/unifiedcsvgeocoding

Envío de un CSV con los elementos a solicitar para su geolocalización.

🔹**RESPUESTA**

Devuelve un CSV con la misma cabecera que el envíado, añadiendo un nuevo campo OBSERVACIONES, que estable una observación de la búsqueda. Este CSV contiene los elementos geolocalizados con sus parámetros correspondientes.

## 📁  Archivos de referencia
A continuación se mencionan los archivos importantes del servicio REST Geocoder y del *ElasticSearch*.

### 💻 Configuración del servicio REST Geocoder
[👆 Volver](#-contenidos)

La propiedades de este servicio se recogen en el fichero [***configuration.properties***](https://github.com/IDEESpain/Cartociudad/tree/main/src/main/resources)
En la siguiente tabla se recogen aquellos que son configurables, dando el nombre, una descripción y un ejemplo de los mismos:

| Nombre | Descripción | Ejemplo |
| ------ | ------ | ------ |
| elastic_url | URL de acceso a Elasticsearch (incluyendo el puerto si se mapea junto con el dominio). | http://elastic:9200 / http://elastic-organizacion.com |
| elastic_user | Usuario de Elasticsearch cuando tiene activada la autenticación mediante cabecera Authorization: Basic |
| elastic_pass | Clave para el usuario de Elasticsearch cuando tiene activada la autenticación mediante cabecera Authorization: Basic |
| elements_per_bucket | Indica el número de registros de cada grupo agregado que existe, donde cada grupo es una tipología de dato. El valor debe ser, al menos, como el mayor de los xxx_table_name_elements siguientes. | 8 |
| poblacion_table_name_elements | Número de elementos a mostrar de la tipología Población | 2 |
| municipio_table_name_elements | Número de elementos a mostrar de la tipología Municipio | 3 |
| provincia_table_name_elements | Número de elementos a mostrar de la tipología Provincia | 1 |
| comunidad_autonoma_table_name_elements | Número de elementos a mostrar de la tipología Comuniodad Autónoma | 1 |
| callejero_table_name_elements | Número de elementos a mostrar de la tipología Vial | 8 |
| carretera_table_name_elements | Número de elementos a mostrar de la tipología Carreteras | 4 |
| portal_table_name_elements | Número de elementos a mostrar de la tipología Portal (que incluye Puntos Kilométricos) | 6 |
| toponimo_table_name_elements | Número de elementos a mostrar de la tipología Topónimo | 5 |
| expendeduria_table_name_elements | Número de elementos a mostrar de la tipología expededuría | 2 |
| ngbe_table_name_elements | Número de elementos a mostrar de la tipología ngbe (Nomenclator Geográfico Básico de España) | 2 |
| punto_recarga_electrica_table_name | Número de elementos a mostrar de la tipología punto de recarga eléctrica| 2 |
| reverse_buffer | Buffer para el filtro de la consulta reverse (obtener dirección a partir las coordenadas geográficas de latitud y longitud en WGS84; EPSG:4326) | 350m |
| unified_max_rows | Filas que se procesan del CSV (sin contar cabecera) | 60000 |

### 🛠️ GeocodingServiceElasticCore.java - Servicio SOAP de la D.G de Catastro
[👆 Volver](#-contenidos)

El servicio REST está configurado para que se conecte al servicio SOAP de la Dirección General de Catastro, para hacer consultas a referencias catastrales. 

Mediante la geocodificación por identificador geográfico (*candidates o find*) se obtiene una dirección y coordenadas geográficas a partir de una referencia catastral. Para ello se hacen consultas con solo los primeros 14 dígitos. Ejemplo: 8128701YJ2782G

* https://www.cartociudad.es/geocoder/api/geocoder/candidates?q=8128701YJ2782G
* https://www.cartociudad.es/geocoder/api/geocoder/find?q=8128701YJ2782G&type=refcatastral&id=8128701YJ2782G

El archivo que tiene este servicio incrustado es el [***GeocodingServiceElasticCore.java***](https://github.com/IDEESpain/Cartociudad/blob/main/src/main/java/com/ign/cartociudad/ws/core/GeocodingServiceElasticCore.java):

  ```
   */
  *URL Consulta de Referencia Catastral
	 */
	public static final String REF_CATASTRAL_URL = "http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCoordenadas.asmx/Consulta_CPMRC";
	
  ```

### 🗺️ Index.html - Visualizador propio del Geocoder
[👆 Volver](#-contenidos)

Por otro lado, se tiene un visualizador que se ha creado con la API-IDEE, para que se puedan hacer búsquedas directamente al geocoder a partir del *plugin locator*. Así en la URL https://IP:puerto/geocoder/ se tiene una herramienta visual del geocoder.

El archivo de configuración es [***index.html***](https://github.com/IDEESpain/Cartociudad/blob/main/WebContent/index.html).

A continuación se muestra como está configurado el archivo, pero se puede modificar o añadir elementos. Para más información, se tiene el [Github público](https://github.com/Desarrollos-IDEE/API-IDEE/blob/master/api-idee-js/src/plugins/locator/README.md).


```
            const mp = new M.plugin.Locator({
              position: 'TL',
              collapsed: false,
              collapsible:false,
              useProxy:false,
              tooltip:'Servicio REST geocoder',
              ignsearchlocator:'entityFound',
              byParcelCadastre:false,
              byCoordinates:false,
              byPlaceAddressPostal: {
            	  servicesToSearch: 'g',
            	  noProcess: '',
            	  reverse: true,
            	  maxResults: 33,
            	  urlCandidates:'/geocoder/api/geocoder/candidatesJsonp',
                  urlFind:'/geocoder/api/geocoder/findJsonp',
                  urlReverse:'/geocoder/api/geocoder/reverseGeocode'
              }
            });
            
            mapjs.addPlugin(mp);   

```

## 🚀 Despliegue manual e indexación de Elasticsearch
Este apartado describe la configuración e indexación manual de Elasticsearch y el despliegue tradicional del Geocoder.

Para realizar una instalación local automatizada mediante Docker Compose, consultar el apartado [Despliegue con Docker](#-despliegue-con-docker).

[👆 Volver](#-contenidos)

Requisitos:
- Java 8
- Tomcat 9
- Visibilidad sobre Elasticsearch

### 🔸 Mapeo e indexación en Elasticsearch

La información se encuentra en: *src/main/resources/elasticsearch*
- *src/main/resources/elasticsearch/configuration*: configuración para que Elasticsearch tenga en cuenta
  - *stopwords*: palabras y letras, que para cuando se busquen en el geocoder, Elasticsearch no las tenga en cuenta y vaya la búsqueda más rápida
  - *synonyms*: sinónimos y abreviaturas de tipos de viales o de palabras en general, para que cuando se haga una búsqueda por ejemplo por *Colegio...* y se tenga en el JSON como *CEIP...* el geocoder de respuesta.
    - **Nota**: Si se cambia el contenido de algunos de estos ficheros, para que funcione correctamente, hay que indexar todo de nuevo 
- *src/main/resources/elasticsearch/mappings*: ficheros de ejecución *sh* para crear el mapeo de los índices (vacíos) por cada entidad:
  - codigo_postal --> codigo_postal_mapping.json
  - division_administrativa --> division_administrativa_mapping.json
  - portal_pk --> portal_pk_mapping.json
  - toponimo --> toponimo_mapping.json
  - vial --> vial_mapping.json
- *src/main/resources/elasticsearch/scripts*: fichero para indexar los datos
  - codigo_postal --> carga_codigo_postal.sh
  - division_administrativa --> carga_division_administrativa.sh
  - portal_pk --> carga_portal_pk.sh
  - toponimo --> carga_toponimo_mapping.sh
  - vial --> carga_vial.sh
- *src/main/resources/elasticsearch/sample_data*: ejemplo de datos del municipios de *Humanes de Madrid* en formato JSON a indexar

**Pasos a realizar**

1º) Mapeo de índices de cada entidad:

  ```
  sh mapping_ENTIDAD.sh
  ```

2º) Indexación de los datos JSON de cada entidad:

  ```
  bash carga_ENTIDAD_cnig.sh
  ```

3º) Comprobación de indexación:
- http://[IP_ELASTIC]:9200/_cat/indices?v&pretty 

# Servicio REST Geocoder CartoCiudad
## ✍️ Descripción
Servicio REST Geocoder proporciona funcionalidades de búsqueda y geolocalización sobre el conjunto de datos de entidades poblacionales, viales, portales, puntos kilométricos, puntos de interés (POI), códigos postales y referencias Catastrales (Servicio SOAP de la Dirección General de Catastro [D.G. Catastro]). Las fuentes de datos de estos elementos, se encuenta [aquí](https://www.cartociudad.es/web/portal/fuentes-oficiales)

La principal mejora de este servicio sobre otras versiones anteriores es que funciona sobre Elasticsearch, que ejerce de motor de persistencia y búsqueda.
Geocoder sustenta las operaciones de búsqueda de [Cartociudad](https://www.cartociudad.es) y se dispone de documentación oficial sobre los servicios [aquí](https://www.idee.es/resources/documentos/Cartociudad/CartoCiudad_ServiciosWeb.pdf) 

## 🚂 Funcionalidades de los servicios
El servicio REST geocoder tiene dos funcionalidades:
- **Búsqueda por nombre geográfico**: a partir de una búsqueda de un elemento geográfico el servicio devuelve como resultado sus correspondientes coordenadas geográficas.
  - Búsqueda por referencia catastral: en el propio código del geocoder se ha integrado el servicio SOAP de la D. G de Catastro  
- **Búsqueda por coordenadas geográficas**: a partir de unas coordenadas el servicio devuelve una dirección aproximada a dicho punto

## 🌍 Búsqueda por nombre geográfico
Para obtener las coordenadas de un elemento geográfico hay que partir de dos métodos, uno seguido de otro:

### 🔍1. CANDIDATES 
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
*Ejemplo: No localizar las direcciones postales (Salamanca), y que solamente muestre municipios, población o topónimos que tengan la mayor similitud a la búsqueda.*
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
CartoCiudad. Así, si se tiene duda se puede hacer primero una consulta al candidates del nombre del municipio, por ejemplo, y a continuación hacer la petición con el filtro de municipios.

- **countrycodes** (opcional): identificador del país (por defecto 'es').
- **limit** (opcional): Número máximo de coincidencias o resultados próximos a la consulta que se devolverán. Por defecto son 33, si se quieren menos hay que indicar con limit cuantos

🔸**Lógica básica de consulta de candidatos**

Es conveniente aclarar cómo funciona la búsqueda de candidatos en cuanto a la cadena de búsqueda:
- Si la cadena de búsqueda no contiene número, se busca sobre Divisiones Administrativas (Poblaciones, Municipios, Provincias, Comunidades Autónomas), Viales, Carreteras y Topónimos
- Si la cadena de búsqueda contiene algún número, se busca sobre Divisiones Administrativas (Poblaciones, Municipios, Provincias, Comunidades Autónomas), Viales, Carreteras, Topónimos, **Portales y Puntos Kilométricos**
- Si la cadena solo contiene números y son de la longitud adecuada, se buscan Códigos Postales

Es importante comentar el orden intrínseco de las tipologías, y el número de registros de cada tipología, siendo lo siguiente:
- Poblaciones: 2 registros
- Municipio:  3 registros
- Callejero (viales urbanos): 7 registros
- Carretera (viales interurbana): 4 registros
- Portales y puntos kilométricos: 6 registros
- Puntos de interés: 4 registros
- Expendedurías (procedentes de Comisión de Tabacos): 2 registros
- Puntos de recarga eléctrica (procedentes del Geoportal de Hidrocarburos): 2 registros
- Topónimos (procedentes del Nomenclátor Geográfico Básico de España): 3 registros
- Provincias: 1 registro
- Comunidades autónomas: 1 registro

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
- lat: Coordenada que representa la latitud de la entidad de los elementos puntuales(portales, puntos kilométricos, puntos de interés y topónimos).
- lng: Coordenada que representa la longitud de la entidad de los elementos puntuales (portales, puntos kilométricos, puntos de interés y topónimos).
- geom: no disponible con esta petición.
- state: 0 (este valor con la versión actual del geocoder, se ha suprimido, ya que se empleaelasticsearch y no se puede configurar la salida de candidates según grado de coincidencia).
- stateMsg: Vacío (este valor con la versión actual del geocoder, se ha suprimido, ya que se emplea elasticsearch y no se puede configurar la salida candidates según grado de coincidencia)

### 📍 2. FIND 
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
A partir de unas coordenadas geográficas (EPGS:4326) el servicio devuelve la dirección más próxima a dicho punto en un radio de 350 metros, elemento parametrizable  (reverse_buffer).

🔸**PETICIÓN HTTP GET**:
- /geocoder/api/geocoder/reverseGeocode

🔸**PARÁMETROS**: 
- lon (obligatoria): Coordenada que representa la longitud
- lat (obligatoria): Coordenada que representa la latitud

*Ejemplo: http://www.cartociudad.es/geocoder/api/geocoder/reverseGeocode?lon=-1.371939&lat=41.487733*

🔹**RESPUESTA**
Devuelve un JSON  con los mismo parámetros que en los casos anteriores: *candidates* y en el *find*

** **NOTA**: los campos longitud y latitud que se devuelven no son los que se muestran como parámetros de entrada en la petición, sino los correspondientes a la entidad que se devuelve en el resultado.

## 📊 Calculadora unificada de direcciones postales - lógica y requisitos

Esta funcionalidad, que se encuentra accesible desde [aquí](https://www.cartociudad.es/web/portal/herramientas-calculos/conversor) implementa, en el mismo servicio, la geocodificación por nombre o por coordenadas geográficas de forma masiva y según un fichero CSV de entrada.

Tanto los requisitos del CSV como su funcionamiento, se encuentran [aquí](https://www.idee.es/resources/documentos/Cartociudad/Instrucciones_conversor.pdf)

🔸**PETICIÓN HTTP POST**:
- /geocoder/api/geocoder/unifiedcsvgeocoding

Envío de un CSV con los elementos a solicitar para su geolocalización.

🔹**RESPUESTA**

Devuelve un CSV con la misma cabecera que el envíado, añadiendo un nuevo campo OBSERVACIONES, que estable una observación de la búsqueda. Este CSV contiene los elementos geolocalizados con sus parámetros correspondientes.

## 🛠️ Configuración del proyecto
La propiedades de este servicio se recogen en el fichero */src/main/resources/configuration.properties*
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
| reverse_buffer | Buffer para el filtro de la consulta reverse (obtener direccion a partir de una x e y) | 350m |
| unified_max_rows | Filas que se procesan del CSV (sin contar cabecera) | 60000 |


## 🚀 Despliegue

Requisitos:
- Java 8
- Tomcat 9
- Visibilidad sobre Elasticsearch

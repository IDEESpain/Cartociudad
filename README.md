# Geocoder
## Descripción
Servicio REST que proporciona, sobre todo, funcionalidades de búsqueda sobre el conjunto de datos de entidades poblacionales, viales, portales, topónimos y códigos postales.Las fuentes de datos de estos elementos, se encuenta [aquí](https://www.cartociudad.es/web/portal/fuentes-oficiales)

La principal mejora de este servicio sobre otras versiones anteriores es que funciona sobre Elasticsearch, que ejerce de motor de persistencia y búsqueda.
Geocoder sustenta las operaciones de búsqueda de [Cartociudad](https://www.cartociudad.es) y se dispone de documentación oficial sobre los servicios [aquí](https://www.idee.es/resources/documentos/Cartociudad/CartoCiudad_ServiciosWeb.pdf) 

## Particularidades de los servicios

### Candidates - lógica básica de consulta de candidatos

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


Por último, esto se aplica a los siguientes paths:
- (GET) /geocoder/api/geocoder/candidates
- (GET) /geocoder/api/geocoder/candidatesJsonp

### Calculadora unificada de direcciones postales - lógica y requisitos

Esta funcionalidad, que se encuentra accesible desde [aquí](https://www.cartociudad.es/web/portal/herramientas-calculos/conversor) implementa, en el mismo servicio, la geocodificación directa e inversa de forma masiva y según un fichero CSV de entrada.

Tanto los requisitos del CSV como su funcionamiento, se encuentran [aquí](https://www.idee.es/resources/documentos/Cartociudad/Instrucciones_conversor.pdf)

Por último, el servicio atiende al path: (POST) /geocoder/api/geocoder/unifiedcsvgeocoding


## Configuración del proyecto
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


## Despliegue

Requisitos:
- Java 8
- Tomcat 9
- Visibilidad sobre Elasticsearch

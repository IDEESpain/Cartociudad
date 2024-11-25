package com.ign.cartociudad.configuration;

import java.util.ResourceBundle;

/**
 * Clase para recoger la configuracion
 * 
 * @author Guadaltel S.A
 *
 */
public abstract class Configuration {
	
	/*
	 * Nombre del fichero de configuracion
	 */
	private static final String FILE_NAME = "configuration";
	
	/**
	 * Nombre de la propiedad de la URL de Elasticsearch
	 */
	public static final String URL_ELASTIC_PROP_NAME = "elastic_url";
	
	/**
	 * Nombre de la propiedad del usuario de Elasticsearch
	 */
	public static final String USER_ELASTIC_PROP_NAME = "elastic_user";
	
	/**
	 * Nombre de la propiedad del password de Elasticsearch
	 */
	public static final String PASS_ELASTIC_PROP_NAME = "elastic_pass";
	
	/**
	 * Nombre de la propiedad de numero de elementos por bucket de agregacion
	 */
	public static final String ELEMENTS_PER_BUCKET_PROP_NAME = "elements_per_bucket";
	
	/**
	 * Nombre de la propiedad de agregación
	 */
	public static final String AGGREGATION_PROP_NAME = "aggregation";
	
	/**
	 * Nombre de la propiedad de nombre del indice de portales
	 */
	public static final String PORTAL_INDEX_NAME_PROP_NAME = "portal_index_name";
	
	/**
	 * Nombre de la propiedad de nombre del indice de viales
	 */
	public static final String VIAL_INDEX_NAME_PROP_NAME = "vial_index_name";
	
	/**
	 * Nombre de la propiedad de nombre del indice de toponimos
	 */
	public static final String TOPONIMO_INDEX_NAME_PROP_NAME = "toponimo_index_name";
	
	/**
	 * Nombre de la propiedad de nombre del indice de toponimos
	 */
	public static final String DIVISION_ADMINISTRATIVA_INDEX_NAME_PROP_NAME = "division_administrativa_index_name";
	
	/**
	 * Nombre de la propiedad de nombre del indice de codigos postales
	 */
	public static final String CODIGO_POSTAL_INDEX_NAME_PROP_NAME = "codigo_postal_index_name";
	
	/**
	 * Valor Poblacion del atributo table_name de los indices
	 */
	public static final String POBLACION_TABLE_NAME = "poblacion_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Poblacion de table_name
	 */
	public static final String POBLACION_TABLE_NAME_ELEMENTS = "poblacion_table_name_elements";
	
	/**
	 * Valor Municipio del atributo table_name de los indices
	 */
	public static final String MUNICIPIO_TABLE_NAME = "municipio_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Municipio de table_name
	 */
	public static final String MUNICIPIO_TABLE_NAME_ELEMENTS = "municipio_table_name_elements";
	
	/**
	 * Valor Provincia del atributo table_name de los indices
	 */
	public static final String PROVINCIA_TABLE_NAME = "provincia_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Provincia de table_name
	 */
	public static final String PROVINCIA_TABLE_NAME_ELEMENTS = "provincia_table_name_elements";
	
	/**
	 * Valor Comunidad Autónoma del atributo table_name de los indices
	 */
	public static final String COMUNIDAD_AUTONOMA_TABLE_NAME = "comunidad_autonoma_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Comunidad Autónoma de table_name
	 */
	public static final String COMUNIDAD_AUTONOMA_TABLE_NAME_ELEMENTS = "comunidad_autonoma_table_name_elements";
	
	/**
	 * Valor Expendeduria del atributo table_name de los indices
	 */
	public static final String EXPENDEDURIA_TABLE_NAME = "expendeduria_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Expendeduria de table_name
	 */
	public static final String EXPENDEDURIA_TABLE_NAME_ELEMENTS = "expendeduria_table_name_elements";
	
	/**
	 * Valor Punto Recarga Eléctrica del atributo table_name de los indices
	 */
	public static final String PUNTO_RECARGA_ELECTRICA_TABLE_NAME = "punto_recarga_electrica_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Punto Recarga Eléctrica de table_name
	 */
	public static final String PUNTO_RECARGA_ELECTRICA_TABLE_NAME_ELEMENTS = "punto_recarga_electrica_table_name_elements";
	
	/**
	 * Valor NGBE del atributo table_name de los indices
	 */
	public static final String NGBE_TABLE_NAME = "ngbe_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor NGBE de table_name
	 */
	public static final String NGBE_TABLE_NAME_ELEMENTS = "ngbe_table_name_elements";
	
	/**
	 * Valor Codigo Postal del atributo table_name de los indices
	 */
	public static final String CODIGO_POSTAL_TABLE_NAME = "codigo_postal_table_name";
	
	/**
	 * Valor Vial - Callejero del atributo table_name de los indices
	 */
	public static final String CALLEJERO_TABLE_NAME = "callejero_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Vial - Callejero de table_name
	 */
	public static final String CALLEJERO_TABLE_NAME_ELEMENTS = "callejero_table_name_elements";
	
	/**
	 * Valor Vial - Carretera del atributo table_name de los indices
	 */
	public static final String CARRETERA_TABLE_NAME = "carretera_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Vial - Carretera de table_name
	 */
	public static final String CARRETERA_TABLE_NAME_ELEMENTS = "carretera_table_name_elements";
	
	/**
	 * Valor Portal del atributo table_name de los indices
	 */
	public static final String PORTAL_TABLE_NAME = "portal_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Portal de table_name
	 */
	public static final String PORTAL_TABLE_NAME_ELEMENTS = "portal_table_name_elements";
	
	/**
	 * Valor Toponimo del atributo table_name de los indices
	 */
	public static final String TOPONIMO_TABLE_NAME = "toponimo_table_name";
	
	/**
	 * Numero de elementos a mostrar para el valor Toponimo de table_name
	 */
	public static final String TOPONIMO_TABLE_NAME_ELEMENTS = "toponimo_table_name_elements";

	/**
	 * Buffer para el filtro de la consulta reverse (obtener direccion a partir de una x e y)
	 */
	public static final String REVERSE_BUFFER = "reverse_buffer";
	
	/**
	 * Cadena o cadenas a considerar en la busqueda de portal sin numero
	 */
	public static final String NO_NUMBER_QUERY_STR = "no_number_query_str";
	
	/**
	 * Numero maximo de filas que se procesan en el calculso masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_MAX_ROWS = "csvfindlist_max_rows";
	
	/**
	 * Mensaje para cuando el resultado de la busqueda es exacto en el calculo
	 * masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_EXACT_RESULT = "csvfindlist_exact_result";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal difiere en el
	 * tipo de vial en el calculo masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_PORTAL_DIFFERENT_ROAD_TYPE = "csvfindlist_portal_different_road_type";
	
	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal devuelve el
	 * portal mas cercano en el calculo masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_PORTAL_NEAREST = "csvfindlist_portal_nearest";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal devuelve el
	 * portal mas cercano y difiere en el tipo de via en el calculo masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_PORTAL_DIFFERENT_ROAD_TYPE_AND_NEAREST = "csvfindlist_portal_different_road_type_and_nearest";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre pk devuelve el
	 * portal mas cercano en el calculo masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_PK_NEAREST = "csvfindlist_pk_nearest";

	/**
	 * Mensaje para cuando no hay resultado en el calculo masivo de coordenadas
	 * (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_NO_RESULT = "csvfindlist_no_result";

	/**
	 * Mensaje para cuando se ha introducido portal/pk con letra en una busqueda en
	 * el calculo masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_NUMBER_NOT_VALID = "csvfindlist_number_not_valid";
	
	/**
	 * Mensaje para cuando se ha producido un error en una busqueda en el calculo
	 * masivo de coordenadas (servicio csvfindlist)
	 */
	public static final String CSVFINDLIST_ERROR = "csvfindlist_error";
	
	/**
	 * Numero maximo de filas que se procesan en el calculso masivo de coordenadas (servicio csvreversefindlist)
	 */
	public static final String CSVREVERSEFINDLIST_MAX_ROWS = "csvreverselist_max_rows";
	
	/**
	 * Mensaje para cuando no hay resultado en el calculo masivo de direcciones
	 * (servicio csvreverselist)
	 */
	public static final String CSVREVERSEFINDLIST_NO_RESULT = "csvreverselist_no_result";
	
	/**
	 * Numero maximo de filas que se procesan en el calculo masivo para SETELECO (servicio /seteleco/csv)
	 */
	public static final String SETELECO_MAX_ROWS = "setelecocsv_max_rows";
	
	/**
	 * Numero maximo de filas que se procesan en el calculo masivo UNIFICADO (servicio /unifiedcsvgeocoding)
	 */
	public static final String UNIFIED_MAX_ROWS = "unified_max_rows";
	
	/**
	 * Tipos de via que se chequean para avisar al usuario que debe vaciar el campo para buscar puntos kilometricos
	 */
	public static final String UNIFIED_PK_ROAD_TYPES = "unified_pk_road_types";
	
	/**
	 * Mensaje para cuando no hay resultado para una fila que es geocodificacion indirecta en el UNIFICADO
	 */
	public static final String UNIFIED_REVERSE_NO_RESULT = "unified_reverse_no_result";
	
	/**
	 * Mensaje para cuando hay resultado para una fila que es geocodificacion indirecta en el UNIFICADO
	 */
	public static final String UNIFIED_REVERSE_RESULT = "unified_reverse_result";
	
	/**
	 * Mensaje para cuando el resultado de la busqueda es exacto en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_EXACT_RESULT = "unified_direct_exact_result";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal difiere en el
	 * tipo de vial en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE = "unified_direct_portal_different_road_type";
	
	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal devuelve el
	 * portal mas cercano en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_PORTAL_NEAREST = "unified_direct_portal_nearest";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre portal devuelve el
	 * portal mas cercano y difiere en el tipo de via en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE_AND_NEAREST = "unified_direct_portal_different_road_type_and_nearest";

	/**
	 * Mensaje para cuando el resultado de la busqueda sobre pk devuelve el
	 * portal mas cercano en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_PK_NEAREST = "unified_direct_pk_nearest";

	/**
	 * Mensaje para cuando no hay resultado en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_NO_RESULT = "unified_direct_no_result";

	/**
	 * Mensaje para cuando se ha introducido portal/pk con letra en una busqueda en
	 * la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_NUMBER_NOT_VALID = "unified_direct_number_not_valid";
	
	/**
	 * Mensaje para cuando se ha producido un error en una busqueda en la geocodificacion directa UNIFICADA
	 */
	public static final String UNIFIED_DIRECT_ERROR = "unified_direct_error";
	
	/**
	 * Mensaje para indicar que para buscar un pk es necesario dejar el tipo de via vacio
	 */
	public static final String UNIFIED_DIRECT_TIPO_VIA_PK = "unified_direct_tipo_via_pk";
	
	/**
	 * Mensaje de validacion para indicar que cuando se busca por CCPP hay que rellenar tambien la Poblacion
	 */
	public static final String UNIFIED_DIRECT_VAL_PC_NO_LOCALITY = "unified_direct_val_pc_no_locality";
	
	/**
	 * Mensaje de validacion para indicar que cuando se busca por Poblacion hay que rellenar tambien el Municipio
	 */
	public static final String UNIFIED_DIRECT_VAL_LOCALITY_NO_MUNICIPALITY = "unified_direct_val_locality_no_municipality";
	
	/**
	 * Propiedad que indica si la funcionalidad de comprobacion de los dominios esta activa o no
	 */
	public static final String POSTAL_CODE_FIND_RESTRICTION = "postal_code_find_restriction";
	
	/**
	 * Lista blanca de dominios a los que se les puede devolver la geometria completa de una direccion que es un codigo postal
	 */
	public static final String POSTAL_CODE_FIND_DOMAIN_WITHELIST = "postal_code_find_domain_withelist";
	
	/**
	 * Inicializacion del fichero de propiedades
	 */
	private static final ResourceBundle bundle = ResourceBundle.getBundle(FILE_NAME);
	
	
	/**
	 * Constructor privado
	 */
	private Configuration() {
	
	}
	
	
	/**
	 * Lectura de propiedad del fichero de propiedades
	 * 
	 * @param name Nombre de la propiedad
	 * @return valor de la propiedad
	 */
	public static String getReadedProperty(String name) {
		
		try {
			
			return bundle.getString(name);
		
		} catch (Exception e) {
			
			return null;
		}
	}
	
}

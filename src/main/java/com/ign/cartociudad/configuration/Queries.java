package com.ign.cartociudad.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Esta clase se encarga de almacenar todas las constantes e impide la instanciación
 * @author Guadaltel S.A
 *
 */
public final class Queries {	
	
	/**
	 * Constructor privado
	 */
	private Queries() {
	   
		throw new AssertionError();
	}
	
	
	/**
	 * ELASTICSEARCH
	 */
	
	/**
	 * URL Elasticsearch
	 */
	public static final String URL = Configuration.getReadedProperty(Configuration.URL_ELASTIC_PROP_NAME);
	
	/**
	 * URL Elasticsearch
	 */
	public static final String USER = Configuration.getReadedProperty(Configuration.USER_ELASTIC_PROP_NAME);
	
	/**
	 * URL Elasticsearch
	 */
	public static final String PASS = Configuration.getReadedProperty(Configuration.PASS_ELASTIC_PROP_NAME);
	
	/**
	 * AGREGACIONES
	 */
	
	/**
	 * Indica si se aplica agregacion o no
	 */
	public static final String AGGREGATION = Configuration.getReadedProperty(Configuration.AGGREGATION_PROP_NAME);
	
	/**
	 * Para la agregacion, numero de elementos por bucket (general)
	 */
	public static final String ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.ELEMENTS_PER_BUCKET_PROP_NAME);
	
	/**
	 * INDICES
	 */
	
	/**
	 * Nombre del indice de portales
	 */
	public static final String PORTAL_INDEX_NAME = Configuration.getReadedProperty(Configuration.PORTAL_INDEX_NAME_PROP_NAME);
	
	/**
	 * Nombre del indice de viales
	 */
	public static final String VIAL_INDEX_NAME = Configuration.getReadedProperty(Configuration.VIAL_INDEX_NAME_PROP_NAME);
	
	/**
	 * Nombre del indice de toponimos
	 */
	public static final String TOPONIMO_INDEX_NAME = Configuration.getReadedProperty(Configuration.TOPONIMO_INDEX_NAME_PROP_NAME);
	
	/**
	 * Nombre del indice de divisones administrativas
	 */
	public static final String DIVISION_ADMINISTRATIVA_INDEX_NAME = Configuration.getReadedProperty(Configuration.DIVISION_ADMINISTRATIVA_INDEX_NAME_PROP_NAME);
	
	/**
	 * Nombre del indice de codigos postales
	 */
	public static final String CODIGO_POSTAL_INDEX_NAME = Configuration.getReadedProperty(Configuration.CODIGO_POSTAL_INDEX_NAME_PROP_NAME);
	
	/**
	 * TABLE NAMEs (Tipos dentro de los indices que a su vez son los BUCKETS)
	 */
	
	/**
	 * Nombre del bucket de portales
	 */
	public static final String PORTAL_BUCKET_NAME = Configuration.getReadedProperty(Configuration.PORTAL_TABLE_NAME);
	
	/**
	 * Nombre del bucket de viales - callejero
	 */
	public static final String CALLEJERO_BUCKET_NAME = Configuration.getReadedProperty(Configuration.CALLEJERO_TABLE_NAME);
	
	/**
	 * Nombre del bucket de viales - carreteras
	 */
	public static final String CARRETERA_BUCKET_NAME = Configuration.getReadedProperty(Configuration.CARRETERA_TABLE_NAME);
	
	/**
	 * Nombre del bucket de toponimos
	 */
	public static final String TOPONIMO_BUCKET_NAME = Configuration.getReadedProperty(Configuration.TOPONIMO_TABLE_NAME);
	
	/**
	 * Nombre del bucket de poblaciones
	 */
	public static final String POBLACION_BUCKET_NAME = Configuration.getReadedProperty(Configuration.POBLACION_TABLE_NAME);
	
	/**
	 * Nombre del bucket de municipios
	 */
	public static final String MUNICIPIO_BUCKET_NAME = Configuration.getReadedProperty(Configuration.MUNICIPIO_TABLE_NAME);
	
	/**
	 * Nombre del bucket de provincias
	 */
	public static final String PROVINCIA_BUCKET_NAME = Configuration.getReadedProperty(Configuration.PROVINCIA_TABLE_NAME);
	
	/**
	 * Nombre del bucket de comunidades autonomas
	 */
	public static final String COMUNIDAD_AUTONOMA_BUCKET_NAME = Configuration.getReadedProperty(Configuration.COMUNIDAD_AUTONOMA_TABLE_NAME);
	
	/**
	 * Nombre del bucket de expendedurias
	 */
	public static final String EXPENDEDURIA_BUCKET_NAME = Configuration.getReadedProperty(Configuration.EXPENDEDURIA_TABLE_NAME);
	
	/**
	 * Nombre del bucket de punto_recarga_electrica
	 */
	public static final String PUNTO_RECARGA_ELECTRICA_BUCKET_NAME = Configuration.getReadedProperty(Configuration.PUNTO_RECARGA_ELECTRICA_TABLE_NAME);
	
	/**
	 * Nombre del bucket de ngbe
	 */
	public static final String NGBE_BUCKET_NAME = Configuration.getReadedProperty(Configuration.NGBE_TABLE_NAME);
	
	/**
	 * Nombre del bucket de codigo postal
	 */
	public static final String CODIGO_POSTAL_BUCKET_NAME = Configuration.getReadedProperty(Configuration.CODIGO_POSTAL_TABLE_NAME);
	
	/**
	 * ELEMENTOS POR TABLE NAME (Tamanyo de cada bucket)
	 */
	
	/**
	 * Numero de elementos para el bucket de portales
	 */
	public static final String PORTAL_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.PORTAL_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de viales - callejero
	 */
	public static final String CALLEJERO_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.CALLEJERO_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de viales - carreteras
	 */
	public static final String CARRETERA_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.CARRETERA_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de toponimos
	 */
	public static final String TOPONIMO_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.TOPONIMO_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de poblaciones
	 */
	public static final String POBLACION_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.POBLACION_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de municipios
	 */
	public static final String MUNICIPIO_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.MUNICIPIO_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de provincias
	 */
	public static final String PROVINCIA_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.PROVINCIA_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de comunidades autonomas
	 */
	public static final String COMUNIDAD_AUTONOMA_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.COMUNIDAD_AUTONOMA_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de expendedurias
	 */
	public static final String EXPENDEDURIA_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.EXPENDEDURIA_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de puntos recargas electricas
	 */
	public static final String PUNTO_RECARGA_ELECTRICA_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.PUNTO_RECARGA_ELECTRICA_TABLE_NAME_ELEMENTS);
	
	/**
	 * Numero de elementos para el bucket de ngbe
	 */
	public static final String NGBE_ELEMENTS_PER_BUCKET = Configuration.getReadedProperty(Configuration.NGBE_TABLE_NAME_ELEMENTS);
	
	
	/**
	 * Mapa para facilitar la toma de datos de las agregaciones
	 */
	public static final Map<String, Integer> aggregationPropertiesMap = getAggregationPropertiesMap();
	
	
	/*
	 * REVERSE
	 */
	
	public static final String REVERSE_BUFFER = Configuration.getReadedProperty(Configuration.REVERSE_BUFFER);
	
	
	/*
	 * QUERIES BUSCADOR TEXTUAL
	 */
	
	/**
	 * Constante para tipo de busqueda sin numero (viales)
	 */
	public static final String SEARCH_TYPE_VIAL = "VIAL";
	
	/**
	 * Constante para tipo de busqueda con numero (portales)
	 */
	public static final String SEARCH_TYPE_PORTAL = "PORTAL";
	
	/**
	 * Constante para tipo de busqueda de portales sin numero
	 */
	public static final String SEARCH_TYPE_PORTAL_NO_NUMBER = "PORTAL_NO_NUMBER";
	
	/**
	 * Constante para tipo de busqueda codigo postal
	 */
	public static final String SEARCH_TYPE_POSTAL_CODE = "CODIGO_POSTAL";
	
	
	/**
	 * Agregaciones
	 */
	public static final String AGGREGATION_KEY = "--aggregation--";
	public static final String AGGREGATION_QUERY = ",\"aggs\":{\"table_name\":{\"terms\":{\"field\":\"table_name.keyword\",\"order\":{\"max_score\":\"desc\"}},\"aggs\":{\"includes\":{\"top_hits\":{\"sort\":[\"_score\",\"text_original.keyword\",{\"nom_muni_original.keyword\":{\"unmapped_type\":\"string\"}},{\"numero\":{\"unmapped_type\": \"integer\"}}],\"_source\":{\"excludes\":[\"geom\"]},\"size\":--size--}},\"max_score\":{\"max\":{\"script\":\"_score\"}}}}}".replace("--size--", ELEMENTS_PER_BUCKET);
	
	// Query pensada para todas las búsquedas que no incluyen número
	public static final String VIAL_QUERY = "{--exclude--\"size\":--limit--,\"indices_boost\":[{\"division_administrativa\":1.6},{\"vial\":1.01},{\"toponimo\":1}],\"query\":{\"function_score\":{\"query\":{\"bool\":{--filters--\"should\":[{\"multi_match\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"type\":\"bool_prefix\",\"fields\":[\"text_original.es^0.7\",\"direccion.es^0.4\",\"text_original.es_literal^0.05\",\"direccion.es_literal^0.05\",\"tipo_via.es^0.5\",\"nom_muni_original.es^0.2\"]}},{\"multi_match\":{\"query\": \"--q--\",\"type\":\"bool_prefix\",\"fields\":[\"text_original.es_literal^3.5\",\"tipo_via.es^0.5\",\"nom_muni_original.es^0.2\"]}},{\"multi_match\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\": \"--q--\",\"type\": \"most_fields\",\"fields\":[\"text_original.es^0.1\"]}}]--no_process--,\"filter\":[{\"bool\":{\"should\":[{\"match_bool_prefix\":{\"direccion.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"minimum_should_match\":\"100%\"}}},{\"bool\":{\"must\":{\"match_bool_prefix\":{\"direccion.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"minimum_should_match\":\"3<-1\"}}},\"must_not\":[{\"match\":{\"tipo_via.es\":{\"query\":\"--q--\"}}},{\"terms\":{\"_index\":[\"toponimo\",\"division_administrativa\"]}}]}}]}},{\"match_bool_prefix\":{\"text_original.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\"}}}]}},\"script_score\":{\"script\":{\"source\": \"double new_score = _score/(1+_score);if(doc['table_name.keyword'].value.equals('poblacion')) return 10 + new_score; else if(doc['table_name.keyword'].value.equals('municipio')) return 9 + new_score; else if(doc['_index'].value.equals('vial')) return 8 + new_score; else if(doc['table_name.keyword'].value.equals('toponimo')) return 7 + new_score; else if(doc['table_name.keyword'].value.equals('expendeduria')) return 6 + new_score; else if(doc['table_name.keyword'].value.equals('punto_recarga_electrica')) return 5 + new_score; else if(doc['table_name.keyword'].value.equals('ngbe')) return 4 + new_score; else if(doc['table_name.keyword'].value.equals('provincia')) return 3 + new_score; else if(doc['table_name.keyword'].value.equals('comunidad autonoma')) return 2 + new_score;\"}},\"boost_mode\":\"replace\"}},\"sort\":[\"_score\",\"text_original.keyword\",{\"nom_muni_original.keyword\":{\"unmapped_type\": \"string\"}}]" + AGGREGATION_KEY + "}";
	public static final String VIAL_URL = URL + "/" + DIVISION_ADMINISTRATIVA_INDEX_NAME + "," + TOPONIMO_INDEX_NAME + "," + VIAL_INDEX_NAME + "/_search?search_type=dfs_query_then_fetch";
	public static final String[] VIAL = {VIAL_QUERY, VIAL_URL};
	
	// Query pensada para todas las búsquedas de códigos postales
	public static final String CODPOSTAL_QUERY = "{--exclude--\"size\": --limit--,\"explain\": false,\"query\":{\"regexp\":{\"text_original.es\":{\"value\": \"--q--.*\"}}},\"track_scores\": true,\"sort\":[\"text_original.keyword\"]}";
	public static final String CODPOSTAL_URL = URL + "/" + CODIGO_POSTAL_INDEX_NAME + "/_search";
	public static final String[] CODPOSTAL = {CODPOSTAL_QUERY, CODPOSTAL_URL};

	// Query pensada para todas las búsquedas que incluyen número y no son códigos postales
	public static final String PORTAL_QUERY = "{--exclude--\"size\":--limit--,\"indices_boost\":[{\"division_administrativa\":1.6},{\"vial\":1.01},{\"portal_pk\":1},{\"toponimo\":1}],\"query\":{\"function_score\":{\"query\":{\"bool\":{--filters--\"should\":[{\"multi_match\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"type\":\"bool_prefix\",\"fields\":[\"text_original.es^0.7\",\"direccion.es^0.4\",\"text_original.es_literal^0.05\",\"direccion.es_literal^0.05\",\"tipo_via.es^0.5\",\"nom_muni_original.es^0.2\",\"extension.es^0.5\"]}},{\"match\":{\"numero.es\":{\"query\":\"--q--\",\"boost\":\"0.5\"}}}]--no_process----no_number--,\"filter\":[{\"bool\":{\"should\":[{\"match_bool_prefix\":{\"direccion.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"minimum_should_match\":\"100%\"}}},{\"bool\":{\"must\":{\"match_bool_prefix\":{\"direccion.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\",\"minimum_should_match\":\"3<-1\"}}}--no_number_filter--,\"must_not\":[{\"match\":{\"numero.es\":{\"query\":\"--q--\"}}},{\"terms\":{\"_index\":[\"toponimo\",\"division_administrativa\",\"vial\"]}}]}}]}},{\"match_bool_prefix\":{\"text_original.es\":{\"fuzziness\": \"AUTO:2,7\",\"max_expansions\":100,\"query\":\"--q--\"}}}]}},\"script_score\":{\"script\":{\"source\":\"double new_score = _score;if(doc['_index'].value.equals('portal_pk')){int num = -1;Pattern p1 = \\/([0-9]+\\\\s+[0-9]+)(?!.*[0-9])\\/;Matcher m1 = p1.matcher(params.search);if(m1.find()){String numStr = m1.group();int split = numStr.indexOf(' ');numStr = numStr.substring(0, split);num = Integer.parseInt(numStr);}else{Pattern p2 = \\/([0-9]+)(?!.*[0-9])\\/;Matcher m2 = p2.matcher(params.search);if(m2.find()){num = Integer.parseInt(m2.group());}}if(num != -1){if(doc['numero'].size() != 0){if((num -doc['numero'].value) % 2 == 0){new_score = new_score + (2 \\/ (Math.pow(Math.abs(num - doc['numero'].value),2) + 1)); } else{new_score = new_score + (1 \\/ (Math.pow(Math.abs(num - doc['numero'].value),2) + 1));}}}} else{new_score = new_score + 2} return new_score;\",\"params\":{\"search\":\"--q--\"}}},\"boost_mode\":\"replace\"}},\"sort\": [\"_score\",\"text_original.keyword\",{\"nom_muni_original.keyword\":{\"unmapped_type\": \"string\"}},{\"numero\":{\"unmapped_type\": \"integer\"}}]" + AGGREGATION_KEY + "}";
	public static final String PORTAL_URL = URL + "/" + DIVISION_ADMINISTRATIVA_INDEX_NAME + "," + VIAL_INDEX_NAME + "," + TOPONIMO_INDEX_NAME + "," + PORTAL_INDEX_NAME + "/_search?search_type=dfs_query_then_fetch";
	public static final String[] PORTAL = {PORTAL_QUERY, PORTAL_URL};
	
	/**
	 * Filter para contemplar que tenga campo numero
	 */
	public static final String FILTER_EXIST_FIELD_NUMERO = ",\"filter\":{\"exists\":{\"field\":\"numero\"}}";
	
	/**
	 * Must para filtrar por no_number
	 */
	public static final String TERM_NO_NUMBER = "{\"term\":{\"sin_numero\":true}}";
	public static final String MUST_NO_NUMBER_TRUE = ",\"must\":{\"term\":{\"sin_numero\":true}}";
	
	// Query general para buscar por find (id y type)
	public static final String FIND_QUERY = "{--exclude--\"size\": 1,\"explain\": false,\"query\":{\"bool\":{\"must\":[{\"term\":{\"obj_id\":--id--}},{\"term\":{\"table_name.keyword\":\"--type--\"}}]}}}";
	public static final String FIND_URL = URL + "/" + DIVISION_ADMINISTRATIVA_INDEX_NAME + "," + VIAL_INDEX_NAME + "," + PORTAL_INDEX_NAME + "," + CODIGO_POSTAL_INDEX_NAME + "/_search?search_type=dfs_query_then_fetch";
	public static final String[] FIND = {FIND_QUERY, FIND_URL};
	
	public static final String FIND_QUERY_OBJ_ID_TEXT = "{\"size\": 1,\"explain\": false,\"query\":{\"bool\":{\"must\":[{\"match\":{\"obj_id\":\"--id--\"}},{\"term\":{\"table_name.keyword\":\"--type--\"}}]}}}";
	public static final String FIND_URL_OBJ_ID_TEXTO = URL + "/--index_name--/_search?search_type=dfs_query_then_fetch";
	public static final String[] FIND_OBJ_ID_TEXT = {FIND_QUERY_OBJ_ID_TEXT, FIND_URL_OBJ_ID_TEXTO};
	
	public static final String CLOSEST_PORTAL_QUERY = "{\"size\":1,\"_source\":{\"includes\":[\"obj_id\",\"id_vial\",\"numero\"]},\"query\":{\"multi_match\":{\"query\":--id--,\"fields\":[\"obj_id\",\"id_vial\"]}},\"sort\":{\"_script\":{\"type\":\"number\",\"script\":{\"lang\":\"painless\",\"source\":\"Math.abs(doc['numero'].value - params.auxportalfounded)\",\"params\":{\"auxportalfounded\":\"--auxportalfounded--\"}},\"order\":\"asc\"}}}";
	public static final String CLOSEST_PORTAL_URL = URL + "/" + PORTAL_INDEX_NAME + "/_search";
	public static final String[] CLOSEST_PORTAL = {CLOSEST_PORTAL_QUERY, CLOSEST_PORTAL_URL};
	
	public static final String REVERSE_GEOCODE_QUERY = "{\"size\":1,\"query\":{\"bool\":{\"filter\":{\"geo_distance\":{\"distance\":\"" + REVERSE_BUFFER + "\",\"geom\":{\"lat\":--lat--,\"lon\":--lon--}}}}},\"sort\":[{\"_geo_distance\":{\"geom\":{\"lat\":--lat--,\"lon\":--lon--},\"order\":\"asc\",\"unit\":\"m\",\"mode\":\"min\",\"distance_type\":\"arc\",\"ignore_unmapped\":true}}]}";
	public static final String REVERSE_GEOCODE_URL = URL + "/" + PORTAL_INDEX_NAME + "/_search";
	public static final String[] REVERSE_GEOCODE_PORTAL = {REVERSE_GEOCODE_QUERY, REVERSE_GEOCODE_URL};
	
	/*
	 * Filtros para candidates (xxx_filter)
	 */
	
	/*
	 * QUERIES CALCULO MASIVO COORDENADAS
	 */
	
	/**
	 * Constante para busqueda sobre portales
	 */
	public static final String CSVFINDLIST_TYPE_PORTAL = "PORTAL";
	
	/**
	 * Constante para busqueda sobre puntos kilometricos
	 */
	public static final String CSVFINDLIST_TYPE_PK = "PK";
	
	/**
	 * Constante para busqueda sobre toponimos
	 */
	public static final String CSVFINDLIST_TYPE_TOPONIMO = "TOPONIMO";
	
	/*
	 * URL para la busqueda sobre portales y puntos kilometricos (es el mismo indice)
	 */
	public static final String CSVFINDLIST_PORTAL_PK_URL = URL + "/" + PORTAL_INDEX_NAME + "/_search?search_type=dfs_query_then_fetch";
	
	/*
	 * URL para la busqueda sobre toponimos
	 */
	public static final String CSVFINDLIST_TOPONIMO_URL = URL + "/" + TOPONIMO_INDEX_NAME + "/_search?search_type=dfs_query_then_fetch";
	
	/**
	 * Query sobre portales
	 */
	public static final String CSVFINDLIST_PORTAL_QUERY = "{\"_source\":{\"exclude\":[\"geom\"]},\"size\":1,\"query\":{\"bool\":{\"should\":[{\"match\":{\"tipo_via.es\":\"--tipoVia--\"}}],\"must\":[{\"match_phrase\":{\"text_original.es\":\"--nombreVia--\"}}--codpostal----codinemun----municipio----poblacion----provincia----extension--,{\"term\":{\"tipo_porpk\":\"1\"}}]}},\"sort\":[{\"_script\":{\"type\":\"number\",\"script\":{\"source\":\"double new_score = _score; if(doc['numero'].size() != 0){if((params.num -doc['numero'].value) % 2 == 0){new_score = new_score + (2 / (Math.pow(Math.abs(params.num - doc['numero'].value),2) + 1)); } else {new_score = new_score + (1 / (Math.pow(Math.abs(params.num - doc['numero'].value),2) + 1));}} return new_score;\",\"params\":{\"num\":--numero--}},\"order\":\"desc\"}},{\"_script\":{\"type\":\"number\",\"script\":{\"source\":\"int res = 0; if(doc['numero'].size() != 0){if(doc['numero'].value == params.num){res = 1;}} return res;\",\"params\":{\"num\":--numero--}},\"order\":\"desc\"}},\"_score\",\"numero\"],\"highlight\":{\"fields\":{\"tipo_via.es\":{\"type\": \"plain\"}}}}";
	
	/**
	 * Query sobre puntos kilometricos
	 */
	public static final String CSVFINDLIST_PK_QUERY = "{\"_source\":{\"exclude\":[\"geom\"]},\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"text_original.es\":\"--nombreVia--\"}}--codpostal----codinemun----municipio----poblacion----provincia--,{\"term\":{\"tipo_porpk\":\"2\"}}]}},\"sort\":[{\"_script\":{\"type\":\"number\",\"script\":{\"source\":\"double new_score = _score; if(doc['numero'].size() != 0){new_score = new_score + (2 / (Math.pow(Math.abs(params.num - doc['numero'].value),2) + 1)); } return new_score;\",\"params\":{\"num\":--numero--}},\"order\":\"desc\"}},{\"_script\":{\"type\":\"number\",\"script\":{\"source\":\"int res = 0; if(doc['numero'].size() != 0){if(doc['numero'].value == params.num){res = 1;}} return res;\",\"params\":{\"num\":--numero--}},\"order\":\"desc\"}}]}";
	
	/**
	 * Query sobre toponimos
	 */
	public static final String CSVFINDLIST_TOPONIMO_QUERY = "{\"_source\":{\"exclude\":[\"geom\"]},\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"text_original.es\":\"--nombre--\"}}--codinemun----municipio----provincia--]}}}";
	
	
	/**
	 * Fragmento de la query para cuando venga rellena el codigo postal
	 */
	public static final String CSVFINDLIST_CODPOSTAL_QUERY_FRAGMENT = ",{\"term\":{\"cod_postal\":\"--codpostal--\"}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena el codigo ine del municipio
	 */
	public static final String CSVFINDLIST_CODINEMUN_QUERY_FRAGMENT = ",{\"term\":{\"ine_mun\":\"--codinemun--\"}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena el municipio
	 */
	public static final String CSVFINDLIST_MUNICIPIO_QUERY_FRAGMENT = ",{\"match_phrase\":{\"nom_muni_original.es\":{\"query\":\"--municipio--\",\"slop\":5}}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena la poblacion
	 */
	public static final String CSVFINDLIST_POBLACION_QUERY_FRAGMENT = ",{\"match_phrase\":{\"ent_pob_original.es\":{\"query\":\"--poblacion--\",\"slop\":5}}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena la provincia
	 */
	public static final String CSVFINDLIST_PROVINCIA_QUERY_FRAGMENT = ",{\"match_phrase\":{\"nom_prov_original.es\":{\"query\":\"--provincia--\",\"slop\":5}}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena la extension
	 */
	public static final String CSVFINDLIST_EXTENSION_QUERY_FRAGMENT = ",{\"match_phrase\":{\"extension\":\"--extension--\"}}";
	
	/**
	 * Fragmento de la query para cuando venga rellena una division administrativa (general)
	 */
	//public static final String CSVFINDLIST_DIVISION_ADMINISTRATIVA_QUERY_FRAGMENT = "{\"match_phrase\":{\"--key--\":{\"query\":\"--value--\",\"slop\":0}}}";
	public static final String CSVFINDLIST_DIVISION_ADMINISTRATIVA_QUERY_FRAGMENT = "{\"term\":{\"--key--\":{\"value\":\"--value--\",\"case_insensitive\":true}}}";
	
	/**
	 * Obtiene el mapa con las propiedades de las agregaciones para aplicar a los
	 * resultados de las queries textuales
	 * 
	 * @return Map<String, AggregationProperties>
	 */
	private static Map<String, Integer> getAggregationPropertiesMap() {
		
		Map<String, Integer> map = null;
		if (StringUtils.isNotBlank(Queries.AGGREGATION) && Queries.AGGREGATION.equals("true")) {
			
			map = new HashMap<String, Integer>();
			map.put(PORTAL_BUCKET_NAME, Integer.valueOf(PORTAL_ELEMENTS_PER_BUCKET));
			map.put(CALLEJERO_BUCKET_NAME, Integer.valueOf(CALLEJERO_ELEMENTS_PER_BUCKET));
			map.put(CARRETERA_BUCKET_NAME, Integer.valueOf(CARRETERA_ELEMENTS_PER_BUCKET));
			map.put(TOPONIMO_BUCKET_NAME, Integer.valueOf(TOPONIMO_ELEMENTS_PER_BUCKET));
			map.put(EXPENDEDURIA_BUCKET_NAME, Integer.valueOf(EXPENDEDURIA_ELEMENTS_PER_BUCKET));
			map.put(PUNTO_RECARGA_ELECTRICA_BUCKET_NAME, Integer.valueOf(PUNTO_RECARGA_ELECTRICA_ELEMENTS_PER_BUCKET));
			map.put(NGBE_BUCKET_NAME, Integer.valueOf(NGBE_ELEMENTS_PER_BUCKET));
			map.put(POBLACION_BUCKET_NAME, Integer.valueOf(POBLACION_ELEMENTS_PER_BUCKET));
			map.put(MUNICIPIO_BUCKET_NAME, Integer.valueOf(MUNICIPIO_ELEMENTS_PER_BUCKET));
			map.put(PROVINCIA_BUCKET_NAME, Integer.valueOf(PROVINCIA_ELEMENTS_PER_BUCKET));
			map.put(COMUNIDAD_AUTONOMA_BUCKET_NAME, Integer.valueOf(COMUNIDAD_AUTONOMA_ELEMENTS_PER_BUCKET));
		}
		
		return map;
	}
}

package com.ign.cartociudad.ws.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ign.cartociudad.configuration.Configuration;
import com.ign.cartociudad.configuration.Queries;
import com.ign.cartociudad.elastic.ElasticClient;
import com.ign.cartociudad.util.GeocodingUtils;
import com.ign.cartociudad.ws.model.Address;
import com.ign.cartociudad.ws.model.AddressPriorized;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Implementacion de los metodos del servicio
 * 
 * @author guadaltel
 *
 */
public class GeocodingServiceElasticCore {
	
	/**
	 * Log
	 */
	private static  Logger log = Logger.getLogger(GeocodingServiceElasticCore.class.getName());
	
	/*
	 * Inicializacion cliente Elasticsearch
	 */
	private ElasticClient elastic = new ElasticClient();
	
	/*
	 * Sistema de referencia para la consulta de Referencias Catastrales
	 */
	public static final String SRS_CODE = "EPSG:4258";
	
	/*
	 * URL Consulta de Referencia Catastral
	 */
	public static final String REF_CATASTRAL_URL = "http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCoordenadas.asmx/Consulta_CPMRC";
	
	/*
	 * URL Consulta de Referencia Catastral (Reverse)
	 */
	public static final String REF_CATASTRAL_URL_REVERSE = "http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCoordenadas.asmx/Consulta_RCCOOR";
    
	/*
     * Nombre del fichero de salida para el calculo masivo de direcciones
     */
	private static final String XSLX_REVERSE_OUTPUT_FILE_NAME = "_Output_Direcciones";
	
	/*
     * Nombre del fichero de salida para el calculo masivo de coordenadas
     */
	private static final String XSLX_FIND_OUTPUT_FILE_NAME = "_Output_Coordenadas_Geográficas";
	
	/*
     * Nombre del fichero de salida para el calculo masivo para SETELECO
     */
	private static final String SETELECO_OUTPUT_FILE_NAME = "_procesado";
	
	/*
     * Nombre del fichero de salida para el calculo masivo unificado
     */
	private static final String UNIFIED_OUTPUT_FILE_NAME = "_procesado";
	
	/*
	 * Extension para fichero de salida de los calculos masivos
	 */
	private static final String XSLX_EXTENSION = ".xlsx";
	
	/*
	 * Extension para fichero de salida de los calculos masivos
	 */
	private static final String CSV_EXTENSION = ".csv";
	
	/**
	 * Separador para los ficheros csv de entrada de los calculos masivos
	 */
	private static final char CSV_SEPARATOR = ';';
	
	/**
	 * Separador para los ficheros csv de entrada del calculo UNIFICADO
	 */
	private static final char UNIFIED_CSV_SEPARATOR = '|';
	
	/*
	 * Total de lineas que puede tener un fichero de entrada para el calculo masivo directo
	 */
	private static final int CSVFINDLIST_MAX_ROWS = Integer.parseInt(Configuration.getReadedProperty(Configuration.CSVFINDLIST_MAX_ROWS));
	
	/*
	 * Total de lineas que puede tener un fichero de entrada para el calculo masivo indirecto
	 */
	private static final int CSVREVERSEFINDLIST_MAX_ROWS = Integer.parseInt(Configuration.getReadedProperty(Configuration.CSVREVERSEFINDLIST_MAX_ROWS));
	
	/*
	 * Total de lineas que puede tener un fichero de entrada para el calculo masivo de SETELECO
	 */
	private static final int SETELECO_MAX_ROWS = Integer.parseInt(Configuration.getReadedProperty(Configuration.SETELECO_MAX_ROWS));
	
	/*
	 * Total de lineas que puede tener un fichero de entrada para el calculo masivo UNIFICADO
	 */
	private static final int UNIFIED_MAX_ROWS = Integer.parseInt(Configuration.getReadedProperty(Configuration.UNIFIED_MAX_ROWS));
	
	/*
	 * Tamanyo cabecera del fichero de entrada del calculo masivo indirecto
	 */
	private static final int LINEAS_XSLX_REVERSE_CABECERA = 2;
	
	/*
	 * Tamanyo cabecera del fichero de entrada del calculo masivo directo
	 */
	private static final int LINEAS_XSLX_CABECERA = 2;
	
	/*
	 * Constantes - identificadores varios
	 */
	public static final String TYPE_TOPONIMO = "toponimo";
	public static final String TYPE_MUNICIPIO = "Municipio";
	public static final String TYPE_REFCATASTRAL = "refcatastral";
	public static final String TYPE_POBLACION = "poblacion";
	public static final String ONLY_DIRECCION = TYPE_MUNICIPIO+","+TYPE_POBLACION+","+TYPE_TOPONIMO;
	
	/*
	 * Constante con el acronimo de Kilometro
	 */
	public static final String PORPK_KM = "km";
	
	/*
	 * Constantes para las geometrias MULTIPOLYGON
	 */
	private static final String MULTIPOLYGON = "MULTIPOLYGON";
	private static final int MULTIPOLYGON_STRING_LENGTH = MULTIPOLYGON.length();
	
	/*
	 * Tipo de busqueda que se esta realizando
	 */
	private String SEARCH_TYPE = ""; 
	
	/**
	 * Indica si la funcionalidad de usar el origen para la busqueda de codigos postales esta o no activa
	 */
	private Boolean POSTAL_CODE_FIND_RESTRICTION = Boolean.valueOf(Configuration.getReadedProperty(Configuration.POSTAL_CODE_FIND_RESTRICTION));
	/**
	 * Indica si el origen desde el que se hace la llamada
	 * esta en la lista blanca de dominios para la funcionalidad de codigos postales
	 */
	private Boolean ORIGIN_IS_IN_WITHE_LIST = null;
	
	/**
	 * Lista blanca de dominios para la funcionalidad de filtrar la busqueda cuando codigos postales
	 */
	private final String POSTAL_CODE_FIND_DOMAIN_WITHELIST = Configuration.getReadedProperty(Configuration.POSTAL_CODE_FIND_DOMAIN_WITHELIST);
	
	/*
	 * Tipo de busqueda con los valores Queries.CSVFINDLIST_TYPE_PORTAL,
	 * Queries.CSVFINDLIST_TYPE_PK, Queries.CSVFINDLIST_TYPE_TOPONIMO para el
	 * calculo masivo directo de coordenadas
	 */
	private String CSVFINDLIST_TYPE = "";
	
	/**
	 * Columnas del documento inicial de SETELECO
	 */
	
	private static final String SETELECO_CSV_TIPO_VIA = "TIPO VIA";
	private static final String SETELECO_CSV_NOMBRE_VIA = "NOMBRE VIA";
	private static final String SETELECO_CSV_PORTAL1_PK = "PORTAL1/PK";
	private static final String SETELECO_CSV_CODPOSTAL = "CODPOSTAL";
	private static final String SETELECO_CSV_COD_INE_MUNICIPIO = "COD_INE_MUNICIPIO";
	private static final String SETELECO_CSV_MUNICIPIO = "MUNICIPIO";
	private static final String SETELECO_CSV_PROVINCIA = "PROVINCIA";
	private static final String SETELECO_CSV_LATITUD = "LATITUD";
	private static final String SETELECO_CSV_LONGITUD = "LONGITUD";
	
	/**
	 * Columnas del servicio unificiado de GEOCODIFICACION
	 */
	
	private static final String UNIFIED_CSV_TIPO_VIA = "TIPO_VIA";
	private static final String UNIFIED_CSV_NOMBRE_VIA = "NOMBRE_VIA";
	private static final String UNIFIED_CSV_PORTAL1 = "PORTAL1";
	private static final String UNIFIED_CSV_PORTAL2 = "PORTAL2";
	private static final String UNIFIED_CSV_CODPOSTAL = "CODPOSTAL";
	private static final String UNIFIED_CSV_COD_INE_MUNICIPIO = "COD_INE_MUNICIPIO";
	private static final String UNIFIED_CSV_MUNICIPIO = "MUNICIPIO";
	private static final String UNIFIED_CSV_PROVINCIA = "PROVINCIA";
	private static final String UNIFIED_CSV_LATITUD = "LATITUD_WGS84_4326";
	private static final String UNIFIED_CSV_LONGITUD = "LONGITUD_WGS84_4326";
	private static final String UNIFIED_CSV_REFCAT14 = "REFCAT14";
	private static final String UNIFIED_CSV_POBLACION = "POBLACION";
	private static final String UNIFIED_CSV_OBSERVACIONES = "OBSERVACIONES_GEOCODIFICACION"; // Esta columna es la unica que se anyade por parte del proceso
	
	
	/**
	 * Mensajes para el campo OBSERVACIONES_GEOCODIFICACION del UNIFICADO
	 */
	
	private static final String UNIFIED_REVERSE_RESULT = Configuration.getReadedProperty(Configuration.UNIFIED_REVERSE_RESULT);
	private static final String UNIFIED_REVERSE_NO_RESULT = Configuration.getReadedProperty(Configuration.UNIFIED_REVERSE_NO_RESULT);
	private static final String UNIFIED_DIRECT_EXACT_RESULT = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_EXACT_RESULT);
	private static final String UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE);
	private static final String UNIFIED_DIRECT_PORTAL_NEAREST = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_PORTAL_NEAREST);
	private static final String UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE_AND_NEAREST = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE_AND_NEAREST);
	private static final String UNIFIED_DIRECT_PK_NEAREST = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_PK_NEAREST);
	private static final String UNIFIED_DIRECT_NO_RESULT = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_NO_RESULT);
	private static final String UNIFIED_DIRECT_NUMBER_NOT_VALID = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_NUMBER_NOT_VALID);
	private static final String UNIFIED_DIRECT_ERROR = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_ERROR);
	private static final String UNIFIED_DIRECT_TIPO_VIA_PK = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_TIPO_VIA_PK);
	private static final String UNIFIED_DIRECT_VAL_PC_NO_LOCALITY = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_VAL_PC_NO_LOCALITY);
	private static final String UNIFIED_DIRECT_VAL_LOCALITY_NO_MUNICIPALITY = Configuration.getReadedProperty(Configuration.UNIFIED_DIRECT_VAL_LOCALITY_NO_MUNICIPALITY);
	
	
	/**
	 * Tipos de via a chequear para los pk en el servicio masivo unificado
	 */
	private static final String UNIFIED_PK_ROAD_TYPES = Configuration.getReadedProperty(Configuration.UNIFIED_PK_ROAD_TYPES);
	
	/**
	 * Cadena o cadenas a considerar para la busqueda sin numero
	 */
	private static final String NO_NUMBER_QUERY_STR = Configuration.getReadedProperty(Configuration.NO_NUMBER_QUERY_STR);
	
	/**
	 * Cadena que se anyade a la direccion completa cuando es un portal sin numero
	 */
	private static final String SIN_NUMERO = "S-N";
	
	/**
	 * Logica para devolver los datos de la llamada a candidates
	 * 
	 * @param q          Query
	 * @param type       Tipo
	 * @param tipVia     Tipo de via
	 * @param id         Identificador
	 * @param numPortal  Numero de portal
	 * @param no_process Parametro no_process
	 * @param state      Estado
	 * @param request    Peticion
	 * @return Resultado (Address)
	 */
	public Address find(String q, String type, String tipVia, String id, String numPortal, String no_process, int state,
			HttpServletRequest request) {

		Address address = new Address();
		String[] hug = new String[2];
		String query, url, responseStr;
		JSONObject responseJson;
		JSONArray hits;

		if (id == null || type == null) {
			
			q = q.trim();
			hug = determinaHUG(q);
			
			// devuelve null en el caso de que no determine ninguna HUG
			// podría sustituirse por una HUG por defecto
			if (hug == null) {
				
				address = null;
			
			} else {
				
				query = hug[0];
				url = hug[1];
				
				query = ajustaQuery(query, q, no_process, null, null, null, null, null, 1, false, false, false, request);
				responseStr = elastic.search(query, url);
				responseJson = responseStrToJson(responseStr);
				
				hits = getHits(responseJson, null, false);

				// comprueba si la respuesta tiene algún objeto
				if (hits != null && hits.length() == 0) {
					
					address = null;
				
				} else {
					
					address = jsonToAddress(hits.getJSONObject(0), false, false);
				}
			}
		
		} else if (type.equals("refcatastral") && (id.length() >= 2) && StringUtils.isNumeric(id.substring(0,2))) {
			
			if (address != null) {
				
				state = address.getState();
			}

			if (StringUtils.isBlank(numPortal) || numPortal.equalsIgnoreCase("null")) {
				
				numPortal = "0";
			}

			Integer portal = Integer.parseInt(numPortal);
			List<AddressPriorized> list = this.getRefCatastral(id, 1);
			if (!list.isEmpty()) {
				
				address = list.get(0);
				address.setPortalNumber(portal);
			}
		
		} else {
			
			type = type.toLowerCase();
			
			if (!type.equals(Queries.TOPONIMO_BUCKET_NAME) 
					&& !type.equals(Queries.POBLACION_BUCKET_NAME) 
					&& !type.equals(Queries.PORTAL_BUCKET_NAME) 
					&& !type.equals(Queries.EXPENDEDURIA_BUCKET_NAME)
					&& !type.equals(Queries.PUNTO_RECARGA_ELECTRICA_BUCKET_NAME)
					&& !type.equals(Queries.NGBE_BUCKET_NAME)) { // Sirve el bucket name porque es la agrupacion por table_name, que  es el type realmente
				
				// find "normal", con obj_id integer
				hug = Queries.FIND;
				query = hug[0];
				url = hug[1];
				
				
				if (!type.equals(Queries.CODIGO_POSTAL_BUCKET_NAME)) {
					
					query = query.replace("--exclude--", "");
				
				} else {
					
					if (this.POSTAL_CODE_FIND_RESTRICTION.booleanValue()) {

						// Se puede indicar el SEARCH_TYPE aunque no haya pasado por determinaHUG para actuar en el jsonToAddress
						this.SEARCH_TYPE = Queries.SEARCH_TYPE_POSTAL_CODE;
						
						// Busqueda de codigos postales y find / findJsonp ... se comprueba origen
						this.ORIGIN_IS_IN_WITHE_LIST = originIsInWhiteList(request);

						if (this.ORIGIN_IS_IN_WITHE_LIST == null || (this.ORIGIN_IS_IN_WITHE_LIST != null
								&& this.ORIGIN_IS_IN_WITHE_LIST.equals(Boolean.FALSE))) {

							// Se excluye la geometria porque se devuelve el centroide, que se puede obtener
							// de lat / lng
							query = query.replace("--exclude--", "\"_source\":{\"exclude\":[\"geom\"]},");

						} else {

							// El origen esta en la lista, no se excluye la geom porque se puede devolver
							// tal cual
							query = query.replace("--exclude--", "");
						}
					}
				}
				
			} else {
				
				/*
				 * Ahora, el toponimo y el portal_pk tienen obj_id text y tienen su propia query
				 * 
				 * Los toponimos tienen table_name toponimo, poblacion, expendeduria o ngbe
				 * Ojo, porque poblacion no se pisa con documentos de division_administrativa, porque son comunidad autonoma, provincia y municipio (no hay subdivision para entidades poblacionales)
				 */
				hug = Queries.FIND_OBJ_ID_TEXT;
				query = hug[0];
				url = hug[1];
				
				if (type.equals(Queries.PORTAL_BUCKET_NAME)) {
					
					url = url.replace("--index_name--", Queries.PORTAL_INDEX_NAME);
				
				} else {
					
					url = url.replace("--index_name--", Queries.TOPONIMO_INDEX_NAME);
				}
			
			}
			
			query = query.replace("--id--", id);
			query = query.replace("--type--", type);
			
			responseStr = elastic.search(query, url);
			responseJson = responseStrToJson(responseStr);
			
			hits = responseJson.getJSONObject("hits").getJSONArray("hits");
			
			// comprueba si la respuesta tiene algún objeto
			if (hits.length() == 0) {
				
				address = null;
			
			} else {
				
				address = jsonToAddress(hits.getJSONObject(0), false, false);
			}	
		}

		return address;
	}

	/**
	 * Logica para devolver los datos de la llamada a candidates
	 * 
	 * @param q                         Query
	 * @param no_process                Parametro no_process
	 * @param limit                     Numero maximo de resultados
	 * @param cod_postal_filter         Indica el filtro para codigo postal (uno o
	 *                                  varios separados por coma)
	 * @param municipio_filter          Indica el filtro para municipio (uno o
	 *                                  varios separados por coma)
	 * @param provincia_filter          Indica el filtro para provincia (una o
	 *                                  varias separados por coma)
	 * @param comunidad_autonoma_filter Indica el filtro para comunidad autonoma
	 *                                  (una o varias separados por coma)
	 * @param poblacion_filter          Indica el filtro para poblacion (una o
	 *                                  varias separados por coma)
	 * @return Lista de resultados (Address)
	 */
	public List<Address> candidates(String q, String no_process, int limit, String cod_postal_filter,
			String municipio_filter, String provincia_filter, String comunidad_autonoma_filter, String poblacion_filter) {
		
		List<Address> listaAddress = new ArrayList<Address>();
		String[] hug = new String[2];
		String query, url, responseStr;
		JSONObject responseJson;
		JSONArray hits;
		q = q.trim();
		hug = determinaHUG(q);

		// devuelve null en el caso de que no determine ninguna HUG
		// podría sustituirse por una HUG por defecto
		if (hug == null) {
			
			listaAddress = null;
		
		} else {
			
			if ((q.length() >= 2) && StringUtils.isNumeric(q.substring(0,2))) {
				
				listaAddress.addAll(getRefCatastral(q, limit));
			}
			
			if (StringUtils.isBlank(cod_postal_filter) && (q.matches("\\d+") && q.length() >= 2 && q.length() <= 5)) {
				
				cod_postal_filter = q;
			}
			
			query = hug[0];
			url = hug[1];
			
			boolean aggregation = !this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_POSTAL_CODE) && StringUtils.isNotBlank(Queries.AGGREGATION) && Queries.AGGREGATION.equals("true");
			boolean noNumber = this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL_NO_NUMBER);
			
			query = ajustaQuery(query, q, no_process, cod_postal_filter, municipio_filter, provincia_filter,
					comunidad_autonoma_filter, poblacion_filter, limit, true, aggregation, noNumber, null);
			
			responseStr = elastic.search(query, url);
			responseJson = responseStrToJson(responseStr);
			
			hits = getHits(responseJson, limit, aggregation);
			
			// comprueba si la respuesta tiene algún objeto
			if (hits != null && hits.length() > 0) {
				
				for (int i = 0; i < hits.length(); i++) {
					
					Address address = jsonToAddress(hits.getJSONObject(i), true, true);
					listaAddress.add(address);
				}
			}
		
		}
		
		return listaAddress;
	}
	
	/**
	 * Obtiene la referencia catastral
	 * 
	 * @param lon Longitud
	 * @param lat Latitud
	 * @return Resultado (AddressPriorized)
	 */
	public AddressPriorized getRefCatastral(double lon, double lat) {
        
		AddressPriorized address = new AddressPriorized();
        try {
        	
        	address = dataXMLAccess(REF_CATASTRAL_URL_REVERSE + "?SRS=" + SRS_CODE + "&Coordenada_X=" + lon + "&Coordenada_Y=" + lat, address);
        
        } catch (Exception e) {
        	
        	e.printStackTrace();
        }
        
        return address;
    }
	
	/**
	 * Metodo que implementa el servicio de calculo masivo unificado y que hace la
	 * geocodificacion indirecta o directa para cada fila
	 * 
	 * @param filePath Path del fichero
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processUnifiedCSVFile(String filePath) throws IOException  {
		
		log.info("GEOCODIFICACION UNIFICADA: Comienza proceso: " + new Date());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File output = File.createTempFile(dt.format(new Date()) + UNIFIED_OUTPUT_FILE_NAME, CSV_EXTENSION);
		
		File input = new File(filePath);
		CSVReader csvReader = new CSVReader(new FileReader(input), UNIFIED_CSV_SEPARATOR);
		
		List<String[]> rows = new ArrayList<String[]>();
		
		// La primera linea es la cabecera, que se toma y se amplia con las columnas necesarias
		String[] inputRow = csvReader.readNext();
		
		int numColumnasInicial = inputRow.length;
		int numColumnasFinal = numColumnasInicial + 1;
		
		rows.add(getUnifiedCSVHeader(inputRow, numColumnasInicial, numColumnasFinal));
		
		// Ahora se buscan las posiciones que interesan y qye se toman de lo que todavia contiene la cabecera, que es inputRow
		int latPos = getFieldPosition(UNIFIED_CSV_LATITUD, inputRow);
		int lonPos = getFieldPosition(UNIFIED_CSV_LONGITUD, inputRow);
		int tipoViaPos = getFieldPosition(UNIFIED_CSV_TIPO_VIA, inputRow);
		int nombreViaPos = getFieldPosition(UNIFIED_CSV_NOMBRE_VIA, inputRow);
		int portalPkPos = getFieldPosition(UNIFIED_CSV_PORTAL1, inputRow);
		int extensionPos = getFieldPosition(UNIFIED_CSV_PORTAL2, inputRow);
		int municipioPos = getFieldPosition(UNIFIED_CSV_MUNICIPIO, inputRow);
		int codIneMunicipioPos = getFieldPosition(UNIFIED_CSV_COD_INE_MUNICIPIO, inputRow);
		int provinciaPos = getFieldPosition(UNIFIED_CSV_PROVINCIA, inputRow);
		int codigoPostalPos = getFieldPosition(UNIFIED_CSV_CODPOSTAL, inputRow);
		int refCatastralPos = getFieldPosition(UNIFIED_CSV_REFCAT14, inputRow);
		int poblacionPos = getFieldPosition(UNIFIED_CSV_POBLACION, inputRow);
		int observacionesPos = numColumnasInicial; // Ultima columna para las observaciones de cada proceso para cada fila
		
		Address address = null;
		int count = 1;
		String lat = null;
		String lon = null;
		Double latDouble = null;
		Double lonDouble = null;
		String tipoVia = null;
		String portalPk = null;
		String extension = null;
		String codIneMun = null;
		String codigoPostal = null;
		String municipio = null;
		String provincia = null;
		String poblacion = null;
		String[] outputRow = null;
		
		List<String> pkRoadTypes = Arrays.asList(UNIFIED_PK_ROAD_TYPES.split(","));
		
		while ((inputRow = csvReader.readNext()) != null && count <= UNIFIED_MAX_ROWS) {
			
			outputRow = initializeOutputRow(inputRow, numColumnasInicial, numColumnasFinal);
			lat = outputRow[latPos];
			lon = outputRow[lonPos];
			
			// Primero se comprueba lat / lon para indirecta
			if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lon)) {
				
				// Indirecta
				
				latDouble = getDouble(lat);
				lonDouble = getDouble(lon);
				
				if (latDouble != null && lonDouble != null) {
					
					address = reverseGeocode(lonDouble.doubleValue(), latDouble.doubleValue());
					
					// En este caso no hay campo donde indicar que no hay resultados asi que solo se hace algo si los hay
					if (address != null) {
						
						outputRow[tipoViaPos] = address.getTip_via();
						outputRow[nombreViaPos] = address.getAddress();
						
						if (address.getPortalNumber() != null) {
							
							outputRow[portalPkPos] = address.getPortalNumber().toString();
						}
						
						outputRow[extensionPos] = getExtension(address.getExtension());
						
						outputRow[municipioPos] = address.getMuni();
						
						
						if (StringUtils.isNotBlank(address.getMuniCode())) {
							
							outputRow[codIneMunicipioPos] = address.getMuniCode();
						}
						
						outputRow[provinciaPos] = address.getProvince();
						
						if (StringUtils.isNotBlank(address.getPostalCode())) {
							
							outputRow[codigoPostalPos] = address.getPostalCode();
						}
						
						if (StringUtils.isNotBlank(address.getPoblacion())) {
							
							outputRow[poblacionPos] = address.getPoblacion();
						}
						
						if (refCatastralPos != -1 && StringUtils.isNotBlank(address.getRefCatastral())) {
							
							outputRow[refCatastralPos] = address.getRefCatastral();
						}
						
						outputRow[latPos] = String.valueOf(address.getLat());
						outputRow[lonPos] = String.valueOf(address.getLng());
						outputRow[observacionesPos] = UNIFIED_REVERSE_RESULT;
						
					} else {
						
						outputRow[observacionesPos] = UNIFIED_REVERSE_NO_RESULT;
						
						// Se parsean los datos de salida para conseguir el punto decimal.
						outputRow[latPos] = String.valueOf(latDouble);
						outputRow[lonPos] = String.valueOf(lonDouble);
					}
					
				}
			
			} else {
				
				// Directa
				
				// Se hace la misma comprobacion que en el proceso normal
				String nombreVia = outputRow[nombreViaPos];
				if (StringUtils.isNotBlank(nombreVia)) {
					
					// Se comprueba ahora que no se este buscando un pk con tipo de via
					
					portalPk = outputRow[portalPkPos];
					extension = outputRow[extensionPos];
					
					// Validaciones
					if (StringUtils.isNumeric(portalPk)) {
						
						tipoVia = outputRow[tipoViaPos];
						
						// Si el tipo de via no es de los de pk
						if ((StringUtils.isNotBlank(tipoVia) && !pkRoadTypes.contains(tipoVia)) || StringUtils.isBlank(tipoVia)) {
							
							codigoPostal = inputRow[codigoPostalPos];
							
							municipio = null;
							provincia = null;
							if (StringUtils.isNotBlank(codIneMun)) {
								
								if (codIneMun.length() == 4) {
								
									codIneMun = "0" + codIneMun;
								}
							
							} else {
								
								municipio = inputRow[municipioPos];
								provincia = inputRow[provinciaPos];
							}
							
							poblacion = outputRow[poblacionPos];
							
							if (StringUtils.isNotBlank(codigoPostal) && StringUtils.isBlank(poblacion)) {
								
								// Si se rellena CCPP hay que rellenar tambien Poblacion
								outputRow[observacionesPos] = UNIFIED_DIRECT_VAL_PC_NO_LOCALITY;
							
							} else if (StringUtils.isBlank(codigoPostal) && StringUtils.isNotBlank(poblacion) && StringUtils.isBlank(municipio)) {
								
								// Si se rellena Poblacion hay que rellenar tambien Municipio
								// Para que no se mezcle con el anterior se pone la primera condicion referida al codigo postal
								outputRow[observacionesPos] = UNIFIED_DIRECT_VAL_LOCALITY_NO_MUNICIPALITY;
								
							} else {
								
								codIneMun = outputRow[codIneMunicipioPos];
								
								// Se pasa el portalpk a integer primero para quitar los posibles 0s que vengan delante
								address = searchCsvFindListAddress(tipoVia, nombreVia, Integer.valueOf(portalPk).toString(),
										municipio, provincia, codIneMun, extension, codigoPostal, poblacion);
								
								if (StringUtils.isNotBlank(address.getId())) {
									
									outputRow[latPos] = String.valueOf(address.getLat());
									outputRow[lonPos] = String.valueOf(address.getLng());
									
									if (StringUtils.isBlank(municipio)) {
										
										outputRow[municipioPos] = address.getMuni();
									}
									
									if (StringUtils.isBlank(provincia)) {
										
										outputRow[provinciaPos] = address.getProvince();
									}
									
									if (StringUtils.isBlank(codIneMun) && StringUtils.isNotBlank(address.getMuniCode())) {
										
										outputRow[codIneMunicipioPos] = address.getMuniCode();
									}
									
									if (refCatastralPos != -1 && StringUtils.isNotBlank(address.getRefCatastral())) {
										
										outputRow[refCatastralPos] = address.getRefCatastral();
									}
									
									if (StringUtils.isNotBlank(address.getPostalCode())) {
										
										outputRow[codigoPostalPos] = address.getPostalCode();
									}
									
									if (StringUtils.isNotBlank(address.getPoblacion())) {
										
										outputRow[poblacionPos] = address.getPoblacion();
									}
								}
								
								outputRow[observacionesPos] = address.getStateMsg();
							}
							
						} else {
							
							outputRow[observacionesPos] = UNIFIED_DIRECT_TIPO_VIA_PK;
						}
					
					} else {
						
						outputRow[observacionesPos] = UNIFIED_DIRECT_NUMBER_NOT_VALID;
					}
				}
			}
			
			rows.add(outputRow);
			
			count++;
		}
		
		csvReader.close();
		
		ByteArrayOutputStream csvSStream = new ByteArrayOutputStream();
        OutputStreamWriter csvStreamWriter = new OutputStreamWriter(csvSStream);
        CSVWriter csvWriter = new CSVWriter(csvStreamWriter, UNIFIED_CSV_SEPARATOR);
        csvWriter.writeAll(rows);
        csvWriter.close();
		
        FileUtils.writeByteArrayToFile(output, csvSStream.toByteArray());
		
		log.info("GEOCODIFICACION UNIFICADA: Finaliza proceso: " + new Date());
		
		return output.getAbsolutePath();
	}
	
	/**
	 * Geocoder inverso
	 * 
	 * @param lon Longitud
	 * @param lat Latitud
	 * @return Resultado (Address)
	 */
	public Address reverseGeocode(double lon, double lat) {
		Address address = null;
		String[] hug = Queries.REVERSE_GEOCODE_PORTAL;
		String query = hug[0];
		String url = hug[1];
		query = query.replace("--lat--", lat + "");
		query = query.replace("--lon--", lon + "");
		String responseStr = elastic.search(query, url);
		JSONObject responseJson = responseStrToJson(responseStr);
		JSONArray hits = responseJson.getJSONObject("hits").getJSONArray("hits");
		if (hits.length() > 0) {
			JSONObject result = hits.getJSONObject(0);
			address = jsonToAddress(result, true, false);
			address.setCountryCode("011");
		}
		
		return address;
	}

	/**
	 * @deprecated
	 * Metodo que implementa el servicio de calculo masivo de coordenadas para
	 * fichero CSV
	 * 
	 * @param filePath Path del fichero
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processCSVFindFile(String filePath) throws IOException  {
		
		log.info("GEOCODIFICACION DIRECTA: Comienza proceso: " + new Date());
		
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File output = File.createTempFile(dt.format(new Date()) + XSLX_FIND_OUTPUT_FILE_NAME, CSV_EXTENSION);
		
		File input = new File(filePath);
		CSVReader csvReader = new CSVReader(new FileReader(input), CSV_SEPARATOR);
		
		List<String[]> rows = new ArrayList<String[]>();
		String[] header = new String[16];
		header[0] = "Id.";
		header[1] = "Num. resultado";
		header[2] = "Tipo vía origen";
		header[3] = "Dirección origen";
		header[4] = "Coincidencia";
		header[5] = "Tipo vía";
		header[6] = "Nombre vía";
		header[7] = "Portal/PK";
		header[8] = "Extensión";
		header[9] = "Código postal";
		header[10] = "Municipio";
		header[11] = "Código Municipio";
		header[12] = "Provincia";
		header[13] = "Código Provincia";
		header[14] = "Latitud";
		header[15] = "Longitud";
		rows.add(header);
		
		String id = null;
		String tipoVia = null;
		String nombreVia = null;
		String portalPk = null;
		String municipio = null;
		String provincia = null;
		String[] inputRow = null;
		String[] ouputRow = null;
		Address address = null;
		int count = 1;
		// Se obvia la primera línea que es la cabecera
		csvReader.readNext();
		while ((inputRow = csvReader.readNext()) != null && count <= CSVFINDLIST_MAX_ROWS) {
			
			id = inputRow[0];
			tipoVia = inputRow[1];
			nombreVia = inputRow[2];
			portalPk = inputRow[3];
			municipio = inputRow[4];
			provincia = inputRow[5];
			
			ouputRow = new String[16];
			
			if (StringUtils.isNotBlank(nombreVia)) {
				
				// Validaciones
				if (!StringUtils.isNumeric(portalPk)) {
					
					address = new Address();
					address.setStateMsg(Configuration.getReadedProperty(Configuration.CSVFINDLIST_NUMBER_NOT_VALID));
				
				} else {
				
					address = searchCsvFindListAddress(tipoVia, nombreVia, portalPk, municipio, provincia, null, null, null, null);
				}
				
				if (StringUtils.isNotBlank(id)) {
					
					ouputRow[0] = id;
				}
				
				ouputRow[1] = String.valueOf(0);
				ouputRow[2] = tipoVia;
				ouputRow[3] = nombreVia;
				ouputRow[4] = address.getStateMsg();
				
				if (StringUtils.isNotBlank(address.getId())) {
					
					ouputRow[1] = String.valueOf(1);
					ouputRow[5] = address.getTip_via();
					ouputRow[6] = address.getAddress();
				
					if (StringUtils.isNotBlank(portalPk)) {
						
						ouputRow[7] = address.getPortalNumber().toString();
						ouputRow[8] = getExtension(address.getExtension());
						ouputRow[9] = address.getPostalCode();
						ouputRow[10] = address.getMuni();
						ouputRow[11] = address.getMuniCode();
						ouputRow[12] = address.getProvince();
						ouputRow[13] = address.getProvinceCode();
						ouputRow[14] = String.valueOf(address.getLat());
						ouputRow[15] = String.valueOf(address.getLng());
					}
				}
				
				rows.add(ouputRow);
			}
			
			count++;
		}
		
		csvReader.close();
		
		ByteArrayOutputStream csvSStream = new ByteArrayOutputStream();
        OutputStreamWriter csvStreamWriter = new OutputStreamWriter(csvSStream);
        CSVWriter csvWriter = new CSVWriter(csvStreamWriter, CSV_SEPARATOR);
        csvWriter.writeAll(rows);
        csvWriter.close();
		
        FileUtils.writeByteArrayToFile(output, csvSStream.toByteArray());
        
        log.info("GEOCODIFICACION DIRECTA: Finaliza proceso: " + new Date());
		
		return output.getAbsolutePath();
	}
	
	/**
	 * @deprecated
	 * Metodo que implementa el servicio de calculo masivo de coordenadas para un fichero en formato XLSX
	 * 
	 * @param filePath    Path del fichero
	 * @param httpRequest Peticion HTTP
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processXSLFindFile(String filePath, HttpServletRequest httpRequest) throws IOException  {
		log.info("GEOCODIFICACION DIRECTA: Comienza proceso: " + new Date());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File distances = File.createTempFile(dt.format(new Date()) + XSLX_FIND_OUTPUT_FILE_NAME, XSLX_EXTENSION);
		Workbook outworkbook;
		outworkbook = new XSSFWorkbook();
		Sheet outsheet = outworkbook.createSheet("Coordenadas geográficas");
	
		Font font = outworkbook.createFont();
	    font.setFontHeightInPoints((short)10);
	    font.setFontName("Arial");
	    font.setColor(IndexedColors.WHITE.getIndex());
	    font.setBoldweight((short) 700);
	    font.setItalic(false);

	    CellStyle style = outworkbook.createCellStyle();
	    style.setFillBackgroundColor(IndexedColors.LAVENDER.getIndex());
	    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    style.setAlignment(CellStyle.ALIGN_CENTER);
	    style.setFont(font);
		
		Row houtrow = outsheet.createRow(0);
		Cell houtcell0 = houtrow.createCell(0);
		houtcell0.setCellValue("Id.");
		houtcell0.setCellStyle(style);
		
		Cell houtcell1 = houtrow.createCell(1);
		houtcell1.setCellValue("Num. resultado");
		houtcell1.setCellStyle(style);
		
		Cell houtcell2 = houtrow.createCell(2);
		houtcell2.setCellValue("Tipo vía origen");
		houtcell2.setCellStyle(style);
		
		Cell houtcell3 = houtrow.createCell(3);
		houtcell3.setCellValue("Dirección origen");
		houtcell3.setCellStyle(style);
		
		Cell houtcell4 = houtrow.createCell(4);
		houtcell4.setCellValue("Coincidencia");
		houtcell4.setCellStyle(style);
		
		Cell houtcell5 = houtrow.createCell(5);
		houtcell5.setCellValue("Tipo vía");
		houtcell5.setCellStyle(style);
		
		Cell houtcell6 = houtrow.createCell(6);
		houtcell6.setCellValue("Nombre vía");
		houtcell6.setCellStyle(style);
		
		Cell houtcell7 = houtrow.createCell(7);
		houtcell7.setCellValue("Portal/PK");
		houtcell7.setCellStyle(style);
		
		Cell houtcell8 = houtrow.createCell(8);
		houtcell8.setCellValue("Municipio");
		houtcell8.setCellStyle(style);

		Cell houtcell9 = houtrow.createCell(9);
		houtcell9.setCellValue("Provincia");
		houtcell9.setCellStyle(style);
		
		Cell houtcell10 = houtrow.createCell(10);
		houtcell10.setCellValue("Latitud");
		houtcell10.setCellStyle(style);
		
		Cell houtcell11 = houtrow.createCell(11);
		houtcell11.setCellValue("Longitud");
		houtcell11.setCellStyle(style);
		
		FileInputStream fileis = new FileInputStream(filePath);
		Workbook workbook =  new XSSFWorkbook(fileis);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();

		int total = 0;
		int procesadas = 0;

		Row row = null;
		Cell cell = null;
		Iterator<Cell> cellIterator = null;
		Row outRow = null;
		
		Address address = null;
		
		Cell outCell0 = null;
		Cell outCell1 = null;
		Cell outCell2 = null;
		Cell outCell3 = null;
		Cell outCell4 = null;
		Cell outCell5 = null;
		Cell outCell6 = null;
		Cell outCell7 = null;
		Cell outCell8 = null;
		Cell outCell9 = null;
		Cell outCell10 = null;
		Cell outCell11 = null;
		
		while (rowIterator.hasNext() && total < (CSVFINDLIST_MAX_ROWS + LINEAS_XSLX_CABECERA)) {
			
			// Se empieza por la fila correcta
			total++;
			row = (Row) rowIterator.next();
			if (total <= LINEAS_XSLX_CABECERA){
				
				continue;
			}
			
			String id = null;
			String tipoVia = null;
			String nombreVia = null;
			Integer portalPk = null;
			String portalPkStr = null;
			String municipio = null;
			String provincia = null;
			//int numResultado;
			
			// Se obtienen los datos de las columnas para la fila
			cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				
				cell = (Cell) cellIterator.next();
				int type = cell.getCellType();
				if (type == Cell.CELL_TYPE_NUMERIC) {
					
					switch (cell.getColumnIndex()){
						case 0:
							id = ((int) cell.getNumericCellValue()) + "";
							break;
						case 3:
							portalPk = (int) cell.getNumericCellValue();
							break;
						default:
							break;
					}
				
				} else if (type == Cell.CELL_TYPE_STRING) {
					
					switch (cell.getColumnIndex()) {
						case 0:
							id = cell.getStringCellValue();
							break;
						case 1:
							tipoVia = cell.getStringCellValue();
							break;
						case 2:
							nombreVia = cell.getStringCellValue();
							break;
						case 3:
							portalPkStr = cell.getStringCellValue();
							break;
						case 4:
							municipio = cell.getStringCellValue();
							break;
						case 5:
							provincia = cell.getStringCellValue();
							break;	
						default:
							break;
					}
				}
			}
			
			// Validar completitud de otros campos?
			if (StringUtils.isNotBlank(nombreVia)) {
		
				procesadas++;
				
				// Validaciones
				if (StringUtils.isNotBlank(portalPkStr) && portalPk == null) {
					
					address = new Address();
					address.setStateMsg(Configuration.getReadedProperty(Configuration.CSVFINDLIST_NUMBER_NOT_VALID));
				
				} else {
					
					String portalPkAux = null;
					if (portalPk != null) {
						portalPkAux = portalPk.toString();
					}
					address = searchCsvFindListAddress(tipoVia, nombreVia, portalPkAux, municipio, provincia, null, null, null, null);
				}
				
				outRow = outsheet.createRow(procesadas);
				
				if (StringUtils.isNotBlank(id)) {
					
					outCell0 = outRow.createCell(0);
					outCell0.setCellValue(id);
				}

				outCell1 = outRow.createCell(1);
				outCell1.setCellValue(0);

				outCell2 = outRow.createCell(2);
				outCell2.setCellValue(tipoVia);

				outCell3 = outRow.createCell(3);
				outCell3.setCellValue(nombreVia);

				outCell4 = outRow.createCell(4);
				outCell4.setCellValue(address.getStateMsg());
				
				if (!StringUtils.isBlank(address.getId())) {
					
					outCell1.setCellValue(1);
					
					outCell5 = outRow.createCell(5);
					outCell5.setCellValue(address.getTip_via());
					
					outCell6 = outRow.createCell(6);
					outCell6.setCellValue(address.getAddress());

					if (portalPk != null) {
						
						outCell7 = outRow.createCell(7);
						outCell7.setCellValue(address.getPortalNumber().intValue());
					}
					
					outCell8 = outRow.createCell(8);
					outCell8.setCellValue(address.getMuni());

					outCell9 = outRow.createCell(9);
					outCell9.setCellValue(address.getProvince());

					outCell10 = outRow.createCell(10);
					outCell10.setCellValue(String.valueOf(address.getLat()));

					outCell11 = outRow.createCell(11);
					outCell11.setCellValue(String.valueOf(address.getLng()));
				}
				
			}
			
		}

		outsheet.autoSizeColumn(0,true);
		outsheet.autoSizeColumn(1,true);
		outsheet.autoSizeColumn(2,true);
		outsheet.autoSizeColumn(3,true);
		outsheet.autoSizeColumn(4,true);
		outsheet.autoSizeColumn(5,true);
		outsheet.autoSizeColumn(6,true);
		outsheet.autoSizeColumn(7,true);
		outsheet.autoSizeColumn(8,true);
		outsheet.autoSizeColumn(9,true);
		outsheet.autoSizeColumn(10,true);
		outsheet.autoSizeColumn(11,true);
		
		
		try {
			
			FileOutputStream out = 
					new FileOutputStream(distances);
			outworkbook.write(out);
			out.close();
			
		} catch (Exception e) {
			
			log.error("GEOCODIFICACION DIRECTA: Se ha producido un error al montar el fichero de salida: ", e);
		}
		
		log.info("GEOCODIFICACION DIRECTA: Finaliza proceso: " + new Date());
		
		return distances.getAbsolutePath();
	}
	
	/**
	 * Busca la direccion segun los datos del fichero del servicio de calculo masivo
	 * de coordenadas (servicio csvfindlist)
	 * 
	 * @param tipoVia   Tipo de via
	 * @param nombreVia Nombre de la via
	 * @param portalPk  Portal o Punto Kilometrico
	 * @param municipio Nombre del municipio
	 * @param provincia Nombre de la provincia
	 * @param codIneMun Codigo INE del municipio
	 * @param extension Extension
	 * @param codigoPostal Codigo Postal
	 * @param poblacion Nombre de la poblacion
	 * @return Direccion
	 */
	private Address searchCsvFindListAddress(String tipoVia, String nombreVia, String portalPk, String municipio,
			String provincia, String codIneMun, String extension, String codigoPostal, String poblacion) {
		
		Address address = null;
		String elasticUrl = null;
		String elasticQuery = null;
		
		/*
			
		 	
		 	-- Caso 3: Si viene tipo de via sin rellenar y viene portal/punto kilometrico sin rellenar -> Se busca toponimo
		*/
		/*
		 * La tipologia sobre la que se realiza la consulta depende de los siguientes casos:
		 * 
		 * Caso 1: Si viene tipo de via relleno y portal/punto kilometrico relleno -> Se busca realmente portal
		 * Caso 2: Si viene tipo de via sin rellenar y viene portal/punto kilometrico relleno -> Se busca realmente punto kilometrico
		 * -- DESCARTADA-- Caso 3: Si viene tipo de via sin rellenar y viene portal/punto kilometrico sin rellenar -> Se busca toponimo
		 */
		
		String codPostalFragment = "";
		String municipioFragment = "";
		String provinciaFragment = "";
		String poblacionFragment = "";
		String codIneMunFragment = "";
		
		/*
		 * El filtro sobre territorio se aplica una sola vez segun la siguiente lista:
		 * 
		 * 1) Codigo Postal && Poblacion
		 * 2) Poblacion & Municipio
		 * 3) Municpio || (Municipio & Provincia)
		 * 4) IneMun
		 */
		if (StringUtils.isNotBlank(codigoPostal) && StringUtils.isNotBlank(poblacion)) {
			
			codPostalFragment = Queries.CSVFINDLIST_CODPOSTAL_QUERY_FRAGMENT.replace("--codpostal--", codigoPostal);
			poblacionFragment = Queries.CSVFINDLIST_POBLACION_QUERY_FRAGMENT.replace("--poblacion--", poblacion);
			
		} else if (StringUtils.isNotBlank(poblacion) && StringUtils.isNotBlank(municipio)) {
			
			poblacionFragment = Queries.CSVFINDLIST_POBLACION_QUERY_FRAGMENT.replace("--poblacion--", poblacion);
			municipioFragment = Queries.CSVFINDLIST_MUNICIPIO_QUERY_FRAGMENT.replace("--municipio--", municipio);			
		
		} else if (StringUtils.isNotBlank(municipio)) {
		
			municipioFragment = Queries.CSVFINDLIST_MUNICIPIO_QUERY_FRAGMENT.replace("--municipio--", municipio);
			
			// Se trata el fragmento de provincia (ya que no es realmente un dato obligatorio)
			if (StringUtils.isNotBlank(provincia)) {
				
				provinciaFragment = Queries.CSVFINDLIST_PROVINCIA_QUERY_FRAGMENT.replace("--provincia--", provincia);
			}
		
		} else if (StringUtils.isNotBlank(codIneMun)) {

			codIneMunFragment = Queries.CSVFINDLIST_CODINEMUN_QUERY_FRAGMENT.replace("--codinemun--", codIneMun);
			
		} 
		
		// Se limpia el nombre de la via
		nombreVia = cleanNombreVia(nombreVia);
		
		if (StringUtils.isNotBlank(tipoVia) && StringUtils.isNotBlank(portalPk)) {
			
			String extensionFragment = "";
			if (StringUtils.isNotBlank(extension)) {
				
				extensionFragment = Queries.CSVFINDLIST_EXTENSION_QUERY_FRAGMENT.replace("--extension--", extension);
			}
			
			// Caso 1
			this.CSVFINDLIST_TYPE = Queries.CSVFINDLIST_TYPE_PORTAL;
			elasticUrl = Queries.CSVFINDLIST_PORTAL_PK_URL;
			elasticQuery = Queries.CSVFINDLIST_PORTAL_QUERY
					.replace("--tipoVia--", tipoVia)
					.replace("--nombreVia--", nombreVia)
					.replace("--numero--", portalPk)
					.replace("--codpostal--", codPostalFragment)
					.replace("--municipio--", municipioFragment)
					.replace("--poblacion--", poblacionFragment)
					.replace("--provincia--", provinciaFragment)
					.replace("--extension--", extensionFragment)
					.replace("--codinemun--", codIneMunFragment);

		} else if (StringUtils.isBlank(tipoVia) && StringUtils.isNotBlank(portalPk)) {
			
			// Caso 2
			this.CSVFINDLIST_TYPE = Queries.CSVFINDLIST_TYPE_PK;
			elasticUrl = Queries.CSVFINDLIST_PORTAL_PK_URL;
			elasticQuery = Queries.CSVFINDLIST_PK_QUERY
					.replace("--nombreVia--", nombreVia)
					.replace("--numero--", portalPk)
					.replace("--codpostal--", codPostalFragment)
					.replace("--municipio--", municipioFragment)
					.replace("--poblacion--", poblacionFragment)
					.replace("--provincia--", provinciaFragment)
					.replace("--codinemun--", codIneMunFragment);
			
		}
		/* -- DESCARTADA-- la busqueda de toponimos
		else if (StringUtils.isBlank(tipoVia) && StringUtils.isBlank(portalPk)) {
			
			// Caso 3
			this.CSVFINDLIST_TYPE = Queries.CSVFINDLIST_TYPE_TOPONIMO;
			elasticUrl = Queries.CSVFINDLIST_TOPONIMO_URL;
			elasticQuery = Queries.CSVFINDLIST_TOPONIMO_QUERY.replace("--nombre--", nombreVia)
					.replace("--municipio--", municipioFragment).replace("--provincia--", provinciaFragment);
			
		}
		*/
		
		try {
			
			address = doCsvFindListQuery(elasticUrl, elasticQuery);
			
		} catch (Exception e) {
			
			log.error("GEOCODIFICACION DIRECTA: Se ha producido un error al realizar la consulta sobre Elasticsearch: ", e);
			address = new Address();
			address.setStateMsg(UNIFIED_DIRECT_ERROR);
		}
		
		return address;
	}
	
	/**
	 * Limpia el nombre de la vía y le quita los caracteres que pueden provocar
	 * errores al ejecutar la búsqueda en Elasticsearch vía HTTP
	 * 
	 * @param nombreVia Nombre de la vía
	 * @return Nombre de la vía limpio para poder ser un término de búsqueda
	 *         correcto para Elasticsearch
	 */
	private String cleanNombreVia(String nombreVia) {
		
		if (StringUtils.isNotBlank(nombreVia)) {
			
			return nombreVia.replace("\"", "").replace("“", "").replace("”", "");
		} 
		
		return null;
	}
	
	/**
	 * Realiza la query para el servicio de calculo masivo de coordenadas
	 * (csvfindlist)
	 * 
	 * @param elasticUrl   URL a la que hacer la query
	 * @param elasticQuery Query
	 * @return Address
	 */
	private Address doCsvFindListQuery(String elasticUrl, String elasticQuery) {
		
		Address address = null;
		
		String elasticResult = elastic.search(elasticQuery, elasticUrl);
		JSONObject responseJson = responseStrToJson(elasticResult);
		JSONArray hits = getHits(responseJson, null, false);
		JSONObject hit = null;
		JSONArray sort = null;
		Double sortTipoVia = null;
		Double sortNumero = null;
		
		if (hits.length() == 1) { // Ya se controla en la query que venga un solo resultado
			
			hit = hits.getJSONObject(0);
			
			address = jsonToAddress(hit, false, false);
			
			if (address != null) {	
				
				// Para los casos Portal y PK, se comprueba el sort
				
				if (this.CSVFINDLIST_TYPE.equals(Queries.CSVFINDLIST_TYPE_PORTAL)) {
					
					
					if (!hit.isNull("highlight")) {
					
						sort = hit.getJSONArray("sort");
						sortNumero = sort.getDouble(1); // el segundo número indica si coincide en número (1 si, 0 no)
						sortTipoVia = sort.getDouble(2); // el tercero si coincide en el tipo de vía (0 no, > 0 si)
						
						if (sortNumero.doubleValue() == 1 && sortTipoVia.doubleValue() > 0) {
							
							// Exacta
							address.setStateMsg(Configuration.getReadedProperty(Configuration.CSVFINDLIST_EXACT_RESULT));
						
						} else if (sortNumero.doubleValue() == 1 && sortTipoVia.doubleValue() == 0) {
							
							// Tipo de via distinta
							//address.setStateMsg(UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE); // Ya se comprueba con el highlight
						
						} else if (sortNumero.doubleValue() == 0 && sortTipoVia.doubleValue() > 0) {
							
							// Numero distinto obtenido, por tanto mas cercano
							address.setStateMsg(UNIFIED_DIRECT_PORTAL_NEAREST);
						
						} else {
						
							// Ni tipo de via ni numero (se obtiene mas cercano)
							address.setStateMsg(UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE_AND_NEAREST);
						}
					
					} else {
						
						// Si no viene el highlight es tipo de vía diferente
						address.setStateMsg(UNIFIED_DIRECT_PORTAL_DIFFERENT_ROAD_TYPE);
					}
					
				} else if (this.CSVFINDLIST_TYPE.equals(Queries.CSVFINDLIST_TYPE_PK)) {
				
					sort = hit.getJSONArray("sort");
					sortNumero = sort.getDouble(1); // el segundo número indica si coincide en número (1 si, 0 no)
					
					if (sortNumero.doubleValue() == 1) {
						
						// Exacta
						address.setStateMsg(UNIFIED_DIRECT_EXACT_RESULT);
						
					} else {
						
						// No coincide el numero por lo que se toma el mas cercano
						address.setStateMsg(UNIFIED_DIRECT_PK_NEAREST);
					}
					
				} else if (this.CSVFINDLIST_TYPE.equals(Queries.CSVFINDLIST_TYPE_TOPONIMO)) {
					
					address.setStateMsg(UNIFIED_DIRECT_EXACT_RESULT);
				}
			
			} else {
				
				address = new Address();
				address.setStateMsg(UNIFIED_DIRECT_NO_RESULT);
			}
			
		} else {
			
			address = new Address();
			address.setStateMsg(UNIFIED_DIRECT_NO_RESULT);
		}
		
		return address;
	}
	
	/**
	 * Metodo que implementa el servicio de calculo masivo de direcciones para
	 * fichero CSV
	 * 
	 * @param filePath Path del fichero
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processCSVReverseFile(String filePath) throws IOException  {
		
		log.info("GEOCODIFICACION INVERSA: Comienza proceso: " + new Date());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File output = File.createTempFile(dt.format(new Date()) + XSLX_REVERSE_OUTPUT_FILE_NAME, CSV_EXTENSION);
		
		File input = new File(filePath);
		CSVReader csvReader = new CSVReader(new FileReader(input), CSV_SEPARATOR);
		
		List<String[]> rows = new ArrayList<String[]>();
		String[] header = new String[10];
		header[0] = "Latitud origen";
		header[1] = "Longitud origen";
		header[2] = "Latitud";
		header[3] = "Longitud";
		header[4] = "Tipo vía";
		header[5] = "Dirección";
		header[6] = "Número portal";
		header[7] = "Municipio";
		header[8] = "Provincia";
		header[9] = "Observaciones";
		rows.add(header);
		
		String lat = null;
		String lon = null;
		Double latDouble = null;
		Double lonDouble = null;
		String[] inputRow = null;
		String[] ouputRow = null;
		Address address = null;
		int count = 1;
		// Se obvia la primera línea que es la cabecera
		csvReader.readNext();
		while ((inputRow = csvReader.readNext()) != null && count <= CSVREVERSEFINDLIST_MAX_ROWS) {
		
			lat = inputRow[0];
			lon = inputRow[1];
			
			if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lon)) {
				
				latDouble = getDouble(lat);
				lonDouble = getDouble(lon);
				
				if (latDouble != null && lonDouble != null) {
					
					ouputRow = new String[10];
					ouputRow[0] = lat;
					ouputRow[1] = lon;
					
					address = reverseGeocode(lonDouble.doubleValue(), latDouble.doubleValue());
					
					if (address != null) {
						
						ouputRow[2] = String.valueOf(address.getLat());
						ouputRow[3] = String.valueOf(address.getLng());
						ouputRow[4] = address.getTip_via();
						ouputRow[5] = address.getAddress();
						
						if (address.getPortalNumber() != null) {
							
							ouputRow[6] = address.getPortalNumber().toString();
						}
						
						ouputRow[7] = address.getMuni();
						ouputRow[8] = address.getProvince();
					
					} else {
						
						ouputRow[9] = Configuration.getReadedProperty(Configuration.CSVREVERSEFINDLIST_NO_RESULT);
					}
					
					rows.add(ouputRow);
				}
			}
			
			count++;
		}
		
		csvReader.close();
		
		ByteArrayOutputStream csvSStream = new ByteArrayOutputStream();
        OutputStreamWriter csvStreamWriter = new OutputStreamWriter(csvSStream);
        CSVWriter csvWriter = new CSVWriter(csvStreamWriter, CSV_SEPARATOR);
        csvWriter.writeAll(rows);
        csvWriter.close();
		
        FileUtils.writeByteArrayToFile(output, csvSStream.toByteArray());
		
		log.info("GEOCODIFICACION INVERSA: Finaliza proceso: " + new Date());
		
		return output.getAbsolutePath();
	}
	
	/**
	 * @deprecated
	 * Metodo que implementa el servicio de calculo masivo para SETELECO y que
	 * realiza los dos métodos, directo e indirecto, según los datos del propio CSV
	 * 
	 * @param filePath Path del fichero
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processSETELECOCSVFile(String filePath) throws IOException  {
		
		log.info("GEOCODIFICACION SETELECO: Comienza proceso: " + new Date());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File output = File.createTempFile(dt.format(new Date()) + SETELECO_OUTPUT_FILE_NAME, CSV_EXTENSION);
		
		File input = new File(filePath);
		CSVReader csvReader = new CSVReader(new FileReader(input), CSV_SEPARATOR);
		
		List<String[]> rows = new ArrayList<String[]>();
		
		// La cabecera es la misma, se anyade ya al resultado en primer lugar
		String[] inputRow = csvReader.readNext();
		rows.add(inputRow);
		
		// Ahora se buscan las posiciones que interesan y qye se toman de lo que todavia contiene la cabecera, que es inputRow
		int latPos = getFieldPosition(SETELECO_CSV_LATITUD, inputRow);
		int lonPos = getFieldPosition(SETELECO_CSV_LONGITUD, inputRow);
		int tipoViaPos = getFieldPosition(SETELECO_CSV_TIPO_VIA, inputRow);
		int nombreViaPos = getFieldPosition(SETELECO_CSV_NOMBRE_VIA, inputRow);
		int portalPkPos = getFieldPosition(SETELECO_CSV_PORTAL1_PK, inputRow);
		int municipioPos = getFieldPosition(SETELECO_CSV_MUNICIPIO, inputRow);
		int codIneMunicipioPos = getFieldPosition(SETELECO_CSV_COD_INE_MUNICIPIO, inputRow);
		int provinciaPos = getFieldPosition(SETELECO_CSV_PROVINCIA, inputRow);
		int codigoPostalPos = getFieldPosition(SETELECO_CSV_CODPOSTAL, inputRow);
		
		Address address = null;
		int count = 1;
		String lat = null;
		String lon = null;
		Double latDouble = null;
		Double lonDouble = null;
		StringBuilder portalpk = null;
		String tipoVia = null;
		String portalPk = null;
		String codIneMun = null;
		String municipio = null;
		String provincia = null;
		
		while ((inputRow = csvReader.readNext()) != null && count <= SETELECO_MAX_ROWS) {
			
			lat = inputRow[latPos];
			lon = inputRow[lonPos];
			
			// Primero se comprueba lat / lon para indirecta
			if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lon)) {
				
				// Indirecta
				
				latDouble = getDouble(lat);
				lonDouble = getDouble(lon);
				
				if (latDouble != null && lonDouble != null) {
					
					address = reverseGeocode(lonDouble.doubleValue(), latDouble.doubleValue());
					
					// En este caso no hay campo donde indicar que no hay resultados asi que solo se hace algo si los hay
					if (address != null) {
						
						inputRow[tipoViaPos] = address.getTip_via();
						inputRow[nombreViaPos] = address.getAddress();
						
						if (address.getPortalNumber() != null) {
							
							portalpk = new StringBuilder(address.getPortalNumber().toString());
							portalpk.append(getExtensionBlank(address.getExtension()));
							inputRow[portalPkPos] = portalpk.toString();
						}
						
						inputRow[municipioPos] = address.getMuni();
						
						if (StringUtils.isNotBlank(address.getMuniCode())) {
							
							inputRow[codIneMunicipioPos] = address.getMuniCode();
						}
						
						inputRow[provinciaPos] = address.getProvince();

						if (StringUtils.isNotBlank(address.getPostalCode())) {
							
							inputRow[codigoPostalPos] = address.getPostalCode();
						}
						
						inputRow[latPos] = String.valueOf(address.getLat());
						inputRow[lonPos] = String.valueOf(address.getLng());
					}
					
				}
			
			} else {
				
				// Directa
				
				// Se hace la misma comprobacion que en el proceso normal
				String nombreVia = inputRow[nombreViaPos];
				if (StringUtils.isNotBlank(nombreVia)) {
					
					portalPk = inputRow[portalPkPos];
					
					// Validaciones
					if (StringUtils.isNumeric(portalPk)) {
						
						tipoVia = inputRow[tipoViaPos];
						codIneMun = inputRow[codIneMunicipioPos];
						
						if (StringUtils.isBlank(codIneMun)) {
							
							municipio = inputRow[municipioPos];
							provincia = inputRow[provinciaPos];
						}
						
						address = searchCsvFindListAddress(tipoVia, nombreVia, portalPk, municipio, provincia, codIneMun, null, null, null);
						
						if (StringUtils.isNotBlank(address.getId())) {
							
							inputRow[latPos] = String.valueOf(address.getLat());
							inputRow[lonPos] = String.valueOf(address.getLng());
						}
					}
				}
			}
			
			rows.add(inputRow);
			
			count++;
		}
		
		csvReader.close();
		
		ByteArrayOutputStream csvSStream = new ByteArrayOutputStream();
        OutputStreamWriter csvStreamWriter = new OutputStreamWriter(csvSStream);
        CSVWriter csvWriter = new CSVWriter(csvStreamWriter, CSV_SEPARATOR);
        csvWriter.writeAll(rows);
        csvWriter.close();
		
        FileUtils.writeByteArrayToFile(output, csvSStream.toByteArray());
		
		log.info("GEOCODIFICACION SETELECO: Finaliza proceso: " + new Date());
		
		return output.getAbsolutePath();
	}
	
	/**
	 * Metodo que implementa el servicio de calculo masivo de direcciones
	 * 
	 * @param filePath    Path del fichero
	 * @param httpRequest Peticion HTTP
	 * @return Path del fichero de salida
	 * @throws IOException
	 */
	public String processXSLReverseFile(String filePath, HttpServletRequest httpRequest) throws IOException  {
		log.info("GEOCODIFICACION INVERSA: Comienza proceso: " + new Date());
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd_hh_mm_ss");
		File distances = File.createTempFile(dt.format(new Date()) + XSLX_REVERSE_OUTPUT_FILE_NAME, XSLX_EXTENSION);
		Workbook outworkbook;
		outworkbook = new XSSFWorkbook();
		Sheet outsheet = outworkbook.createSheet("Direcciones");
		
		Font font = outworkbook.createFont();
	    font.setFontHeightInPoints((short)10);
	    font.setFontName("Arial");
	    font.setColor(IndexedColors.WHITE.getIndex());
	    font.setBoldweight((short) 700);
	    font.setItalic(false);

	    CellStyle style = outworkbook.createCellStyle();
	    style.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
	    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    style.setAlignment(CellStyle.ALIGN_CENTER);
	    style.setFont(font);
		
		Row houtrow = outsheet.createRow(0);
		Cell houtcell0 = houtrow.createCell(0);
		houtcell0.setCellValue("Latitud");
		houtcell0.setCellStyle(style);
		
		Cell houtcell1 = houtrow.createCell(1);
		houtcell1.setCellValue("Longitud");
		houtcell1.setCellStyle(style);
		
		Cell houtcell2 = houtrow.createCell(2);
		houtcell2.setCellValue("Tipo vía");
		houtcell2.setCellStyle(style);
		
		Cell houtcell3 = houtrow.createCell(3);
		houtcell3.setCellValue("Dirección");
		houtcell3.setCellStyle(style);
		
		Cell houtcell4 = houtrow.createCell(4);
		houtcell4.setCellValue("Número portal");
		houtcell4.setCellStyle(style);
		
		Cell houtcell5 = houtrow.createCell(5);
		houtcell5.setCellValue("Municipio");
		houtcell5.setCellStyle(style);
		
		Cell houtcell6 = houtrow.createCell(6);
		houtcell6.setCellValue("Provincia");
		houtcell6.setCellStyle(style);
		
		Cell houtcell7 = houtrow.createCell(7);
		houtcell7.setCellValue("Observaciones");
		houtcell7.setCellStyle(style);
			
		int total = 0;
		int procesadas = 0;
		
		FileInputStream fileis = new FileInputStream(filePath);
		Workbook workbook =  new XSSFWorkbook(fileis);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext() && total < (CSVREVERSEFINDLIST_MAX_ROWS + LINEAS_XSLX_REVERSE_CABECERA)) {
			
			total++;
			Row row = (Row) rowIterator.next();
			if (total <= LINEAS_XSLX_REVERSE_CABECERA) {
				
				continue;
			}

			double lon_alu = 0;
			double lat_alu = 0;
			boolean has_lon_alu = false;
			boolean has_lat_alu = false;
			Iterator<Cell> cellIterator = row.cellIterator();
			
			while (cellIterator.hasNext()) {
				
				Cell cell = (Cell) cellIterator.next();
				int type = cell.getCellType();
				
				if (type == Cell.CELL_TYPE_NUMERIC) {
					
					switch (cell.getColumnIndex()) {
						case 1:
							lon_alu = cell.getNumericCellValue();
							has_lon_alu=true;
							break;
						case 0:
							lat_alu = cell.getNumericCellValue();
							has_lat_alu=true;
							break;
						default:
							break;
					}
				}

				if (type == Cell.CELL_TYPE_STRING) {
					
					switch (cell.getColumnIndex()) {
						case 1:
							lon_alu = Double.parseDouble(cell.getStringCellValue());
							has_lon_alu=true;
							break;
						case 0:
							lat_alu = Double.parseDouble(cell.getStringCellValue());
							has_lat_alu=true;
							break;
						default:
							break;
					}
				}
			}
			
			if (has_lon_alu && has_lat_alu) {
				
				Address a = reverseGeocode(lon_alu, lat_alu);
				procesadas++;
				Row outrow = outsheet.createRow(procesadas);
				if (a != null) {
					
					Cell outcell0 = outrow.createCell(0);
					outcell0.setCellValue(String.valueOf(a.getLat()));
					
					Cell outcell1 = outrow.createCell(1);
					outcell1.setCellValue(String.valueOf(a.getLng()));
					
					Cell outcell2 = outrow.createCell(2);
					outcell2.setCellValue(a.getTip_via());
					
					Cell outcell3 = outrow.createCell(3);
					outcell3.setCellValue(a.getAddress());
					
					Cell outcell4 = outrow.createCell(4);
					if (a.getPortalNumber() != null) {
						
						outcell4.setCellValue(a.getPortalNumber().intValue());
					}
					
					Cell outcell5 = outrow.createCell(5);
					outcell5.setCellValue(a.getMuni());
					
					Cell outcell6 = outrow.createCell(6);
					outcell6.setCellValue(a.getProvince());
				
				} else {
					
					Cell outcell0 = outrow.createCell(0);
					outcell0.setCellValue(String.valueOf(lat_alu));
					
					Cell outcell1 = outrow.createCell(1);
					outcell1.setCellValue(String.valueOf(lon_alu));
					
					Cell outcell7 = outrow.createCell(7);
					outcell7.setCellValue(Configuration.getReadedProperty(Configuration.CSVREVERSEFINDLIST_NO_RESULT));
				}
			}
		}
		
		outsheet.autoSizeColumn(0,true);
		outsheet.autoSizeColumn(1,true);
		outsheet.autoSizeColumn(2,true);
		outsheet.autoSizeColumn(3,true);
		outsheet.autoSizeColumn(4,true);
		outsheet.autoSizeColumn(5,true);
		outsheet.autoSizeColumn(6,true);
		outsheet.autoSizeColumn(7,true);

		try {
			
			FileOutputStream out = new FileOutputStream(distances);
			outworkbook.write(out);
			out.close();
		
		} catch (Exception e) {
			
			log.error("GEOCODIFICACION INVERSA: Se ha producido un error al montar el fichero de salida: ", e);
		}	
		
		log.info("GEOCODIFICACION INVERSA: Finaliza proceso: " + new Date());
		
		return distances.getAbsolutePath();
	}
	
	/**
	 * Dertermina, segun el texto de busqueda, a partir de que HUG de Elastic se
	 * debe realizar la busqueda
	 * 
	 * @param q Query realizada al servicio
	 * @return Array con lo necesario
	 */
	private String[] determinaHUG(String q) {
		
		String[] hug = null;
		if (GeocodingUtils.containsDigit(q)) {
			
			if (q.matches("\\d+") && q.length() >= 2 && q.length() <= 5) {
				
				// Codigo postal
				hug = Queries.CODPOSTAL;
				this.SEARCH_TYPE = Queries.SEARCH_TYPE_POSTAL_CODE;
			
			} else {
				
				// Busqueda de direccion con numero
				hug = Queries.PORTAL;
				this.SEARCH_TYPE = Queries.SEARCH_TYPE_PORTAL;
			
			}
		
		} else {
			
			if (!noNumberContains(q))	{
				
				// Sin numeros
				hug = Queries.VIAL;
				this.SEARCH_TYPE = Queries.SEARCH_TYPE_VIAL;
			
			} else {
				
				// Busqueda de portales sin numero
				hug = Queries.PORTAL;
				this.SEARCH_TYPE = Queries.SEARCH_TYPE_PORTAL_NO_NUMBER;
			}
			
		}
		return hug;
	}
	
	/**
	 * Transforma la query para Elasticsearch segun los parametros de entrada en la
	 * peticion al servicio
	 * 
	 * @param query                     Query
	 * @param q                         Termino de busqueda para el servicio
	 * @param no_process                Parametro no_process del servicio
	 * @param cod_postal_filter         Indica el filtro para codigo postal (uno o
	 *                                  varios separados por coma)
	 * @param municipio_filter          Indica el filtro para municipio (uno o
	 *                                  varios separados por coma)
	 * @param provincia_filter          Indica el filtro para provincia (una o
	 *                                  varias separados por coma)
	 * @param comunidad_autonoma_filter Indica el filtro para comunidad autonoma
	 *                                  (una o varias separados por coma)
	 * @param poblacion_filter          Indica el filtro para poblacion (una o
	 *                                  varias separados por coma)
	 * @param limit                     Parametro limite del servicio
	 * @param isCandidates              Indica si es o no una llamada de candidates
	 * @param aggregation               Indica si aplica o no una agregacion
	 * @param noNumber                  Indica si se aplica el filtro de sin numero
	 * @return Query para mandar a Elasticsearch
	 */
	private String ajustaQuery(String query, String q, String no_process, String cod_postal_filter,
			String municipio_filter, String provincia_filter, String comunidad_autonoma_filter, String poblacion_filter,
			int limit, boolean isCandidates, boolean aggregation, boolean noNumber, HttpServletRequest request) {

		String queryAjustada = query;
		String terms, must_not;
		queryAjustada = queryAjustada.replace("--q--", q);
		
		if (isCandidates) {
			
			/*
			 * candidates / candidatesJsonp
			 */
			
			String excludeGeom = "\"_source\":{\"exclude\":[\"geom\", \"lat\", \"lng\"]},";
			if (!aggregation) {
				
				queryAjustada = queryAjustada.replace("--limit--", String.valueOf(limit)).replace("--exclude--", excludeGeom);
			
			} else {
				
				// Si es agregacion quitamos el size porque ya lo tiene en la parte de la agregacion y se ahorra este trabajo a Elastic y para el trafico
				queryAjustada = queryAjustada.replace("--limit--", "0").replace("--exclude--", excludeGeom);
			}
			
			// Seccion de filtos que se pasan por parametros
			String filters = getCandidatesFilterQuerySection(cod_postal_filter, municipio_filter, provincia_filter,
					comunidad_autonoma_filter, poblacion_filter);
			
			if (this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL) 
					|| this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL_NO_NUMBER)) {
				
				if (!noNumber) {
					
					queryAjustada = queryAjustada.replace("--no_number--", "");
					queryAjustada = queryAjustada.replace("--no_number_filter--", Queries.FILTER_EXIST_FIELD_NUMERO);
				
				} else { 
					
					// Se hace esta comprobacion aqui (por ahora) en lugar de meterlo en el getCandidatesFilterQuerySection para no comprobar dos veces los SEARCH_TYPE
					if (StringUtils.isBlank(filters)) {
						
						// NO existe el must anyadido por filters
						queryAjustada = queryAjustada.replace("--no_number--", Queries.MUST_NO_NUMBER_TRUE);
					
					} else {
						
						// EXISTE ya el must, anyadimos la parte del no_number a la cadena
						queryAjustada = queryAjustada.replace("--no_number--", "");
						filters = filters.replace("[", "[" + Queries.TERM_NO_NUMBER + ",");
					}
					
					queryAjustada = queryAjustada.replace("--no_number_filter--", "");
				}
			}
			
			queryAjustada = queryAjustada.replace("--filters--", filters);
			
		} else {
			
			/*
			 * find / findJsonp
			 */
			
			queryAjustada = queryAjustada.replace("--limit--", "1")
					.replace("--filters--", "");
			
			if (!this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_POSTAL_CODE) 
					|| !this.POSTAL_CODE_FIND_RESTRICTION.booleanValue()) {
			
				queryAjustada = queryAjustada.replace("--exclude--", "");
			
			} else {
				
				if (this.POSTAL_CODE_FIND_RESTRICTION.booleanValue()) {	
					
					// Busqueda de codigos postales y find / findJsonp ... se comprueba origen
					this.ORIGIN_IS_IN_WITHE_LIST = originIsInWhiteList(request);
					
					if (this.ORIGIN_IS_IN_WITHE_LIST == null 
							|| (this.ORIGIN_IS_IN_WITHE_LIST != null && this.ORIGIN_IS_IN_WITHE_LIST.equals(Boolean.FALSE))) {			
						
						// Se excluye la geometria porque se devuelve el centroide, que se puede obtener de lat / lng
						queryAjustada = queryAjustada.replace("--exclude--", "\"_source\":{\"exclude\":[\"geom\"]},");
					
					} else {
						
						// El origen esta en la lista, no se excluye la geom porque se puede devolver tal cual
						queryAjustada = queryAjustada.replace("--exclude--", "");
					}
				}
			
			}
			
			if (this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL) 
					|| this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL_NO_NUMBER)) {
				
				queryAjustada = queryAjustada.replace("--no_number--", "")
					.replace("--no_number_filter--", Queries.FILTER_EXIST_FIELD_NUMERO);
			}
		}
		
		// añade must_not a la query según el prámetro no_process
		if (no_process != null) {
			
			no_process = no_process.toLowerCase();
			terms = "";
			
			if (no_process.contains(Queries.MUNICIPIO_BUCKET_NAME)) {
				
				terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.MUNICIPIO_BUCKET_NAME + "\"}}";
			}
			
			if (no_process.contains(Queries.POBLACION_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.POBLACION_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.POBLACION_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.TOPONIMO_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.TOPONIMO_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.TOPONIMO_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.CALLEJERO_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.CALLEJERO_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.CALLEJERO_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.CARRETERA_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.CARRETERA_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.CARRETERA_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.PORTAL_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.PORTAL_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.PORTAL_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.COMUNIDAD_AUTONOMA_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.COMUNIDAD_AUTONOMA_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.COMUNIDAD_AUTONOMA_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.PROVINCIA_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.PROVINCIA_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.PROVINCIA_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.EXPENDEDURIA_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.EXPENDEDURIA_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.EXPENDEDURIA_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.PUNTO_RECARGA_ELECTRICA_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.PUNTO_RECARGA_ELECTRICA_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.PUNTO_RECARGA_ELECTRICA_BUCKET_NAME + "\"}}";
				}
			}
			
			if (no_process.contains(Queries.NGBE_BUCKET_NAME)) {
				
				if (StringUtils.isNotBlank(terms)) {
					
					terms = terms + ",{\"term\":{\"table_name.keyword\":\"" + Queries.NGBE_BUCKET_NAME + "\"}}";
				
				} else {
					
					terms = "{\"term\":{\"table_name.keyword\":\"" + Queries.NGBE_BUCKET_NAME + "\"}}";
				}
			}
			
			if (StringUtils.isNotBlank(terms)) {
			
				must_not = ",\"must_not\":[" + terms + "]";
				queryAjustada = queryAjustada.replace("--no_process--", must_not);
			
			} else {
				
				queryAjustada = queryAjustada.replace("--no_process--", "");
			}
		
		} else {
			
			queryAjustada = queryAjustada.replace("--no_process--", "");
		}
		
		// Comprueba si se aplican o no agregaciones
		if (aggregation) {
			
			queryAjustada = queryAjustada.replace(Queries.AGGREGATION_KEY, Queries.AGGREGATION_QUERY);
		
		} else {
			
			queryAjustada = queryAjustada.replace(Queries.AGGREGATION_KEY, "");
		}
		
		return queryAjustada;
	}

	/**
	 * Transforma la respuesta string a un objeto json
	 * 
	 * @param responseStr Cadena
	 * @return JSONObject
	 */
	private JSONObject responseStrToJson(String responseStr) {
		JSONObject responseJson = new JSONObject(responseStr);
		return responseJson;
	}

	/**
	 * Transforma la respuesta json al modelo Address
	 * 
	 * @param json            JSON de respuesta
	 * @param isCandidates    Indica si es o no una llamada a candidates
	 * @param completeAddress Indica si hay que dar el atributo address completo o
	 *                        no
	 * @return Address
	 */
	private Address jsonToAddress(JSONObject json, boolean isCandidates, boolean completeAddress) {
		
		Address address = new Address();
		String index = json.getString("_index");
		JSONObject source = json.getJSONObject("_source");

		// Comprueba si la respuesta es para find / findJsonp
		if (!isCandidates) {
			
			boolean points = false;
			if (!source.isNull("lat") && !source.isNull("lng")) {
			
				address.setLat(source.getDouble("lat"));
				address.setLng(source.getDouble("lng"));
				points = true;
			}
			
			if (!source.isNull("geom")) {
				
				address.setGeom(source.getString("geom"));
			
			} else {
				
				/*
				 * Se comprueba aqui porque si hay geom es que el origen habria podido pasar el
				 * filtro de la lista blanca (el exclude de la query ha sido vacio) en el caso
				 * que hubiese sido busqueda de codigo postal
				 */
				if (this.POSTAL_CODE_FIND_RESTRICTION.booleanValue()
						&& points 
						&& this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_POSTAL_CODE) 
						&& (this.ORIGIN_IS_IN_WITHE_LIST == null
							|| (this.ORIGIN_IS_IN_WITHE_LIST != null && this.ORIGIN_IS_IN_WITHE_LIST.equals(Boolean.FALSE)))) {
					
					// Si es find / findJsonp y el origen no esta en la lista blanca ... se monta el centroide si tiene los puntos que lo representan
					address.setGeom("POINT(" + address.getLng() + " " + address.getLat() + ")");
				}
			}
		}

		/*
		 * Campos especificos
		 */
		
		if (index.equals(Queries.PORTAL_INDEX_NAME)) {
			
			// id
			address.setId(String.valueOf(source.getString("obj_id")));
			
			// type
			address.setType(source.getString("table_name"));

			// address
			address.setAddress(getAddress(source, completeAddress));
			
			// tip_via
			if (!source.isNull("tipo_via")) {
				
				address.setTip_via(source.getString("tipo_via"));
			}

			// portalNumber
			if (!source.isNull("numero")) {
				
				address.setPortalNumber(source.getInt("numero"));
			}
			
			// extension
			if (!source.isNull("extension")) {
				
				address.setExtension(source.getString("extension"));
			}

			// muni
			if (!source.isNull("nom_muni_original")) {
				
				address.setMuni(source.getString("nom_muni_original"));
			}	
				
			// province
			if (!source.isNull("nom_prov_original")) {
				
				address.setProvince(source.getString("nom_prov_original"));
			}

			// comunidadAutonoma
			if (!source.isNull("nom_comunidad_original")) {
				
				address.setComunidadAutonoma(source.getString("nom_comunidad_original"));
			}

			// poblacion
			if(!source.isNull("ent_pob_original")) {
				
				address.setPoblacion(source.getString("ent_pob_original"));
			}
			
			// lng
			if (!source.isNull("lng")) {
				
				address.setLng(source.getDouble("lng"));
			}
			
			// lat
			if (!source.isNull("lat")) {
				
				address.setLat(source.getDouble("lat"));
			}
			
			// geom
			if (!source.isNull("geom")) {
				
				address.setGeom(source.getString("geom"));
			}

			// postalCode
			if (!source.isNull("cod_postal") && source.has("cod_postal") && source.getJSONArray("cod_postal").length() > 0) {
				
				address.setPostalCode(source.getJSONArray("cod_postal").getString(0));
			}
			
			// ine municipio
			if (!source.isNull("ine_mun")) {
				
				address.setMuniCode(source.getString("ine_mun"));
			}
			
			// ine provincia
			if (!source.isNull("ine_prov")) {
				
				address.setProvinceCode(source.getString("ine_prov"));
			}
			
			// ine comunidad
			if (!source.isNull("ine_comunidad")) {
				
				address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
			}
			
			// referencia catastral
			if (!source.isNull("ref_catastral")) {
				
				address.setRefCatastral(source.getString("ref_catastral"));
			}
			
			// sin number
			if (!source.isNull("sin_numero")) {
				
				address.setNoNumber(source.getBoolean("sin_numero"));
			}
		
		} else if (index.equals(Queries.VIAL_INDEX_NAME)) {
			
			// id
			address.setId(String.valueOf(source.getLong("obj_id")));
			
			// type
			address.setType(source.getString("table_name"));

			// address
			address.setAddress(getAddress(source, completeAddress));
			
			// tip_via
			if (!source.isNull("tipo_via")) {
				
				address.setTip_via(source.getString("tipo_via"));
			}

			// muni
			if (!source.isNull("nom_muni_original")) {
				
				address.setMuni(source.getString("nom_muni_original"));
			}

			// province
			if (!source.isNull("nom_prov_original")) {
				
				address.setProvince(source.getString("nom_prov_original"));
			}

			// comunidadAutonoma
			if (!source.isNull("nom_comunidad_original")) {
				
				address.setComunidadAutonoma(source.getString("nom_comunidad_original"));
			}
			
			// ine municipio
			if (!source.isNull("ine_mun")) {
				
				address.setMuniCode(source.getString("ine_mun"));
			}
			
			// ine provincia
			if (!source.isNull("ine_prov")) {
				
				address.setProvinceCode(source.getString("ine_prov"));
			}
			
			// ine comunidad
			if (!source.isNull("ine_comunidad")) {
				
				address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
			}

			// poblacion
			address.setPoblacion(getEntPobOriginalVial(source, false));

			// postalCode
			address.setPostalCode(getCodPostalVial(source));

		} else if (index.equals(Queries.TOPONIMO_INDEX_NAME)) {
			
			// id
			address.setId(String.valueOf(source.getString("obj_id")));
			
			// type -> toponimo, poblacion
			address.setType(source.getString("table_name"));

			// address
			address.setAddress(getAddress(source, completeAddress));

			// tip_via -> solo para toponimos
			if (address.getType().equals("toponimo") && !source.isNull("tipo")) {
				
				address.setTip_via(source.getString("tipo"));
			}

			// muni
			if (!source.isNull("nom_muni_original")) {
				
				address.setMuni(source.getString("nom_muni_original"));
			}

			// province
			if (!source.isNull("nom_prov_original")) {
				
				address.setProvince(source.getString("nom_prov_original"));
			}

			// comunidadAutonoma
			if (!source.isNull("nom_comunidad")) {
				
				address.setComunidadAutonoma(source.getString("nom_comunidad"));
			}
			
			// ine municipio
			if (!source.isNull("ine_mun")) {
				
				address.setMuniCode(source.getString("ine_mun"));
			}
			
			// ine provincia
			if (!source.isNull("ine_prov")) {
				
				address.setProvinceCode(source.getString("ine_prov"));
			}
			
			// ine comunidad
			if (!source.isNull("ine_comunidad")) {
				
				address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
			}
			
			// poblacion
			if (!source.isNull("ent_pob_original") && source.has("ent_pob_original")) {
				
				address.setPoblacion(source.getString("ent_pob_original"));
			}
			
			// postalCode
			if (!source.isNull("cod_postal") && source.has("cod_postal")) {
				
				address.setPostalCode(source.getString("cod_postal"));
			}
			
			if (!address.getType().equals(Queries.POBLACION_BUCKET_NAME)) {
				
				// lng
				if (!source.isNull("lng")) {
					
					address.setLng(source.getDouble("lng"));
				}
				
				// lat
				if (!source.isNull("lat")) {
					
					address.setLat(source.getDouble("lat"));
				}
			}

		} else if (index.equals(Queries.DIVISION_ADMINISTRATIVA_INDEX_NAME)) {
			
			// id
			address.setId(String.valueOf(source.getLong("obj_id")));
			
			// type -> municipio, provincia, comunidad autonoma
			address.setType(source.getString("table_name"));

			if (address.getType().equals("municipio")) {
				
				// Para que funcione igual el plugin
				address.setType("Municipio");
				
				// address
				if (isCandidates) {	
					
					String fullAddress = source.getString("text_original");
					if (!source.isNull("nom_prov_original")) {
						
						fullAddress += ", " + source.getString("nom_prov_original");
					}
					
					address.setAddress(fullAddress);
				
				} else {
					
					address.setAddress(source.getString("text_original"));
				}

				// muni
				address.setMuni(source.getString("text_original"));

				// province
				if (!source.isNull("nom_prov_original")) {
					
					address.setProvince(source.getString("nom_prov_original"));
				}
				
				// comunidadAutonoma
				if (!source.isNull("nom_comunidad_original")) {
					
					address.setComunidadAutonoma(source.getString("nom_comunidad_original"));
				}
				
				// ine municipio
				address.setMuniCode(source.getString("ine_mun"));
				
				// ine provincia
				if (!source.isNull("ine_prov")) {
					
					address.setProvinceCode(source.getString("ine_prov"));
				}
				
				// ine comunidad
				if (!source.isNull("ine_comunidad")) {
					
					address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
				}
			
			} else {
				
				if (address.getType().equals("provincia")) {
					
					address.setAddress(source.getString("text_original"));
					
					address.setProvince(source.getString("text_original"));
					
					address.setComunidadAutonoma(source.getString("nom_comunidad_original"));
					
					// ine provincia
					address.setProvinceCode(source.getString("ine_prov"));
					
					// ine comunidad
					address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
					
				} else {
					
					address.setAddress(source.getString("text_original"));
					
					address.setComunidadAutonoma(source.getString("text_original"));
					
					// ine comunidad
					address.setComunidadAutonomaCode(source.getString("ine_comunidad"));
				
				}
			}
			
			// Para que funcione el plugin
			saveGeometryForPlugin(address);
			
		} else if (index.equals("codigo_postal")) {
			
			// id
			address.setId(String.valueOf(source.getLong("obj_id")));
			
			// type
			address.setType("Codpost");

			// address
			// null si es find
			if (isCandidates) {
				
				address.setAddress(source.getString("text_original"));
			}

			address.setPostalCode(source.getString("text_original"));
		}

		return address;
	}
	
	/**
	 * Obtiene el valor de la direccion
	 * 
	 * @param source JSONObject de respuesta
	 * @param completeAddress Indica si es hay que dar el address completo o no
	 * @return Cadena
	 */
	private String getAddress(JSONObject source, boolean completeAddress) {
		
		if (completeAddress) {	
			
			String tipoVia = source.isNull("tipo_via") ? "" : source.getString("tipo_via");
			String poblacion = getEntPobOriginalFullAddress(source);
			String municipio = source.isNull("nom_muni_original") ? "" : source.getString("nom_muni_original");
			String fullAddress = !source.isNull("text_original") ? source.getString("text_original") : null;
			
			if (!tipoVia.equals("")) {
				fullAddress = tipoVia + " " + fullAddress;
			}
			
			String numero =  source.isNull("numero") || !source.has("numero") ? "" : new Integer(source.getInt("numero")).toString();
			if (source.has("tipo") && !source.isNull("tipo") && source.getString("tipo").equals("portalPK") && source.has("tipo_porpk") && !source.isNull("tipo_porpk") && source.getInt("tipo_porpk") == 2) {
				
				fullAddress += " " + PORPK_KM;
			}
			
			if (!numero.equals("")) {
				
				fullAddress += " " + numero;
			
			} else if (this.SEARCH_TYPE.equals(Queries.SEARCH_TYPE_PORTAL_NO_NUMBER)) {
				
				fullAddress += " " + SIN_NUMERO;
			}
			
			String extension = source.isNull("extension") || !source.has("extension") ? "" : source.getString("extension");
			if (!extension.equals("")) {
				
				fullAddress += " " + extension;
			}
			
			if (!poblacion.equals("")) {
				
				fullAddress += ", " + poblacion;
			}
			
			if (!municipio.equals("") && !poblacion.equals(municipio)) {
				
				fullAddress += " (" + municipio + ")";
			}
			
			return fullAddress;
		
		} else {
			
			return source.getString("text_original");
		}
	}
	
	/**
	 * Realiza la obtencion de los datos del servicio de Referencias Catastrales
	 * 
	 * @param urlStr  URL del Servicio
	 * @param address Datos de la direccion
	 * @return Direccion final
	 * @throws Exception
	 */
	private AddressPriorized dataXMLAccess(String urlStr, AddressPriorized address) throws Exception {
		URL url2 = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
        InputStream inputStream = connection.getInputStream();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        document.getDocumentElement().normalize();
        Double x = null;
        Double y = null;
        String srs = null;
        String refCatastral = "";
        NodeList nodeList3 = document.getElementsByTagName("pc");
        for (int getChild = 0; getChild < nodeList3.getLength(); getChild++) {
        	Node node = nodeList3.item(getChild);
            for (int childs = 0; childs < node.getChildNodes().getLength(); childs++){
                Node child = node.getChildNodes().item(childs);
                if (child.getNodeName().equalsIgnoreCase("pc1")){
                    refCatastral=child.getChildNodes().item(0).getNodeValue();
                }
                
                if (child.getNodeName().equalsIgnoreCase("pc2")){
                    refCatastral=refCatastral+child.getChildNodes().item(0).getNodeValue();
                }
                
                address.setAddress(refCatastral);
            }
        }
        
        NodeList nodeList = document.getElementsByTagName("geo");
        for (int getChild = 0; getChild < nodeList.getLength(); getChild++) {
            Node node = nodeList.item(getChild);
            for (int childs = 0; childs < node.getChildNodes().getLength(); childs++){
                Node child = node.getChildNodes().item(childs);
                if (child.getNodeName().equalsIgnoreCase("xcen")){
                    x=Double.parseDouble(child.getChildNodes().item(0).getNodeValue());
                }
                
                if (child.getNodeName().equalsIgnoreCase("ycen")){
                    y=Double.parseDouble(child.getChildNodes().item(0).getNodeValue());
                }
                
                if (child.getNodeName().equalsIgnoreCase("srs")){
                    srs=child.getChildNodes().item(0).getNodeValue();
                }
            }
        }
        
        if (x != null && y != null && srs != null){
            int srsInt = 25830;
            if (srs != null){
                String srs2 = srs.replace("EPSG:", "");
                srsInt = Integer.parseInt(srs2);
            }
            
            Coordinate[] coordinates = new Coordinate[1];
            coordinates[0] = new Coordinate(x, y);
            Point point = new Point(new CoordinateArraySequence(coordinates,2) , new GeometryFactory(new PrecisionModel(), srsInt));
            if (!srs.equalsIgnoreCase(SRS_CODE)){
                CoordinateReferenceSystem current = CRS.decode(srs);
                CoordinateReferenceSystem auto = CRS.decode(SRS_CODE);
                MathTransform toTransform = CRS.findMathTransform(auto, current);
                point = (Point) JTS.transform(point, toTransform);
            }
            
            NodeList nodeList2 = document.getElementsByTagName("ldt");
            for (int getChild = 0; getChild < nodeList2.getLength(); getChild++) {
                Node node = nodeList2.item(getChild);
                String q = node.getChildNodes().item(0).getNodeValue();
                address.setRefCatastral(q);
            }
            
            address.setGeom(point.toText());
            address.setLng(point.getX());
            address.setLat(point.getY());
            return address;
        }
        
        return null;
     }
	
	/**
	 * Realiza la query sobre las Referencias Catastrales
	 * 
	 * @param q     Query
	 * @param limit Limite
	 * @return Lista de AddressPriorized
	 */
	private List<AddressPriorized> getRefCatastral(String q, int limit) {
		List<AddressPriorized> list = new ArrayList<AddressPriorized>();
        if (q.length() == 14) {
            String regex = "[0-9]{5}[A-Z][0-9]{8}";
            String regex3 = "[0-9]{7}[A-Z]{2}[0-9]{4}[A-Z]";
            if (q.matches(regex) || q.matches(regex3)) {
                AddressPriorized address = new AddressPriorized();
                try {
                    address = dataXMLAccess(REF_CATASTRAL_URL+"?Provincia=&Municipio=&SRS="+SRS_CODE+"&RC="+q, address);
                    if (address != null && StringUtils.isNotBlank(address.getGeom())) {
                        address.setId(q);
                        address.setType(TYPE_REFCATASTRAL);
                        address.setAddress(q);
                        list.add(address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
 
        return list;
    }
	
	/**
	 * Obtiene el valor de ent_pob_original del JSONObject en función de como se sepa
	 * que viene el dato
	 * 
	 * @param source JSONObject
	 * @return Valor o cadena vacia
	 */
	private String getEntPobOriginalFullAddress(JSONObject source) {
		
		String poblacion = "";
		
		if (!source.isNull("table_name") && source.getString("table_name").equals("callejero")) {
			
			poblacion = getEntPobOriginalVial(source, true);
			
			if (poblacion == null) {
				
				poblacion = ""; // Al ser para el fullAddress tiene que ser cadena vacia
			}
			
		} else {
			
			if (!source.isNull("ent_pob_original")) {
				
				poblacion = source.getString("ent_pob_original");
			}
		}
		
		return poblacion;
	}
	
	/**
	 * Obtiene el valor de ent_pob_original del JSONObject
	 * 
	 * @param source  JSONObject
	 * @param soloUno Indica si solo se obtiene eñ valor cuando hay un solo elemento
	 *                en el array de ent_pob_original
	 * @return Primer elemento del array, valores separados por coma o null
	 */
	private String getEntPobOriginalVial(JSONObject source, boolean soloUno) {
		
		String poblacion = null;
		
		if(!source.isNull("ent_pob_original")) {
			
			JSONArray jsonArray = source.getJSONArray("ent_pob_original");
			int arrayLength = jsonArray.length();
			if (soloUno) {	
				
				if (arrayLength == 1) {
					
					poblacion = (String) jsonArray.get(0);
				}
			
			} else {
				
				StringBuilder poblacionBuilder = new StringBuilder();
				for (int i = 0; i < arrayLength; i++) {
					
					poblacionBuilder.append((String) jsonArray.get(i));
					poblacionBuilder.append(",");
				}
				
				poblacion = poblacionBuilder.substring(0, poblacionBuilder.length() - 1);
			}
		}
		
		return poblacion;
	}
	
	/**
	 * Obtiene el valor de cod_postal para el indice de vial
	 * 
	 * @param source JSONObject
	 * @return Codigo postal, codigos postales separados por espacio o null
	 */
	private String getCodPostalVial(JSONObject source) {
		
		String strCodPostales = null;
		if (!source.isNull("table_name") 
				&& !source.getString("table_name").equals("carretera")
				&& !source.isNull("nom_comunidad_original")
				&& !source.isNull("cod_postal")) {
	
			JSONArray arrCodPostales = source.getJSONArray("cod_postal");
			int arrayLength = arrCodPostales.length();
			if (arrayLength > 0) {
				
				StringBuilder codPostalesBuilder = new StringBuilder();
				for (int i = 0; i < arrayLength; i++) {
		
					if (i == arrayLength - 1) {
		
						codPostalesBuilder.append(arrCodPostales.get(i));
		
					} else {
		
						codPostalesBuilder.append(arrCodPostales.get(i));
						codPostalesBuilder.append(" ");
					}
				}
				
				strCodPostales = codPostalesBuilder.toString();
			}
			
		}
		
		return strCodPostales;
	}

	/**
	 * Obtiene los hits de la respuesta de Elasticsearch
	 * 
	 * @param responseJson Respuesta de la consulta a Elasticsearch
	 * @param limit        Limite de resultados de la peticion (solo para el caso de
	 *                     la agregacion, para el resto ya se ha usado en la query)
	 * @param aggregation  Indica si aplica o no una agregacion
	 * @return JSONArray
	 */
	private JSONArray getHits(JSONObject responseJson, Integer limit, boolean aggregation) {
		
		if (aggregation) {
			
			return getHitsFromAggregations(responseJson, limit);
		
		} else {
			
			return responseJson.getJSONObject("hits").getJSONArray("hits");
		}
	}
	
	/**
	 * Obtiene el array de valores (hits) cuando se tienen agregaciones
	 * 
	 * @param responseJson Respuesta JSON de Elasticsearch
	 * @param limit        Limite de resultados de la peticion
	 * @return JSONArray
	 */
	private JSONArray getHitsFromAggregations(JSONObject responseJson, Integer limit) {
		
		JSONArray hits = null;
		JSONArray buckets = responseJson.getJSONObject("aggregations").getJSONObject("table_name").getJSONArray("buckets");
		if (buckets != null) {
			
			int bucketLength = buckets.length();
			if (bucketLength > 0) {
				
				hits = new JSONArray();
				JSONObject bucket = null;
				JSONArray includesHits = null;
				int cont = 0;
				for (int i = 0; i < bucketLength; i++) {
					
					bucket = buckets.getJSONObject(i);
					includesHits = bucket.getJSONObject("includes").getJSONObject("hits").getJSONArray("hits");
					for (int j = 0; j < includesHits.length() && j < Queries.aggregationPropertiesMap.get(bucket.getString("key")).intValue(); j++) {
						
						hits.put(includesHits.get(j));
						cont++;
						if (limit != null && cont == limit.intValue()) {
							
							return hits;
						}
					}
				}
			}
		}
		
		return hits;
	}
	
	/**
	 * Guarda la geometria para mantener la compatibilidad con los plugins actuales
	 * 
	 * @param address Objeto con la informacion a devolver
	 */
	private void saveGeometryForPlugin(Address address) {
		
		// Para que funcione el plugin
		if (checkMultiPolygon(address.getGeom())) {
			
			address.setGeom(address.getGeom().replace("),(", "), (").replace(MULTIPOLYGON, MULTIPOLYGON + " "));
		}
	}
	
	/**
	 * Comprueba si la geometria es multipolygon
	 * 
	 * @param geom Geometria en formato texto
	 * @return true si es, false en caso contrario
	 */
	private boolean checkMultiPolygon(String geom) {
		
		if (StringUtils.isNotBlank(geom)) {
			
			String sub = geom.substring(0, MULTIPOLYGON_STRING_LENGTH);
			if (sub.toUpperCase().equals(MULTIPOLYGON)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Obtiene el double de la cadena
	 * 
	 * @param strDouble Cadena con el double
	 * @return Double o null si no es un valor valido
	 */
	private Double getDouble(String strDouble) {
		
		try { 
			
			if (StringUtils.isNotBlank(strDouble)) {
			
				return Double.parseDouble(strDouble.replace(",", "."));
			}
		
		} catch (Exception e) {
			
		}
		return null;
	}
	
	/**
	 * Obtiene la extension
	 * 
	 * @param extension Extension
	 * @return Extension
	 */
	private String getExtension(String extension) {
		
		if (StringUtils.isNotBlank(extension)) {
			
			return extension.trim();
		}
		
		return null;
	}

	/**
	 * Obtiene la extension
	 * 
	 * @param extension Extension
	 * @return Extension
	 */
	private String getExtensionBlank(String extension) {
		
		if (StringUtils.isNotBlank(extension)) {
			
			return extension.trim();
		}
		
		return "";
	}
	
	/**
	 * Obtiene la cabecera para el CSV de respuesta del servicio de geocodificacion
	 * UNIFICADO
	 * 
	 * @param inputRow Fila de cabecera del csv de entrada
	 * @param inputLength Tamanyo del array de entrada
	 * @param headerLength Tamanyo del array de salida (cabecera)
	 * @return Array con la cabecera completa
	 */
	private String[] getUnifiedCSVHeader(String[] inputRow, int inputLength, int headerLength) {
		
		String[] header = new String[headerLength];
		System.arraycopy(inputRow, 0, header, 0, inputLength);
		header[inputLength] = UNIFIED_CSV_OBSERVACIONES;
		return header;
	}
	
	/**
	 * Inicializa el array que representa una fila para el CSV de salida del proceso
	 * de geocodificacion UNIFICADO
	 * 
	 * @param inputRow     Fila de entrada
	 * @param inputLength  Tamanyo del array de entrada
	 * @param outputLength Tamanyo del array de salida
	 * @return Array con una copia del de entrada y con las columnas extra
	 */
	private String[] initializeOutputRow(String[] inputRow, int inputLength, int outputLength) {
		
		String[] outputRow = new String[outputLength];
		System.arraycopy(inputRow, 0, outputRow, 0, inputLength);
		return outputRow;
	}
	
	/**
	 * Obtiene la posicion de field sobre inputFields
	 * 
	 * @param field       Field
	 * @param inputFields Input field
	 * @return posicion o -1
	 */
	private int getFieldPosition(String field, String[] inputFields) {
		
		int pos = 0;
		for (String inputField : inputFields) {
			
			if (inputField.equalsIgnoreCase(field)) {
				
				return pos;
			}
			
			pos++;
		}
		
		return -1;
	}
	
	/**
	 * Busca en la cadena de busqueda si aparece alguna de las cadenas
	 * correspondientes al sin numero
	 * 
	 * @param q Cadena de busqueda
	 * @return true si esta contenida (se esta buscando sin numero), false en caso
	 *         contrario
	 */
	public boolean noNumberContains(String q) {
		
		String[] terms = q.split(" ");
		for (String s : terms) {
			
			if (s.toLowerCase().contains(NO_NUMBER_QUERY_STR.toLowerCase())) {
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Monta y devuelve la parte de filtrado de la consulta de candidates si aplica
	 * 
	 * @param cod_postal_filter         Indica el filtro para codigo postal (uno o
	 *                                  varios separados por coma)
	 * @param municipio_filter          Indica el filtro para municipio (uno o
	 *                                  varios separados por coma)
	 * @param provincia_filter          Indica el filtro para provincia (una o
	 *                                  varias separados por coma)
	 * @param comunidad_autonoma_filter Indica el filtro para comunidad autonoma
	 *                                  (una o varias separados por coma)
	 * @param poblacion_filter          Indica el filtro para poblacion (una o
	 *                                  varias separados por coma)
	 */
	private String getCandidatesFilterQuerySection(String cod_postal_filter, String municipio_filter,
			String provincia_filter, String comunidad_autonoma_filter, String poblacion_filter) {
		
		StringBuilder filterSection = new StringBuilder("");
		
		if (StringUtils.isNotBlank(cod_postal_filter)
				|| StringUtils.isNotBlank(municipio_filter)
				|| StringUtils.isNotBlank(provincia_filter) 
				|| StringUtils.isNotBlank(comunidad_autonoma_filter)
				|| StringUtils.isNotBlank(poblacion_filter)) {
			
			// Se le añade el must que recogera el/los filtro/s
			filterSection.append("\"must\":[");
					
			if (StringUtils.isNotBlank(cod_postal_filter)) {
				
				filterSection.append("{\"regexp\":{\"cod_postal\":{\"value\": \"" + cod_postal_filter.replace(" ", "").replace(",", "|") + "\"}}},");
			}
			
			filterSection.append(getDivisionAdministrativaBoolSection(municipio_filter, "nom_muni_original.keyword"));
			filterSection.append(getDivisionAdministrativaBoolSection(provincia_filter, "nom_prov_original.keyword"));
			filterSection.append(getDivisionAdministrativaBoolSection(comunidad_autonoma_filter, "nom_comunidad_original.keyword"));	
			filterSection.append(getDivisionAdministrativaBoolSection(poblacion_filter, "ent_pob_original.keyword"));
			
			// Se quita la ultima coma
			int filterSectionLength = filterSection.length();
			filterSection.replace(filterSectionLength - 1, filterSectionLength, "");
			
			// Se cierra el must
			filterSection.append("],");
		}
		
		return filterSection.toString();
		
	}
	
	/**
	 * Monta la seccion bool dentro del must en relacion a los filtros relativos a
	 * divisiones administrativas
	 * 
	 * @param filter Filtro
	 * @param key    Clave (atributo del mapping)
	 * @return Seccion booleana
	 */
	private String getDivisionAdministrativaBoolSection(String filter, String key) {
		
		StringBuilder divisionAdministrativa = new StringBuilder("");
		if (StringUtils.isNotBlank(filter)) {
			
			divisionAdministrativa.append("{\"bool\":{\"should\":[");
			
			StringBuilder block = new StringBuilder("");
			String[] items = filter.toLowerCase().split(","); // Por alguna razon no termina de funcionar el case_insensitive del term cuando hay tildes en mayusculas, asi que se pone siempre todo en minusculas
			for (String item : items) {
				
				block.append(Queries.CSVFINDLIST_DIVISION_ADMINISTRATIVA_QUERY_FRAGMENT.replace("--key--", key).replace("--value--", item));
				block.append(",");
			}
			
			divisionAdministrativa.append(block.substring(0, block.length() - 1));
			divisionAdministrativa.append("]}},");
		}
		
		return divisionAdministrativa.toString();
	}

	/**
	 * Comprueba si el origen de la peticion esta en la lista blanca para la
	 * funcionalidad de los codigos postales
	 * 
	 * @param request Peticion
	 * @return true si lo esta, false en caso contrario
	 */
	private Boolean originIsInWhiteList(HttpServletRequest request) {
		
		Boolean isInWhiteList = null;
		
		String origin = request.getHeader("Referer");
		
		/*
		if (StringUtils.isBlank(origin)) {
			
			log.info("Se toma la cabecera Origin en lugar de Referer");
			origin = request.getHeader("Origin");
		}
		*/
		
		log.info("----> " + origin);
		/*
		log.info("----> ORIGIN " + request.getHeader("Origin"));
		
		log.info("----> getRemoteAddr " + request.getRemoteAddr());
		log.info("----> getRemoteHost " + request.getRemoteHost());
		log.info("----> getLocalAddr " + request.getLocalAddr());
		log.info("----> getLocalName " + request.getLocalName());
		*/
		
		if (StringUtils.isNotBlank(origin)) {
			
			/*
			if (origin.charAt(origin.length() - 1) != '/') {
				
				origin += "/";
			}
			*/
			
			if (POSTAL_CODE_FIND_DOMAIN_WITHELIST.contains(origin)
					|| origin.contains(POSTAL_CODE_FIND_DOMAIN_WITHELIST)) {

				// Busqueda mas restrictiva por dominios completos

				isInWhiteList = Boolean.TRUE;

			} else {

				// Dominios simples
				
				String originAux = origin.replace("//", "--");
				String simpleOrigin = origin.substring(0, originAux.indexOf("/") + 1);
				
				if (POSTAL_CODE_FIND_DOMAIN_WITHELIST.contains(simpleOrigin)
						|| simpleOrigin.contains(POSTAL_CODE_FIND_DOMAIN_WITHELIST)) {

					isInWhiteList = Boolean.TRUE;

				}
				
			}
		}
		
		return isInWhiteList;
	}
	
}
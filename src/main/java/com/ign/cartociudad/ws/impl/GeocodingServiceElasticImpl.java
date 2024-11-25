package com.ign.cartociudad.ws.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;

import com.ign.cartociudad.util.GeocodingUtils;
import com.ign.cartociudad.ws.IGeocodingServiceElastic;
import com.ign.cartociudad.ws.core.GeocodingServiceElasticCore;
import com.ign.cartociudad.ws.model.Address;
import com.ign.cartociudad.ws.model.AddressPriorized;
import com.ign.cartociudad.ws.model.GeoAddress;
import com.ign.cartociudad.ws.model.IAddress;
import com.sun.jersey.api.json.JSONWithPadding;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.spi.resource.Singleton;

/**
 * Implementacion de los servicios
 * 
 * @author guadaltel
 *
 */
@Path("/geocoder")
@Singleton
public class GeocodingServiceElasticImpl implements IGeocodingServiceElastic {
	
	/**
	 * Core
	 */
	private GeocodingServiceElasticCore core = new GeocodingServiceElasticCore();
	
	/**
	 * Localizacion del servidor donde se guardan los ficheros subidos
	 */
	public static String SERVER_UPLOAD_LOCATION_FOLDER = System.getProperty("java.io.tmpdir");
	
	/**
	 * Identificador del tipo Referencia Catastral
	 */
	//public static final String TYPE_REFCATASTRAL = "refcatastral";
	
	/**
	 * Limite por defecto para candidates cuando no se indica ninguno
	 */
	private final int DEFAULT_LIMIT_CANDIDATES = 35; 
	
	@Override
	public IAddress find(String q, String type, String tipVia, String id, String numPortal, String no_process,
			int state, String outputformat, HttpServletRequest request) {

		Address a = (Address) core.find(q, type, tipVia, id, numPortal, no_process, state, request);

		if (StringUtils.isBlank(outputformat) || !outputformat.equalsIgnoreCase("geojson")) {

			return a;

		} else {
			
			GeoAddress g = new GeoAddress(a);
			return g;
		}
	}

	@Override
	public JSONWithPadding findJsonp(String q, String type, String tipVia, String id, String numPortal, String state,
			String outputformat, String no_process, String callback, HttpServletRequest request) {

		int stateint = 1;
		if (StringUtils.isNotBlank(state)) {
			
			stateint = Integer.parseInt(state);
		}

		Address a = (Address) find(q, type, tipVia, id, numPortal, no_process, stateint, null, request);
		
		if (a == null) {
			
			JSONWithPadding json = new JSONWithPadding(new ArrayList<Address>(), callback);
			return json;
		}

		if (StringUtils.isBlank(outputformat) || !outputformat.equalsIgnoreCase("geojson")) {
			
			JSONWithPadding json = new JSONWithPadding(a, callback);
			return json;

		} else {
			
			GeoAddress g = new GeoAddress(a);
			JSONWithPadding json = new JSONWithPadding(g, callback);
			return json;
		}
	}

	@Override
	public List<Address> candidates(String q, String no_process, int limit, String cod_postal_filter,
			String municipio_filter, String provincia_filter, String comunidad_autonoma_filter, String poblacion_filter,
			boolean autocancel) {

		if (q == null) {

			return null;

		} else {

			if (limit == 0) {

				limit = DEFAULT_LIMIT_CANDIDATES;
			}
			
			return core.candidates(q, no_process, limit, cod_postal_filter, municipio_filter, provincia_filter,
					comunidad_autonoma_filter, poblacion_filter);
		}
	}

	@Override
	public JSONWithPadding candidatesJsonp(String q, String no_process, int limit,
			String cod_postal_filter, String municipio_filter, String provincia_filter,
			String comunidad_autonoma_filter, String poblacion_filter, boolean autocancel, String callback) {

		List<Address> res = candidates(q, no_process, limit, cod_postal_filter, municipio_filter, provincia_filter,
				comunidad_autonoma_filter, poblacion_filter, autocancel);
		
		return new JSONWithPadding(new GenericEntity<Collection<Address>>(res) {}, callback);
	}

	@Override
	public AddressPriorized geocode(String id, String type, String q) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONWithPadding geocodeJsonp(String id, String type, String q, String callback) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * Se comenta y se deja el mismo sin type porque ahora la ref catastral es un atributo del indice portal_pk y se dara siempre
	@Override
	public Address reverseGeocode(double lon, double lat, String type) {
		if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(TYPE_REFCATASTRAL)){
			return core.getRefCatastral(lon, lat);
		}
		
		Address closest = core.reverseGeocode(lon, lat);
		return closest;
	}
	*/
	
	@Override
	public Address reverseGeocode(double lon, double lat) {
		
		Address closest = core.reverseGeocode(lon, lat);
		
		return closest;
	}

	/*
	 * Se comenta y se deja el mismo sin type porque ahora la ref catastral es un atributo del indice portal_pk y se dara siempre
	@Override
	public JSONWithPadding reverseGeocodeJsonp(double lon, double lat, String type, String callback) {
		Address a = reverseGeocode(lon, lat, type);
		if (a == null) {
			return null;
		}
			
		return new JSONWithPadding(a, callback);
	}
	*/

	@Override
	public JSONWithPadding reverseGeocodeJsonp(double lon, double lat, String callback) {
		
		Address a = reverseGeocode(lon, lat);
		
		if (a == null) {
			return null;
		}
			
		return new JSONWithPadding(a, callback);
	}
	
	@SuppressWarnings("resource")
	@Override
	public Response getCSVFindList(InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader, HttpServletRequest httpRequest) {
		
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + File.separator + contentDispositionHeader.getFileName();
		GeocodingUtils.saveFile(fileInputStream, filePath);
		StreamingOutput stream = null;
		File file = null;
		
		try {
			
			String resul = core.processCSVFindFile(filePath);
			file = new File(resul);
			final InputStream in = new FileInputStream(file);
			
			stream = new StreamingOutput() {
				public void write(OutputStream out) throws IOException,	WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = in.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};
		
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new WebApplicationException(e);
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
			throw new WebApplicationException(e1);
		
		} finally {
			
			if (file != null) {
				
				file.delete();
			}
		}

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + file.getName()).build();
	}

	@SuppressWarnings("resource")
	@Override
	public Response getCSVReverseList(InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader, HttpServletRequest httpRequest) {
		
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + File.separator + contentDispositionHeader.getFileName();
		GeocodingUtils.saveFile(fileInputStream, filePath);
		StreamingOutput stream = null;
		File file = null;
		
		try {
			
			String resul = core.processCSVReverseFile(filePath);
			file = new File(resul);
			final InputStream in = new FileInputStream(file);
			
			stream = new StreamingOutput() {
				public void write(OutputStream out) throws IOException,	WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = in.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};
		
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new WebApplicationException(e);
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
			throw new WebApplicationException(e1);
		
		} finally {
			
			if (file != null) {
				file.delete();
			}
		}

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + file.getName()).build();
	}
	
	@SuppressWarnings("resource")
	@Override
	public Response getSETELECOCSV(InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader, HttpServletRequest httpRequest) {
		
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + File.separator + contentDispositionHeader.getFileName();
		GeocodingUtils.saveFile(fileInputStream, filePath);
		StreamingOutput stream = null;
		File file = null;
		
		try {
			
			String resul = core.processSETELECOCSVFile(filePath);
			file = new File(resul);
			final InputStream in = new FileInputStream(file);
			
			stream = new StreamingOutput() {
				public void write(OutputStream out) throws IOException,	WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = in.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};
		
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new WebApplicationException(e);
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
			throw new WebApplicationException(e1);
		
		} finally {
			
			if (file != null) {
				file.delete();
			}
		}

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + file.getName()).build();
	}

	
	@SuppressWarnings("resource")
	@Override
	public Response getUnifiedCSVGeocoding(InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader, HttpServletRequest httpRequest) {
		
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + File.separator + contentDispositionHeader.getFileName();
		GeocodingUtils.saveFile(fileInputStream, filePath);
		StreamingOutput stream = null;
		File file = null;
		
		try {
			
			String resul = core.processUnifiedCSVFile(filePath);
			file = new File(resul);
			final InputStream in = new FileInputStream(file);
			
			stream = new StreamingOutput() {
				public void write(OutputStream out) throws IOException,	WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = in.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};
		
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			throw new WebApplicationException(e);
		
		} catch (IOException e1) {
			
			e1.printStackTrace();
			throw new WebApplicationException(e1);
		
		} finally {
			
			if (file != null) {
				file.delete();
			}
		}

		return Response.ok(stream).header("content-disposition", "attachment; filename = " + file.getName()).build();
	}
}

package com.ign.cartociudad.ws;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ign.cartociudad.ws.model.Address;
import com.ign.cartociudad.ws.model.AddressPriorized;
import com.ign.cartociudad.ws.model.IAddress;
import com.sun.jersey.api.json.JSONWithPadding;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/**
 * Interfaz que recoge la definicion de los servicios a implementar
 * 
 * @author guadaltel
 *
 */
public interface IGeocodingServiceElastic {
	
	@GET
	@Path("find")
	@Produces(MediaType.APPLICATION_JSON)
	public IAddress find(
			@QueryParam("q") String q,
			@QueryParam("type") String type,
			@QueryParam("tip_via") String tipVia, 
			@QueryParam("id") String id,
			@QueryParam("portal") String numPortal,
			@QueryParam("no_process") String no_process,
			@QueryParam("state") int state,
			@QueryParam("outputformat") String outputformat,
			@Context HttpServletRequest req);
	
	@GET
	@Path("findJsonp")
	@Produces({ "application/x-javascript", "application/javascript"})
	public JSONWithPadding findJsonp(
			@QueryParam("q") String q,
			@QueryParam("type") String type,
			@QueryParam("tip_via") String tipVia, 
			@QueryParam("id") String id,
			@QueryParam("portal") String numPortal,
			@QueryParam("state") String state,
			@QueryParam("outputformat") String outputformat,
			@QueryParam("no_process") String no_process,
			@QueryParam("callback") String callback,
			@Context HttpServletRequest req);
	
	@GET
	@Path("candidates")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Address> candidates(
			@QueryParam("q") String q,
			@QueryParam("no_process") String no_process,
			@QueryParam("limit") int limit,
			@QueryParam("cod_postal_filter") String cod_postal_filter,
			@QueryParam("municipio_filter") String municipio_filter,
			@QueryParam("provincia_filter") String provincia_filter,
			@QueryParam("comunidad_autonoma_filter") String comunidad_autonoma_filter,
			@QueryParam("poblacion_filter") String poblacion_filter,
			@QueryParam("autocancel") boolean autocancel);
	
	@GET
	@Path("candidatesJsonp")
	@Produces({ "application/x-javascript", "application/javascript" })
	public JSONWithPadding candidatesJsonp(
			@QueryParam("q") String q,
			@QueryParam("no_process") String no_process,
			@QueryParam("limit") int limit, 
			@QueryParam("cod_postal_filter")  String cod_postal_filter,
			@QueryParam("municipio_filter") String municipio_filter,
			@QueryParam("provincia_filter") String provincia_filter,
			@QueryParam("comunidad_autonoma_filter") String comunidad_autonoma_filter,
			@QueryParam("poblacion_filter") String poblacion_filter,
			@QueryParam("autocancel") boolean autocancel, 
			@QueryParam("callback") String callback);
	
	@GET
	@Path("geocode")
	@Produces(MediaType.APPLICATION_JSON)
	public AddressPriorized geocode(
			@QueryParam("id") String id,
			@QueryParam("type") String type,
			@QueryParam("q") String q);
	
	@GET
	@Path("geocodeJsonp")
	@Produces({ "application/x-javascript", "application/javascript" })
	public JSONWithPadding geocodeJsonp(
			@QueryParam("id") String id,
			@QueryParam("type") String type, 
			@QueryParam("q") String q,
			@QueryParam("callback") String callback);
	
	/*
	 * Se comenta y se deja el mismo sin type porque ahora la ref catastral es un atributo del indice portal_pk y se dara siempre
	@GET
	@Path("reverseGeocode")
	@Produces(MediaType.APPLICATION_JSON)
	public Address reverseGeocode(
			@QueryParam("lon") double lon,
			@QueryParam("lat") double lat,
			@QueryParam("type") String type);
	*/
	
	@GET
	@Path("reverseGeocode")
	@Produces(MediaType.APPLICATION_JSON)
	public Address reverseGeocode(
			@QueryParam("lon") double lon,
			@QueryParam("lat") double lat);
	
	/*
	 * Se comenta y se deja el mismo sin type porque ahora la ref catastral es un atributo del indice portal_pk y se dara siempre
	@GET
	@Path("reverseGeocodeJsonp")
	@Produces({ "application/x-javascript", "application/javascript" })
	public JSONWithPadding reverseGeocodeJsonp(
			@QueryParam("lon") double lon,
			@QueryParam("lat") double lat,
			@QueryParam("type") String type,
			@QueryParam("callback") String callback);
	*/
	
	@GET
	@Path("reverseGeocodeJsonp")
	@Produces({ "application/x-javascript", "application/javascript" })
	public JSONWithPadding reverseGeocodeJsonp(
			@QueryParam("lon") double lon,
			@QueryParam("lat") double lat,
			@QueryParam("callback") String callback);
	
	@POST
	@Path("/csvfindlist")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8" })
	public Response getCSVFindList(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader, 
			@Context HttpServletRequest httpRequest);
	
	@POST
	@Path("/csvreverselist")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8" })
	public Response getCSVReverseList(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader, 
			@Context HttpServletRequest httpRequest);
	
	@POST
	@Path("/seteleco/csv")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8" })
	public Response getSETELECOCSV(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader, 
			@Context HttpServletRequest httpRequest);

	@POST
	@Path("/unifiedcsvgeocoding")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8" })
	public Response getUnifiedCSVGeocoding(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader, 
			@Context HttpServletRequest httpRequest);
	
}

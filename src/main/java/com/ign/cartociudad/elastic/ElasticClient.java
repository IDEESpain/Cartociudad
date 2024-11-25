package com.ign.cartociudad.elastic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import com.ign.cartociudad.configuration.Queries;

/**
 * Clase que implementa el cliente para consultar Elasticsearch
 * 
 * @author guadaltel
 *
 */
public class ElasticClient {	
	
	/**
	 * Realiza la query a Elasticsearch
	 * 
	 * @param query    Query
	 * @param queryUrl URL
	 * @return JSON resultado
	 */
	public String search(String query, String queryUrl) {
		
		String responseStr = "";
		/*
		System.out.println("URL:___");
		System.out.println(queryUrl);
		System.out.println("QUERY:___");
		System.out.println(query);
		System.out.println("____");
		*/
		try {
			
			URL url = new URL(queryUrl);
			
			// Abre conexión (http o https)
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			
			// Content type
			con.setRequestProperty("Content-Type", "application/json");
			
			// Response type
			con.setRequestProperty("Accept", "application/json");
			
			// Autenticacion, si la hay
			if (StringUtils.isNotBlank(Queries.USER) && StringUtils.isNotBlank(Queries.PASS)) {
				
				String valueToEncode = Queries.USER + ":" + Queries.PASS;
				con.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes()));
			
			}
			
			// Afirma que se enviará contenido
			con.setDoOutput(true);
			
			// Crea el body
			String jsonQuery = query;
			OutputStream os = con.getOutputStream();
		    byte[] input = jsonQuery.getBytes("utf-8");
		    os.write(input, 0, input.length);			
			
			// Lee la respuesta
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
		    StringBuilder response = new StringBuilder();
		    String responseLine = null;
		    while ((responseLine = br.readLine()) != null) {
		        
		    	response.append(responseLine.trim());
		    }
		    
		    responseStr = response.toString(); 
		
		} catch(Exception e) {
			
			e.printStackTrace();
		}
		
		return responseStr;
	}
	
	/**
	 * Busqueda mediante JSOUP
	 * 
	 * @param query    Query
	 * @param queryUrl URL
	 * @return Resultado
	 * @throws IOException
	 */
	public String searchJsoup(String query, String queryUrl) throws IOException {
		
		String responseStr = "";
		
		try {
			
			Connection.Response response = Jsoup.connect(queryUrl).requestBody(query)
					.header("Content-Type", "application/json").ignoreContentType(true).ignoreHttpErrors(true)
					.method(Method.POST).execute();
			
			int status = response.statusCode();
			
			responseStr = response.body();
			
			switch(status) {
				case 200:
				case 201:
					return responseStr;	
				case 400:
					throw new IOException("Bad elastic search request (400) - response message: " + responseStr);
				default:
					throw new IOException("Bad elastic search request - response message: " + responseStr);
			}
		
		} catch (IOException e) {			
			
			throw new IOException("Bad elastic search request (400) - response message: " + responseStr);
		}
	}
}
